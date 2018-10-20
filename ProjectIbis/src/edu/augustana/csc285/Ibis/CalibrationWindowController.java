package edu.augustana.csc285.Ibis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import edu.augustana.csc285.Ibis.datamodel.AnimalTrack;
import edu.augustana.csc285.Ibis.datamodel.ProjectData;
import edu.augustana.csc285.Ibis.datamodel.TimePoint;
import edu.augustana.csc285.Ibis.datamodel.Video;
import edu.augustana.csc285.Ibis.utils.UtilsForOpenCV;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class CalibrationWindowController {
	@FXML
	private ImageView videoView;
	@FXML
	private Canvas canvasView;
	@FXML
	private Slider videoSlider;
	@FXML
	private Label timeDisplayed;
	@FXML
	private Button addButton;
	@FXML
	private Button removeButton;
	@FXML
	private Button finishButton;
	
	private ProjectData project;
	private AnimalTrack animalTrack = new AnimalTrack(null);
	
	@FXML
	public void initialize() {
		videoSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number initalVal, Number finalVal) {
					updateTimeLabel();
					showFrameAt(finalVal.intValue());
			}
		});
		
	}
	
	@FXML
	public void handleFinishButton() throws IOException {
		
		System.out.println("handle Finish!");
		FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
		AnchorPane root = (AnchorPane) loader.load();
		
		MainWindowController nextController = loader.getController();
		nextController.setProject(project);
		
		Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		Stage primary = (Stage) finishButton.getScene().getWindow();
		primary.setScene(nextScene);
		primary.setTitle("Chick Tracker 1.0");		
	}
	public void setVideo(Video video) throws FileNotFoundException {
		project = new ProjectData(video);
		project.getVideo().setXPixelsPerCm(6);
		project.getVideo().setYPixelsPerCm(6);
		videoSlider.setMax(project.getVideo().getTotalNumFrames()-1); // need the minus one to not go off the video and resolve the errors.
		showFrameAt(0);
	}
	
	public void showFrameAt(int frameNum) {
			project.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			videoView.setImage(curFrame);
			
			GraphicsContext drawingPen = canvasView.getGraphicsContext2D();
			drawingPen.clearRect(0, 0, canvasView.getWidth(), canvasView.getHeight());
			// want to draw the correct dots that had been previously stored for this frame
		}		

	//timeLabel updates as slider moves	
	public void updateTimeLabel() {			
		int timeInSecs = (int)Math.round(project.getVideo().convertFrameNumsToSeconds((int) videoSlider.getValue()));
		String timeString = String.format("%d:%02d", timeInSecs / 60, timeInSecs % 60);
		timeDisplayed.setText(timeString);
	}
	
	@FXML
	public void handleAddbutton() {
		TextInputDialog dialog = new TextInputDialog("");
		dialog.setTitle("Id selector");
		dialog.setHeaderText("Id Assigner");
		dialog.setContentText("Please enter the name of this chick: ");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
		    System.out.println("Your name: " + result.get());
		}
	}
	
	@FXML
	public void handleRemoveButton() {
		
	}
}
