package convert

import dk.ange.stowbase.parse.vessel.ParseVessel;
import dk.ange.stowbase.parse.vessel.VesselFormatDocumentation;

class VesselParserController {

    def index = {}

    def convert = {
        def file = request.getFile("file")
        session.fileName = (file.getOriginalFilename() =~ /\.xls/).replaceFirst("") + ".json.gz"
        def result = ParseVessel.parse(file.inputStream)
        session.result = result
        if (result.json == null) {
            redirect(action: "failed")
        }
    }

    def failed = {}

    def download = {
        new Attachment(response, "application/x-old-vessel", session.fileName).send(
            ParseVessel.compress(session.result.json.getBytes("UTF-8")))
    }

    def format = {
        session.format = VesselFormatDocumentation.asHtml()
    }

}
