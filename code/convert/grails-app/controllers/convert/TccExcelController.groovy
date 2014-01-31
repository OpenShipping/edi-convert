package convert

import java.io.OutputStreamWriter
import java.io.StringWriter;

import org.springframework.web.multipart.MultipartFile;

import dk.ange.tcc.convert.ExcelConvert


class TccExcelController {

    def index = {}

    def save = {
        def file = request.getFile("file")
        def buffer = new StringWriter()
        ExcelConvert.convert(file.inputStream, params.imoNumber, buffer)
        new Attachment(response, "text/json", file.getOriginalFilename() + ".json").send(buffer)
    }

}
