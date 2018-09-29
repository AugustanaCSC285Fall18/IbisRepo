package edu.augustana.csc285.Ibis;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.Videoio;

import edu.augustana.csc285.Ibis.utils.UtilsForOpenCV;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MainWindowController {
	@FXML
	private ImageView videoView;
	@FXML
	private Slider videoSlider;
	@FXML
	private Label messageScreen;
	@FXML
	private Label timeDisplayed;
	@FXML
	private Button exportToCSV;

	@FXML
	public void handleExport() {

	}

	// Jacob Bell 9/29/18

	// handles the slider behavior and the browse button
	// we can rip this into the video class and the gui
	// to deal with the aforementioned features.
	// private VideoCapture capture = new VideoCapture();
	
	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;

	private Video video;

	public void setVideo(Video video) {
		this.video = video;
		videoSlider.setMax(video.getTotalNumFrames());
	}
	
	@FXML
	public void initialize() {
		videoSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number initalVal, Number finalVal) {
				if (videoSlider.isValueChanging()) {
					
					//timeLabel updates as slider moves				
					int timeInSecs = (int)Math.round(video.convertFrameNumsToSeconds((int) videoSlider.getValue()));
					String timeString = String.format("%d:%02d", timeInSecs / 60, timeInSecs % 60);
					timeDisplayed.setText(timeString);
					System.out.println(timeDisplayed.getText());
					
					try {
						if (timer != null) {
							timer.shutdown();
							timer.awaitTermination(1000, TimeUnit.MILLISECONDS);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//					System.out.println(finalVal);
					video.setCurrentFrameNum((double) finalVal);
					grabFrame();

					// resumes player
					//startPlaying();
				}
			}
		});
		
	}
	
	// called to play to video continuously by repeatedly calling the grab frame
	// method every 33 milliseconds
	protected void startPlaying() {
		Runnable frameGrabber = new Runnable() {
			@Override
			public void run() {
				grabFrame();
			}
		};
		// timer for 33 milliseconds, can call in larger increments of frames to play
		// faster
		this.timer = Executors.newSingleThreadScheduledExecutor();
		this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
	}

	// displays the next frame in the input stream of the video
	public void grabFrame() {
			// grabs video's info and puts it into a usable Mat object.
			
			Mat frame = video.read();
			videoView.setImage(UtilsForOpenCV.matToJavaFXImage(frame));
			
			// when slider is dragged (not clicked, problem?) the timer from player() will
			// pause, display is updated to desired frame and player resumes
			// ISSUE while clicked but not dragged video plays
			// ISSUE slider node does not move with video
			
		}
}
