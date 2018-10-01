package edu.augustana.csc285.Ibis;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.ButtonGroup;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import edu.augustana.csc285.Ibis.datamodel.ProjectData;
import edu.augustana.csc285.Ibis.datamodel.TimePoint;
import edu.augustana.csc285.Ibis.autotracking.AutoTrackListener;
import edu.augustana.csc285.Ibis.autotracking.AutoTracker;
import edu.augustana.csc285.Ibis.datamodel.AnimalTrack;
import edu.augustana.csc285.Ibis.datamodel.Video;
import edu.augustana.csc285.Ibis.utils.UtilsForOpenCV;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class MainWindowController implements AutoTrackListener {
	@FXML
	private ImageView videoView;
	@FXML
	private Canvas canvasView;
	@FXML
	private Slider videoSlider;
	@FXML
	private Label messageScreen;
	@FXML
	private Label timeDisplayed;
	@FXML
	private Button exportToCSV;
	@FXML
	private TextField textFieldCurFrameNum;
	@FXML
	private Button btnTrack;
	@FXML
	private TextField textfieldStartFrame;
	@FXML
	private TextField textfieldEndFrame;
	@FXML
	private ProgressBar progressAutoTrack;
	
	ToggleGroup buttonGroup = new ToggleGroup();
	
	@FXML
	private RadioButton chickOneButton;
	@FXML
	private RadioButton chickTwoButton;
	@FXML
	private RadioButton chickThreeButton;

	
	private ScheduledExecutorService timer;
	private Video video;
	
	private TimePoint timePoint;
	
	private AnimalTrack animalTrack1 = new AnimalTrack("Chick manual 1");
	private AnimalTrack animalTrack2 = new AnimalTrack("Chick manual 2");
	private AnimalTrack animalTrack3 = new AnimalTrack("Chick manual 3");
	
	
	private ProjectData project;
	private AutoTracker autoTracker = new AutoTracker();

	
	
	@FXML
	public void initialize() {
		
		chickOneButton.setToggleGroup(buttonGroup);
		chickTwoButton.setToggleGroup(buttonGroup);
		chickThreeButton.setToggleGroup(buttonGroup);
		
		canvasView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			//modify the location to reflect the actual location and not with the comparison to the whole GUI
			public void handle(MouseEvent event) {
				GraphicsContext drawingPen = canvasView.getGraphicsContext2D();
				drawingPen.setFill(Color.TOMATO);
				drawingPen.fillOval(event.getX(),event.getY() , 5, 5);
				
				timePoint= new TimePoint (event.getX(),event.getY(),(int)videoSlider.getValue());
				
				if (buttonGroup.getSelectedToggle()==chickOneButton) {
					if(timePoint.getFrameNum()==animalTrack1.getTimePointAtIndex(index))
					animalTrack1.add(timePoint);
					System.out.println(animalTrack1.toString());
				} else if (buttonGroup.getSelectedToggle()==chickTwoButton) {
					animalTrack2.add(timePoint);
					System.out.println(animalTrack2.toString());
				} else if (buttonGroup.getSelectedToggle()==chickThreeButton){
					animalTrack3.add(timePoint);
					System.out.println(animalTrack3.toString());
				}
				
					
					System.out.println("Point that's being stored " + timePoint.toString());

				//	System.out.println(animalTrack.getTimePointAtTime((int) videoSlider.getValue()));
	
			}
		});
		videoSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number initalVal, Number finalVal) {
				if (videoSlider.isValueChanging()) {

					updateTimeLabel();
					
					try {
						if (timer != null) {
							timer.shutdown();
							timer.awaitTermination(1000, TimeUnit.MILLISECONDS);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// System.out.println(finalVal);
					video.setCurrentFrameNum((double) finalVal);
					grabFrame();

					// resumes player
					// startPlaying();
				}
			}
		});
	}
	
	@FXML
	public void handleExport() {

	}
	
	public void setVideo(Video video) throws FileNotFoundException {
		// project = new ProjectData(video.getPath());
		this.video = video;
		videoSlider.setMax(this.video.getTotalNumFrames()-1); // need the minus one to not go off the video and resolve the errors.
		grabFrame();

	}

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

		Mat frame = video.readFrame();
		Image image = UtilsForOpenCV.matToJavaFXImage(frame);
		
		findRealImageSize(image);
		videoView.setImage(image);
			
		
	}
	
	public void findRealImageSize(Image image) {
		//thanks johnniegf on StackExchange. You the real G
		double aspectRatio = image.getWidth() / image.getHeight();
		double realWidth = Math.min(videoView.getFitWidth(), videoView.getFitHeight() * aspectRatio);
		double realHeight = Math.min(videoView.getFitHeight(), videoView.getFitWidth() / aspectRatio);
		
		videoView.setFitHeight(realHeight);
		videoView.setFitWidth(realWidth);
		
		canvasView.setHeight(videoView.getFitHeight());
		canvasView.setWidth(videoView.getFitWidth());
	}
	
	//timeLabel updates as slider moves	
	public void updateTimeLabel() {			
		int timeInSecs = (int)Math.round(video.convertFrameNumsToSeconds((int) videoSlider.getValue()));
		String timeString = String.format("%d:%02d", timeInSecs / 60, timeInSecs % 60);
		timeDisplayed.setText(timeString);
		System.out.println(timeDisplayed.getText());
		
		textFieldCurFrameNum.setText(String.format("%05d",(int)videoSlider.getValue()));

		
	}


	public void showFrameAt(int frameNum) {
		if (autoTracker == null || !autoTracker.isRunning()) {
			//project.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			videoView.setImage(curFrame);
			textFieldCurFrameNum.setText(String.format("%05d",frameNum));
			
		}		
	}
	@FXML
	public void	handleAutoTrack (){
		if (autoTracker == null || !autoTracker.isRunning()) {
			//Video video = project.getVideo();
			video.setStartFrameNum(Integer.parseInt(textfieldStartFrame.getText())); 
			video.setEndFrameNum(Integer.parseInt(textfieldEndFrame.getText()));
			autoTracker = new AutoTracker();
			// Use Observer Pattern to give autotracker a reference to this object, 
			// and call back to methods in this class to update progress.
			autoTracker.addAutoTrackListener(this);
			
			// this method will start a new thread to run AutoTracker in the background
			// so that we don't freeze up the main JavaFX UI thread.
			autoTracker.startAnalysis(video);
			btnTrack.setText("CANCEL auto-tracking");
		} else {
			autoTracker.cancelAnalysis();
			btnTrack.setText("Start auto-tracking");
			updateTimeLabel();
		}
	}
	@Override
	public void handleTrackedFrame(Mat frame, int frameNumber, double fractionComplete) {
		Image imgFrame = UtilsForOpenCV.matToJavaFXImage(frame);
		// this method is being run by the AutoTracker's thread, so we must
		// ask the JavaFX UI thread to update some visual properties
		Platform.runLater(() -> { 
			updateTimeLabel();
			videoView.setImage(imgFrame);
			progressAutoTrack.setProgress(fractionComplete);
			videoSlider.setValue(frameNumber);
			textFieldCurFrameNum.setText(String.format("%05d",frameNumber));
		});	
		
	}
	

	@Override
	public void trackingComplete(List<AnimalTrack> trackedSegments) {
		project.getUnassignedSegments().clear();
		project.getUnassignedSegments().addAll(trackedSegments);

		for (AnimalTrack track: trackedSegments) {
			System.out.println(track);
//			System.out.println("  " + track.getPositions());
		}
		Platform.runLater(() -> { 
			progressAutoTrack.setProgress(1.0);
			btnTrack.setText("Start auto-tracking");
		});	
		
		
	}

	
}
