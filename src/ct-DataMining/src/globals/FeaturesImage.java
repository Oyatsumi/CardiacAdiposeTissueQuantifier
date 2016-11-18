package globals;

import java.awt.image.BufferedImage;

import features.CooccurrenceMatrix;
import features.Intensity;
import features.Moments;
import features.RunLengthMatrix;

public class FeaturesImage extends ImageData{
	

	/**
	 * If the image is RGB-layered it will be converted to one single layer as the mean of every layer.
	 * @param img - The image to be loaded
	 */
	public FeaturesImage(BufferedImage img){
		super(img);
	}
	public FeaturesImage(short [][] img){super(img);}
	public FeaturesImage(int[][] img){
		super(img);
	}

	

	
	Moments moments = null;
	private void initMoments(){if (moments == null) moments = new Moments(pixelValue);}
	public Moments getMoments(){initMoments(); return moments;}
	public Vector2 getCenterOfGravity(){initMoments(); return moments.getCenterOfGravity();}
	public double getIntensityMean(){return Intensity.getMean(this.pixelValue);}
	public int getRoundedIntensityMean(){return Intensity.getRoundedMean(this.pixelValue);}
	public int getReducedIntensityMean(){return Intensity.getReducedMean(this.pixelValue);}
	
	
	
	private static RunLengthMatrix rlm = null;
	private static CooccurrenceMatrix com = null;
	public double getLGI(){return Intensity.getLGI(this.pixelValue);}
	public double getMean(){return Intensity.getMean(this.pixelValue);}
	private int dx = Integer.MIN_VALUE, dy = Integer.MIN_VALUE;
	public double getCOMMoment(int g, int dx, int dy){
		if (com == null || this.dx != dx || this.dy != dy) {
			com = new CooccurrenceMatrix(this.pixelValue, dx, dy);
			this.dx = dx; this.dy = dy;
		}
		return com.getCOMMoment(g);
	}
	public void disposeCOM(){this.com.dispose();}
	public double getGLNU(int delta){
		if (rlm == null){
			this.rlm = new RunLengthMatrix(this.pixelValue);
		}
		return rlm.glnu(delta);
	}
	public double getRunPercentage(int delta){
		if (rlm == null){
			this.rlm = new RunLengthMatrix(this.pixelValue);
		}
		return this.rlm.runPercentage(delta);
	}
	

	
	
}
