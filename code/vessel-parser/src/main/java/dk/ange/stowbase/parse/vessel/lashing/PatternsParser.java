package dk.ange.stowbase.parse.vessel.lashing;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.LashingPattern;
import org.stowbase.client.objects.StackLashingPattern;

import dk.ange.stowbase.parse.utils.CellValue;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.RowData;
import dk.ange.stowbase.parse.utils.SingleSheetParser;

/**
 * Parses the "Pattern" sheet that contains lashing patterns for stacks
 */
public class PatternsParser extends SingleSheetParser {

    private final List<RowData> data;

    /**
     * Construct and parse
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public PatternsParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        if (sheet == null) {
            data = null;
            return; // Skip if sheet is missing
        }
        data = readColumnSheet(sheet);
    }

    @Override
    public String getSheetName() {
        return "Patterns";
    }

    /**
     * @param lashingPattern
     */
    public void addDataToLashingPattern(final LashingPattern lashingPattern) {
        final List<StackLashingPattern> stackLashingPatterns = new ArrayList<>();
        for (final RowData rowData : data) {
            final CellValue patternName = rowData.get("Pattern name");
            if (patternName.headerMissing()) {
                throw new RuntimeException("'Pattern name' is missing");
            }
            if (rowData.get("Pattern name").asString("").equals("")) {
                continue; // Skip row for empty name
            }
            final StackLashingPattern stackLashingPattern = StackLashingPattern.create(stowbaseObjectFactory);
            stackLashingPatterns.add(stackLashingPattern);
            stackLashingPattern.setPatternName(patternName.asString());

            stackLashingPattern.setMinimumNumberOfContainersInStack(rowData
                    .get("Minimum number of containers in stack").asInt());
            final CellValue minimumHcCountUnderLashing = rowData.get("Minimum HC count under lashing");
            if (minimumHcCountUnderLashing.hasData()) {
                stackLashingPattern.setMinimumHcCountUnderLashing(minimumHcCountUnderLashing.asInt());
            }
            stackLashingPattern.setAttachedToContainerNumber(rowData.get("Attached to container number").asInt());

            stackLashingPattern.setPosition(rowData.get("Position").asString());
            stackLashingPattern.setSide(rowData.get("Side").asString());

            stackLashingPattern.setEModuleInNewtonPerSquareMeter( //
                    rowData.get("E-module / kN/cm^2").asDouble() * 1e3 / 1e-4);
            stackLashingPattern.setDiameterInMeter(rowData.get("Diameter / cm").asDouble() / 100);
            stackLashingPattern.setMaxLashingForceInNewton(rowData.get("Max lashing force / kN").asDouble() * 1000);
            final CellValue lashingBridgeDeformationInMeter = rowData.get("Lashing bridge deformation / cm");
            if (lashingBridgeDeformationInMeter.hasData()) {
                stackLashingPattern
                        .setLashingBridgeDeformationInMeter(lashingBridgeDeformationInMeter.asDouble() / 100);
            }

            final CellValue angleInDegrees = rowData.get("Angle / degrees");
            if (angleInDegrees.hasData()) {
                stackLashingPattern.setAngleInDegrees(angleInDegrees.asDouble());
                stackLashingPattern.setRodLengthInMeter(rowData.get("Rod length / m").asDouble());
            } else {
                stackLashingPattern.setLashingPlatePositionVerticalInMeter(rowData.get(
                        "Lashing plate position vertical / m").asDouble());
                stackLashingPattern.setLashingDistanceTransverseInMeter(rowData.get("Lashing distance transverse / m")
                        .asDouble());
            }
        }
        lashingPattern.setPatterns(stackLashingPatterns);
    }

}
