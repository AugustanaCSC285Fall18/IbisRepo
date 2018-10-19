package edu.augustana.csc285.Ibis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
	@FXML private Button browseButton;
	@FXML private Button okButton;
	@FXML private TextField textField;
	private Video video = new Video();

	
	/**
	 * When the user clicks the browse button the method opens a window for the user
	 * to select a video. 
	 */
	@FXML
	public void handleBrowse()  {
		FileChooser fileChooser = new FileChooser();

		fileChooser.setTitle("Open Video File");
		File videoFile = fileChooser.showOpenDialog(browseButton.getScene().getWindow());
		try {
			video.setVideoFile(videoFile);
		} catch (FileNotFoundException e) {
			System.out.println("Please select a Video");
//			e.printStackTrace();
		}
		
		textField.setText(video.getVideoFile().getAbsolutePath());
	}

	@FXML
	public void handleOK() throws IOException {
		if (video.getVideoFile() != null) {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("CalibrationWindow.fxml"));
				AnchorPane root = (AnchorPane) loader.load();
				
				CalibrationWindowController nextController = loader.getController();
				nextController.setVideo(video);
				
				Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
				nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				Stage primary = (Stage) okButton.getScene().getWindow();
				primary.setScene(nextScene);
				primary.setTitle("Calibration Window");
		}
	}
}
