package edu.augustana.csc285.Ibis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.augustana.csc285.Ibis.datamodel.ProjectData;
import edu.augustana.csc285.Ibis.datamodel.Video;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class LaunchScreenController {
	@FXML private Button newProjectButton;
	@FXML private Button loadProjectButton;

	@FXML private Button okButton;
	@FXML private TextField newProjectTextField;
	@FXML private TextField loadProjectTextField;
	private Video video;
	private ProjectData project;

	
	/**
	 * When the user clicks the browse button the method opens a window for the user
	 * to select a video. 
	 */
	@FXML
	public void handleNewProject()  {
		FileChooser fileChooser = new FileChooser();

		fileChooser.setTitle("Select a file");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video", "*.mp4", "*.mp3", "*.avi"));
		File videoFile = fileChooser.showOpenDialog(newProjectButton.getScene().getWindow());
		if(videoFile !=null){
			try {
				video= new Video(videoFile.getPath());
				newProjectTextField.setText(video.getFilePath());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	@FXML
	public void handleloadProjectButton() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select an existing project");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON file", "*.json"));
		File videoFile = fileChooser.showOpenDialog(newProjectButton.getScene().getWindow());
		if(videoFile !=null){
			try {
				project=project.fromJSON(videoFile.getPath());
				loadProjectTextField.setText(project.getVideo().getFilePath());
				this.newProjectButton.disableProperty(); //does this set it to null? 
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
	}

	@FXML
	public void handleOK() throws IOException {
		if (video.getFilePath() != null) {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("CalibrationWindow.fxml"));
				AnchorPane root = (AnchorPane) loader.load();
				
				CalibrationWindowController nextController = loader.getController();
			
				nextController.setVideo(video.getFilePath());
				
				Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
				nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				Stage primary = (Stage) okButton.getScene().getWindow();
				primary.setScene(nextScene);
				primary.setTitle("Calibration Window");
		}
	}
}
