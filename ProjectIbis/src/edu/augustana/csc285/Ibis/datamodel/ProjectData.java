package edu.augustana.csc285.Ibis.datamodel;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ProjectData {
	private Video video;
	private List<AnimalTrack> tracks;
	private List<AnimalTrack> unassignedSegments;
	
	public ProjectData(String filePath) throws FileNotFoundException {
		this.video = new Video(filePath);
		tracks = new ArrayList<>();
		unassignedSegments = new ArrayList<>();
	}

	public Video getVideo() {
		return video;
	}
	
	public List<AnimalTrack> getTracks() {
		return tracks;
	}

	public List<AnimalTrack> getUnassignedSegments() {
		return unassignedSegments;
	}
	
	
	public void setUnassignedSegments(List<AnimalTrack> track) {
		this.unassignedSegments = track;
	}
	
	public void addAnimalTrack(AnimalTrack track) {
		this.tracks.add(track);
	}
	

	
}
