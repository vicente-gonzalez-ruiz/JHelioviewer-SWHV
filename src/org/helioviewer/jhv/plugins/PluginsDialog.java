package org.helioviewer.jhv.plugins;

import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.helioviewer.jhv.gui.JHVFrame;
import org.helioviewer.jhv.gui.dialogs.CloseButtonPanel;
import org.helioviewer.jhv.gui.interfaces.ShowableDialog;

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;

@SuppressWarnings("serial")
public class PluginsDialog extends StandardDialog implements ShowableDialog {

    public PluginsDialog() {
        super(JHVFrame.getFrame(), "Plug-in Manager", true);
        setResizable(false);
    }

    @Override
    public ButtonPanel createButtonPanel() {
        return new CloseButtonPanel(this);
    }

    @Override
    public JComponent createContentPanel() {
        JComponent component = new JScrollPane(new PluginsList());
        component.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        return component;
    }

    @Nullable
    @Override
    public JComponent createBannerPanel() {
        return null;
    }

    @Override
    public void showDialog() {
        pack();
        setLocationRelativeTo(JHVFrame.getFrame());
        setVisible(true);
    }

}
