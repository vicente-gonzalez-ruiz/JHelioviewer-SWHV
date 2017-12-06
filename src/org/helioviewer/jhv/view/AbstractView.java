package org.helioviewer.jhv.view;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import org.helioviewer.jhv.JHVGlobals;
import org.helioviewer.jhv.base.lut.LUT;
import org.helioviewer.jhv.camera.Camera;
import org.helioviewer.jhv.display.Viewport;
import org.helioviewer.jhv.imagedata.ImageData;
import org.helioviewer.jhv.imagedata.ImageDataHandler;
import org.helioviewer.jhv.io.APIRequest;
import org.helioviewer.jhv.io.DownloadViewTask;
import org.helioviewer.jhv.layers.ImageLayer;
import org.helioviewer.jhv.metadata.HelioviewerMetaData;
import org.helioviewer.jhv.metadata.MetaData;
import org.helioviewer.jhv.time.JHVDate;

public class AbstractView implements View {

    private static final AtomicBoolean fullCache = new AtomicBoolean(true);

    private ImageLayer imageLayer;

    protected final URI uri;
    protected final APIRequest req;
    protected ImageData imageData;
    protected LUT builtinLUT;
    protected MetaData metaData[] = new MetaData[] { null };

    public AbstractView(URI _uri, APIRequest _req) {
        uri = _uri;
        req = _req;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public String getName() {
        MetaData m = metaData[0];
        if (m instanceof HelioviewerMetaData)
            return ((HelioviewerMetaData) m).getFullName();
        else {
            String name = uri.getPath();
            return name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
        }
    }

    @Override
    public APIRequest getAPIRequest() {
        return req;
    }

    @Override
    public void abolish() {
    }

    @Override
    public void render(Camera camera, Viewport vp, double factor) {
        imageData.setViewpoint(camera.getViewpoint());
        if (dataHandler != null) {
            dataHandler.handleData(imageData);
        }
    }

    @Override
    public AtomicBoolean getFrameCacheStatus(int frame) {
        return fullCache;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public int getCurrentFramerate() {
        return 0;
    }

    @Override
    public boolean isMultiFrame() {
        return false;
    }

    @Override
    public int getCurrentFrameNumber() {
        return 0;
    }

    @Override
    public int getMaximumFrameNumber() {
        return 0;
    }

    @Override
    public void setFrame(JHVDate time) {
    }

    @Override
    public JHVDate getNextTime(AnimationMode mode, int deltaT) {
        return null;
    }

    @Override
    public JHVDate getFrameTime(JHVDate time) {
        return getFirstTime();
    }

    @Override
    public JHVDate getFirstTime() {
        return metaData[0].getViewpoint().time;
    }

    @Override
    public JHVDate getLastTime() {
        return getFirstTime();
    }

    @Override
    public JHVDate getFrameTime(int frame) {
        return getFirstTime();
    }

    @Override
    public MetaData getMetaData(JHVDate time) {
        return metaData[0];
    }

    @Override
    public LUT getDefaultLUT() {
        if (builtinLUT != null)
            return builtinLUT;
        MetaData m = metaData[0];
        return m instanceof HelioviewerMetaData ? LUT.get((HelioviewerMetaData) m) : null;
    }

    @Override
    public void setImageLayer(ImageLayer _imageLayer) {
        imageLayer = _imageLayer;
    }

    @Override
    public ImageLayer getImageLayer() {
        return imageLayer;
    }

    protected ImageDataHandler dataHandler;

    @Override
    public void setDataHandler(ImageDataHandler _dataHandler) {
        dataHandler = _dataHandler;
    }

    @Override
    public boolean isDownloading() {
        return false;
    }

    @Override
    public String getXMLMetaData() throws Exception {
        return "<meta/>";
    }

    private DownloadViewTask downloadTask;

    @Override
    public void startDownload() {
        if (downloadTask != null)
            downloadTask.cancel(true);
        downloadTask = new DownloadViewTask(this);
        JHVGlobals.getExecutorService().execute(downloadTask);
    }

    @Override
    public void stopDownload() {
        if (downloadTask != null) {
            downloadTask.cancel(true);
            downloadTask = null;
        }
    }

}
