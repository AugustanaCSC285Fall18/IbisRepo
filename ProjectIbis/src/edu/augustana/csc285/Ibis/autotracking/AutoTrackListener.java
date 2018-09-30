package edu.augustana.csc285.Ibis.autotracking;

import java.util.List;

import org.opencv.core.Mat;

import edu.augustana.csc285.Ibis.datamodel.AnimalTrack;

public interface AutoTrackListener {

	public void handleTrackedFrame(Mat frame, int frameNumber, double percentTrackingComplete);
	public void trackingComplete(List<AnimalTrack> trackedSegments);
}
