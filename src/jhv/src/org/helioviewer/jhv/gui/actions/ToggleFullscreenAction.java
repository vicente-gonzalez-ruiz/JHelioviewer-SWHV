package org.helioviewer.jhv.gui.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.helioviewer.jhv.gui.ImageViewerGui;
import org.helioviewer.jhv.gui.components.MoviePanel;
import org.helioviewer.jhv.input.KeyShortcuts;

import com.jogamp.newt.opengl.GLWindow;

@SuppressWarnings("serial")
public class ToggleFullscreenAction extends AbstractAction {

    private final KeyStroke exitKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
    private final KeyStroke playKey = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true);

    public ToggleFullscreenAction() {
        super("Toggle Full Screen");
        putValue(SHORT_DESCRIPTION, "Toggle full screen");

        KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), true);
        putValue(ACCELERATOR_KEY, toggleKey);
        KeyShortcuts.registerKey(toggleKey, this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final GLWindow window = ImageViewerGui.getGLWindow();
        final boolean full = window.isFullscreen();
        if (full) {
            KeyShortcuts.unregisterKey(exitKey);
            KeyShortcuts.unregisterKey(playKey);
        } else {
            KeyShortcuts.registerKey(exitKey, this);
            KeyShortcuts.registerKey(playKey, MoviePanel.getPlayPauseAction());
        }

        new Thread() {
            @Override
            public void run() {
                Thread t = window.setExclusiveContextThread(null);
                window.setFullscreen(!full);
                window.setExclusiveContextThread(t);
            }
        }.start();
    }

}
