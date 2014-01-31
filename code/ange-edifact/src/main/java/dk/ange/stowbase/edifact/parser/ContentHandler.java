package dk.ange.stowbase.edifact.parser;

import dk.ange.stowbase.edifact.Segment;

/**
 * About position: should be a "path" to the segments using trigger elements when naming the groups. Examples from
 * BAPLIE v95B: EQA should have path "LOC/EQD/EQA", second LOC in group 2 should have path "LOC/LOC2".
 */
public interface ContentHandler {

    /**
     * @param position
     */
    void startGroup(String position);

    /**
     * @param position
     */
    void endGroup(String position);

    /**
     * @param position
     * @param segment
     */
    void segment(String position, Segment segment);

}
