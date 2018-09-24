package edu.augustana.csc285.Ibis;

import java.io.File;
import org.opencv.core.*;

import org.opencv.videoio.VideoCapture;

import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class Video {

	private VideoCapture vidCap = new VideoCapture();
	private FileChooser fileChooser = new FileChooser();
	public Window mainWindow;
	
	public File videoFile;
	private String videoPath;
	
	private double frameRate;
	private double xPixelsPerCm;
	private double yPixelsPerCm;
	private int totalNumFrames;
	private int startFrameNum;
	private int endFrameNum;
	private Rectangle arenaBounds;
	
	
	public File getVideoFile() {
		return this.videoFile;
	}
	
	public void importVideo() {
		this.videoFile = fileChooser.showOpenDialog(mainWindow);
		this.videoPath = videoFile.getAbsolutePath();
		
	}
	
	public String getVideoPath() {
		return videoPath;
	}
	
}


