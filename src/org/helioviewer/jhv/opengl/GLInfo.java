package org.helioviewer.jhv.opengl;

import org.helioviewer.jhv.gui.Message;
import org.helioviewer.jhv.log.Log;

import com.jogamp.nativewindow.ScalableSurface;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

public class GLInfo {

    public static final int GLSAMPLES = 4;

    public static final int[] pixelScale = {1, 1};
    public static final float[] pixelScaleFloat = {1, 1};
    public static String glVersion = "";

    public static int maxTextureSize;

    static void glVersionError(String err) {
        Log.error("GLInfo > " + err);
        Message.err("OpenGL fatal error, JHelioviewer is not able to run:\n", Message.formatMessage(err), true);
    }

    public static void update(GL2 gl) {
        glVersion = "OpenGL " + gl.glGetString(GL2.GL_VERSION);
        Log.info("GLInfo > " + glVersion);
        // Log.debug("GLInfo > Extensions: " + gl.glGetString(GL2.GL_EXTENSIONS));
        if (!gl.isExtensionAvailable("GL_VERSION_3_1")) {
            glVersionError("OpenGL 3.1 not supported.");
        }

        int[] out = {0};
        gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_SIZE, out, 0);
        maxTextureSize = out[0];
    }

    public static void updatePixelScale(ScalableSurface surface) {
        surface.getCurrentSurfaceScale(pixelScaleFloat);
        pixelScale[0] = (int) pixelScaleFloat[0];
        pixelScale[1] = (int) pixelScaleFloat[1];
    }

    public static boolean checkGLErrors(GL2 gl, String message) {
        GLU glu = new GLU();
        int glErrorCode, errors = 0;

        while ((glErrorCode = gl.glGetError()) != GL2.GL_NO_ERROR) {
            Log.error("GL Error (" + glErrorCode + "): " + glu.gluErrorString(glErrorCode) + " - @" + message);
            errors++;
        }
        return errors != 0;
    }

}
