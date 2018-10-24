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
	private Button addButton;
	@FXML
	private Button removeButton;
	@FXML
	private Button finishButton;
	@FXML
	private TextField numberOfChicksLabel;
	private ArrayList<String> names = new ArrayList<String>();

	private int numberOfChicks = 0;

	private ProjectData project;

	private List<Point> pointsToCalibrate = new ArrayList<Point>();

	private int place = 0;

	private double verticleDist;
	private double horizontalDist;
	
	private Rectangle rect = new Rectangle();

	@FXML
	public void initialize() {
		videoSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number initalVal, Number finalVal) {
				updateTimeLabel();
				showFrameAt(finalVal.intValue());
			}
		});
		for (int i = 0; i < 4; i++) {
			pointsToCalibrate.add(new Point());
		}
		System.out.println("Enter two verticle points then two horizontal points at corners of the box.");
		canvasView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			// modify the location to reflect the actual location and not with the
			// comparison to the whole GUI
			public void handle(MouseEvent event) {
				if (place < 4) {
					drawPoint(event, place);
					place++;
				}
			}
		});

	}

	public void addPoint(MouseEvent event, int place) {
		pointsToCalibrate.get(place).setLocation(event.getX(), event.getY());
	}

	public void drawPoint(MouseEvent event, int place) {
		GraphicsContext drawingPen = canvasView.getGraphicsContext2D();
		drawingPen.setFill(Color.FUCHSIA);
		drawingPen.fillOval(event.getX(), event.getY(), 5, 5);

		addPoint(event, place);
	}

	@FXML
	public void handleFinishButton() throws IOException {
		calculateDist();
		setWidthHeight();
		System.out.println("vert" + verticleDist);
		System.out.println("horizon" + horizontalDist);
		System.out.println("rect " + rect.toString());
		if (numberOfChicks > 0) {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
			AnchorPane root = (AnchorPane) loader.load();

			MainWindowController nextController = loader.getController();
			nextController.setProject(project);
//			nextController.animalTrackModifier(numberOfChicks, names); //this is breaking the code, someone fix it

			Scene nextScene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
			nextScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			Stage primary = (Stage) finishButton.getScene().getWindow();
			primary.setScene(nextScene);
			primary.setTitle("Chick Tracker 1.0");
		} else {
			LaunchScreenController.informationalDialog("Please add at least one chick to beging the traking");
		}
	}

	private void setWidthHeight() {
		rect.setX(pointsToCalibrate.get(0).getX());
		rect.setY(pointsToCalibrate.get(0).getY());
		rect.setHeight(verticleDist);
		rect.setWidth(horizontalDist);
	}

	private void calculateDist() {
		verticleDist = pointsToCalibrate.get(0).distance(pointsToCalibrate.get(1));
		horizontalDist = pointsToCalibrate.get(2).distance(pointsToCalibrate.get(3));
	}

	public void setVideo(String filePath) throws FileNotFoundException {
		project = new ProjectData(filePath);
		project.getVideo().setXPixelsPerCm(6); // i think should happen elsewhere
		project.getVideo().setYPixelsPerCm(6);
		videoSlider.setMax(project.getVideo().getTotalNumFrames() - 1); // need the minus one to not go off the video
																		// and resolve the errors.
		showFrameAt(0);
	}

	public void showFrameAt(int frameNum) {
		project.getVideo().setCurrentFrameNum(frameNum);
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
		videoView.setImage(curFrame);

		GraphicsContext drawingPen = canvasView.getGraphicsContext2D(); // not needed?
		drawingPen.clearRect(0, 0, canvasView.getWidth(), canvasView.getHeight());
		// want to draw the correct dots that had been previously stored for this frame
	}

	// timeLabel updates as slider moves
	public void updateTimeLabel() {
		int timeInSecs = (int) Math.round(project.getVideo().convertFrameNumsToSeconds((int) videoSlider.getValue()));
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
		if (result.isPresent()) {
			numberOfChicks++;
			numberOfChicksLabel.setText("" + numberOfChicks);
		}
		names.add(result.get());
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

	public double getHorizontalDist() {
		return horizontalDist;
	}

	public double getVerticleDist() {
		return verticleDist;
	}
	
	public Rectangle getRect() {
		return rect;
	}
}
