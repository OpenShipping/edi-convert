package dk.ange.stowbase.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import dk.ange.stowbase.parse.vessel.ParseVessel;
import dk.ange.stowbase.parse.vessel.ParseVessel.Result;

/**
 * Program for parsing an XLS vessel profile to JSON
 */
public class XlsVesselToJson {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XlsVesselToJson.class);

    /**
     * @param args
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {
        if (args.length != 1) {
            throw new RuntimeException("Need exactly one argument, the filename of the XLS file");
        }
        final String xlsFileName = args[0];
        convert(xlsFileName);
        log.info("DONE.");
    }

    /**
     * @param xlsFileName
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void convert(final String xlsFileName) throws FileNotFoundException, IOException {
        final File xlsFile = new File(xlsFileName);
        if (!xlsFile.isFile()) {
            throw new RuntimeException("XLS file '" + xlsFile + "' not found");
        }

        log.info("Parse {}", xlsFile);
        final Result result = ParseVessel.parse(new FileInputStream(xlsFile));

        // Status contains all the debug info we want
        log.info("Status: {}", result.messages.getStatus().replaceAll("\n$", ""));
        final String jsonFileName = xlsFileName.replaceFirst("\\.xls$", "") + ".json";
        final FileWriter jsonWriter = new FileWriter(jsonFileName);
        try {
            if (result.json == null) {
                return; // Don't write null as that fails, but still open the file in order to delete it
            }
            jsonWriter.write(result.json);
        } finally {
            jsonWriter.close();
        }
    }

}
