package globals;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;



public class ImageData {
	private ArrayList<Short> hues = null; //ordenado crescente
	protected short[][] pixelValue;
	

	public ImageData(short [][] img){this.pixelValue = img;}
	public ImageData(int[][] img){
		pixelValue = new short[img.length][img[0].length];
		for (int i=0; i<img.length; i++){
			for (int j=0; j<img[0].length; j++){
				pixelValue[i][j] = (short) img[i][j];
			}
		}
	}
	/**
	 * If the image is RGB-layered it will be converted to one single layer as the mean of every layer.
	 * @param img - The image to be loaded
	 */
	public ImageData(BufferedImage img){
		this.pixelValue = new short[img.getHeight()][img.getWidth()];
		Raster r = img.getRaster();
		for (int i=0; i<img.getHeight(); i++){
			for (int j=0; j<img.getWidth(); j++){
				int sum = 0;
				for (int l=0; l<r.getNumBands(); l++){
					sum += r.getSample(j, i, l);
				}
				this.pixelValue[i][j] = (short) (sum/r.getNumBands());
			}
		}
	}

	
	private void buildHues(){
		hues = new ArrayList<Short>();
		for (int i=0; i<this.getHeight(); i++){
			for (int j=0; j<this.getWidth(); j++){
				if (!hues.contains(this.pixelValue[i][j])){
					hues.add(this.pixelValue[i][j]);
				}
			}
		}
		
		//ordenar o vetor de hues/ -pode ser retirado se preferir
		ArrayList<Short> aux = new ArrayList<Short>();
		short menor, menori = 0;
		while (hues.size() != 0){
			menor = hues.get(0);
			menori = 0;
			for (short i=0; i<hues.size(); i++){
				if (menor > hues.get(i)) {
					menor = hues.get(i);
					menori = i;
				}
			}
			hues.remove(menori);
			aux.add(menor);
		}
		this.hues.clear();
		this.hues = aux;
	}
	
	
	public short getPixel(int x, int y){if (x >= 0 && y >= 0 && x<pixelValue[0].length && y<pixelValue.length) return this.pixelValue[y][x]; else return 0;}
	public short[][] getImg(){return this.pixelValue;}
	public ArrayList<Short> getHues(){if (this.hues == null) this.buildHues(); return this.hues;}
	public int getHeight(){return this.pixelValue.length;}
	public int getWidth(){return this.pixelValue[0].length;}
	public float getHypotenuse(){return (float) Math.pow(Math.pow(this.pixelValue[0].length, 2) + Math.pow(this.pixelValue.length, 2), 1/2d);}
	public boolean containsHue(short hue){return this.hues.contains(hue);}
	
	
	
	private Vector2 minMax = null; 
	public Vector2 getMinMaxIntensity(){
		if (minMax == null){
			int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
			int value = 0;
			for (int i=0; i<this.getHeight(); i++){
				for (int j=0; j<this.getWidth(); j++){
					value = this.getPixel(j, i);
					if (value < min)
						min = value;
					if (value > max)
						max = value;
				}
			}
			minMax = new Vector2(min, max);
		}
		return minMax;
	}
	public int getMinimumIntesity(){
		return (int) this.getMinMaxIntensity().x;
	}
	public int getMaximumIntensity(){
		return (int) this.getMinMaxIntensity().y;
	}
	public int getMeanIntensity(){
		return (int) ((this.getMinMaxIntensity().y + this.getMinMaxIntensity().x)/2);
	}
	public int getBitDepth(){return 8;}//acertar dps
	
	public void dispose(){if (this.hues != null) this.hues.clear(); this.hues = null;}
	
}
