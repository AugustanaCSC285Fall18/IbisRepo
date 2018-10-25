package edu.augustana.csc285.Ibis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;

import com.google.gson.stream.JsonWriter;

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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

public class MainWindowController implements AutoTrackListener {
	@FXML
	private ImageView videoView;
	@FXML
	private Canvas canvasView;
	@FXML
	private Slider videoSlider;
	@FXML
	private Label messageLabel;
	@FXML
	private Label timeDisplayed;
	@FXML
	private Button btnForward;
	@FXML
	private Button btnBackward;
	

	@FXML
	private MenuItem exportToCSVItem;
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
	@FXML 
	private FlowPane flowPanel;
	private List<AnimalTrack> animalTrackList;
	private List<RadioButton> radioButtonList;
	ToggleGroup buttonGroup = new ToggleGroup();

	
	private ScheduledExecutorService timer;
	
//	private TimePoint timePoint;
	
//	private AnimalTrack animalTrack1 = new AnimalTrack("Chick manual 1");
//	private AnimalTrack animalTrack2 = new AnimalTrack("Chick manual 2");
//	private AnimalTrack animalTrack3 = new AnimalTrack("Chick manual 3");
	
	

	private AutoTracker autoTracker;
	
	private ProjectData project;

	
	
	@FXML
	public void initialize() {
	
		
		canvasView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			//modify the location to reflect the actual location and not with the comparison to the whole GUI
			public void handle(MouseEvent event) {
//				timePoint= new TimePoint (event.getX(),event.getY(),(int)videoSlider.getValue());
				drawPoint(event);
//				addTimePointToAnimalTrack();
			}
		});
		
		
		videoSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number initalVal, Number finalVal) {
					updateTimeLabel();
					showFrameAt(finalVal.intValue());
			}
		});
		
	}
	
	
	
	public void setProject(ProjectData project){
		this.project = project;
		videoSlider.setMax(project.getVideo().getTotalNumFrames()-1); // need the minus one to not go off the video and resolve the errors.
		showFrameAt(this.project.getVideo().getStartFrameNum()); 
	}

	@FXML
	public void SaveProjectItem (ActionEvent e) throws IOException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Saving the project");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(" JSON file", "*.json"));
		File file = fileChooser.showSaveDialog(this.btnTrack.getScene().getWindow());
		if(file !=null) {
			project.saveToFile(file);	
		}
	}
	
	@FXML
	public void ExportToCSVItem (ActionEvent e) {
		
	}
	public Slider getSlider() {
		return this.videoSlider;
	}
	
	protected void startPlaying() {
		Runnable frameGrabber = new Runnable() {
			@Override
			public void run() {
				// TODO: this playing approach doesn't work yet... may need a different approach...
				showFrameAt(project.getVideo().getCurrentFrameNum()+1);
			}
		};
		// timer for 33 milliseconds, can call in larger increments of frames to play
		// faster
		this.timer = Executors.newSingleThreadScheduledExecutor();
		this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
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
		int timeInSecs = (int)Math.round(project.getVideo().convertFrameNumsToSeconds((int) videoSlider.getValue()));
		String timeString = String.format("%d:%02d", timeInSecs / 60, timeInSecs % 60);
		timeDisplayed.setText(timeString);
	
		textFieldCurFrameNum.setText(String.format("%05d",(int)videoSlider.getValue()));
	}


	public void drawPoint(MouseEvent event) {
		GraphicsContext drawingPen = canvasView.getGraphicsContext2D();
		drawingPen.setFill(Color.TOMATO);
		drawingPen.fillOval(event.getX(),event.getY() , 5, 5);
	}
	
	public void showFrameAt(int frameNum) {
		if (autoTracker == null || !autoTracker.isRunning()) {
			project.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			videoView.setImage(curFrame);
			textFieldCurFrameNum.setText(String.format("%05d",frameNum));
			
			GraphicsContext drawingPen = canvasView.getGraphicsContext2D();
			drawingPen.clearRect(0, 0, canvasView.getWidth(), canvasView.getHeight());
			// want to draw the correct dots that had been previously stored for this frame
		}		
	}
	
	@FXML
	public void	handleAutoTrack(){
		if (autoTracker == null || !autoTracker.isRunning()) {
			Video video = project.getVideo();
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
		System.out.println("TRACKING COMPLETE");
		System.out.println("Size: " + trackedSegments.size());
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

	public void animalTrackModifier(int numberOfChicks, ArrayList<String> names) {
		for (int i = 0; i<numberOfChicks;i++) {
			animalTrackList.add(new AnimalTrack (names.get(i)));
			radioButtonList.add(new RadioButton());
			radioButtonList.get(i).setText(names.get(i));
			radioButtonList.get(i).setToggleGroup(buttonGroup);
			flowPanel.getChildren().add(radioButtonList.get(i));
		}
	}
	
	public void handleBtnForward() {
		 int newFrameNum = this.project.getVideo().getCurrentFrameNum() + 30;
		showFrameAt(newFrameNum);
		this.project.getVideo().setCurrentFrameNum(newFrameNum);
		updateTimeLabel();
		System.out.println(newFrameNum);
		System.out.println(this.project.getVideo().getCurrentFrameNum());
		
	}
	
	public void handleBtnBackward() {
		 int newFrameNum = this.project.getVideo().getCurrentFrameNum() - 30;
		showFrameAt(newFrameNum);
		this.project.getVideo().setCurrentFrameNum(newFrameNum);
		updateTimeLabel();
		System.out.println(newFrameNum);
		System.out.println(this.project.getVideo().getCurrentFrameNum());
		}
	
}
