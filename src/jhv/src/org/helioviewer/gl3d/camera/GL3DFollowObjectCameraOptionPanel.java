package org.helioviewer.gl3d.camera;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Date;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.helioviewer.base.logging.Log;
import org.helioviewer.basegui.components.TimeTextField;
import org.helioviewer.jhv.display.Displayer;
import org.helioviewer.jhv.gui.components.calendar.JHVCalendarDatePicker;
import org.helioviewer.jhv.gui.components.calendar.JHVCalendarEvent;
import org.helioviewer.jhv.gui.components.calendar.JHVCalendarListener;
import org.helioviewer.jhv.layers.LayersModel;

public class GL3DFollowObjectCameraOptionPanel extends GL3DCameraOptionPanel {
    private final JLabel beginDateLabel;
    private JPanel beginDatetimePanel;
    JHVCalendarDatePicker beginDatePicker;
    TimeTextField beginTimePicker;

    private final JLabel endDateLabel;
    private JPanel endDatetimePanel;
    JHVCalendarDatePicker endDatePicker;
    TimeTextField endTimePicker;
    JComboBox objectCombobox;
    private final GL3DFollowObjectCamera camera;

    public GL3DFollowObjectCameraOptionPanel(GL3DFollowObjectCamera camera) {
        this.camera = camera;
        setLayout(new GridLayout(0, 1));
        addObjectCombobox();
        beginDateLabel = new JLabel("Begin date");
        addDatePanel(beginDateLabel, beginDatetimePanel, beginDatePicker, beginTimePicker, true);
        endDateLabel = new JLabel("End date");
        addDatePanel(endDateLabel, endDatetimePanel, endDatePicker, endTimePicker, false);
    }

    private void addObjectCombobox() {
        objectCombobox = new JComboBox();
        objectCombobox.addItem("Solar Orbiter");
        objectCombobox.addItem("Venus");
        add(objectCombobox);
    }

    private void addDatePanel(JLabel dateLabel, JPanel datetimePanel, JHVCalendarDatePicker datePickerr, TimeTextField timePickerr, boolean begin) {
        add(dateLabel);
        datetimePanel = new JPanel();
        datetimePanel.setLayout(new GridLayout(0, 2));
        final TimeTextField timePicker = new TimeTextField();
        final JHVCalendarDatePicker datePicker = new JHVCalendarDatePicker();
        datePickerr = datePicker;
        datePicker.addJHVCalendarListener(new JHVCalendarListener() {
            @Override
            public void actionPerformed(JHVCalendarEvent e) {
                try {
                    Date dt = TimeTextField.formatter.parse(timePicker.getText());
                    camera.setBeginDate(new Date(datePicker.getDate().getTime() + dt.getTime()));
                } catch (ParseException e1) {
                    Log.error("Date parsing failed", e1);
                }
                Displayer.getSingletonInstance().render();
            }
        });
        Date startDate = null;
        if (begin) {
            startDate = LayersModel.getSingletonInstance().getFirstDate();
            datePicker.setDate(startDate);
        } else {
            startDate = LayersModel.getSingletonInstance().getLastDate();
            datePicker.setDate(startDate);
        }
        timePickerr = timePicker;
        timePicker.setText(TimeTextField.formatter.format(startDate));
        timePicker.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Date dt = TimeTextField.formatter.parse(timePicker.getText());
                    camera.setBeginDate(new Date(datePicker.getDate().getTime() + dt.getTime()));
                } catch (ParseException e1) {
                    Log.error("Date parsing failed", e1);
                }
                Displayer.getSingletonInstance().render();
            }
        });
        datetimePanel.add(datePicker);
        datetimePanel.add(timePicker);
        add(datetimePanel);
    }
}
