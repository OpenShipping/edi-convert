package dk.ange.stowbase.edifact.format;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Read an EDIFACT format from UNTID (http://www.unece.org/trade/untdid/), read from a file.
 */
public final class FormatReader {

    /**
     * Read the format from a stream
     * 
     * @param stream
     * @return the format
     */
    // Silence warning about unclosed BufferedReader and InputStreamReader:
    @SuppressWarnings("resource")
    public static SegmentGroupFormat readFormat(final InputStream stream) {
        final SegmentGroupFormatBuilder messageFormatBuilder = new SegmentGroupFormatBuilder("----", "Message", true,
                1, 0);

        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "IBM437"));
            while (!reader.readLine().equals("Pos    Tag Name                                      S   R")) {
                // Slurp until header is found
            }

            final List<SegmentGroupFormatBuilder> stack = new ArrayList<SegmentGroupFormatBuilder>();
            stack.add(messageFormatBuilder);

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("") || line.startsWith("    ")) {
                    continue;
                }

                final String position = line.substring(0, 4);
                final String tag = line.substring(7, 10);
                final String fullName = line.substring(11, 52);
                final String name = fullName.replace('─', ' ').trim();
                final String status = line.substring(53, 54);
                final boolean mandatory;
                if (status.equals("M")) {
                    mandatory = true;
                } else if (status.equals("C")) {
                    mandatory = false;
                } else {
                    throw new RuntimeException("Unknown status '" + status + "'");
                }
                final String occurrencesString = line.substring(57, 57 + 6).replace('─', ' ').trim();
                final int occurrences = Integer.parseInt(occurrencesString);

                SegmentGroupFormatBuilder currentGroup = stack.get(stack.size() - 1);

                final AbstractSegmentFormatBuilder builder;
                if (!tag.equals("   ")) { // Segment
                    builder = new SegmentFormatBuilder(position, tag, name, mandatory, occurrences);
                    currentGroup.members.add(builder);
                } else { // Group
                    final String boundaryString = line.substring(58);
                    final int groupBoundaryColumn = 58 + boundaryString.indexOf("┐");

                    final SegmentGroupFormatBuilder groupBuilder = new SegmentGroupFormatBuilder(position, name,
                            mandatory, occurrences, groupBoundaryColumn);
                    currentGroup.members.add(groupBuilder);
                    stack.add(groupBuilder);
                    builder = groupBuilder;
                }

                while (currentGroup.groupBoundaryColumn != 0) { // Be able to pop many in one line
                    final char groupBoundaryChar = line.charAt(currentGroup.groupBoundaryColumn);
                    if (!(groupBoundaryChar == '┘' || groupBoundaryChar == '┴')) {
                        break;
                    }
                    stack.remove(stack.size() - 1);
                    currentGroup = stack.get(stack.size() - 1);
                }
            }
            reader.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                stream.close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        return messageFormatBuilder.build();
    }

}
