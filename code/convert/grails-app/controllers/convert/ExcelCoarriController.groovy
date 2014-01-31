package convert

import dk.ange.tcc.convert.ExcelCoarriParser


class ExcelCoarriController {

    def index = {}

    def convert = {
        def file = request.getFile("file")
        session.coarriFileName = file.getOriginalFilename() + ".edi"
        def vesselImo  = params.vesselImo
        def vesselName  = params.vesselName
        def result = ExcelCoarriParser.parse(file.inputStream, vesselImo, vesselName)
        session.result = result
        if (result.coarriData == null) {
            redirect(action: "failed")
        }
    }

    def failed = {}

    def downloadCoarri = {
        new Attachment(response, "application/x-edifact", session.coarriFileName).send(session.result.coarriData)
    }
}

