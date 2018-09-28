package edu.augustana.csc285.Ibis;

import java.awt.Rectangle;
import java.io.File;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Mat;
import org.opencv.videoio.Videoio;


public class Video {

	public VideoCapture vidCap = new VideoCapture();
	
		
	private File videoFile;   
	private String videoPath;
	
	private double frameRate;
	private double xPixelsPerCm;
	private double yPixelsPerCm;
	private int totalNumFrames;
	private int startFrameNum;
	private int endFrameNum;
	private Rectangle arenaBounds;
	
	
	
	public Video() {
		try {
			this.videoPath = this.videoFile.getAbsolutePath();
			setVideoFile(this.videoFile);
		} catch (NullPointerException n) {
			
		}
	}
	
	public File getVideoFile() {
		return this.videoFile;
	}
	
	public void setVideoFile(File vidFile) {
		this.videoFile = vidFile;
		vidCap.open(vidFile.getAbsolutePath());
	}
	
	public VideoCapture returnVidCap() {
		return this.vidCap;
	}
	
	public Mat read() {
		Mat frame = new Mat();
		vidCap.read(frame);
		return frame;
	}

	public int getTotalNumFrames() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_COUNT);
	}
	
	public void setCurrentFrameNum(double seekFrame) {
		vidCap.set(Videoio.CV_CAP_PROP_POS_FRAMES, (double) seekFrame);
	}
	
	public String getFilePath() {
		return this.videoFile.getAbsolutePath();
	}
	
	public void setFilePath(String videoPath) {
		this.videoPath = videoFile.getAbsolutePath();
	}
	
}


