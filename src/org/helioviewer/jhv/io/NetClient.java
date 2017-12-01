package org.helioviewer.jhv.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

//import java.util.logging.Level;
//import java.util.logging.Logger;

import org.helioviewer.jhv.JHVGlobals;
import org.helioviewer.jhv.base.FileUtils;
import org.helioviewer.jhv.log.Log;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;

public class NetClient implements AutoCloseable {

    //static {
    //    Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
    //}

    private static final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(JHVGlobals.getStdConnectTimeout(), TimeUnit.MILLISECONDS)
        .readTimeout(JHVGlobals.getStdReadTimeout(), TimeUnit.MILLISECONDS)
        //.addInterceptor(new LoggingInterceptor())
        .build();

    private final Response response;
    private final InputStream stream;

    public NetClient(String url) throws IOException {
        this(new URL(url), false);
    }

    public NetClient(URL url) throws IOException {
        this(url, false);
    }

    public NetClient(String url, boolean allowError) throws IOException {
        this(new URL(url), allowError);
    }

    public NetClient(URL url, boolean allowError) throws IOException {
        if ("file".equals(url.getProtocol())) {
            stream = FileUtils.newBufferedInputStream(new File(url.getPath()));
            response = null;
            return;
        }

        Request request = new Request.Builder().header("User-Agent", JHVGlobals.userAgent).url(url).build();
        response = client.newCall(request).execute();
        if (!allowError && !response.isSuccessful())
            throw new IOException(response.toString());
        stream = null;
    }

    public boolean isSuccessful() {
        return response.isSuccessful();
    }

    public InputStream getStream() {
        return stream == null ? response.body().byteStream() : stream;
    }

    public Reader getReader() {
        return stream == null ? response.body().charStream() : new InputStreamReader(stream, StandardCharsets.UTF_8);
    }

    public BufferedSource getSource() {
        return response.body().source();
    }

    public long getContentLength() {
        return response.body().contentLength();
    }

    @Override
    public void close() throws IOException {
        if (stream != null)
            stream.close();
        if (response != null)
            response.close();
    }

    private static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            long t1 = System.nanoTime();
            Request r1 = chain.request();
            Log.info(String.format("Sending request %s on %s%n%s", r1.url(), chain.connection(), r1.headers()));

            Response r2 = chain.proceed(r1);
            long t2 = System.nanoTime();
            Log.info(String.format("Received response for %s in %.1fms%n%s", r1.url(), (t2 - t1) / 1e6d, r2.networkResponse().headers()));

            return r2;
        }
    }

}
