package org.helioviewer.plugins.eveplugin.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.helioviewer.base.logging.Log;
import org.helioviewer.base.message.Message;
import org.helioviewer.jhv.internal_plugins.filter.SOHOLUTFilterPlugin.LUT;
import org.helioviewer.plugins.eveplugin.radio.filter.FilterModel;

public class RadioOptionsPanel extends JPanel implements ActionListener {

    private JComboBox lut;
    private JLabel color;

    private final Map<String, LUT> lutMap;

    public RadioOptionsPanel() {
        super();
        lutMap = LUT.getStandardList();
        initVisualComponents();
    }

    private void initVisualComponents() {

        setLayout(new GridBagLayout());

        lut = new JComboBox(lutMap.keySet().toArray());
        lut.setSelectedItem("Rainbow 2");
        lut.addActionListener(this);
        color = new JLabel("Color:");

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;

        panel.add(color, gc);

        gc.gridx = 1;
        gc.weightx = 1;
        panel.add(lut, gc);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.fill = GridBagConstraints.BOTH;
        add(panel, gc);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LUT newMap = lutMap.get(lut.getSelectedItem());
        FilterModel.getInstance().setLUT(LUT.getStandardList().get((lut.getSelectedItem())));
    }

    /**
     * Adds a color table to the available list and set it active
     * 
     * @param lut
     *            Color table to add
     */
    public void addLut(LUT newLut) {
        if (lutMap.put(newLut.getName(), newLut) == null) {
            lut.addItem(newLut.getName());
        }
        lut.setSelectedItem(newLut.getName());
        FilterModel.getInstance().setLUT(newLut);
    }

}
