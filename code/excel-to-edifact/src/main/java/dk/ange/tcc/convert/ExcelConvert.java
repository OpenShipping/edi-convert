package dk.ange.tcc.convert;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObjectFactory;

/**
 * Converter that will read Excel files and put the result in a Stowbase or export as COPRAR.
 */
public class ExcelConvert {

    private final StowbaseObjectFactory stowbase;

    private final CoprarExporter coprar;

    private final String vesselImo;

    /**
     * @param stowbase
     * @param coprar
     * @param vesselImo
     */
    public ExcelConvert(final StowbaseObjectFactory stowbase, final CoprarExporter coprar, final String vesselImo) {
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
        // We could try many formats here, but in order to show the "correct" error we only try one.
        new ExcelConvert2(stowbase, coprar, vesselImo).convert(workbook);
    }

}
