package edu.augustana.csc285.Ibis.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javafx.scene.paint.Color;




public class AnimalTrack implements Iterable<TimePoint>{
	private String animalID;
	private List<TimePoint> positions;
	private Color color;
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
		int first=0;
		int last = positions.size();		
		while(first<= last) {
			int middle= (first+last)/2;
			if(positions.get(middle).getFrameNum()<frameNum) {
				first=middle+1;
			}else if(positions.get(middle).getFrameNum()==frameNum) {
				return positions.get(middle);
			}else {
				last = middle-1;
				middle=(first+last)/2;
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
	
	public void setColor(Color newColor) {
		color = newColor;
	}
	
	public Color getColor() {
		return color;
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
