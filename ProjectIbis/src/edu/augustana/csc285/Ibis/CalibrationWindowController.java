package edu.augustana.csc285.Ibis;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.augustana.csc285.Ibis.datamodel.ProjectData;

import edu.augustana.csc285.Ibis.utils.UtilsForOpenCV;
import javafx.application.Platform;
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
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
	private TextField startTimeTextField;
	@FXML
	private TextField endTimeTextField;
	@FXML
	private TextField emptyFrameTextField;
	@FXML
	private Button addButton;
	@FXML
	private Button removeButton;
	@FXML
	private Button startTimeButton;
	@FXML
	private Button endTimeButton;
	@FXML
	private Button finishButton;
	@FXML
	private Button setEmptyFrameButton;
	@FXML
	private TextField numberOfChicksLabel;
	
	private ArrayList<String> names = new ArrayList<String>();

	private int numberOfChicks = 0;

	private ProjectData project;

	private List<Point> pointsToCalibrate = new ArrayList<Point>();

	private double verticleDist;
	private double horizontalDist;
	private boolean fishiedAllCalibration=true;
	
	@FXML
	public void initialize() {
		videoSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number initalVal, Number finalVal) {
				timeDisplayed.setText(getTimeString());
				showFrameAt(finalVal.intValue());
			}
		});
	}

	public void drawPoint(Point pt) {
		GraphicsContext drawingPen = canvasView.getGraphicsContext2D();
		drawingPen.setFill(Color.FUCHSIA);
		drawingPen.fillOval(pt.getX()-2, pt.getY()-2, 5, 5);

	}

	@FXML
	public void handleFinishButton() throws IOException {
		if (numberOfChicks > 0 && fishiedAllCalibration) {
			//project.getVideo().getArenaBounds().setBounds((int)pointsToCalibrate.get(0).getX(), (int)pointsToCalibrate.get(0).getY(), horizontalDist, verticleDist);
			FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
			AnchorPane root = (AnchorPane) loader.load();

			MainWindowController nextController = loader.getController();
			nextController.setProject(project);

			nextController.animalTrackModifier(numberOfChicks, names); //this is breaking the code, someone fix it
		
			Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
			nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			Stage primary = (Stage) finishButton.getScene().getWindow();
			primary.setScene(nextScene);
			primary.setTitle("Chick Tracker 1.0");
		} else if (numberOfChicks==0) {
			LaunchScreenController.informationalDialog("Please add at least one chick to begin the traking");
		}else if(!fishiedAllCalibration) {
			LaunchScreenController.informationalDialog("Please finish the calibration before proceeding");
		}
	}
	
	@FXML
	public void handleCalibrateRatio() {
		LaunchScreenController.informationalDialog("Place two vertical points First, then two horizontal points");
		canvasView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
					if (pointsToCalibrate.size() < 4) {
						Point newPoint =new Point((int)event.getX(), (int)event.getY()); 
						pointsToCalibrate.add(newPoint);
						drawPoint(newPoint);
					}if(pointsToCalibrate.size()==4) {
						calculateDist();
					}
			}
		});
		
	}
	
	private void calculateDist() {
		verticleDist = pointsToCalibrate.get(0).distance(pointsToCalibrate.get(1));
		horizontalDist = pointsToCalibrate.get(2).distance(pointsToCalibrate.get(3));
	//	project.getVideo().setXPixelsPerCm(horizontalDist/userHorizantal); 
	//	project.getVideo().setYPixelsPerCm(verticleDist/userVertical);
	}

	public void setVideo(String filePath) throws FileNotFoundException {
		project = new ProjectData(filePath);
		videoSlider.setMax(project.getVideo().getTotalNumFrames() - 1); 
		showFrameAt(0);
	}

	public void showFrameAt(int frameNum) {
		project.getVideo().setCurrentFrameNum(frameNum);
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
		videoView.setImage(curFrame);
		//GraphicsContext drawingPen = canvasView.getGraphicsContext2D(); // not needed?
	//	drawingPen.clearRect(0, 0, canvasView.getWidth(), canvasView.getHeight());
		// want to draw the correct dots that had been previously stored for this frame
	}

	// returns the time in seconds as a formatted string
	public String getTimeString() {
		int timeInSecs = (int) Math.round(project.getVideo().convertFrameNumsToSeconds((int) videoSlider.getValue()));
		String timeString = String.format("%02d:%02d", timeInSecs / 60, timeInSecs % 60);
		return timeString;
	}

	@FXML
	public void handleAddbutton() {
		String suggestedInput = "Chick #" + (names.size() + 1);
		TextInputDialog dialog = new TextInputDialog(suggestedInput);
		dialog.setTitle("Id assigner");
		dialog.setHeaderText(null);
		dialog.setContentText("Please enter the name of this chick: ");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			numberOfChicks++;
			numberOfChicksLabel.setText("" + numberOfChicks);
			names.add(result.get()); // add animaltrack instead of the string 
		}
		System.out.println(names.size());
	}

	@FXML
	public void handleRemoveButton() {
		if (numberOfChicks > 0) {
			numberOfChicks--;
			numberOfChicksLabel.setText("" + numberOfChicks);
			names.remove(names.size() - 1);
			System.out.println(names.size());
		}
	}
	
	@FXML
	public void handleSetStartTimeButton() {
		startTimeTextField.setText(getTimeString());
		project.getVideo().setStartFrameNum((int)videoSlider.getValue());
	}
	
	@FXML
	public void handleSetEndTimeButton() {
		endTimeTextField.setText(getTimeString());
		project.getVideo().setEndFrameNum((int)videoSlider.getValue());
	}

	@FXML
	public void handleSetEmptyFrame() {
		emptyFrameTextField.setText(getTimeString());
		project.getVideo().setEmptyFrameNum((int)videoSlider.getValue());
	}
	
}
