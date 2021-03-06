package org.helioviewer.jhv.io;

import java.net.URI;

import org.helioviewer.jhv.JHVGlobals;
import org.helioviewer.jhv.layers.ImageLayer;
import org.helioviewer.jhv.view.BaseView;

public interface Load {

    void get(URI uri);

    Load image = new Image();
    Load request = new Request();
    Load fits = new FITS();
    Load state = new State();

    class Image implements Load {
        @Override
        public void get(URI uri) {
            String scheme = uri.getScheme();
            ImageLayer layer = ImageLayer.create(null);
            JHVGlobals.getExecutorService().execute("http".equals(scheme) || "https".equals(scheme) ?
                    new DownloadViewTask(layer, new BaseView(uri, null)) :
                    new LoadViewTask(layer, uri));
        }
    }

    class Request implements Load {
        @Override
        public void get(URI uri) {
            JHVGlobals.getExecutorService().execute(new LoadRequestTask(uri));
        }
    }

    class FITS implements Load {
        @Override
        public void get(URI uri) {
            JHVGlobals.getExecutorService().execute(new LoadFITSTask(ImageLayer.create(null), uri));
        }
    }

    class State implements Load {
        @Override
        public void get(URI uri) {
            String name = uri.getPath().toLowerCase();
            if (name.endsWith("jhvz"))
                JHVGlobals.getExecutorService().execute(new LoadZipTask(uri));
            else
                JHVGlobals.getExecutorService().execute(new LoadStateTask(uri));
        }
    }

}
