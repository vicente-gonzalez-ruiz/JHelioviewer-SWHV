package org.helioviewer.jhv.renderable.components;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.helioviewer.jhv.display.Displayer;
import org.helioviewer.jhv.gui.components.base.DegreeFormatterFactory;
import org.helioviewer.jhv.gui.components.base.WheelSupport;

@SuppressWarnings("serial")
public class RenderableGridOptionsPanel extends JPanel {

    private JSpinner gridResolutionXSpinner;
    private JSpinner gridResolutionYSpinner;
    RenderableGrid grid;

    public RenderableGridOptionsPanel(RenderableGrid renderableGrid) {
        grid = renderableGrid;
        createGridResolutionX(grid);
        createGridResolutionY(grid);

        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);

        GridBagConstraints c0 = new GridBagConstraints();
        c0.anchor = GridBagConstraints.EAST;
        c0.weightx = 1.;
        c0.weighty = 1.;
        c0.gridy = 0;

        c0.gridx = 0;
        JCheckBox labels = new JCheckBox("Labels", true);
        labels.setHorizontalTextPosition(SwingConstants.LEFT);
        labels.setPreferredSize(new Dimension(labels.getPreferredSize().width, 22));
        labels.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                grid.showLabels(e.getStateChange() == ItemEvent.SELECTED);
                Displayer.display();
            }
        });
        add(labels, c0);

        c0.gridx = 1;
        add(new JLabel("Longitude", JLabel.RIGHT), c0);

        gridResolutionXSpinner.setMinimumSize(new Dimension(42, 22));
        gridResolutionXSpinner.setPreferredSize(new Dimension(62, 22));
        gridResolutionXSpinner.setMaximumSize(new Dimension(82, 22));
        JFormattedTextField fx = ((JSpinner.DefaultEditor) gridResolutionXSpinner.getEditor()).getTextField();
        fx.setFormatterFactory(new DegreeFormatterFactory("%.1f\u00B0"));

        c0.anchor = GridBagConstraints.WEST;
        c0.gridx = 2;
        add(gridResolutionXSpinner, c0);

        c0.anchor = GridBagConstraints.EAST;
        c0.gridx = 3;
        add(new JLabel("Latitude", JLabel.RIGHT), c0);

        gridResolutionYSpinner.setMinimumSize(new Dimension(42, 22));
        gridResolutionYSpinner.setPreferredSize(new Dimension(62, 22));
        gridResolutionYSpinner.setMaximumSize(new Dimension(82, 22));
        JFormattedTextField fy = ((JSpinner.DefaultEditor) gridResolutionYSpinner.getEditor()).getTextField();
        fy.setFormatterFactory(new DegreeFormatterFactory("%.1f\u00B0"));

        c0.anchor = GridBagConstraints.WEST;
        c0.gridx = 4;
        add(gridResolutionYSpinner, c0);
    }

    public void createGridResolutionX(RenderableGrid renderableGrid) {
        gridResolutionXSpinner = new JSpinner();
        gridResolutionXSpinner.setModel(new SpinnerNumberModel(new Double(renderableGrid.getLonstepDegrees()), new Double(1), new Double(90), new Double(0.1)));
        gridResolutionXSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                grid.setLonstepDegrees((Double) gridResolutionXSpinner.getValue());
                Displayer.display();
            }
        });
        WheelSupport.installMouseWheelSupport(gridResolutionXSpinner);
    }

    public void createGridResolutionY(RenderableGrid renderableGrid) {
        gridResolutionYSpinner = new JSpinner();
        gridResolutionYSpinner.setModel(new SpinnerNumberModel(new Double(renderableGrid.getLatstepDegrees()), new Double(1), new Double(90), new Double(0.1)));
        gridResolutionYSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                grid.setLatstepDegrees((Double) gridResolutionYSpinner.getValue());
                Displayer.display();
            }
        });
        WheelSupport.installMouseWheelSupport(gridResolutionYSpinner);
    }

}
