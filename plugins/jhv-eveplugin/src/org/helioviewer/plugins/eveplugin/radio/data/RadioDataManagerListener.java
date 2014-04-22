package org.helioviewer.plugins.eveplugin.radio.data;

import java.awt.Rectangle;
import java.util.Date;
import java.util.List;

import org.helioviewer.base.math.Interval;

public interface RadioDataManagerListener {
	public abstract void downloadRequestAnswered(FrequencyInterval freqInterval, Interval<Date> timeInterval,long ID, String identifier);
	public abstract void additionDownloadRequestAnswered(Long downloadID);
	public abstract void newDataAvailable(DownloadRequestData downloadRequestData, long ID);
	public abstract void downloadFinished(long ID);
	public abstract void dataNotChanged(Interval<Date> timeInterval,FrequencyInterval freqInterval, Rectangle area, List<Long> IDList, String identifier, long imageID); 
	public abstract void newGlobalFrequencyInterval(FrequencyInterval interval);
	public abstract void newDataReceived(byte[] data, Interval<Date> timeInterval, FrequencyInterval freqInterval, Rectangle area, List<Long> ID, String identifier, Long imageID);
	public abstract void clearAllSavedImages(String plotIdentifier);
	public abstract void downloadRequestDataRemoved(DownloadRequestData drd, long ID);
	public abstract void downloadRequestDataVisibilityChanged(DownloadRequestData drd, long ID);
	public abstract void newDataForIDReceived(byte[] data, Interval<Date> timeInterval, FrequencyInterval freqInterval, Rectangle area, Long downloadID, String identifier, Long imageID);
	public abstract void clearAllSavedImagesForID(Long downloadID, Long imageID, String plotIdentifier);
	public abstract void intervalTooBig(long iD, String identifier);
}
