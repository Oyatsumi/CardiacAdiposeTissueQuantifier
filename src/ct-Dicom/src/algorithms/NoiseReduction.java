package algorithms;

import java.awt.image.WritableRaster;

public abstract class NoiseReduction {
	//Alguns parâmetros bons: 4 - 15 ~ 
	private final static byte windowSizeX = 4, windowSizeY = 6, minPixels = 20;
	
	
	//deprecated
	public static WritableRaster reduceNoise(WritableRaster r){
		int sum = 0;
		//clone raster values
		short[][] img = new short[r.getHeight()][r.getWidth()];
		for (int i=0; i<r.getHeight(); i++){
			for (int j=0; j<r.getWidth(); j++){
				img[i][j] = (short) r.getSample(j, i, 0);
			}
		}
		//reduce noise
		for (int i=windowSizeY; i<r.getHeight() - windowSizeY; i++){
			for (int j=windowSizeX; j<r.getWidth() - windowSizeX; j++){
				sum = 0;
				//window loop
				for (int l=i-windowSizeY; l<=i+windowSizeY; l++){
					for (int c=j-windowSizeX; c<=j+windowSizeX; c++){
						sum += (img[l][c] > 0) ? 1 : 0;
					}
				}
				if (sum < minPixels)
					r.setSample(j, i, 0, 0);


			}
		}
		
		return r;
	}
	
	
	public static short[][] reduceNoise(short[][] fatimg, short[][] fluidimg){
		for (int i=0; i<fatimg.length; i++){
			for (int j=0; j<fatimg[0].length; j++){
				if (isNoisePixel(j, i, 0, fluidimg) && !isFatArea(j, i, 0, fatimg)){
					fatimg[i][j] = 0;
				}
			}
		}
		return fatimg;
	}
	public static boolean isNoisePixel(int x, int y, int iteration, short[][] fluidimg){
		//melhor param = 2, 3
		int sum = 0;
		final byte itTimes = 2, numPixels = 3;
		if (iteration < itTimes){
			for (int y2=y-1; y2<=y+1; y2++){
				for (int x2=x-1; x2<=x+1; x2++){
					if (x2 >= 0 && x2 < fluidimg[0].length && y2 >= 0 && y2 < fluidimg.length && (Math.abs(x2) != Math.abs(y2))){
						if (fluidimg[y2][x2] > 0) sum ++;
					}
				}
			}
		}else
			return false;
		
		if (sum >= numPixels + iteration) return true; 
		else {
			boolean n = false;
			for (int i=-1; i<=1; i++){
				for (int j=-1; j<=1 && (Math.abs(i) != Math.abs(j)); j++){
					n |= isNoisePixel(x + j, y + i, iteration+1, fluidimg);
				}
			}
			return n;
		}
	}
	public static boolean isFatPixel(int x, int y, int iteration, short[][] fatimg){
		//melho resultado par: 3, 4
		final byte itTimes = 3, numPixels = 4;
		int sum = 0;
		if (iteration < itTimes){
			for (int y2=y-1; y2<=y+1; y2++){
				for (int x2=x-1; x2<=x+1; x2++){
					if (x2 >= 0 && x2 < fatimg[0].length && y2 >= 0 && y2 < fatimg.length && (Math.abs(x2) != Math.abs(y2))){
						if (fatimg[y2][x2] > 0) sum ++;
					}
				}
			}
		}else
			return false;
		
		if (sum >= numPixels - iteration) return true; 
		else {
			boolean n = false;
			for (int i=-1; i<=1; i++){
				for (int j=-1; j<=1 && (Math.abs(i) != Math.abs(j)); j++){
					n |= isFatPixel(x + j, y + i, iteration+1, fatimg);
				}
			}
			return n;
		}
	}
	public static boolean isFatArea(int x, int y, int iteration, short[][] fatimg){
		final byte windowSizeX = 3, windowSizeY = 2, 
				numPixels = (byte) Math.abs(0.1f*((windowSizeX*2+1)*(windowSizeY*2+1)));;
		int sum = 0;
		for (int i=y-windowSizeY; i<=y+windowSizeY; i++){
			for (int j=x-windowSizeX; j<x+windowSizeX; j++){
				if (j >= 0 && j < fatimg[0].length && i >= 0 && i < fatimg.length){
					if (fatimg[i][j] > 0) sum ++;
				}
			}
		}
		if (sum > numPixels) return true;
		return false;
	}
	
	
	
	
	
	
	
	
	
}
