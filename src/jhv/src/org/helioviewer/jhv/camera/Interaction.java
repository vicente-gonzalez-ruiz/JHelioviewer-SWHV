package org.helioviewer.jhv.camera;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.helioviewer.jhv.display.Displayer;
import org.helioviewer.jhv.display.Viewport;

import com.jogamp.opengl.GL2;

public class Interaction implements MouseWheelListener, MouseMotionListener, MouseListener, KeyListener {

    protected Camera camera;

    public Interaction(Camera _camera) {
        camera = _camera;
    }

    public void drawInteractionFeedback(Viewport vp, GL2 gl) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        camera.zoom(Displayer.CAMERA_ZOOM_MULTIPLIER_WHEEL * e.getWheelRotation());
        Displayer.render(1);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            camera.reset();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

}
