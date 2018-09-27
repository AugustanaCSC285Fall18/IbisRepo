package edu.augustana.csc285.Ibis;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.Videoio;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MainWindowController {
	@FXML
	private ImageView videoView;
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

	// Jacob Bell 9/29/18

	// handles the slider behavior and the browse button
	// we can rip this into the video class and the gui
	// to deal with the aforementioned features.
	// private VideoCapture capture = new VideoCapture();
	
	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;

	@FXML
	public void initialize() {
	}
	Video capture = new Video();
	
	// called to play to video continuously by repeatedly calling the grab frame
	// method every 33 milliseconds
	protected void player() {
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
			Mat frame = new Mat();
			capture.read(frame);

			// takes a new MatOfByte object and with frame reads in the Mat image into a BMP
			// file
			MatOfByte buffer = new MatOfByte();

			// LOOK INTO MOST EFFICIENT FILE TYPE
			Imgcodecs.imencode(".bmp", frame, buffer);
			Image imageToShow = new Image(new ByteArrayInputStream(buffer.toArray()));

			// sets the video to display the desired frame
			videoView.setImage(imageToShow);
		

		/*public void handleBrowse() {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open Image File");
			Window mainWindow = videoView.getScene().getWindow();
			File chosenFile = fileChooser.showOpenDialog(mainWindow);
			if (chosenFile != null) {
				try {
					// takes chosenFile uses it to make a new FileInputStream which is used to make
					// a new image, this image is then set as the new image of videoView
					videoView.setImage(new Image(new FileInputStream(chosenFile)));

					capture.open(chosenFile.getAbsolutePath());

					// sets slide bar to the appropriate length to match the videos length.
					double frames = capture.get(Videoio.CV_CAP_PROP_FRAME_COUNT);
					videoSlider.setMax(frames - 1);

					// calls the recurring player
					player();

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}*/
			
			// when slider is dragged (not clicked, problem?) the timer from player() will
			// pause, display is updated to desired frame and player resumes
			// ISSUE while clicked but not dragged video plays
			// ISSUE slider node does not move with video
			videoSlider.valueProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> observable, Number initalVal, Number finalVal) {
					if (videoSlider.isValueChanging()) {
						try {
							timer.shutdown();
							Thread.sleep(33);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						capture.setCurrentFrameNum((double) finalVal);
						// resumes player
						player();
					}
				}
			});
		}
}
