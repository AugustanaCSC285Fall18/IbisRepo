package edu.augustana.csc285.Ibis;

import org.opencv.core.Mat;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;

public class MainWindowController {
	@FXML private ImageView videoView;
	@FXML private Slider videoSlider;
	@FXML private Label messageScreen; 
	@FXML private Label timeDisplayed; 
	@FXML private Button exportToCSV;	
	
	@FXML 
	public void handleExport() {
	
	}
	
}
