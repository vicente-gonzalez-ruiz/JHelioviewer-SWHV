package org.helioviewer.jhv.view.j2k.image;

public class ImageParams {

    public boolean priority;
    public final int serialNo;
    public final DecodeParams decodeParams;

    public ImageParams(boolean _priority, int _serialNo, DecodeParams _decodeParams) {
        priority = _priority;
        serialNo = _serialNo;
        decodeParams = _decodeParams;
    }

}