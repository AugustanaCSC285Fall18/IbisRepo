package edu.augustana.csc285.Ibis.datamodel;

import java.awt.Rectangle;
import java.io.FileNotFoundException;

import org.opencv.videoio.VideoCapture;
import org.opencv.core.Mat;
import org.opencv.videoio.Videoio;


public class Video {

	private transient VideoCapture vidCap;
	
	private String filePath;
	
	private double xPixelsPerCm;
	private double yPixelsPerCm;
	private int emptyFrameNum;
	private int startFrameNum;
	private int endFrameNum;
	private Rectangle arenaBounds;
	
	
	
	public Video(String filePath) throws FileNotFoundException {
		this.filePath= filePath;
		connectVideoCapture();
		this.emptyFrameNum = 0;
		this.startFrameNum = 0;
		this.endFrameNum = this.getTotalNumFrames()-1;
		
		int frameWidth = (int)vidCap.get(Videoio.CAP_PROP_FRAME_WIDTH);
		int frameHeight = (int)vidCap.get(Videoio.CAP_PROP_FRAME_HEIGHT);
		this.arenaBounds = new Rectangle(0,0,frameWidth,frameHeight);
	}
	
	public void connectVideoCapture() throws FileNotFoundException {
		this.vidCap = new VideoCapture(this.filePath);
		if (!vidCap.isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + this.filePath);
		} 
	}
	
	public Mat readFrame() {
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

	public int getCurrentFrameNum() {
		return (int) vidCap.get(Videoio.CV_CAP_PROP_POS_FRAMES);
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public double getFrameRate() {
		return vidCap.get(Videoio.CAP_PROP_FPS);
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


