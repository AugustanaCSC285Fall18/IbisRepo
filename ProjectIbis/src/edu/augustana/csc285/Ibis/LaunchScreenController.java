package edu.augustana.csc285.Ibis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.augustana.csc285.Ibis.datamodel.ProjectData;
import edu.augustana.csc285.Ibis.datamodel.Video;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video", "*.mp4", "*.avi"));
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
	/**
	 * Handles button that prompts user to open existing json project.
	 * Used for resuming work on a previously saved project.
	 */
	@FXML
	public void handleLoadProjectButton() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select an existing project");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON file", "*.json"));
		File projectFile = fileChooser.showOpenDialog(newProjectButton.getScene().getWindow());
		if(projectFile !=null){
			try {
				project=ProjectData.loadFromFile(projectFile);
				loadProjectTextField.setText(projectFile.getPath());
				this.newProjectButton.disableProperty(); 
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}			
		}
		
	}
	/**
	 * Handles button that takes user to next window.
	 * if newProjectTextField is filled and loadProjectTextField is empty takes user to calibration.
	 * if loadProjectTextField is filled and newProjectTextField is empty takes user to main window.
	 * 
	 * @throws IOException
	 */
	@FXML
	public void handleOK() throws IOException {
		if (!newProjectTextField.getText().equals("") && this.loadProjectTextField.getText().equals("")) {
			proceedToCalibration();
		}else if(newProjectTextField.getText().equals("") && !this.loadProjectTextField.getText().equals("")) {
			proceedToMainWindow();
		}else if(!newProjectTextField.getText().equals("") &&!this.loadProjectTextField.getText().equals("")) {
			informationalDialog("Only one file is allowed please try again");
			this.newProjectTextField.setText("");
			this.loadProjectTextField.setText("");
		}else {
			informationalDialog("Please select a file");
		}

	}
	/**
	 * Helper method for when warning messages or prompts are to be displayed to the user.
	 * creates alert popup with proper information displayed.
	 * 
	 * @param message
	 */
	public static void informationalDialog(String message) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setResizable(false);
		alert.setHeaderText(null);
		alert.setTitle("Error");
		alert.setContentText(message);
		alert.showAndWait();
	}
	/**
	 * Helper method that takes the user to calibration screen.
	 * @throws IOException
	 */
	public void proceedToCalibration() throws IOException {
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
	/**
	 * Helper method that takes the user to main window screen.
	 * @throws IOException
	 */
	public void proceedToMainWindow() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
		AnchorPane root = (AnchorPane) loader.load();
		
		MainWindowController nextController = loader.getController();
				
		nextController.setProject(project);
		
		Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		Stage primary = (Stage) okButton.getScene().getWindow();
		primary.setScene(nextScene);
		primary.setTitle("Chick Tracker 1.0");		
	}
}
