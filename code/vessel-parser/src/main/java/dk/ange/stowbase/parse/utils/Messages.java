package dk.ange.stowbase.parse.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Messages generated when parsing
 */
public class Messages {

    private Workbook workbook;

    private final Collection<String> parsedSheets = new ArrayList<String>();

    private final Collection<String> warnings = new ArrayList<String>();

    private Exception exception = null;

    /**
     * @param workbook
     */
    public void setWorkbook(final Workbook workbook) {
        this.workbook = workbook;
    }

    /**
     * @param sheets
     */
    public void addParsedSheets(final Sheet... sheets) {
        for (Sheet sheet : sheets) {
            parsedSheets.add(sheet.getSheetName());
        }
    }

    /**
     * @param warning
     */
    public void addWarning(final String warning) {
        warnings.add(warning);
    }

    /**
     * @param sheetName
     * @param warning
     */
    public void addSheetWarning(final String sheetName, final String warning) {
        warnings.add(sheetName + ": " + warning);
    }

    /**
     * @param sheet
     * @param warning
     */
    public void addSheetWarning(final Sheet sheet, final String warning) {
        addSheetWarning(sheet.getSheetName(), warning);
    }

    /**
     * The parser threw an exception
     *
     * @param e
     */
    public void setException(final Exception e) {
        exception = e;
    }

    /**
     * @return the exception
     */
    public Exception getException() {
        return exception;
    }

    /**
     * @return Get the status
     */
    public String getStatus() {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);

        if (exception == null) {
            printWriter.println("OK");
        } else {
            printWriter.println("ERROR: " + exception.getMessage());
            printWriter.println("<!--");
            exception.printStackTrace(printWriter);
            printWriter.print("-->");
        }
        printWriter.println("Parsed the following sheets: " + parsedSheets);

        if (workbook == null) {
            printWriter.println("Could not read Excel format.");
        } else {
            final Collection<String> unusedSheets = getSheetNames(workbook);
            unusedSheets.removeAll(parsedSheets);
            if (!unusedSheets.isEmpty()) {
                printWriter.println("Unused sheets: " + unusedSheets);
            }
        }

        for (final String warning : warnings) {
            printWriter.println(warning);
        }

        printWriter.close();
        return stringWriter.toString();
    }

    /**
     * @return Get the stack trace
     */
    public String getDeveloperStatus() {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);

        if (exception != null) {
            exception.printStackTrace(printWriter);
        }

        printWriter.close();
        return stringWriter.toString();
    }

    private static Collection<String> getSheetNames(final Workbook workbook) {
        final Collection<String> sheetNames = new ArrayList<String>();
        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); ++sheetIndex) {
            sheetNames.add(workbook.getSheetName(sheetIndex));
        }
        return sheetNames;
    }

}
