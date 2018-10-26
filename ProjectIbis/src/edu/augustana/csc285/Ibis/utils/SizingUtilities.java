package edu.augustana.csc285.Ibis.utils;

import edu.augustana.csc285.Ibis.datamodel.Video;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;

public class SizingUtilities {


	/**
	 * Takes in an image and sets videoView and canvasView equal to its width and
	 * height. A METHOD LIKELY TO HELP WITH CALIBRATION.
	 * 
	 * @param image
	 */
	public static void setCanvasSizeToMatchVideo(Video video, ImageView videoView, Canvas canvasView) {
		double aspectRatio = (double) video.getFrameWidth() / video.getFrameHeight();
		double realWidth = Math.min(videoView.getFitWidth(), videoView.getFitHeight() * aspectRatio);
		double realHeight = Math.min(videoView.getFitHeight(), videoView.getFitWidth() / aspectRatio);

		videoView.setFitHeight(realHeight);
		videoView.setFitWidth(realWidth);

		canvasView.setHeight(videoView.getFitHeight());
		canvasView.setWidth(videoView.getFitWidth());
	}
}
