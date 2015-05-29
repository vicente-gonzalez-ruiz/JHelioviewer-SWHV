package org.helioviewer.jhv.gui.dialogs.observation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.helioviewer.base.datetime.TimeUtils;
import org.helioviewer.base.logging.Log;
import org.helioviewer.base.message.Message;
import org.helioviewer.jhv.Settings;
import org.helioviewer.jhv.gui.ImageViewerGui;
import org.helioviewer.jhv.gui.components.base.TimeTextField;
import org.helioviewer.jhv.gui.components.calendar.JHVCalendarDatePicker;
import org.helioviewer.jhv.gui.components.calendar.JHVCalendarEvent;
import org.helioviewer.jhv.gui.components.calendar.JHVCalendarListener;
import org.helioviewer.jhv.gui.dialogs.model.ObservationDialogDateModel;
import org.helioviewer.jhv.gui.dialogs.model.ObservationDialogDateModelListener;
import org.helioviewer.jhv.io.APIRequestManager;
import org.helioviewer.jhv.io.DataSourceServerListener;
import org.helioviewer.jhv.io.DataSourceServers;
import org.helioviewer.jhv.io.DataSources;
import org.helioviewer.jhv.io.DataSources.Item;
import org.helioviewer.jhv.layers.LayersModel;
import org.helioviewer.jhv.renderable.components.RenderableDummy;
import org.helioviewer.viewmodel.view.AbstractView;

/**
 * In order to select and load image data from the Helioviewer server this class
 * provides the corresponding user interface. The UI will be displayed within
 * the {@link ObservationDialog}.
 *
 * @author Stephan Pagel
 * */
@SuppressWarnings({"unchecked","rawtypes","serial"})
public class ImageDataPanel extends ObservationDialogPanel implements DataSourceServerListener {

    private boolean isSelected = false;

    private final TimeSelectionPanel timeSelectionPanel = new TimeSelectionPanel();
    private final CadencePanel cadencePanel = new CadencePanel();
    private final InstrumentsPanel instrumentsPanel;

    private boolean isFirst = true;

    /**
     * Used format for the API of the data and time
     */

    public ImageDataPanel() {
        super();
        instrumentsPanel = new InstrumentsPanel(this);

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JPanel timePane = new JPanel();
        timePane.setLayout(new BoxLayout(timePane, BoxLayout.PAGE_AXIS));
        timePane.add(timeSelectionPanel);
        timePane.add(cadencePanel);

        JPanel instrumentsPane = new JPanel();
        instrumentsPane.setLayout(new BorderLayout());
        instrumentsPane.add(instrumentsPanel, BorderLayout.CENTER);

        add(timePane);
        add(instrumentsPane);

        DataSourceServers.getSingletonInstance().addListener(this);
    }

    /**
     * Adds available data to the displayed components
     * */
    @Override
    public void serverChanged(boolean donotloadStartup) {
        instrumentsPanel.setupSources(DataSources.getSingletonInstance());
        // Check if we were able to set it up
        if (instrumentsPanel.validSelection()) {
            // first time ignore donotloadStartup - comes via comboServer.setSelectedItem() below
            if (isFirst || !donotloadStartup) {
                isFirst = false;
                timeSelectionPanel.setupTime(Boolean.parseBoolean(Settings.getSingletonInstance().getProperty("startup.loadmovie")));
            }
        } else {
            Message.err("Could not retrieve data sources", "The list of avaible data could not be fetched. So you cannot use the GUI to add data!" + System.getProperty("line.separator") + " This may happen if you do not have an internet connection or the there are server problems. You can still open local files.", false);
        }
    }

    /**
     * Returns the selected start time.
     *
     * @return selected start time.
     * */
    public String getStartTime() {
        return timeSelectionPanel.getStartTime();
    }

    /**
     * Returns the selected end time.
     *
     * @return seleted end time.
     */
    public String getEndTime() {
        return timeSelectionPanel.getEndTime();
    }

    /**
     * Set a new end date and time
     *
     * @param newEnd
     *            new start date and time
     */
    public void setEndDate(Date newEnd, boolean byUser) {
        timeSelectionPanel.setEndDate(newEnd, byUser);
    }

    /**
     * Set a new start date and time
     *
     * @param newStart
     *            new start date and time
     */
    public void setStartDate(Date newStart, boolean byUser) {
        timeSelectionPanel.setStartDate(newStart, byUser);
    }

    /**
     * Returns the selected cadence.
     *
     * @return selected cadence.
     */
    public String getCadence() {
        return Integer.toString(cadencePanel.getCadence());
    }

    /**
     * Returns the selected observatory.
     *
     * @return selected observatory.
     */
    public String getObservation() {
        return instrumentsPanel.getObservatory();
    }

    /**
     * Returns the selected instrument.
     *
     * @return selected instrument.
     * */
    public String getInstrument() {
        return instrumentsPanel.getInstrument();
    }

    /**
     * Returns the selected detector.
     *
     * @return selected detector.
     * */
    public String getDetector() {
        return instrumentsPanel.getDetector();
    }

    /**
     * Returns the selected measurement.
     *
     * @return selected measurement.
     * */
    public String getMeasurement() {
        return instrumentsPanel.getMeasurement();
    }

    /**
     * Updates the visual behavior of the component.
     */
    public void updateComponent() {
        timeSelectionPanel.updateDateFormat();
    }

    /**
     * Loads an image series from the Helioviewer server and adds a new layer to
     * the GUI which represents the image series.
     * */
    private void loadRemote(final boolean isImage) {
        // download and open the requested movie in a separated thread and hide
        // loading animation when finished
        final RenderableDummy renderableDummy = new RenderableDummy();
        ImageViewerGui.getRenderableContainer().addBeforeRenderable(renderableDummy);

        SwingWorker<AbstractView, Void> remoteTask = new SwingWorker<AbstractView, Void>() {

            @Override
            protected AbstractView doInBackground() {
                Thread.currentThread().setName("LoadRemote");
                AbstractView view = null;

                try {
                    if (isImage)
                        view = APIRequestManager.requestAndOpenRemoteFile(null, getStartTime(), "", getObservation(), getInstrument(), getDetector(), getMeasurement(), true);
                    else
                        view = APIRequestManager.requestAndOpenRemoteFile(getCadence(), getStartTime(), getEndTime(), getObservation(), getInstrument(), getDetector(), getMeasurement(), true);
                } catch (IOException e) {
                    Log.error("An error occured while opening the remote file!", e);
                    Message.err("An error occured while opening the remote file!", e.getMessage(), false);
                }

                return view;
            }

            @Override
            public void done() {
                ImageViewerGui.getRenderableContainer().removeRenderable(renderableDummy);
                try {
                    LayersModel.addLayer(get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };
        remoteTask.execute();
    }

    // Methods derived from Observation Dialog Panel

    /**
     * {@inheritDoc}
     * */
    @Override
    public void selected() {
        isSelected = true;
    }

    /**
     * {@inheritDoc}
     * */
    @Override
    public void deselected() {
        isSelected = false;
    }

    /**
     * {@inheritDoc}
     * */
    @Override
    public boolean loadButtonPressed() {
        // Add some data if its nice
        if (!instrumentsPanel.validSelection()) {
            Message.err("Data is not selected", "There is no information on what to add", false);
            return false;
        }

        try {
            ObservationDialogDateModel.getInstance().setStartDate(TimeUtils.apiDateFormat.parse(timeSelectionPanel.getStartTime()), true);
            ObservationDialogDateModel.getInstance().setEndDate(TimeUtils.apiDateFormat.parse(timeSelectionPanel.getEndTime()), true);
        } catch (ParseException e) {
            Log.debug("Date could not be parsed" + e);
        }

        // check if start date is before end date -> if not show message
        if (!timeSelectionPanel.isStartDateBeforeEndDate()) {
            JOptionPane.showMessageDialog(null, "End date is before start date", "", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        loadRemote(timeSelectionPanel.getStartTime().equals(timeSelectionPanel.getEndTime()));
        return true;
    }

    /**
     * {@inheritDoc}
     * */
    @Override
    public void cancelButtonPressed() {
    }

    /**
     * {@inheritDoc}
     * */
    @Override
    public void dialogOpened() {
    }

    // Time Selection Panel

    /**
     * The panel bundles the components to select the start and end time.
     *
     * @author Stephan Pagel
     * */
    private class TimeSelectionPanel extends JPanel implements JHVCalendarListener, ObservationDialogDateModelListener {

        private final TimeTextField textStartTime;
        private final TimeTextField textEndTime;
        private final JHVCalendarDatePicker calendarStartDate;
        private final JHVCalendarDatePicker calendarEndDate;

        private boolean setFromOutside = false;

        public TimeSelectionPanel() {
            // set up the visual components (GUI)
            ObservationDialogDateModel.getInstance().addListener(this);

            setLayout(new GridLayout(2, 2, GRIDLAYOUT_HGAP, GRIDLAYOUT_VGAP));

            // create end date picker
            calendarEndDate = new JHVCalendarDatePicker();
            calendarEndDate.setDateFormat(Settings.getSingletonInstance().getProperty("default.date.format"));
            calendarEndDate.addJHVCalendarListener(this);
            calendarEndDate.setToolTipText("UTC date for observation end");

            // create end time field
            textEndTime = new TimeTextField();
            textEndTime.setToolTipText("UTC time for observation end.\nIf equal to start time, a single image closest to the time will be added.");

            // create start date picker
            calendarStartDate = new JHVCalendarDatePicker();
            calendarStartDate.setDateFormat(Settings.getSingletonInstance().getProperty("default.date.format"));
            calendarStartDate.addJHVCalendarListener(this);
            calendarStartDate.setToolTipText("UTC date for observation start");

            // create start time field
            textStartTime = new TimeTextField();
            textStartTime.setToolTipText("UTC time for observation start");

            // set date format to components
            updateDateFormat();

            // add components to panel
            JPanel startDatePane = new JPanel(new BorderLayout());
            startDatePane.add(new JLabel("Start date"), BorderLayout.PAGE_START);
            startDatePane.add(calendarStartDate, BorderLayout.CENTER);

            JPanel startTimePane = new JPanel(new BorderLayout());
            startTimePane.add(new JLabel("Start time"), BorderLayout.PAGE_START);
            startTimePane.add(textStartTime, BorderLayout.CENTER);

            JPanel endDatePane = new JPanel(new BorderLayout());
            endDatePane.add(new JLabel("End date"), BorderLayout.PAGE_START);
            endDatePane.add(calendarEndDate, BorderLayout.CENTER);

            JPanel endTimePane = new JPanel(new BorderLayout());
            endTimePane.add(new JLabel("End time"), BorderLayout.PAGE_START);
            endTimePane.add(textEndTime, BorderLayout.CENTER);

            add(startDatePane);
            add(startTimePane);
            add(endDatePane);
            add(endTimePane);
        }

        /**
         * Sets the latest available image (or now if fails) to the end time and
         * the start 24h earlier.
         */
        public void setupTime(final boolean load) {

            class SetupTimeTask extends SwingWorker<Date, Void> {

                private final String observatory;
                private final String instrument;
                private final String detector;
                private final String measurement;

                SetupTimeTask(String _observatory, String _instrument, String _detector, String _measurement) {
                    observatory = _observatory;
                    instrument = _instrument;
                    detector = _detector;
                    measurement = _measurement;
                }

                @Override
                protected Date doInBackground() {
                    Thread.currentThread().setName("SetupTime");
                    return APIRequestManager.getLatestImageDate(observatory, instrument, detector, measurement, true);
                }

                @Override
                public void done() {
                    try {
                        Date endDate = get();
                        GregorianCalendar gregorianCalendar = new GregorianCalendar();
                        gregorianCalendar.setTime(endDate);

                        gregorianCalendar.add(GregorianCalendar.SECOND, cadencePanel.getCadence());
                        setEndDate(gregorianCalendar.getTime(), false);

                        gregorianCalendar.add(GregorianCalendar.DAY_OF_MONTH, -1);
                        setStartDate(gregorianCalendar.getTime(), false);

                        if (load)
                            loadRemote(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            SetupTimeTask setupTimeTask = new SetupTimeTask(
                                                instrumentsPanel.getObservatory(),
                                                instrumentsPanel.getInstrument(),
                                                instrumentsPanel.getDetector(),
                                                instrumentsPanel.getMeasurement());
            setupTimeTask.execute();
        }

        /**
         * Set a new end date and time
         *
         * @param newEnd
         *            new start date and time
         */
        public void setEndDate(Date newEnd, boolean byUser) {
            calendarEndDate.setDate(newEnd);
            textEndTime.setText(TimeUtils.timeDateFormat.format(newEnd));
            if (!setFromOutside) {
                ObservationDialogDateModel.getInstance().setEndDate(newEnd, byUser);
            } else {
                setFromOutside = false;
            }
        }

        /**
         * Set a new start date and time
         *
         * @param newStart
         *            new start date and time
         */
        public void setStartDate(Date newStart, boolean byUser) {
            calendarStartDate.setDate(newStart);
            textStartTime.setText(TimeUtils.timeDateFormat.format(newStart));
            if (!setFromOutside) {
                ObservationDialogDateModel.getInstance().setStartDate(newStart, byUser);
            } else {
                setFromOutside = false;
            }
        }

        /**
         * Updates the date format to the calendar components.
         */
        public void updateDateFormat() {
            String pattern = Settings.getSingletonInstance().getProperty("default.date.format");

            calendarStartDate.setDateFormat(pattern);
            calendarEndDate.setDateFormat(pattern);

            calendarStartDate.setDate(calendarStartDate.getDate());
            calendarEndDate.setDate(calendarEndDate.getDate());
        }

        /**
         * JHV calendar listener which notices when the user has chosen a date
         * by using the calendar component.
         */
        @Override
        public void actionPerformed(JHVCalendarEvent e) {
            if (e.getSource() == calendarStartDate) {
                GregorianCalendar calendar = new GregorianCalendar();
                try {
                    calendar.setTime(TimeUtils.apiDateFormat.parse(getStartTime()));
                    setStartDate(calendar.getTime(), true);
                } catch (ParseException e1) {
                    Log.error("Could not parse start date " + getStartTime());
                }
            }

            if (e.getSource() == calendarEndDate) {
                GregorianCalendar calendar = new GregorianCalendar();
                try {
                    calendar.setTime(TimeUtils.apiDateFormat.parse(getEndTime()));
                    setEndDate(calendar.getTime(), true);
                } catch (ParseException e1) {
                    Log.error("Could not parse end date " + getEndTime());
                }
            }
        }

        /**
         * Checks if the selected start date is before selected end date. The
         * methods checks the entered times when the dates are equal. If the
         * start time is greater or equal than the end time the method will
         * return false.
         *
         * @return boolean value if selected start date is before selected end
         *         date.
         */
        public boolean isStartDateBeforeEndDate() {
            return getStartTime().compareTo(getEndTime()) <= 0;
        }

        /**
         * Returns the selected start time.
         *
         * @return selected start time.
         * */
        public String getStartTime() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'");
            return dateFormat.format(calendarStartDate.getDate()) + textStartTime.getFormattedInput() + "Z";
        }

        /**
         * Returns the selected end time.
         *
         * @return selected end time.
         */
        public String getEndTime() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'");
            return dateFormat.format(calendarEndDate.getDate()) + textEndTime.getFormattedInput() + "Z";
        }

        @Override
        public void startDateChanged(Date startDate) {
            setFromOutside = true;
            setStartDate(startDate, false);
        }

        @Override
        public void endDateChanged(Date endDate) {
            setFromOutside = true;
            setEndDate(endDate, false);
        }
    }

    // Cadence Panel

    /**
     * The panel bundles the components to select the cadence.
     *
     * @author Stephan Pagel
     * */
    @SuppressWarnings("unused")
    private class CadencePanel extends JPanel implements ActionListener {

        private final String[] timeStepUnitStrings = { "sec", "min", "hours", "days", "get all" };

        private final static int TIMESTEP_SECONDS = 0;
        private final static int TIMESTEP_MINUTES = 1;
        private final static int TIMESTEP_HOURS = 2;
        private final static int TIMESTEP_DAYS = 3;
        private final static int TIMESTEP_ALL = 4;

        private final JSpinner spinnerCadence = new JSpinner();
        private final JComboBox comboUnit = new JComboBox(timeStepUnitStrings);

        /**
         * Default constructor.
         * */
        public CadencePanel() {
            // set up the visual components (GUI)
            setLayout(new GridLayout(1, 2, GRIDLAYOUT_HGAP, GRIDLAYOUT_VGAP));
            setBorder(new EmptyBorder(3, 0, 0, 0));

            spinnerCadence.setPreferredSize(new Dimension(50, 25));
            spinnerCadence.setModel(new SpinnerNumberModel(30, 1, 1000000, 1));

            comboUnit.setSelectedIndex(TIMESTEP_MINUTES);
            comboUnit.addActionListener(this);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add(spinnerCadence);
            panel.add(comboUnit);

            JLabel labelTimeStep = new JLabel("Time step", JLabel.RIGHT);
            add(labelTimeStep);
            add(panel);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == comboUnit) {
                spinnerCadence.setEnabled(comboUnit.getSelectedIndex() != 4);
            }
        }

        /**
         * Returns the number of seconds of the selected cadence.
         *
         * If no cadence is specified, returns -1.
         *
         * @return number of seconds of the selected cadence.
         * */
        public int getCadence() {
            int value = ((SpinnerNumberModel) spinnerCadence.getModel()).getNumber().intValue();

            switch (comboUnit.getSelectedIndex()) {
            case 1: // min
                value *= 60;
                break;
            case 2: // hour
                value *= 3600;
                break;
            case 3: // day
                value *= 86400;
                break;
            case 4:
                value = -1;
                break;
            }

            return value;
        }
    }

    // Instruments Panel

    /**
     * The panel bundles the components to select the instrument etc.
     * <p>
     * Reads the available data from org.helioviewer.jhv.io.DataSources
     *
     * @author rewritten Helge Dietert
     * @author original Stephan Pagel
     * */
    private static class InstrumentsPanel extends JPanel implements DataSourceServerListener {
        /**
         * Combobox to select observatory
         */
        private final JComboBox comboObservatory = new JComboBox(new String[] { "Loading..." });
        /**
         * Combobox to select instruments
         */
        private final JComboBox comboInstrument = new JComboBox(new String[] { "Loading..." });
        /**
         * Combobox to select detector and/or measurement
         */
        private final JComboBox comboDetectorMeasurement = new JComboBox(new String[] { "Loading..." });

        private final String[] serverList;
        private final JComboBox comboServer;

        private boolean setFromOutside;

        /**
         * Default constructor which will setup the components and add listener
         * to update the available choices
         *
         * @param imageDataPanel
         */
        public InstrumentsPanel(final ImageDataPanel imageDataPanel) {
            // Setup grid
            setFromOutside = false;
            serverList = DataSourceServers.getSingletonInstance().getServerList();
            DataSourceServers.getSingletonInstance().addListener(this);

            setLayout(new GridLayout(4, 2, GRIDLAYOUT_HGAP, GRIDLAYOUT_VGAP));

            JLabel labelServer = new JLabel("Server", JLabel.RIGHT);
            add(labelServer);
            comboServer = new JComboBox(serverList);
            add(comboServer);

            JLabel labelObservatory = new JLabel("Observatory", JLabel.RIGHT);
            add(labelObservatory);
            add(comboObservatory);

            JLabel labelInstrument = new JLabel("Instrument", JLabel.RIGHT);
            add(labelInstrument);
            add(comboInstrument);

            JLabel labelDetectorMeasurement = new JLabel("Detector/Measurement", JLabel.RIGHT);
            add(labelDetectorMeasurement);
            add(comboDetectorMeasurement);

            comboObservatory.setEnabled(false);
            comboInstrument.setEnabled(false);
            comboDetectorMeasurement.setEnabled(false);

            // Advanced rendering with tooltips for the items
            final ListCellRenderer itemRenderer = new DefaultListCellRenderer() {
                /**
                 * Override display component to show tooltip
                 *
                 * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList,
                 *      java.lang.Object, int, boolean, boolean)
                 */

                @Override
                public Component getListCellRendererComponent(JList list, Object value, int arg2, boolean arg3, boolean arg4) {
                    JLabel result = (JLabel) super.getListCellRendererComponent(list, value, arg2, arg3, arg4);
                    if (value instanceof DataSources.Item) {
                        DataSources.Item item = (DataSources.Item) value;
                        result.setToolTipText(item.getDescription());
                    } else if (value instanceof ItemPair) {
                        ItemPair item = (ItemPair) value;
                        result.setToolTipText(item.getDescription());
                    }
                    return result;
                }
            };

            comboObservatory.setRenderer(itemRenderer);
            comboInstrument.setRenderer(itemRenderer);
            comboDetectorMeasurement.setRenderer(itemRenderer);
            comboServer.setRenderer(itemRenderer);

            comboServer.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if (!setFromOutside) {
                        String server = (String) comboServer.getSelectedItem();
                        DataSourceServers.getSingletonInstance().changeServer(server, true);
                    } else {
                        setFromOutside = false;
                    }
                }
            });
            String datasourcesPath = Settings.getSingletonInstance().getProperty("API.dataSources.path");
            if (datasourcesPath.contains("ias")) {
                comboServer.setSelectedItem(serverList[2]);
            } else if (datasourcesPath.contains("helioviewer")) {
                comboServer.setSelectedItem(serverList[1]);
            } else {
                comboServer.setSelectedItem(serverList[0]);
            }

            comboObservatory.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    setComboBox(comboInstrument, DataSources.getSingletonInstance().getInstruments(InstrumentsPanel.this.getObservatory()));
                }
            });

            comboInstrument.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    String obs = InstrumentsPanel.this.getObservatory();
                    String ins = InstrumentsPanel.this.getInstrument();

                    Vector<ItemPair> values = new Vector<ItemPair>();
                    Item[] detectors = DataSources.getSingletonInstance().getDetectors(obs, ins);

                    for (Item detector : detectors) {

                        Item[] measurements = DataSources.getSingletonInstance().getMeasurements(obs, ins, detector.getKey());

                        ItemPair.PrintMode printMode = ItemPair.PrintMode.BOTH;
                        if (detectors.length == 1) {
                            printMode = ItemPair.PrintMode.SECONDITEM_ONLY;
                        } else if (measurements.length == 1) {
                            printMode = ItemPair.PrintMode.FIRSTITEM_ONLY;
                        }

                        for (Item measurement : measurements) {
                            values.add(new ItemPair(detector, measurement, printMode));
                        }
                    }

                    setComboBox(comboDetectorMeasurement, values);
                    comboDetectorMeasurement.setEnabled(true);
                }
            });
        }

        public void setupSources(DataSources source) {
            InstrumentsPanel.this.setComboBox(comboObservatory, source.getObservatories());
        }

        /**
         * Set the items combobox to the to the given parameter and selects the
         * first default item or otherwise the first item
         *
         * @param items
         *            string array which contains the names for the items of the
         *            combobox.
         * @param container
         *            combobox where to add the items.
         */
        private void setComboBox(JComboBox container, Item[] items) {
            container.setModel(new DefaultComboBoxModel(items));
            container.setEnabled(true);
            for (int i = 0; i < items.length; i++) {
                if (items[i].isDefaultItem()) {
                    container.setSelectedIndex(i);
                    return;
                }
            }
            container.setSelectedIndex(0);
        }

        /**
         * Set the items combobox to the to the given parameter and selects the
         * first default item or otherwise the first item
         *
         * @param items
         *            string array which contains the names for the items of the
         *            combobox.
         * @param container
         *            combobox where to add the items.
         */
        private void setComboBox(JComboBox container, Vector<ItemPair> items) {
            container.setModel(new DefaultComboBoxModel(items));
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).isDefaultItem()) {
                    container.setSelectedIndex(i);
                    return;
                }
            }
            container.setSelectedIndex(0);
        }

        /**
         * Checks whether the user did some valid selection
         *
         * @return true if the user did some valid selecion
         */
        public boolean validSelection() {
            return getObservatory() != null && getInstrument() != null && getDetector() != null && getMeasurement() != null;
        }

        /**
         * Returns the selected observation.
         *
         * @return selected observation (key value), null if no is selected
         * */
        public String getObservatory() {
            Object selectedItem = comboObservatory.getSelectedItem();
            if (selectedItem instanceof DataSources.Item) {
                return ((DataSources.Item) selectedItem).getKey();
            } else {
                return null;
            }
        }

        /**
         * Returns the selected instrument.
         *
         * @return selected instrument (key value), null if no is selected
         * */
        public String getInstrument() {
            Object selectedItem = comboInstrument.getSelectedItem();
            if (selectedItem instanceof DataSources.Item) {
                return ((DataSources.Item) selectedItem).getKey();
            } else {
                return null;
            }
        }

        /**
         * Returns the selected detector.
         *
         * @return selected detector (key value), null if no is selected
         * */
        public String getDetector() {
            Object selectedItem = comboDetectorMeasurement.getSelectedItem();
            if (selectedItem instanceof ItemPair) {
                return ((ItemPair) selectedItem).getFirstItem().getKey();
            } else {
                return null;
            }
        }

        /**
         * Returns the selected measurement.
         *
         * @return selected measurement (key value), null if no is selected
         * */
        public String getMeasurement() {
            Object selectedItem = comboDetectorMeasurement.getSelectedItem();
            if (selectedItem instanceof ItemPair) {
                return ((ItemPair) selectedItem).getSecondItem().getKey();
            } else {
                return null;
            }
        }

        private static class ItemPair {

            enum PrintMode {
                FIRSTITEM_ONLY, SECONDITEM_ONLY, BOTH
            }

            private final Item firstItem;
            private final Item secondItem;
            private final PrintMode printMode;

            public ItemPair(Item first, Item second, PrintMode newPrintMode) {
                firstItem = first;
                secondItem = second;
                printMode = newPrintMode;
            }

            /**
             * Returns the first item.
             *
             * @return the fist item
             */
            public Item getFirstItem() {
                return firstItem;
            }

            /**
             * Returns the second item.
             *
             * @return the second item
             */
            public Item getSecondItem() {
                return secondItem;
            }

            /**
             * True if it was created as default item
             *
             * @return the defaultItem
             */
            public boolean isDefaultItem() {
                return firstItem.isDefaultItem() && secondItem.isDefaultItem();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                switch (printMode) {
                case FIRSTITEM_ONLY:
                    return firstItem.toString();
                case SECONDITEM_ONLY:
                    return secondItem.toString();
                default:
                    return firstItem.toString() + " " + secondItem.toString();
                }
            }

            /**
             * @return the description
             */
            public String getDescription() {
                switch (printMode) {
                case FIRSTITEM_ONLY:
                    return firstItem.getDescription();
                case SECONDITEM_ONLY:
                    return secondItem.getDescription();
                default:
                    return firstItem.getDescription() + " " + secondItem.getDescription();
                }
            }
        }

        @Override
        public void serverChanged(boolean donotloadStartup) {
            setFromOutside = true;
            comboServer.setSelectedItem(DataSourceServers.getSingletonInstance().getSelectedServer());
        }
    }

}
