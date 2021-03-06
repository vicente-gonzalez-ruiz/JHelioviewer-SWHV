package org.helioviewer.jhv.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.helioviewer.jhv.base.interval.Interval;
import org.helioviewer.jhv.base.interval.RequestCache;
import org.helioviewer.jhv.time.TimeUtils;

public class JHVEventCache {

    private static final double FACTOR = 0.2;
    private static final long DELTAT_GET = TimeUtils.DAY_IN_MILLIS;

    private static final HashSet<JHVEventHandler> cacheEventHandlers = new HashSet<>();
    private static final HashMap<SWEKSupplier, SortedMap<Interval, JHVRelatedEvents>> events = new HashMap<>();
    private static final HashMap<Integer, JHVRelatedEvents> relEvents = new HashMap<>();
    private static final HashSet<SWEKSupplier> activeEventTypes = new HashSet<>();
    private static final HashMap<SWEKSupplier, RequestCache> downloadedCache = new HashMap<>();
    private static final ArrayList<JHVAssociation> assocs = new ArrayList<>();

    private static JHVRelatedEvents lastHighlighted = null;

    public static void requestForInterval(long start, long end, JHVEventHandler handler) {
        long deltaT = Math.max((long) ((end - start) * FACTOR), TimeUtils.DAY_IN_MILLIS);
        long newStart = start - deltaT;
        long newEnd = end + deltaT;

        cacheEventHandlers.add(handler);
        getMissingIntervals(start, end, newStart, newEnd).forEach((key, value) -> value.forEach(interval -> SWEKDownloadManager.startDownloadSupplier(key, interval)));
        handler.newEventsReceived();
    }

    static void fireEventCacheChanged() {
        cacheEventHandlers.forEach(JHVEventHandler::cacheUpdated);
    }

    static void intervalNotDownloaded(SWEKSupplier eventType, Interval interval) {
        downloadedCache.get(eventType).removeRequestedInterval(interval.start, interval.end);
        // getMissingIntervals(interval.start, interval.end, interval.start, interval.end); side-effect?
    }

    static void supplierActivated(SWEKSupplier supplier) {
        activeEventTypes.add(supplier);
        downloadedCache.putIfAbsent(supplier, new RequestCache());
        fireEventCacheChanged();
    }

    public static void highlight(JHVRelatedEvents event) {
        if (event == lastHighlighted) {
            return;
        }
        if (event != null) {
            event.highlight(true);
        }
        if (lastHighlighted != null) {
            lastHighlighted.highlight(false);
        }
        lastHighlighted = event;
    }

    public static void add(JHVEvent event) {
        Integer id = event.getUniqueID();
        if (relEvents.containsKey(id)) {
            relEvents.get(id).swapEvent(event, events);
        } else {
            createNewRelatedEvent(event);
        }
        checkAssociation(event);
    }

    public static JHVRelatedEvents getRelatedEvents(int id) {
        return relEvents.get(id);
    }

    private static void checkAssociation(JHVEvent event) {
        int uid = event.getUniqueID();
        JHVRelatedEvents rEvent = relEvents.get(uid);
        for (Iterator<JHVAssociation> iterator = assocs.iterator(); iterator.hasNext(); ) {
            JHVAssociation tocheck = iterator.next();
            if (tocheck.left == uid && relEvents.containsKey(tocheck.right)) {
                merge(rEvent, relEvents.get(tocheck.right));
                rEvent.addAssociation(tocheck);
                iterator.remove();
            }
            if (tocheck.right == uid && relEvents.containsKey(tocheck.left)) {
                merge(rEvent, relEvents.get(tocheck.left));
                rEvent.addAssociation(tocheck);
                iterator.remove();
            }
        }
    }

    private static void createNewRelatedEvent(JHVEvent event) {
        JHVRelatedEvents revent = new JHVRelatedEvents(event, events);
        relEvents.put(event.getUniqueID(), revent);
    }

    private static void merge(JHVRelatedEvents current, JHVRelatedEvents found) {
        if (current == found) {
            return;
        }
        current.merge(found, events);
        for (JHVEvent foundev : found.getEvents()) {
            Integer key = foundev.getUniqueID();
            relEvents.remove(key);
            relEvents.put(key, current);
        }
    }

    static void add(JHVAssociation association) {
        if (relEvents.containsKey(association.left) && relEvents.containsKey(association.right)) {
            JHVRelatedEvents ll = relEvents.get(association.left);
            JHVRelatedEvents rr = relEvents.get(association.right);
            if (ll != rr) {
                merge(ll, rr);
                ll.addAssociation(association);
            }
        } else {
            assocs.add(association);
        }
    }

    public static Map<SWEKSupplier, SortedMap<Interval, JHVRelatedEvents>> getEvents(long start, long end) {
        if (activeEventTypes.isEmpty())
            return Collections.emptyMap();

        Interval first = new Interval(start - DELTAT_GET, start - DELTAT_GET);
        Interval last = new Interval(end + DELTAT_GET, end + DELTAT_GET);

        HashMap<SWEKSupplier, SortedMap<Interval, JHVRelatedEvents>> result = new HashMap<>();
        for (SWEKSupplier evt : activeEventTypes) {
            SortedMap<Interval, JHVRelatedEvents> sortedEvents = events.get(evt);
            if (sortedEvents != null) {
                result.put(evt, sortedEvents.subMap(first, last));
            }
        }
        return result;
    }

    private static Map<SWEKSupplier, List<Interval>> getMissingIntervals(long start, long end, long extendedStart, long extendedEnd) {
        HashMap<SWEKSupplier, List<Interval>> missingIntervals = new HashMap<>();
        for (SWEKSupplier evt : activeEventTypes) {
            List<Interval> missing = downloadedCache.get(evt).getMissingIntervals(start, end);
            if (!missing.isEmpty()) {
                missing = downloadedCache.get(evt).adaptRequestCache(extendedStart, extendedEnd);
                missingIntervals.put(evt, missing);
            }
        }
        return missingIntervals;
    }

    static void removeSupplier(SWEKSupplier supplier, boolean keepActive) {
        downloadedCache.put(supplier, new RequestCache());
        events.remove(supplier);
        relEvents.entrySet().removeIf(entry -> entry.getValue().getSupplier() == supplier);

        if (!keepActive)
            activeEventTypes.remove(supplier);
        fireEventCacheChanged();
    }

    static List<Interval> getAllRequestIntervals(SWEKSupplier eventType) {
        return downloadedCache.get(eventType).getAllRequestIntervals();
    }

}
