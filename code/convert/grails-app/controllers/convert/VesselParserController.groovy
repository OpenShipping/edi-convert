package convert

import dk.ange.stowbase.parse.vessel.ParseVessel;

class VesselParserController {

    def index = {}

    def convert = {
        def file = request.getFile("file")
        session.fileName = file.getOriginalFilename() + ".json"
        def result = ParseVessel.parse(file.inputStream)
        session.result = result
        if (result.json == null) {
            redirect(action: "failed")
        }
    }

    def failed = {}

    def download = {
        new Attachment(response, "text/json", session.fileName).send(session.result.json)
    }

}
