package edu.augustana.csc285.Ibis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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
import javafx.scene.control.ComboBox;
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
	private Label timeDisplayed;
	@FXML
	private Button btnForward;
	@FXML
	private Button btnBackward;
	@FXML
	private ComboBox<String> comboBoxSegment;

	@FXML
	private MenuItem exportToCSVItem;
//	@FXML
//	private TextField textFieldCurFrameNum;
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
	public static final Color[] TRACK_COLORS = new Color[] { Color.RED, Color.BLUE, Color.GREEN, Color.CYAN,
			Color.MAGENTA, Color.BLUEVIOLET, Color.ORANGE };

	private ToggleGroup buttonGroup = new ToggleGroup();


	private AutoTracker autoTracker;
	private ProjectData project;
	private GraphicsContext drawingPen;


	@FXML
	public void initialize() {
		this.radioButtonList = new ArrayList<RadioButton>();
		drawingPen= canvasView.getGraphicsContext2D();
		videoSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number initalVal, Number finalVal) {
				timeDisplayed.setText(getTimeAsString());
				showFrameAt(finalVal.intValue());
				drawArenaBound();
			}
		});

	}

	/**
	 * Sets current project to the project received from the previous window. 
	 * 
	 * @param project - received project
	 */
	public void setProject(ProjectData project) {
		this.project = project;
		createRadioButtonsForChicks();
//		System.out.println("width " + this.project.getVideo().getArenaBounds().getWidth());
//		System.out.println("height " + this.project.getVideo().getArenaBounds().getHeight());
//		System.out.println("X " + this.project.getVideo().getArenaBounds().getX());
//		System.out.println("y " + this.project.getVideo().getArenaBounds().getY());

		SizingUtilities.setCanvasSizeToMatchVideo(this.project.getVideo(), this.videoView, this.canvasView);
		videoSlider.setMax(project.getVideo().getEndFrameNum() - 1); // need the minus one to not go off the video and
		videoSlider.setMin(project.getVideo().getStartFrameNum());
		showFrameAt(this.project.getVideo().getStartFrameNum());
		drawArenaBound();
	}

	private void drawArenaBound() {
		drawingPen.setStroke(Color.GOLD);
		drawingPen.strokeRect(project.getVideo().getArenaBounds().getX(), project.getVideo().getArenaBounds().getY(),
				project.getVideo().getArenaBounds().getWidth(), project.getVideo().getArenaBounds().getHeight());
	}

	/**
	 * Allows user to save current progress as json file. 
	 * 
	 * @param event
	 * @throws IOException
	 */

	@FXML
	public void SaveProjectItem(ActionEvent event) throws IOException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Saving the project");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(" JSON file", "*.json"));
		File file = fileChooser.showSaveDialog(this.btnTrack.getScene().getWindow());
		if (file != null) {
			project.saveToFile(file);
		}
	}

	/**
	 * Method for when user wants to export the data to a CSV file.
	 * 
	 * @param event 
	 * @throws IOException
	 */

	@FXML
	public void ExportToCSVItem(ActionEvent event) throws IOException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Exporting to CSV file");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
		File file = fileChooser.showSaveDialog(this.btnTrack.getScene().getWindow());
		if (file != null) {
			project.exportToCSV(file);
		}
	}

	public Slider getSlider() {
		return this.videoSlider;
	}

	public String getTimeAsString() {
		int timeInSecs = (int) Math.round(project.getVideo().convertFrameNumsToSeconds((int) videoSlider.getValue()));
//		textFieldCurFrameNum.setText(String.format("%05d", (int) videoSlider.getValue()));
		return String.format("%02d:%02d", timeInSecs / 60, timeInSecs % 60);
	}

	/**
	 * takes in point from mouse click and draws a visual point centered on the x
	 * and y coordinates. 
	 * 
	 * @param event
	 */


	private void addPointToTrack(MouseEvent event) {
		for (int index = 0; index < radioButtonList.size(); index++) {
			if (radioButtonList.get(index).getText() == project.getTracks().get(index).getAnimalId()
					&& radioButtonList.get(index).isSelected()) {
				project.getTracks().get(index).setTimePointAtTime(event.getX(), event.getY(), project.getVideo().getCurrentFrameNum());;
//				System.out.println(project.getTracks().get(index).toString());
			}
		}
	}

	/**
	 * Helper method for displaying correct image for the frame passed in. 
	 * 
	 * @param frameNum - current frame number
	 */
	public void showFrameAt(int frameNum) {
		if (autoTracker == null || !autoTracker.isRunning()) {
			project.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			videoView.setImage(curFrame);
//			textFieldCurFrameNum.setText(String.format("%05d", frameNum));
			drawingPen.clearRect(0, 0, canvasView.getWidth(), canvasView.getHeight());
			double scalingRatio = getImageScalingRatio();
			
			drawingPen.drawImage(curFrame, 0, 0, curFrame.getWidth() * scalingRatio, curFrame.getHeight() * scalingRatio);

			drawUnassignedSegments(drawingPen, scalingRatio, frameNum);
			drawAssignedAnimalTracks(drawingPen, scalingRatio, frameNum);

		}

	}
	
	private void drawAssignedAnimalTracks(GraphicsContext drawingPen, double scalingRatio, int frameNum) {
		for (int i = 0; i < project.getTracks().size(); i++) {
			AnimalTrack track = project.getTracks().get(i);
			Color trackColor = TRACK_COLORS[i % TRACK_COLORS.length];
			Color trackPrevColor = trackColor.deriveColor(0, 0.5, 1.5, 1.0); // subtler variant

			drawingPen.setFill(trackPrevColor);
			// draw chick's recent trail from the last few seconds
			for (TimePoint prevPt : track.getTimePointsWithinInterval(frameNum - 90, frameNum)) {
				drawingPen.fillOval(prevPt.getX()- 3, prevPt.getY()- 3, 7, 7);
			}
			// draw the current point (if any) as a larger dot
			TimePoint currPt = track.getTimePointAtTime(frameNum);
			if (currPt != null) {
				drawingPen.setFill(trackColor);
				drawingPen.fillOval(currPt.getX()- 7, currPt.getY()- 7, 15, 15);
			}
		}
		
	}

	private void drawUnassignedSegments(GraphicsContext drawingPen, double scalingRatio, int frameNum) {
		for (AnimalTrack segment : project.getUnassignedSegments()) {
			drawingPen.setFill(Color.GRAY);
			// draw this segments recent past & near future locations
			for (TimePoint prevPt : segment.getTimePointsWithinInterval(frameNum+30, frameNum+30)) {
				drawingPen.fillRect(prevPt.getX() * scalingRatio - 1, prevPt.getY() * scalingRatio - 1, 2, 2);
			}
			// draw the current point (if any) as a larger square
			TimePoint currPt = segment.getTimePointAtTime(frameNum);
			if (currPt != null) {
				drawingPen.fillRect(currPt.getX() * scalingRatio - 5, currPt.getY() * scalingRatio - 5, 11, 11);
			}
		}
	}
	
	
	
	private double getImageScalingRatio() {
		double widthRatio = canvasView.getWidth() / project.getVideo().getFrameWidth();
		double heightRatio = canvasView.getHeight() / project.getVideo().getFrameHeight();
		return Math.min(widthRatio, heightRatio);
	}

	/**
	 * Starts the autoTracker and checks if its not cancelled
	 * */
	@FXML
	public void handleAutoTrack() {
		if (autoTracker == null || !autoTracker.isRunning()) {
			Video video = project.getVideo();
			video.setStartFrameNum(project.getVideo().getStartFrameNum());
			video.setEndFrameNum(project.getVideo().getEndFrameNum());
			autoTracker = new AutoTracker();
			
			autoTracker.addAutoTrackListener(this);
			autoTracker.startAnalysis(project.getVideo());
			
			btnTrack.setText("CANCEL auto-tracking");
			comboBoxSegment.getItems().clear();
		} else {
			autoTracker.cancelAnalysis();
			btnTrack.setText("Start auto-tracking");
			timeDisplayed.setText(getTimeAsString());
		}
	}

	/**
	 * Assigns the selected track to the current selected chick
	 */
	
	@FXML
	public void handleAssignButton() {
		if (comboBoxSegment.getItems().size() > 0) {
			for (int index = 0; index < radioButtonList.size(); index++) {
				if(radioButtonList.get(index).getText() == project.getTracks().get(index).getAnimalId() && radioButtonList.get(index).isSelected()) {						
					for(int i=0; i<project.getUnassignedSegments().get(comboBoxSegment.getSelectionModel().getSelectedIndex()).size(); i++) {;
					project.getTracks().get(index).add(project.getUnassignedSegments().get(comboBoxSegment.getSelectionModel().getSelectedIndex()).getTimePointAtIndex(i));					
					}
				}
			}
			comboBoxSegment.getItems().remove(comboBoxSegment.getSelectionModel().getSelectedIndex());
		} 
		comboBoxSegment.getSelectionModel().select(0);
	}

	/**
	 * Helper method that videoView, timeDisplayed, progressAutoTrack, videoSlider,
	 * and textFieldCurFrameNum. used but AutoTracker in its Thread.
	 */

	@Override
	public void handleTrackedFrame(Mat frame, int frameNumber, double fractionComplete) {
		Image imgFrame = UtilsForOpenCV.matToJavaFXImage(frame);
		Platform.runLater(() -> {
			double scalingRatio = getImageScalingRatio();
			drawingPen.drawImage(imgFrame, 0, 0, imgFrame.getWidth() * scalingRatio, imgFrame.getHeight() * scalingRatio);
			
			timeDisplayed.setText(getTimeAsString());
			progressAutoTrack.setProgress(fractionComplete);
			videoSlider.setValue(frameNumber);
//			textFieldCurFrameNum.setText(String.format("%05d", frameNumber));
		});
	}

	/**
	 * Takes in list of trackedSegments and adds them to current projects List of
	 * unassignedSegments. Resets progressAutoTrack bar and btnTrack label to base
	 * state.
	 * 
	 * @param List<AnimalTrack> - unassigned tracks received from Autotracker
	 */

	@Override
	public void trackingComplete(List<AnimalTrack> trackedSegments) {
//		System.out.println("TRACKING COMPLETE");
//		System.out.println("Size of unassigned segments before removal: " + trackedSegments.size() + " line 340");
		project.getUnassignedSegments().clear();
		removeTracksWithLessThanFivePoints(trackedSegments);
		project.getUnassignedSegments().addAll(trackedSegments);
//
//		System.out.println("How many chicks are there in the project " + project.getTracks().size() + " line 345");
//		System.out.println("size of unassigned segments after removal :" + project.getUnassignedSegments().size() + " line 346");

		for (int index = 0; index < trackedSegments.size(); index++) {
			comboBoxSegment.getItems().add(trackedSegments.get(index).getAnimalId());
//			System.out.println("number of points in autotracked segment " + trackedSegments.get(index) + " line 345");
		}
		canvasView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				if (!(project.getVideo().getCurrentFrameNum() == project.getVideo().getEndFrameNum())) {
					addPointToTrack(event);
					handleBtnForward();
				}
			}
		});
		Platform.runLater(() -> {
			comboBoxSegment.getSelectionModel().select(0);
			progressAutoTrack.setProgress(1.0);
			btnTrack.setText("Start auto-tracking");
			
		});

	}

	
	public void removeTracksWithLessThanFivePoints(List<AnimalTrack> trackedSegments) {
		List<AnimalTrack> newlist = new ArrayList<AnimalTrack>();
		for (int index = 0; index < trackedSegments.size(); index++) {
			if (trackedSegments.get(index).size() < 5) {
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
	
	

	@FXML
	public void showSelectedAutoTrack() {
		if(comboBoxSegment.getSelectionModel().getSelectedIndex() !=-1) {
			AnimalTrack track = project.getUnassignedSegments().get(comboBoxSegment.getSelectionModel().getSelectedIndex());
			videoSlider.setValue(track.getTimePointAtIndex(0).getFrameNum());
	}
	}
	
	@FXML
	public void showAboutUs() {
		LaunchScreenController.informationalDialog("This project was done by Omidullah Barikzay, Kevin Monroy, Jacob Bell and Mathew Benson. \n Under the supervision of Professor Forest Stonedahl.", "About");
	}

}
