package edu.augustana.csc285.Ibis.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class AnimalTrack implements Iterable<TimePoint>{
	private String animalID;
	private List<TimePoint> positions;

	public AnimalTrack(String id) {
		this.animalID = id;
		positions = new ArrayList<TimePoint>();
	}

	public void add(TimePoint pt) {
		positions.add(pt);
		Collections.sort(positions);
	}

	/**
	 * 
	 * @param startFrameNum - the starting time (inclusive)
	 * @param endFrameNum   - the ending time (inclusive)
	 * @return all time points in that time interval
	 */
	public List<TimePoint> getTimePointsWithinInterval(int startFrameNum, int endFrameNum) {
		List<TimePoint> pointsInInterval = new ArrayList<>();
		for (TimePoint pt : positions) {
			if (pt.getFrameNum() >= startFrameNum && pt.getFrameNum() <= endFrameNum) {
				pointsInInterval.add(pt);
			}
		}
		return pointsInInterval;
	}
	
	/**
	 * Create (or modify, if existing) a timepoint for the specified time & place.
	 */
	public void setTimePointAtTime(double x, double y, int frameNum) {
		TimePoint oldPt = getTimePointAtTime(frameNum);
		if (oldPt != null) {
			oldPt.setX(x);
			oldPt.setY(y);
		} else {
			add(new TimePoint(x, y, frameNum));
		}
	}

	public TimePoint getTimePointAtIndex(int index) {
		return positions.get(index);
	}

	/**
	 * Returns the TimePoint at the specified time, or null
	 * @param frameNum
	 * @return
	 */

	public TimePoint getTimePointAtTime(int frameNum) {
		//binary searching used to be a for loop
		for (TimePoint pt : positions) {
			if (pt.getFrameNum() == frameNum) {
				return pt;
			}
		}
		return null; //if not in the list
	}


	public TimePoint getFinalTimePoint() {
		return positions.get(positions.size()-1);
	}

	public int size() {
		return positions.size();
	}


	public String getAnimalId() {
		return this.animalID;
	}

	public String toString() {
		int startFrame = positions.get(0).getFrameNum();
		int endFrame = getFinalTimePoint().getFrameNum();
		return "AnimalTrack[id="+ animalID + ",numPts=" + positions.size()+" start=" + startFrame + " end=" + endFrame +"]"; 
	}
	@Override
	public Iterator<TimePoint> iterator() {
		return positions.iterator();
	}
}
