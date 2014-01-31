package convert

import dk.ange.tcc.convert.StowageParser


class StowageController {

    def index = {}

    def convert = {
        def file = request.getFile("file")
        session.jsonFileName = file.getOriginalFilename() + ".sto"
        def result = StowageParser.parse(file.inputStream)
        session.result = result
        if (result.jsonData == null) {
            redirect(action: "failed")
        }
    }

    def failed = {}

    def downloadStowage = {
        new Attachment(response, "application/x-stowage", session.jsonFileName).send(session.result.jsonData)
    }

}
