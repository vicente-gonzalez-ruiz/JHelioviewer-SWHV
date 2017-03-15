package org.helioviewer.jhv.plugins.swek.model;

import java.util.HashSet;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.helioviewer.jhv.data.event.SWEKEventType;
import org.helioviewer.jhv.data.event.SWEKSupplier;

/**
 * The model of the event type panel. This model is a TreeModel and is used by
 * the tree on the event type panel.
 */
public class EventTypePanelModel implements TreeModel {

    /** The event type for this model */
    private final SWEKTreeModelEventType eventType;

    // private final List<TreeModelListener> listeners = new HashSet<TreeModelListener>();

    /** Holds the EventPanelModelListeners */
    private final HashSet<EventTypePanelModelListener> panelModelListeners = new HashSet<>();

    /**
     * Creates a SWEKTreeModel for the given SWEK event type.
     *
     * @param _eventType
     *            The event type for which to create the tree model
     */
    public EventTypePanelModel(SWEKTreeModelEventType _eventType) {
        eventType = _eventType;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.
     * TreeModelListener)
     */
    @Override
    public void addTreeModelListener(TreeModelListener l) {
        // listeners.add(l);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.
     * TreeModelListener)
     */
    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        // listeners.remove(l);
    }

    /**
     * Adds a new event panel model listener.
     *
     * @param listener
     *            the listener to add
     */
    public void addEventPanelModelListener(EventTypePanelModelListener listener) {
        panelModelListeners.add(listener);
    }

    /**
     * Removes an event panel model listener.
     *
     * @param listener
     *            the listener to remove
     */
    public void removeEventPanelModelListener(EventTypePanelModelListener listener) {
        panelModelListeners.remove(listener);
    }

    /**
     * Informs the model about the row that was clicked. The clicked row will be
     * selected or unselected if it previously respectively was unselected or
     * selected.
     *
     * @param row
     *            The row that was selected
     */
    public void rowClicked(int row) {
        if (row == 0) {
            eventType.setCheckboxSelected(!eventType.isCheckboxSelected());
            for (SWEKTreeModelSupplier supplier : eventType.getSwekTreeSuppliers()) {
                supplier.setCheckboxSelected(eventType.isCheckboxSelected());
            }
            if (eventType.isCheckboxSelected()) {
                fireNewEventTypeActive(eventType.getSwekEventType());
            } else {
                fireNewEventTypeInactive(eventType.getSwekEventType());
            }
        } else if (row > 0 && row <= eventType.getSwekTreeSuppliers().size()) {
            SWEKTreeModelSupplier supplier = eventType.getSwekTreeSuppliers().get(row - 1);
            supplier.setCheckboxSelected(!supplier.isCheckboxSelected());
            if (supplier.isCheckboxSelected()) {
                eventType.setCheckboxSelected(true);
            } else {
                boolean eventTypeSelected = false;
                for (SWEKTreeModelSupplier stms : eventType.getSwekTreeSuppliers()) {
                    eventTypeSelected = eventTypeSelected || stms.isCheckboxSelected();
                }
                SWEKTreeModel.resetEventType(eventType.getSwekEventType());
                eventType.setCheckboxSelected(eventTypeSelected);
            }
            if (supplier.isCheckboxSelected()) {
                fireNewEventTypeAndSourceActive(eventType.getSwekEventType(), supplier.getSwekSupplier());
            } else {
                fireNewEventTypeAndSourceInactive(eventType.getSwekEventType(), supplier.getSwekSupplier());
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    @Override
    public Object getChild(Object parent, int index) {
        return parent instanceof SWEKTreeModelEventType ? ((SWEKTreeModelEventType) parent).getSwekTreeSuppliers().get(index) : null;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    @Override
    public int getChildCount(Object parent) {
        return parent instanceof SWEKTreeModelEventType ? ((SWEKTreeModelEventType) parent).getSwekEventType().getSuppliers().size() : 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if ((parent instanceof SWEKTreeModelEventType) && (child instanceof SWEKTreeModelSupplier)) {
            return ((SWEKTreeModelEventType) parent).getSwekTreeSuppliers().indexOf(child);
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.tree.TreeModel#getRoot()
     */
    @Override
    public Object getRoot() {
        return eventType;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
     */
    @Override
    public boolean isLeaf(Object node) {
        return !(node instanceof SWEKTreeModelEventType);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath,
     * java.lang.Object)
     */
    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    /**
     * Informs the listeners about an event type and source that became active.
     *
     * @param swekEventType
     *            the event type that became active
     * @param swekSupplier
     *            the supplier that became active
     */
    private void fireNewEventTypeAndSourceActive(SWEKEventType swekEventType, SWEKSupplier swekSupplier) {
        for (EventTypePanelModelListener l : panelModelListeners) {
            l.newEventTypeAndSourceActive(swekEventType, swekSupplier);
        }
    }

    /**
     * Informs the listeners about an event type and source that became
     * inactive.
     *
     * @param swekEventType
     *            the event type that became inactive
     * @param supplier
     *            the source that became inactive
     */
    private void fireNewEventTypeAndSourceInactive(SWEKEventType swekEventType, SWEKSupplier supplier) {
        for (EventTypePanelModelListener l : panelModelListeners) {
            l.newEventTypeAndSourceInactive(swekEventType, supplier);
        }
    }

    /**
     * Informs the listeners about an event type that became active.
     *
     * @param swekEventType
     *            the event type that became active
     */
    private void fireNewEventTypeActive(SWEKEventType swekEventType) {
        for (SWEKSupplier supplier : swekEventType.getSuppliers()) {
            fireNewEventTypeAndSourceActive(swekEventType, supplier);
        }
    }

    /**
     * Informs the listeners about an event type that became inactive.
     *
     * @param swekEventType
     *            the event type that became inactive
     */
    private void fireNewEventTypeInactive(SWEKEventType swekEventType) {
        for (SWEKSupplier supplier : swekEventType.getSuppliers()) {
            fireNewEventTypeAndSourceInactive(swekEventType, supplier);
        }
    }

}