package registration;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import globals.ImageData;
import globals.Vector2;

public class Marker extends ImageData{
	private int intensityLayer = 0;
	private BufferedImage atlasImg, buffReferenceImage;
	private ImageData referenceImage;
	private Point2D pos = new Point2D.Float(0, 0);
	private int referenceImageThreshold = 0;

	public Marker(BufferedImage refImage, BufferedImage atlasImg) {
		super(atlasImg);
		this.atlasImg = atlasImg;
		this.buffReferenceImage = refImage;
		this.referenceImage = new ImageData(refImage);
	}
	public Marker(Marker m){
		super(m.atlasImg);
		this.atlasImg = m.atlasImg;
		this.buffReferenceImage = m.buffReferenceImage;
		this.referenceImage = m.referenceImage;
		this.pos = m.pos;
	}
	
	public void blendatlas(BufferedImage backgroundImage){
		//ImageOp.
	}
	public void setReferenceImgThrehsold(int threshold){
		this.referenceImageThreshold = threshold;
	}
	public void setPos(int x, int y){
		this.pos = new Point2D.Float(x, y);
	}
	public void setPos(Vector2 pos){
		this.pos = new Point2D.Float((float) pos.x, (float) pos.y);
	}
	public void setReferenceImage(BufferedImage i){this.buffReferenceImage = i; this.referenceImage = new ImageData(i);}
	public void setIntensityLayer(int l){this.intensityLayer = l;}
	
	public BufferedImage getatlasImg(){return this.atlasImg;}
	public BufferedImage getReferenceImage(){return this.buffReferenceImage;}
	public int getX(){return (int) this.pos.getX();}
	public int getY(){return (int) this.pos.getY();}
	public Point2D getPos(){return this.pos;}
	public int getReferenceImgThreshold(){return this.referenceImageThreshold;}
	public int getReferencePixel(int x, int y){return this.referenceImage.getPixel(x, y);}
	public ImageData getReferenceImageData(){return this.referenceImage;}
	public int getReferenceBitDepth(){return this.referenceImage.getBitDepth();}
	
	
	public int getIntensityLayer(){
		return this.intensityLayer;
	}
	private Vector2 minMax = null;
	private int bandNumber = -1;
	public Vector2 getatlasMinMaxIntensity(int bandNumber){
		if (minMax == null || this.bandNumber != bandNumber){
			int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
			int value = 0;
			Raster r = this.atlasImg.getRaster();
			for (int i=0; i<this.atlasImg.getHeight(); i++){
				for (int j=0; j<this.atlasImg.getWidth(); j++){
					value = r.getSample(j, i, bandNumber);
					if (value < min)
						min = value;
					if (value > max)
						max = value;
				}
			}
			this.bandNumber = bandNumber;
			minMax = new Vector2(min, max);
		}
		return minMax;
	}
	public int getatlasMinimumIntesity(int bandNumber){
		return (int) this.getatlasMinMaxIntensity(bandNumber).x;
	}
	public int getatlasMaximumIntensity(int bandNumber){
		return (int) this.getatlasMinMaxIntensity(bandNumber).y;
	}
	public int getatlasMeanIntensity(int bandNumber){
		return (int) ((this.getatlasMinMaxIntensity(bandNumber).y + this.getatlasMinMaxIntensity(bandNumber).x)/2);
	}
	
	public void dispose(){
		this.referenceImage = null;
		this.pos = null;
		this.atlasImg = null;
		if (this.referenceImage != null) this.referenceImage.dispose();
		this.buffReferenceImage = null;
	}

}
