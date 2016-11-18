package features;

import globals.ImageData;
import globals.Vector2;

public class Moments {
	private  ImageData img;
	private Vector2 centerOfGravity = null;
	
	public Moments (short[][] img){
		this.img = new ImageData(img);
	}
	public Moments (ImageData img){
		this.img = img;
	}
	
	/**
	 * @param x - Weight of the x coordinate (power of)
	 * @param y - Weight of the y coordinate (power of)
	 * @return - SpatialMoment of the image weighted by (x, y)
	 */
	public double getGeometricMoment(int m, int n){
		double moment = 0;
		for (int i=0; i<img.getHeight(); i++){
			for (int j=0; j<img.getWidth(); j++){
				moment += Math.pow(j, m) * Math.pow(i, n) * img.getPixel(j, i);
			}
		}
		return moment;
	}
	
	public Vector2 getCenterOfGravity(){
		if (centerOfGravity == null){
			Vector2 cog;
			if (getGeometricMoment(0, 0) == 0) //Infinity - NaN
				cog = new Vector2(0, 0);
			else
				cog = new Vector2((float)getGeometricMoment(1, 0)/getGeometricMoment(0, 0), 
						(float)getGeometricMoment(0, 1)/getGeometricMoment(0, 0));
			
			this.centerOfGravity = cog;
		}
		return this.centerOfGravity;
	}
	
	public double getCentralMoment(int x, int y){//moment that is in relation to the center of gravity
		double moment = 0;
		for (int i=0; i<img.getHeight(); i++){
			for (int j=0; j<img.getWidth(); j++){
				moment += Math.pow(j - this.getCenterOfGravity().x, x) * 
						Math.pow(i - this.getCenterOfGravity().y, y) *
						img.getPixel(j, i);
			}
		}
		return moment;
	}
	
	
	public void dispose(){
		this.img = null;
		this.centerOfGravity = null;
	}
	
}
