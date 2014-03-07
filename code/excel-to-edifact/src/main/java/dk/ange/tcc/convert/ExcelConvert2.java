package dk.ange.tcc.convert;

import static dk.ange.tcc.convert.SheetFunctions.cellString;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.References;
import org.stowbase.client.StowbaseObject;
import org.stowbase.client.StowbaseObjectFactory;

import dk.ange.stowbase.parse.utils.Messages;

/**
 * Converter that will read Excel files and put the result in a Stowbase.
 * <p>
 * This handles the second version of the TCC load list format.
 */
public class ExcelConvert2 {

    private final StowbaseObjectFactory stowbase;

    private final CoprarExporter coprar;

    private final String vesselImo;

    /**
     * @param stowbase
     * @param coprar
     * @param vesselImo
     */
    public ExcelConvert2(final StowbaseObjectFactory stowbase, final CoprarExporter coprar, final String vesselImo) {
        this.stowbase = stowbase;
        this.vesselImo = vesselImo;
        this.coprar = coprar;
    }

    /**
     * Convert the Excel file.
     *
     * @param is
     *            data from Excel file
     */
    public void convert(final InputStream is) {
        final Workbook workbook;
        try {
            workbook = new HSSFWorkbook(is);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        convert(workbook);
    }

    void convert(final Workbook workbook) {
        final Sheet sheet = workbook.getSheetAt(0);
        for (int r = 1; r < sheet.getPhysicalNumberOfRows(); ++r) { // Skip first row
            final Row row = sheet.getRow(r);
            try {
                final String containerId = cellString(row.getCell(0));
                if (containerId == null || containerId.length() == 0) {
                    continue; // Skip containers without container ids
                }
                // Container
                final ContainerBuilder builder = new ContainerBuilder(vesselImo, containerId);
                final Messages messages = new Messages();
                builder.parseContainer(row, messages, workbook.getSheetAt(0));
                final References moves = new References();
                builder.build(stowbase, moves, coprar);
                final StowbaseObject stowage = stowbase.create("stowage");
                stowage.put("moves", moves);

            } catch (final Exception e) {
                throw new RuntimeException("Error was in row " + row.getRowNum(), e);
            }
        }
    }

}
