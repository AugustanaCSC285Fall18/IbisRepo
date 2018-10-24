package edu.augustana.csc285.Ibis.datamodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


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
	
	public void saveToFile(File saveFile) throws FileNotFoundException {
		String json = toJSON();
		PrintWriter out = new PrintWriter(saveFile);
		out.print(json);
		out.close();
	}
	
	
	public void exportToCSV(File saveFile) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(saveFile);
		out.print("Name, Time (in seconds), X-location, Y-location");
		out.println();
		for (AnimalTrack trackToSave: tracks) {
			for(TimePoint point: trackToSave) {
			 out.print(trackToSave.getAnimalId()+", "+ String.format("%.2f", (point.getFrameNum()/video.getFrameRate())));
			 out.print(", "+ point.getX());
			 out.print(", "+ point.getY());
			 out.println();
			}
		}
		out.close();
	}
	
	public String toJSON() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();		
		return gson.toJson(this);
	}
	
	public static ProjectData loadFromFile(File loadFile) throws FileNotFoundException {
		String json = new Scanner(loadFile).useDelimiter("\\Z").next();
		return fromJSON(json);
	}
	
	public static ProjectData fromJSON(String jsonText) throws FileNotFoundException {
		Gson gson = new Gson();
		ProjectData data = gson.fromJson(jsonText, ProjectData.class);
		data.getVideo().connectVideoCapture();
		return data;
	}
	
}
