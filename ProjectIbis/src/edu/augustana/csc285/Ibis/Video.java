package edu.augustana.csc285.Ibis;

import java.awt.Rectangle;
import java.io.File;
import org.opencv.videoio.VideoCapture;


public class Video {

	public VideoCapture vidCap = new VideoCapture();
	
		
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
	
	public void setVideoFile(File vidFile) {
		this.videoFile = vidFile;
		vidCap.open(vidFile.getPath());
	}
	
	public VideoCapture returnVidCap() {
		return this.vidCap;
	}
	
}


