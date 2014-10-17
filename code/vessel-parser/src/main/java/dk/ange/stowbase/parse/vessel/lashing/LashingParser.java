package dk.ange.stowbase.parse.vessel.lashing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Workbook;
import org.stowbase.client.StowbaseObjectFactory;
import org.stowbase.client.objects.LashingPattern;
import org.stowbase.client.objects.LashingStackData;

import dk.ange.stowbase.parse.utils.BRL;
import dk.ange.stowbase.parse.utils.Messages;
import dk.ange.stowbase.parse.utils.SingleSheetParser;

/**
 * Parses the "Lashing" sheet
 */
public class LashingParser extends SingleSheetParser {

    final LashingStackParser lashingStackParser = new LashingStackParser();

    /**
     * Construct and parse
     *
     * @param stowbaseObjectFactory
     * @param messages
     * @param workbook
     */
    public LashingParser(final StowbaseObjectFactory stowbaseObjectFactory, final Messages messages,
            final Workbook workbook) {
        super(stowbaseObjectFactory, messages, workbook);
        if (sheet == null) {
            return; // Skip if sheet is missing
        }
        lashingStackParser.readSheet(sheet);
    }

    @Override
    public String getSheetName() {
        return "Lashing";
    }

    /**
     * @param lashingPattern
     */
    public void addDataToLashingPattern(final LashingPattern lashingPattern) {
        final TreeSet<BRL> keys = new TreeSet<>(lashingStackParser.stackPatternNameAftMap.keySet());
        keys.addAll(lashingStackParser.stackPatternNameForeMap.keySet());
        keys.addAll(lashingStackParser.cellGuidesToTierMap.keySet());
        final List<LashingStackData> lashingStackDatas = new ArrayList<>();
        for (final BRL brl : keys) {
            final LashingStackData lashingStackData = LashingStackData.create(stowbaseObjectFactory);
            lashingStackDatas.add(lashingStackData);
            lashingStackData.setStack(brl.bay, brl.row, brl.level);
            final String patternNameAft = lashingStackParser.stackPatternNameAftMap.get(brl);
            if (patternNameAft != null) {
                lashingStackData.setPatternNameAft(patternNameAft);
            }
            final String patternNameFore = lashingStackParser.stackPatternNameForeMap.get(brl);
            if (patternNameFore != null) {
                lashingStackData.setPatternNameFore(patternNameFore);
            }
            final Double cellGuidesToTier = lashingStackParser.cellGuidesToTierMap.get(brl);
            if (cellGuidesToTier != null) {
                lashingStackData.setCellGuidesToTier(cellGuidesToTier);
            }
        }
        lashingPattern.setStack(lashingStackDatas);
    }

    private static class LashingStackParser extends BrlSectionParser {
        final HashMap<BRL, String> stackPatternNameAftMap = new HashMap<>();

        final HashMap<BRL, String> stackPatternNameForeMap = new HashMap<>();

        final HashMap<BRL, Double> cellGuidesToTierMap = new HashMap<>();

        @Override
        protected void handleDataItem(final String sectionType, final BRL brl, final String cellString) {
            switch (sectionType.trim().toUpperCase()) {
            case "# CELL GUIDES":
                cellGuidesToTierMap.put(brl, Double.parseDouble(cellString));
                break;
            case "# LASHING PATTERN AFT":
                stackPatternNameAftMap.put(brl, cellString);
                break;
            case "# LASHING PATTERN FORE":
                stackPatternNameForeMap.put(brl, cellString);
                break;
            default:
                throw new RuntimeException("Unknown section '" + sectionType + "'");
            }
        }
    }

}
