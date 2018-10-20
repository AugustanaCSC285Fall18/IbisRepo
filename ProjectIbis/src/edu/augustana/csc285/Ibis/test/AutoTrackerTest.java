package edu.augustana.csc285.Ibis.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import edu.augustana.csc285.Ibis.autotracking.AutoTrackListener;
import edu.augustana.csc285.Ibis.autotracking.AutoTracker;
import edu.augustana.csc285.Ibis.datamodel.AnimalTrack;
import edu.augustana.csc285.Ibis.datamodel.ProjectData;
import edu.augustana.csc285.Ibis.datamodel.TimePoint;

class AutoTrackerTest implements AutoTrackListener {
	ProjectData project;
	
	@BeforeAll
	static void initialize() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);		
	}
	
	ProjectData createVideo() throws FileNotFoundException {
		ProjectData project = new ProjectData("testVideos/CircleTest1_no_overlap.mp4");
		AnimalTrack track1 = new AnimalTrack("chicken1");
		AnimalTrack track2 = new AnimalTrack("chicken2");
		project.getTracks().add(track1);
		project.getTracks().add(track2);
		track1.add(new TimePoint(100,200,0));
		track1.add(new TimePoint(105,225,30));
		track2.add(new TimePoint(300,400,90));
		return project;
	}

	@Test
	void testAutoTrackerGetProjectData() throws FileNotFoundException {
		project = createVideo();
		AutoTracker autoTracker = new AutoTracker();
		autoTracker.addAutoTrackListener(this);
		autoTracker.startAnalysis(project.getVideo());
		while (autoTracker.isRunning()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {	}
		}
		
		System.out.println(project.getUnassignedSegments().isEmpty());
		System.out.println(project.getUnassignedSegments().toString());
		

		
	}

	@Override
	public void handleTrackedFrame(Mat frame, int frameNumber, double percentTrackingComplete) {
	}

	@Override
	public void trackingComplete(List<AnimalTrack> trackedSegments) {
		project.getUnassignedSegments().addAll(trackedSegments);
		
	}


}
