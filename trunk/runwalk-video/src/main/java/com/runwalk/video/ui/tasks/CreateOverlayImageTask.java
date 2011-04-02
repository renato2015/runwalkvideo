package com.runwalk.video.gui.tasks;

import ij.ImagePlus;
import ij.process.AutoThresholder;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

import com.runwalk.video.blobs.Blob;
import com.runwalk.video.blobs.BlobDetection;
import com.runwalk.video.blobs.EdgeVertex;
import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.AppUtil;

public class CreateOverlayImageTask extends AbstractTask<BufferedImage, Void> {

	private final Image inputImage;

	public CreateOverlayImageTask(Image inputImage) {
		super("createOverlayImage");
		this.inputImage = inputImage;
	}

	/*
	 * TODO The doInBackground method could be further abstracted by creating interface 
	 * methods that hide all these image processing operations so this task isn't coupled  
	 * to any specific library implementation anymore
	 * 
	 * (non-Javadoc)
	 */
	protected BufferedImage doInBackground() throws Exception {
		message("startMessage");
		ImagePlus imgPlus = new ImagePlus("snapshot", getInputImage());
		ImageProcessor greyscaleProcessor = imgPlus.getProcessor().convertToByte(true);
		//		greyscaleProcessor.medianFilter();
		AutoThresholder autoThresholder = new AutoThresholder();
		int threshold = autoThresholder.getThreshold(AutoThresholder.Method.Intermodes, greyscaleProcessor.getHistogram());
		greyscaleProcessor.threshold(threshold);
		//
		//		Binary binaryProcessor = new Binary();
		//		//			binaryProcessor.setup("erode", imgPlus);
		//		//			binaryProcessor.run(greyscaleProcessor);
		//		binaryProcessor.setup("fill", imgPlus);
		//		binaryProcessor.run(greyscaleProcessor);

		int height = greyscaleProcessor.getHeight();
		int width = greyscaleProcessor.getWidth();
		final BufferedImage overlay = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR); 
		BlobDetection blobDetection = new BlobDetection(width, height);
		blobDetection.setPosDiscrimination(true);
		blobDetection.setThreshold(0.5f);
		overlay.createGraphics().drawImage(greyscaleProcessor.getBufferedImage(), 0, 0, Color.black, null);
		int[] pixels = overlay.getRGB(0, 0, width, height, null, 0, width);
		blobDetection.activeCustomFilter(this);
		blobDetection.computeBlobs(pixels);
		final BufferedImage newOverlay = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR); 
		newOverlay.createGraphics().setColor(Color.white);
		biggestBlob = null;
		for (int i = 0; i < blobDetection.getBlobNb(); i++) {
			Blob blob = blobDetection.getBlob(i);
			if (biggestBlob == null || blob.h > biggestBlob.h) {
				biggestBlob = blob;
			}
//			int x = (int) (blob.xMin * width);
//			int y = (int) (blob.yMin * height);
//			int blobWidth = (int) (blob.w * width);
//			int blobHeigth = (int) (blob.h * height);
//			// TODO enable this to display all blobs
//			newOverlay.getGraphics().drawRect(x, y, blobWidth, blobHeigth);
		}

		// draw lines through legs and calculate angles using the biggest found blob
		int xDiff = 0;
		Point firstPoint = new Point();
		firstPoint.y = 0;
		Point secondPoint = new Point();
		int y1 = 0, y2 = 0, x1 = 0, x2 = 0;
		Graphics graphics = newOverlay.getGraphics();
		for (int j = 0; j < biggestBlob.getEdgeNb(); j++) {
			EdgeVertex edgeVertexA = biggestBlob.getEdgeVertexA(j);
			EdgeVertex edgeVertexB = biggestBlob.getEdgeVertexB(j);
			x1 = (int) (edgeVertexA.x * width);
			y1 = (int) (edgeVertexA.y * height);
			x2 = (int) (width * edgeVertexB.x);
			y2 = (int) (height * edgeVertexB.y);
			xDiff = xDiff + (x2 - x1);
			if (j % 10 == 0) {

				if (xDiff >= 6 && secondPoint.y == 0) {
					// save y coordinate here
					secondPoint.x = x1;
					secondPoint.y = y1;
				}
			}
			if (secondPoint.y != 0 && y2 == secondPoint.y) {
				secondPoint.x = secondPoint.x + ( x1 - secondPoint.x ) / 2;
				// draw a horizontal line..
				graphics.setColor(Color.red);
				graphics.drawLine(secondPoint.x - 50, secondPoint.y, x1 + 50, secondPoint.y);
			}
			if (y1 == 0) {
				if (firstPoint.x == 0) {
					firstPoint.x = x1;
				} else {
					firstPoint.x = firstPoint.x + ( x2 - firstPoint.x ) / 2;
				}
			}
			// TODO enable to draw edges around blob!!
			graphics.setColor(Color.white);
			graphics.drawLine(x1, y1, x2, y2);
		}
		// calculate the angle between the line connecting the first and second point and the horizon of the image
		double angle = Math.atan((double) (secondPoint.y - firstPoint.y) / Math.abs(firstPoint.x - secondPoint.x));
		angle = angle * 180 / Math.PI;
		angle = secondPoint.x > firstPoint.x ? 180 - angle : angle;
		// set a nice flashy color for drawing the lines forming the angle of interest
		graphics.setColor(Color.red);
		graphics.drawLine(firstPoint.x, firstPoint.y, secondPoint.x, secondPoint.y);
		graphics.drawArc(secondPoint.x - 35, secondPoint.y - 35, 70, 70, 0,(int) angle);
		// set a nice, big font on the graphics object
		graphics.setFont(AppSettings.MAIN_FONT.deriveFont(25f).deriveFont(Font.BOLD));
		graphics.drawString(AppUtil.round((float) angle, 2) + "°", secondPoint.x + 70, secondPoint.y - 5);
		message("endMessage", getExecutionDuration(TimeUnit.MILLISECONDS));
		return newOverlay;
	}

	public Image getInputImage() {
		return inputImage;
	}

	private Blob biggestBlob;

	/**
	 * Callback method for the {@link Blob} analyzer. 
	 * Returning true will keep the blob in the result set, returning false will discard it.
	 * 
	 * @param blob The encountered blob
	 * @return <code>true</code> if one wants to keep the blob
	 */
	public boolean newBlobDetectedEvent (Blob blob) {
		// return true to keep blob, false to discard
		if (biggestBlob == null || blob.h > biggestBlob.h) {
			biggestBlob = blob;
			return true;
		}
		return false;
	}


}
