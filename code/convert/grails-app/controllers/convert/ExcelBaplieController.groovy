package convert

import dk.ange.tcc.convert.ExcelBaplieParser


class ExcelBaplieController {

    def index = {}

    def convert = {
        def file = request.getFile("file")
        session.baplieFileName = file.getOriginalFilename() + ".edi"
        def vesselImo  = params.vesselImo
        def vesselName  = params.vesselName
        def result = ExcelBaplieParser.parse(file.inputStream, vesselImo, vesselName)
        session.result = result
        if (result.baplieData == null) {
            redirect(action: "failed")
        }
    }

    def failed = {}

    def downloadBaplie = {
        new Attachment(response, "application/x-edifact", session.baplieFileName).send(session.result.baplieData)
    }
}

