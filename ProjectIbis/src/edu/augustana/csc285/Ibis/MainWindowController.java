package edu.augustana.csc285.Ibis;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;

import edu.augustana.csc285.Ibis.datamodel.Video;
import edu.augustana.csc285.Ibis.utils.UtilsForOpenCV;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class MainWindowController {
	@FXML
	private ImageView videoView;
	@FXML
	private Canvas canvasView;
	@FXML
	private Slider videoSlider;
	@FXML
	private Label messageScreen;
	@FXML
	private Label timeDisplayed;
	@FXML
	private Button exportToCSV;

	@FXML
	public void handleExport() {

	}

	private ScheduledExecutorService timer;

	private Video video;

	public void setVideo(Video video) {
		this.video = video;
		videoSlider.setMax(video.getTotalNumFrames());
		
		//video.setCurrentFrameNum();
		grabFrame();

	}

	@FXML
	public void initialize() {
		canvasView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			//modify the location to reflect the actual location and not with the comparison to the whole GUI
			public void handle(MouseEvent event) {
				GraphicsContext drawingPen = canvasView.getGraphicsContext2D();
				drawingPen.setFill(Color.GREENYELLOW);
				drawingPen.fillOval(event.getX(),event.getY() , 10, 10);
					System.out.println(event.getX() + " y= " + event.getY());
			}
		});
		videoSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number initalVal, Number finalVal) {
				if (videoSlider.isValueChanging()) {
					
					updateTimeLabel();
					
					try {
						if (timer != null) {
							timer.shutdown();
							timer.awaitTermination(1000, TimeUnit.MILLISECONDS);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// System.out.println(finalVal);
					video.setCurrentFrameNum((double) finalVal);
					grabFrame();

					// resumes player
					// startPlaying();
				}
			}
		});

	}

	protected void startPlaying() {
		Runnable frameGrabber = new Runnable() {
			@Override
			public void run() {
				grabFrame();
			}
		};
		// timer for 33 milliseconds, can call in larger increments of frames to play
		// faster
		this.timer = Executors.newSingleThreadScheduledExecutor();
		this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
	}

	// displays the next frame in the input stream of the video
	public void grabFrame() {
		// grabs video's info and puts it into a usable Mat object.

		Mat frame = video.read();
		Image image = UtilsForOpenCV.matToJavaFXImage(frame);
		
		findRealImageSize(image);
		videoView.setImage(image);
			
		
	}
	
	public void findRealImageSize(Image image) {
		
		//thanks johnniegf on StackExchange. You the real G
		double aspectRatio = image.getWidth() / image.getHeight();
		double realWidth = Math.min(videoView.getFitWidth(), videoView.getFitHeight() * aspectRatio);
		double realHeight = Math.min(videoView.getFitHeight(), videoView.getFitWidth() / aspectRatio);
		
		videoView.setFitHeight(realHeight);
		videoView.setFitWidth(realWidth);
		
		canvasView.setHeight(videoView.getFitHeight());
		canvasView.setWidth(videoView.getFitWidth());
	}
	
	//timeLabel updates as slider moves	
	public void updateTimeLabel() {			
		int timeInSecs = (int)Math.round(video.convertFrameNumsToSeconds((int) videoSlider.getValue()));
		String timeString = String.format("%d:%02d", timeInSecs / 60, timeInSecs % 60);
		timeDisplayed.setText(timeString);
		System.out.println(timeDisplayed.getText());
	}
}
