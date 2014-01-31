package convert

import java.io.OutputStreamWriter;
import java.io.StringWriter;

import dk.ange.stowbase.edifact.baplie.BaplieContentHandler;

class BaplieController {

    def index = {}
    
    def save = {
        def file = request.getFile("file")
        def buffer = new StringWriter()
        Baplie.convertBaplie(file.inputStream, params.imoNumber, buffer)
        new Attachment(response, "text/json", file.getOriginalFilename() + ".json").send(buffer)
    }
    
}
