package edu.augustana.csc285.Ibis.autotracking;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;

import edu.augustana.csc285.Ibis.datamodel.*;
import javafx.concurrent.Task;

public class AutoTracker {

	private List<AutoTrackListener> listeners = new ArrayList<>();
	private Task<Void> task;
	private ExecutorService execService;
	private int segmentCounter = 0;
	
	 // Note: I think the chicks might be about 50 sq. cm in area, from a top view?
	private final double targetShapeArea = 50;
	
	private final double brightnessTheshold = 55; // must be between 0 to 255.
	
	private final double maxTimeGapWithinSegment = 0.5; // end a segment after this many seconds with no point detected
	private final double maxMovementSpeed = 80.0; // guess for chicks
	
	public ProjectData chickData;

	public AutoTracker() {
		//TODO: pass in some thresholds/parameters for fine-tuning the auto-tracking 
		//        instead of declaring them all as constants up above....
	}
	
	/**
	 * Starts the auto-tracking process
	 * @param vid - the video to analyze     
 	 */
	public void startAnalysis(Video vid) {
		task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				analyzeWholeVideo(vid, chickData);
				return null;
			}
		};
		execService = Executors.newSingleThreadExecutor();
		execService.submit(task);
	}
	
	//Doesn't use the trackData parameter anymore inside the method, haven't looked at
	//other classes and how it's utilized yet. But it's still there.
	private void analyzeWholeVideo(Video vid, ProjectData trackData) {
		List<AnimalTrack> archivedTrackedSegments = new ArrayList<>();
		List<AnimalTrack> currentlyTrackingSegments = new ArrayList<>();

		vid.setCurrentFrameNum(vid.getEmptyFrameNum());
		Mat emptyFrame = vid.readFrame();


		double minShapePixelArea= 0.5*targetShapeArea*vid.getXPixelsPerCm()*vid.getYPixelsPerCm();
		double maxShapePixelArea= 1.5*targetShapeArea*vid.getXPixelsPerCm()*vid.getYPixelsPerCm();
		SingleFrameShapeFinder frameAnalyzer = new SingleFrameShapeFinder(emptyFrame, brightnessTheshold, minShapePixelArea, maxShapePixelArea);
		
		vid.setCurrentFrameNum(vid.getStartFrameNum());
		for (int fNum = vid.getStartFrameNum(); fNum <= vid.getEndFrameNum(); fNum++) {

			// archive all the AnimalTracks that we haven't matched any points to for a while
			Iterator<AnimalTrack> it = currentlyTrackingSegments.iterator();
			while (it.hasNext()) {
				AnimalTrack track = it.next();
				if ((fNum - track.getFinalTimePoint().getFrameNum()) > (maxTimeGapWithinSegment * vid.getFrameRate())) {
					it.remove();
					archivedTrackedSegments.add(track);
				}
			}

			
			Mat matFrame = vid.readFrame();			
			List<DetectedShape> candidateShapes = frameAnalyzer.findShapes(matFrame);
			matFrame.release();
			
			Mat visualizationFrame = frameAnalyzer.getVisualizationFrame();

			for (DetectedShape shape: candidateShapes) {
				TimePoint tpt = new TimePoint(shape.getCentroidX(), shape.getCentroidY(), fNum);
				if (vid.getArenaBounds().contains(tpt.getX(),tpt.getY())) {
					double maxPixelMovementPerFrame = maxMovementSpeed * vid.getAvgPixelsPerCm() / vid.getFrameRate();
					AnimalTrack track = getMatchOrCreateAnimalTrackForPoint(tpt, currentlyTrackingSegments, maxPixelMovementPerFrame);
					track.add(tpt);
				}
			}
			

			if (task.isCancelled()) {
				return; 
			}					
			for (AutoTrackListener listener : listeners) {
				double percentDone = ((double)fNum - vid.getStartFrameNum()) / (vid.getEndFrameNum()-vid.getStartFrameNum()+1);
				listener.handleTrackedFrame(visualizationFrame, fNum, percentDone);
			}					
		}
		
		archivedTrackedSegments.addAll(currentlyTrackingSegments);
		
//		this might be causing an infinite loop or something
//		we need this though to be able to take the list of AnimalTracks
//		out of this method and use them in MainWindowController
//		for manual tracking
//		
//		this.chickData.setUnassignedSegments(archivedTrackedSegments);
		
		for (AutoTrackListener listener : listeners) {
			listener.trackingComplete(archivedTrackedSegments);
			System.out.println(this.chickData.getUnassignedSegments().toString());
		}
		execService.shutdown();
	}
	
	private AnimalTrack getMatchOrCreateAnimalTrackForPoint(TimePoint pt, List<AnimalTrack> currentSegments, double maxPixelMovementPerFrame) {
		AnimalTrack bestMatch = null;
		double slowestSpeed = Double.POSITIVE_INFINITY;
		for (AnimalTrack track: currentSegments) {
			TimePoint maybePredecessor = track.getFinalTimePoint();
			double estimatedSpeed = pt.getDistanceTo(maybePredecessor) / pt.compareTo(maybePredecessor);
//          TODO: delete this debugging code later
//			if (estimatedSpeed >= 0 && estimatedSpeed < Double.POSITIVE_INFINITY) {
//				System.out.printf("%s  est: %.2f  max: %.2f\n", pt, estimatedSpeed, maxPixelMovementPerFrame);
//			}
			if (estimatedSpeed < maxPixelMovementPerFrame && estimatedSpeed < slowestSpeed) {
				slowestSpeed = estimatedSpeed;
				bestMatch = track;
			}
		}
		if (bestMatch==null) {
			bestMatch = new AnimalTrack("<Auto-" + (++segmentCounter) + ">" );
			currentSegments.add(bestMatch);
		}
		return bestMatch;		
	}
	
	public void cancelAnalysis() {
		if (task != null) {
			task.cancel(true);
			execService.shutdown();
			try {
				execService.awaitTermination(1000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ex) { }
		}		
	}
	
	public boolean isRunning() {
		return task != null && task.isRunning();
	}

	/**
	 * Add a listener that will get updated whenever a frame has been processed. 
	 * (This could be useful for visualizing the auto-tracking process in real-time.)
	 *  
	 * @param listener - the object that will received these notification events  
	 */
	public void addAutoTrackListener(AutoTrackListener listener) {
		listeners.add(listener);
	}
	
	public ProjectData getChickData() {
		return this.chickData;
	}
	
}
