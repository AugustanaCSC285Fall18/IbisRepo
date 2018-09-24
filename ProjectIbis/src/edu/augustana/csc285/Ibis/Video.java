package edu.augustana.csc285.Ibis;

import java.io.File;
import org.opencv.videoio.VideoCapture;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class Video {

	public VideoCapture vidCap = new VideoCapture();
	
	private FileChooser fileChooser = new FileChooser();

	private Window mainWindow;
	
	private File videoFile;   
	//private String videoPath; taking this out since can get the path from videoFile.getAbsolutePath() see LanchSreenController 
	
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
		fileChooser.setTitle("Open Video File");
		this.videoFile = fileChooser.showOpenDialog(mainWindow);

	}
	
}


