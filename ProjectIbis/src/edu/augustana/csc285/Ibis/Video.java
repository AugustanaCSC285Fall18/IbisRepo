package edu.augustana.csc285.Ibis;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;

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
	private int emptyFrameNum;
	private int startFrameNum;
	private int endFrameNum;
	private Rectangle arenaBounds;
	
	
	
	public Video() {
		this.videoPath = videoFile.getAbsolutePath();
		this.vidCap = new VideoCapture(videoPath);
		
		
		this.emptyFrameNum = 0;
		this.startFrameNum = 0;
		this.endFrameNum = this.getTotalNumFrames()-1;
		
		int frameWidth = (int)vidCap.get(Videoio.CAP_PROP_FRAME_WIDTH);
		int frameHeight = (int)vidCap.get(Videoio.CAP_PROP_FRAME_HEIGHT);
		this.arenaBounds = new Rectangle(0,0,frameWidth,frameHeight);
	}
	
	public File getVideoFile() {
		return this.videoFile;
	}
	
	public void setVideoFile(File vidFile) throws FileNotFoundException {
		this.videoFile = vidFile;
		vidCap.open(vidFile.getAbsolutePath());
		if (!vidCap.isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + videoPath);
		}
	}
	
	public VideoCapture returnVidCap() {
		return this.vidCap;
	}
	
	public Mat read(Mat frame) {
		frame = new Mat();
		vidCap.read(frame);
		return frame;
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
	
	public int getTotalNumFrames() {
		return this.totalNumFrames;
	}
	
	public double getFrameRate() {
		return this.frameRate;
	}
	
	public double convertFrameNumsToSeconds(int numFrames) {
		return numFrames / getFrameRate();
	}

	public int convertSecondsToFrameNums(double numSecs) {
		return (int) Math.round(numSecs * getFrameRate());
	}
	
	public int getEmptyFrameNum() {
		return emptyFrameNum;
	}

	public void setEmptyFrameNum(int emptyFrameNum) {
		this.emptyFrameNum = emptyFrameNum;
	}
		
	public int getStartFrameNum() {
		return startFrameNum;
	}
	
	public void setStartFrameNum(int startFrameNum) {
		this.startFrameNum = startFrameNum;
	}

	public int getEndFrameNum() {
		return endFrameNum;
	}

	public void setEndFrameNum(int endFrameNum) {
		this.endFrameNum = endFrameNum;
	}

	public double getXPixelsPerCm() {
		return xPixelsPerCm;
	}

	public void setXPixelsPerCm(double xPixelsPerCm) {
		this.xPixelsPerCm = xPixelsPerCm;
	}

	public double getYPixelsPerCm() {
		return yPixelsPerCm;
	}

	public void setYPixelsPerCm(double yPixelsPerCm) {
		this.yPixelsPerCm = yPixelsPerCm;
	}

	public double getAvgPixelsPerCm() {
		return (xPixelsPerCm + yPixelsPerCm)/2;
	}

	public Rectangle getArenaBounds() {
		return arenaBounds;
	}
}


