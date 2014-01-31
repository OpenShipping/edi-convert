package dk.ange.tcc.convert;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.junit.Test;
import org.stowbase.client.export.WriterExporter;

/**
 * Test {@link ExcelConvert}
 */
public class TestConvert {

    /**
     * Test convert()
     * 
     * @throws Exception
     */
    @Test
    public void convert() throws Exception {
        doConvert("loadlist3.xls");
        doConvert("COPRAR_NAD_0310.xls");
    }

    private String doConvert(final String resourceName) throws IOException {
        final StringWriter stringWriter = new StringWriter();
        final WriterExporter exporter = new WriterExporter(stringWriter);
        final FileOutputStream coprarStream = new FileOutputStream(resourceName+".edi");
        final CoprarExporter coprar = new CoprarExporter("9137894", "Ange Test Vessel", coprarStream);
        final ExcelConvert convert = new ExcelConvert(exporter.stowbaseObjectFactory(), coprar, "9137894");
        final InputStream is = TestConvert.class.getResourceAsStream(resourceName);
        convert.convert(is);
        is.close();
        exporter.flush(resourceName + ".json");
        coprarStream.close();
        return stringWriter.toString();
    }

    /**
     * Test {@link ExcelConvert2}
     * 
     * @throws Exception
     */
    @Test
    public void convert2() throws Exception {
        final StringWriter stringWriter = new StringWriter();
        final WriterExporter exporter = new WriterExporter(stringWriter);
        final FileOutputStream coprarStream = new FileOutputStream("convert2.edi");
        final CoprarExporter coprar = new CoprarExporter("9137894", "Ange test vessel", coprarStream);
        final ExcelConvert2 convert = new ExcelConvert2(exporter.stowbaseObjectFactory(), coprar, "9137894");
        final InputStream is = TestConvert.class.getResourceAsStream("loadlist2.xls");
        convert.convert(is);
        is.close();
        exporter.flush("convert2.json");
    }

}
