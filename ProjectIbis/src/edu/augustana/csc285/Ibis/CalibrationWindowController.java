package edu.augustana.csc285.Ibis;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.augustana.csc285.Ibis.datamodel.AnimalTrack;
import edu.augustana.csc285.Ibis.datamodel.ProjectData;
import edu.augustana.csc285.Ibis.utils.SizingUtilities;
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
import java.awt.Rectangle;
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
	private List<Point> arenaPoints = new ArrayList<Point>();

	private int numberOfChicks = 0;

	private ProjectData project;

	private List<Point> pointsToCalibrate = new ArrayList<Point>();


	private boolean finishedAllCalibration=true;
	private boolean specifiedTheRectangle=false;
	
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

		if (numberOfChicks > 0 && finishedAllCalibration) { // specifideTheRectengel
	

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
		}else if(!finishedAllCalibration) {
			LaunchScreenController.informationalDialog("Please finish the calibration before proceeding");
		}else if(!specifiedTheRectangle) {
			LaunchScreenController.informationalDialog("Please specifie the areana bounds");
		}
	}
	
	/**
	 * Handles button that initiates calibration process. Displays message prompting user to select points for List pointsToCalibrate.
	 * creates each point with mouse click and when four points are selected calls calculateDist() method.
	 */
	
	@FXML
	public void handleCalibrateRatio() {
		LaunchScreenController.informationalDialog("Place two vertical points first, then two horizontal points");
		clearTheCanvasView();
		pointsToCalibrate.clear();
		canvasView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
					if (pointsToCalibrate.size() < 4) {
						Point newPoint =new Point((int)event.getX(), (int)event.getY()); 
						pointsToCalibrate.add(newPoint);
						drawPoint(newPoint);

						if(pointsToCalibrate.size() == 2) {
							double distanceInCm = getDoubleFromUser("Vertical Distance");
							if(distanceInCm !=0) {
								setYPerCm(distanceInCm);
								}
							
						} else if (pointsToCalibrate.size() == 4) {						
							double distanceInCm = getDoubleFromUser("Horizantal Distance");
							if(distanceInCm !=0) {
							setXPerCm(distanceInCm);
								}
						}
					}
			}
		});
		
	}
	
	/**
	 * Set the Y pixels per cm for the video
	 * @param distanceInCm - the input taken from the user
	 */
	public void setYPerCm(double distanceInCm) {
		Point ptStart = pointsToCalibrate.get(0);
		Point ptEnd = pointsToCalibrate.get(1);
		double verticalDistInCanvas = ptStart.distance(ptEnd);
		double verticalDistInVideo = verticalDistInCanvas * getVideoToCanvasRatio();

		project.getVideo().setYPixelsPerCm(verticalDistInVideo / distanceInCm);
	}

	/**
	 * Set the X pixels per cm for the video
	 * @param distanceInCm - the input taken from the user
	 */
	public void setXPerCm(double distanceInCm) {
		Point ptStart = pointsToCalibrate.get(2);
		Point ptEnd = pointsToCalibrate.get(3);
		double horizantalDistInCanvas = ptStart.distance(ptEnd);
		double horizantalDistInVideo = horizantalDistInCanvas * getVideoToCanvasRatio();
		
		project.getVideo().setXPixelsPerCm(horizantalDistInVideo / distanceInCm);
		finishedAllCalibration=true;
	}
	
	@FXML
	public void handleArenaBounds() {
		LaunchScreenController.informationalDialog("Place two points in the each conner of the arena bound that should be used");
		clearTheCanvasView();
		arenaPoints.clear();
		canvasView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				if(arenaPoints.size()<2) {
				Point newPoint =new Point((int)event.getX(), (int)event.getY()); 
				arenaPoints.add(newPoint);
				drawPoint(newPoint);
				
				}
				if(arenaPoints.size()==2) {
					int arenaWidth =(int) Math.abs(arenaPoints.get(1).getX() - arenaPoints.get(0).getX());
					int arenaHeight = (int)Math.abs(arenaPoints.get(1).getY() - arenaPoints.get(0).getY());
					Rectangle arenaBounds = new Rectangle((int)(arenaPoints.get(0).getX()), (int)(arenaPoints.get(0).getY()),(arenaWidth), (arenaHeight));
					project.getVideo().setArenaBounds(arenaBounds);
					
				}
				
			}
			
		});
	
		
	}
	
	public void clearTheCanvasView() {
		GraphicsContext drawingPen = canvasView.getGraphicsContext2D(); 
		drawingPen.clearRect(0, 0, canvasView.getWidth(), canvasView.getHeight());
	}
	public double getVideoToCanvasRatio() {
		double aspectRatio = project.getVideo().getFrameWidth() / project.getVideo().getFrameHeight();
		double displayWidth = Math.min(videoView.getFitWidth(), videoView.getFitHeight() * aspectRatio);
		return project.getVideo().getFrameWidth() / displayWidth;
	}

	public double getDoubleFromUser(String msg) {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle(msg);
		dialog.setHeaderText(null);
		dialog.setContentText("Please enter the " + msg +" in (cm): ");
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			String userNumText = result.get();
			try {
				double userNum = Double.parseDouble(userNumText);
				return userNum;
			} catch (NumberFormatException ex) {
				LaunchScreenController.informationalDialog("Please type a number");
				return getDoubleFromUser(msg);
			}			
		} else {
			LaunchScreenController.informationalDialog("Please draw the points agian");
			pointsToCalibrate.clear();
			GraphicsContext drawingPen = canvasView.getGraphicsContext2D(); 
			drawingPen.clearRect(0, 0, canvasView.getWidth(), canvasView.getHeight());
		return 0;
		}		
		
	}

	/**
	 * Takes in filepath of a video and sets the current project to the 0th frame.
	 * Also sets videoSlider bar to proper amount of frame numbers.
	 * @param filePath
	 * @throws FileNotFoundException
	 */
	public void setNewProject(String filePath) throws FileNotFoundException {
		project = new ProjectData(filePath);
		SizingUtilities.setCanvasSizeToMatchVideo(project.getVideo(), this.videoView, this.canvasView);
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
			names.add(result.get());
		}
		System.out.println("Size of the strings adding for the chick names = "+names.size() +" in calibration line 307");
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
			System.out.println("Size of the strings remove for the chick names = "+names.size() +" in calibration line 320");
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
