package edu.augustana.csc285.Ibis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
import edu.augustana.csc285.Ibis.utils.SizingUtilities;
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
	private List<RadioButton> radioButtonList;
	ToggleGroup buttonGroup = new ToggleGroup();

	private ScheduledExecutorService timer;

	private AutoTracker autoTracker;

	private ProjectData project;

	/**
	 * Initializes ArrayList of RadioButton for chick toggle group. Adds listener
	 * for mouseClick that draws points. Adds observer for slideBar that updates
	 * imageView with new frame.
	 */
	
	@FXML
	public void initialize() {
		this.radioButtonList = new ArrayList<RadioButton>();

		canvasView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			// modify the location to reflect the actual location and not with the
			// comparison to the whole GUI
			public void handle(MouseEvent event) {
				drawPoint(event);
				TimePoint point = new TimePoint(event.getX(),event.getY(), project.getVideo().getCurrentFrameNum());
//				addPointToTrack(point); //TODO: polish this line and the respective method
			}
		});

		videoSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number initalVal, Number finalVal) {
				timeDisplayed.setText(getTimeString());
				showFrameAt(finalVal.intValue());
			}
		});

	}

//	protected void addPointToTrack(TimePoint point) {
//		for (RadioButton radioButton: radioButtonList) {
//		for (int i = 0 ; i < radioButtonList.size(); i++) {
//			System.out.println(radioButtonList.size());
//			System.out.println(project.getTracks().size());
//		}
//			if (radioButton.getText()== project.getTracks().iterator().next().getAnimalId()) {
//				
//			}
//		}
//	}

	/**
	 * Sets current project to param passed in. Sets videoSlider bounds to maximum
	 * and minimum values for the video. Displays the first frame of the given
	 * video.
	 * 
	 * @param project
	 */
	public void setProject(ProjectData project) {
		this.project = project;
		createRadioButtonsForChicks();
		//SizingUtilities.setCanvasSizeToMatchVideo(this.project.getVideo(), this.videoView, this.canvasView);
		videoSlider.setMax(project.getVideo().getEndFrameNum() - 1); // need the minus one to not go off the video and
		videoSlider.setMin(project.getVideo().getStartFrameNum());
		showFrameAt(this.project.getVideo().getStartFrameNum());
	}

	/**
	 * Helper method for when user is saving json file. Uses a fileChooser to select
	 * the save destination and name.
	 * 
	 * @param e
	 * @throws IOException
	 */
	
	@FXML
	public void SaveProjectItem(ActionEvent e) throws IOException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Saving the project");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(" JSON file", "*.json"));
		File file = fileChooser.showSaveDialog(this.btnTrack.getScene().getWindow());
		if (file != null) {
			project.saveToFile(file);
		}
	}

	/**
	 * Helper method for when user is saving scv file. Uses a fileChooser to select
	 * save destination and name.
	 * 
	 * @param e
	 * @throws IOException
	 */
	
	@FXML
	public void ExportToCSVItem(ActionEvent e) throws IOException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Exporting to CSV file");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
		File file = fileChooser.showSaveDialog(this.btnTrack.getScene().getWindow());
		if (file != null) {
			project.exportToCSV(file);
		}
	}

	/**
	 * Returns videoSlider object.
	 * 
	 * @return Slider
	 */
	public Slider getSlider() {
		return this.videoSlider;
	}

	/**
	 * Creates a runnable object and timer that automatically plays through the
	 * video
	 */
	protected void startPlaying() {
		Runnable frameGrabber = new Runnable() {
			@Override
			public void run() {
				// TODO: this playing approach doesn't work yet... may need a different
				// approach...
				showFrameAt(project.getVideo().getCurrentFrameNum() + 1);
			}
		};
		// timer for 33 milliseconds, can call in larger increments of frames to play
		// faster
		this.timer = Executors.newSingleThreadScheduledExecutor();
		this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
	}



	/**
	 * updates and formats timeLabel as slider moves
	 * 
	 * @return String
	 */
	public String getTimeString() {
		int timeInSecs = (int) Math.round(project.getVideo().convertFrameNumsToSeconds((int) videoSlider.getValue()));
		textFieldCurFrameNum.setText(String.format("%05d", (int) videoSlider.getValue()));
		return String.format("%02d:%02d", timeInSecs / 60, timeInSecs % 60);
	}

	/**
	 * takes in point from mouse click and draws a visual point centered on the x
	 * and y coordinates. DUPLICATE CODE IN CALIBRATION WINDOW CONTROLLER.
	 * 
	 * @param event
	 */
	public void drawPoint(MouseEvent event) {
		GraphicsContext drawingPen = canvasView.getGraphicsContext2D();
		drawingPen.setFill(Color.TOMATO);
		drawingPen.fillOval(event.getX(), event.getY(), 5, 5);
	}

	/**
	 * Helper method for displaying correct image for the frame passed in. 
	 * TODO want to draw the correct dots previously stored for this frame?
	 * 
	 * @param frameNum
	 */
	public void showFrameAt(int frameNum) {
		if (autoTracker == null || !autoTracker.isRunning()) {
			project.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			videoView.setImage(curFrame);
			textFieldCurFrameNum.setText(String.format("%05d", frameNum));

			GraphicsContext drawingPen = canvasView.getGraphicsContext2D();
			drawingPen.clearRect(0, 0, canvasView.getWidth(), canvasView.getHeight());
			// want to draw the correct dots that had been previously stored for this frame
		}
		
	}

	/**
	 * Handles start autoTrack button. runs autoTrack between
	 * project.getVideo().getStartFrameNum() and
	 * project.getVideo().getEndFrameNum().
	 */
	
	@FXML
	public void handleAutoTrack() {
		if (autoTracker == null || !autoTracker.isRunning()) {
			Video video = project.getVideo();
			video.setStartFrameNum(project.getVideo().getStartFrameNum());
			video.setEndFrameNum(project.getVideo().getEndFrameNum());
			autoTracker = new AutoTracker();
			// Use Observer Pattern to give autotracker a reference to this object,
			// and call back to methods in this class to update progress.
			autoTracker.addAutoTrackListener(this);

			// this method will start a new thread to run AutoTracker in the background
			// so that we don't freeze up the main JavaFX UI thread.
			autoTracker.startAnalysis(project.getVideo());
			btnTrack.setText("CANCEL auto-tracking");
		} else {
			autoTracker.cancelAnalysis();
			btnTrack.setText("Start auto-tracking");
			timeDisplayed.setText(getTimeString());
		}
	}

	/**
	 * Helper method that videoView, timeDisplayed, progressAutoTrack, videoSlider,
	 * and textFieldCurFrameNum. used but AutoTracker in its Thread.
	 */
	
	@Override
	public void handleTrackedFrame(Mat frame, int frameNumber, double fractionComplete) {
		Image imgFrame = UtilsForOpenCV.matToJavaFXImage(frame);
		// this method is being run by the AutoTracker's thread, so we must
		// ask the JavaFX UI thread to update some visual properties
		Platform.runLater(() -> {
			timeDisplayed.setText(getTimeString());
			videoView.setImage(imgFrame);
			progressAutoTrack.setProgress(fractionComplete);
			videoSlider.setValue(frameNumber);
			textFieldCurFrameNum.setText(String.format("%05d", frameNumber));
		});

	}

	/**
	 * Takes in list of trackedSegments and adds them to current projects List of
	 * unassignedSegments. Resets progressAutoTrack bar and btnTrack label to base
	 * state.
	 * 
	 * @param List<AnimalTrack>
	 */
	
	@Override
	public void trackingComplete(List<AnimalTrack> trackedSegments) {
		System.out.println("TRACKING COMPLETE");
		System.out.println("Size: " + trackedSegments.size());
		project.getUnassignedSegments().clear();
		removeTrackWithLessThanFivePoints(trackedSegments);
		project.getUnassignedSegments().addAll(trackedSegments);

		System.out.println(project.getTracks().size());
		System.out.println("unassigned segments :" + project.getUnassignedSegments().size());
		
		for (AnimalTrack track : trackedSegments) {
			System.out.println(track);
//			System.out.println("  " + track.getPositions());
		}

		Platform.runLater(() -> {
			progressAutoTrack.setProgress(1.0);
			btnTrack.setText("Start auto-tracking");
		});

	}
	
	public void removeTrackWithLessThanFivePoints(List<AnimalTrack> trackedSegments) {
		List<AnimalTrack> newlist = new ArrayList<AnimalTrack>();
		for(int index =0; index< trackedSegments.size(); index++) {
			if(trackedSegments.get(index).size()<5) {
				newlist.add(trackedSegments.get(index));
			}
		}
		trackedSegments.removeAll(newlist);
	}

	/**
	 * Iterates through current project.getTracks() and creates a radio button for
	 * each one.
	 */
	public void createRadioButtonsForChicks() {
		for (int i = 0; i < project.getTracks().size(); i++) {
			radioButtonList.add(new RadioButton());
			radioButtonList.get(i).setText(project.getTracks().get(i).getAnimalId());
			radioButtonList.get(i).setToggleGroup(buttonGroup);
			flowPanel.getChildren().add(radioButtonList.get(i));
		}
		radioButtonList.get(0).setSelected(true);
	}

	/**
	 * Handles button that causes videoSlider to jump forward 30 frames.
	 */
	public void handleBtnForward() {
		this.videoSlider.setValue(this.videoSlider.getValue() + 30);
	}

	/**
	 * Handles button that causes videoSlider to jump backward 30 frames.
	 */
	public void handleBtnBackward() {
		this.videoSlider.setValue(this.videoSlider.getValue() - 30);
	}

}
