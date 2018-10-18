package edu.augustana.csc285.Ibis;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.augustana.csc285.Ibis.datamodel.ProjectData;
import edu.augustana.csc285.Ibis.datamodel.Video;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
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
	private Label messageLabel;
	@FXML
	private Button finishButton;
	
	private ProjectData project;
	
	@FXML
	public void handleFinishButton() throws IOException {
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
		//showFrameAt(0);
	}

}
