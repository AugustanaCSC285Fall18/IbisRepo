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

	/**
	 * Constructor for ProjectData objects.
	 * Sets video filepath from param.
	 * creates tracks and unassignedSegments ArrayLists.
	 * @param filePath
	 * @throws FileNotFoundException
	 */
	public ProjectData(String filePath) throws FileNotFoundException {
		this.video = new Video(filePath);
		tracks = new ArrayList<>();
		unassignedSegments = new ArrayList<>();
	}
	/**
	 * Returns Video object.
	 * @return video
	 */
	public Video getVideo() {
		return video;
	}
	/**
	 * Returns List AnimalTracks.
	 * @return tracks
	 */
	public List<AnimalTrack> getTracks() {
		return tracks;
	}
	/**
	 * Returns List of AnimalTracks.
	 * @return unassignedSegments
	 */
	public List<AnimalTrack> getUnassignedSegments() {
		return unassignedSegments;
	}

	/**
	 * Setter method for unassignedSegments.
	 * @param track
	 */
	public void setUnassignedSegments(List<AnimalTrack> track) {
		this.unassignedSegments = track;
	}
	/**
	 * Setter method for tracks.
	 * @param track
	 */
	public void addAnimalTrack(AnimalTrack track) {
		this.tracks.add(track);
	}
	/**
	 * Helper method used when saving project to Json file.
	 * @param saveFile
	 * @throws FileNotFoundException
	 */
	public void saveToFile(File saveFile) throws FileNotFoundException {
		String json = toJSON();
		PrintWriter out = new PrintWriter(saveFile);
		out.print(json);
		out.close();
	}

	/**
	 * Helper method used when exporting project to CSV file.
	 * @param saveFile
	 * @throws FileNotFoundException
	 */
	public void exportToCSV(File saveFile) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(saveFile);
		out.print("Name, Time (in seconds), X-location (cm), Y-location (cm)");
		out.println();
		for (AnimalTrack trackToSave: tracks) {
			for(TimePoint point: trackToSave) {
				out.print(trackToSave.getAnimalId()+", "+ String.format("%.2f", (point.getFrameNum()/video.getFrameRate())));
				out.print(", "+ String.format("%.2f", (point.getX()/ video.getXPixelsPerCm())));
				out.print(", "+ String.format("%.2f", (point.getY()/ video.getYPixelsPerCm())));
				out.println();
			}
		}
		out.close();
	}
	/**
	 * Creates Gson object and returns gson.toJson(this).
	 * @return String
	 */
	public String toJSON() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();		
		return gson.toJson(this);
	}
	/**
	 * Takes in a file to load and enters it into String json
	 * @param loadFile
	 * @return ProjectData
	 * @throws FileNotFoundException
	 */
	public static ProjectData loadFromFile(File loadFile) throws FileNotFoundException {
		String json = new Scanner(loadFile).useDelimiter("\\Z").next();
		return fromJSON(json);
	}
	/**
	 * Creates new Gson object and ProjectData object.
	 * Sets ProjectData to json String passed in.
	 * @param jsonText
	 * @return ProjectData
	 * @throws FileNotFoundException
	 */
	public static ProjectData fromJSON(String jsonText) throws FileNotFoundException {
		Gson gson = new Gson();
		ProjectData data = gson.fromJson(jsonText, ProjectData.class);
		data.getVideo().connectVideoCapture();
		return data;
	}

}
