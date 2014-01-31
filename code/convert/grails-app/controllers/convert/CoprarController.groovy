package convert

import dk.ange.tcc.convert.CoprarParser


class CoprarController {

    def index = {}

    def convert = {
        def file = request.getFile("file")
        session.coprarFileName = file.getOriginalFilename() + ".edi"
        def vesselImo  = params.vesselImo
        def vesselName  = params.vesselName
        def result = CoprarParser.parse(file.inputStream, vesselImo, vesselName)
        session.result = result
        if (result.coprarData == null) {
            redirect(action: "failed")
        }
    }

    def failed = {}

    def downloadCoprar = {
        new Attachment(response, "application/x-edifact", session.coprarFileName).send(session.result.coprarData)
    }
}
