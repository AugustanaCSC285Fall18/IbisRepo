package edu.augustana.csc285.Ibis;

import java.io.File;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;



public class LaunchScreenController {
	@FXML private Button browseButton;
	@FXML private Button okButton;
	@FXML private TextField textField; 
	
	@FXML
	public void handleBrowse() {
		FileChooser fileChooser = new FileChooser();
		
		fileChooser.setTitle("Open Image File");
	}
	
	@FXML
	public void handleOK() {
		
	}
}
