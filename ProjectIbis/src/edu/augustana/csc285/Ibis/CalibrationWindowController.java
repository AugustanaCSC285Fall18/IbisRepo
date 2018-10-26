package edu.augustana.csc285.Ibis;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.augustana.csc285.Ibis.datamodel.AnimalTrack;
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
	/**
	 * initializes a listener that calls showFrameAt(int frameNum) to update imageView.
	 */
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
	/**
	 * takes in point from mouse click and draws a visual point centered on the x and y coordinates.
	 * DUPLICATE CODE IN MAIN WINDOW CONTROLLER.
	 * @param pt
	 */
	public void drawPoint(Point pt) {
		GraphicsContext drawingPen = canvasView.getGraphicsContext2D();
		drawingPen.setFill(Color.FUCHSIA);
		drawingPen.fillOval(pt.getX()-2, pt.getY()-2, 5, 5);

	}
	/**
	 * Handles button that takes user to mainWindowController. If no chicks have been registered with handleAddButton()
	 * or the window has not been calibrated prompts user to go back and complete these steps.
	 * @throws IOException
	 */
	@FXML
	public void handleFinishButton() throws IOException {
		if (numberOfChicks > 0 && fishiedAllCalibration) {
			project.getVideo().getArenaBounds().setBounds((int)pointsToCalibrate.get(0).getX(), (int)pointsToCalibrate.get(0).getY(), (int) horizontalDist, (int) verticleDist);
			FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
			AnchorPane root = (AnchorPane) loader.load();

			MainWindowController nextController = loader.getController();

			for (int i = 0; i < names.size();i++) {
				project.getTracks().add(new AnimalTrack(names.get(i)));
			}
			
			nextController.setProject(project);
		
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
	/**
	 * Handles button that initiates calibration process. Displays message prompting user to select points for List pointsToCalibrate.
	 * creates each point with mouse click and when four points are selected calls calculateDist() method.
	 */
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
	/**
	 * Calculates the distance between the two vertical and horizontal points from pointToCalibrate
	 * measures point 0 and 1 as vertical distance and point 2 and 3 as horizontal distance.
	 */
	private void calculateDist() {
		verticleDist = pointsToCalibrate.get(0).distance(pointsToCalibrate.get(1));
		horizontalDist = pointsToCalibrate.get(2).distance(pointsToCalibrate.get(3));
	//	project.getVideo().setXPixelsPerCm(horizontalDist/userHorizantal); 
	//	project.getVideo().setYPixelsPerCm(verticleDist/userVertical);
	}
	/**
	 * Takes in filepath of a video and sets the current project to the 0th frame.
	 * Also sets videoSlider bar to proper amount of frame numbers.
	 * @param filePath
	 * @throws FileNotFoundException
	 */
	public void setVideo(String filePath) throws FileNotFoundException {
		project = new ProjectData(filePath);
		videoSlider.setMax(project.getVideo().getTotalNumFrames() - 1); 
		showFrameAt(0);
	}
	/**
	 * sets the project to the proper frame number and displays the proper image for that frame
	 * in the videoView.
	 * @param frameNum
	 */
	public void showFrameAt(int frameNum) {
		project.getVideo().setCurrentFrameNum(frameNum);
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
		videoView.setImage(curFrame);
		//GraphicsContext drawingPen = canvasView.getGraphicsContext2D(); // not needed?
	//	drawingPen.clearRect(0, 0, canvasView.getWidth(), canvasView.getHeight());
		// want to draw the correct dots that had been previously stored for this frame
	}

	/**
	 * @return String of formatted time in seconds
	 */
	public String getTimeString() {
		int timeInSecs = (int) Math.round(project.getVideo().convertFrameNumsToSeconds((int) videoSlider.getValue()));
		return String.format("%02d:%02d", timeInSecs / 60, timeInSecs % 60);
	}

	/**
	 * Handles FXML button, prompts user to enter name for new chick and updates
	 * label to display current size of List numberOfChicks.
	 */
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

	/**
	 * Handles FXML button, removes last chick in the List numberOfChicks
	 */
	@FXML
	public void handleRemoveButton() {
		if (numberOfChicks > 0) {
			numberOfChicks--;
			numberOfChicksLabel.setText("" + numberOfChicks);
			names.remove(names.size() - 1);
			System.out.println(names.size());
		}
	}
	/**
	 * Handles button that sets startFrameNum to the current frame displayed in imageView
	 */
	@FXML
	public void handleSetStartTimeButton() {
		startTimeTextField.setText(getTimeString());
		project.getVideo().setStartFrameNum((int)videoSlider.getValue());
	}
	/**
	 * Handles button that sets endFrameNum to the current frame displayed in imageView
	 */
	@FXML
	public void handleSetEndTimeButton() {
		endTimeTextField.setText(getTimeString());
		project.getVideo().setEndFrameNum((int)videoSlider.getValue());
	}
	/**
	 * Handles button that sets emptyFrameNum to the current frame displayed in imageView.
	 * Used for autoTracking methods as blank screen to compare.
	 */
	@FXML
	public void handleSetEmptyFrame() {
		emptyFrameTextField.setText(getTimeString());
		project.getVideo().setEmptyFrameNum((int)videoSlider.getValue());
	}
	
}
