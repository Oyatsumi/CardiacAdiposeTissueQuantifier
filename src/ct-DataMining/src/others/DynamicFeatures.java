package others;

import features.CooccurrenceMatrix;
import features.Intensity;
import features.Moments;
import features.RunLengthMatrix;
import globals.FeaturesImage;

public class DynamicFeatures {
	private String features = null;
	private String[] splittedFeatures = null;
 	
	private CooccurrenceMatrix com = null;
	private Moments m = null;
	private RunLengthMatrix rlm = null;
	
	/*public ExtractFeatures(int sliceid, short pixelvalue, int posx, int posy, byte type, FeaturesImage img, short[][] pixelValues){
		this.features = extractFeatures(sliceid, pixelvalue, posx, posy, type, img, pixelValues);
		this.getSplittedFeatures();
	}*/
	public DynamicFeatures(){
	}
	
	public String[] extractSplittedFeatures(int sliceid, short pixelvalue, int posx, int posy, byte type, FeaturesImage img, short[][] pixelValues){
		return extractFeatures(sliceid, pixelvalue, posx, posy, type, img, pixelValues).split(",");
	}
	/*
	public String old_extractFeatures(int sliceid, short pixelvalue, int posx, int posy, byte type, FeaturesImage img, short[][] pixelValues){
		
		//class: red dot,green dot
		if (type == 0) {this.features = "true,false,false,";}
		else if (type == 1) {this.features = "false,true,false,";}
		else if (type == 2) {this.features = "true,false,true,";}
		else {this.features = "false,false,false,";}
		
		//ct slice
		this.features += sliceid + ",";
		
		//NEW - pixelvalue
		this.features += pixelvalue + ",";
		
		//raw position
		this.features += posx + "," + posy + ",";
		
		//position of the point relative to the center of gravity
		this.features += Math.abs(posx - img.getCenterOfGravity().x) + "," + Math.abs(posy - img.getCenterOfGravity().y) + ",";
		
		//linear gaussian intensity
		this.features += Intensity.getLGI(pixelValues) + ",";
		
		//mean values
		this.features += Intensity.getMean(pixelValues) + ",";
		
		
		//co-occurrence matrix
		com = new CooccurrenceMatrix(pixelValues, 0, 1);
		this.features += com.getCOMMoment(1) + "," + com.getCOMMoment(2) + "," + com.getCOMMoment(3) + "," + com.getCOMMoment(4)
				+ "," + com.getCOMMomentPerHue(1) + "," + com.getCOMMomentPerHue(2) + "," + com.getCOMMomentPerHue(3) + "," + com.getCOMMomentPerHue(4) + ",";
		com.dispose();
		com = new CooccurrenceMatrix(pixelValues, 1, 0);
		this.features += com.getCOMMoment(1) + "," + com.getCOMMoment(2) + "," + com.getCOMMoment(3) + "," + com.getCOMMoment(4)
				+ "," + com.getCOMMomentPerHue(1) + "," + com.getCOMMomentPerHue(2) + "," + com.getCOMMomentPerHue(3) + "," + com.getCOMMomentPerHue(4) + ",";
		com.dispose();
		com = new CooccurrenceMatrix(pixelValues, 1, 1);
		this.features += com.getCOMMoment(1) + "," + com.getCOMMoment(2) + "," + com.getCOMMoment(3) + "," + com.getCOMMoment(4)
				+ "," + com.getCOMMomentPerHue(1) + "," + com.getCOMMomentPerHue(2) + "," + com.getCOMMomentPerHue(3) + "," + com.getCOMMomentPerHue(4) + ",";
		com.dispose();
		com = new CooccurrenceMatrix(pixelValues, 0, 4);
		this.features += com.getCOMMoment(1) + "," + com.getCOMMoment(2) + "," + com.getCOMMoment(3) + "," + com.getCOMMoment(4)
				+ "," + com.getCOMMomentPerHue(1) + "," + com.getCOMMomentPerHue(2) + "," + com.getCOMMomentPerHue(3) + "," + com.getCOMMomentPerHue(4) + ",";
		com.dispose();
		com = null;
		
		//runlengthmatrix
		rlm = new RunLengthMatrix(pixelValues);
		this.features += rlm.glnu(0) + "," + rlm.glnu(45) + "," + rlm.glnu(90) + "," + rlm.glnu(135) + ",";
		this.features += rlm.runPercentage(0) + "," + rlm.runPercentage(45) + "," + rlm.runPercentage(90) + "," + rlm.runPercentage(135) + ",";
		rlm.dispose();
		rlm = null;

		//moments
		m = new Moments(pixelValues);
		this.features += m.getGeometricMoment(0, 1) + "," + m.getGeometricMoment(1, 0) + "," + m.getGeometricMoment(1, 1) + "," + m.getGeometricMoment(2, 2) + ",";
		this.features += m.getCentralMoment(0, 1) + "," + m.getCentralMoment(1, 0) + "," + m.getCentralMoment(1, 1) + "," + m.getCentralMoment(2, 2);
		m.dispose();
		m = null;
		
		return this.features;
	}
	*/
	private double lgi, mean;
	public String extractFeatures(int sliceid, short pixelvalue, int posx, int posy, byte type, FeaturesImage img, short[][] pixelValues){
		
		//class: red dot,green dot
		if (type == 0) {this.features = "true,false,false,";}
		else if (type == 1) {this.features = "false,true,false,";}
		else if (type == 2) {this.features = "true,false,true,";}
		else {this.features = "false,false,false,";}
		
		//ct slice
		this.features += sliceid + ",";
		
		//NEW - pixelvalue
		this.features += pixelvalue + ",";
		
		//raw position
		this.features += posx + "," + posy + ",";
		
		//position of the point relative to the center of gravity
		this.features += Math.abs(posx - img.getCenterOfGravity().x) + "," + Math.abs(posy - img.getCenterOfGravity().y) + ",";
		
		//mean values
		mean = Intensity.getMean(pixelValues);
		this.features += mean + ",";
		
		
		//check if all black
		if (mean != 0){
			//linear gaussian intensity (CSV)
			lgi = Intensity.getLGI(pixelValues);
			this.features += lgi + ",";
			
			//co-occurrence matrix
			com = new CooccurrenceMatrix(pixelValues, 0, 1);
			this.features += com.getCOMMoment(1) + "," + com.getCOMMoment(2) + "," + com.getCOMMoment(3) + "," + com.getCOMMoment(4)
					+ "," + com.getCOMMomentPerHue(1) + ",";
			com.dispose();
			/*
			com = new CooccurrenceMatrix(pixelValues, 1, 0);
			this.features += com.getCOMMoment(1) + "," + com.getCOMMoment(2) + "," + com.getCOMMoment(3) + "," + com.getCOMMoment(4)
					+ "," + com.getCOMMomentPerHue(1) + "," + com.getCOMMomentPerHue(2) + "," + com.getCOMMomentPerHue(3) + "," + com.getCOMMomentPerHue(4) + ",";
			com.dispose();
			*/
			com = new CooccurrenceMatrix(pixelValues, 1, 1);
			this.features += com.getCOMMoment(2) + "," + com.getCOMMoment(3) 
					+ "," + com.getCOMMomentPerHue(4);
			com.dispose();
			com = null;
			
			/*
			//runlengthmatrix
			rlm = new RunLengthMatrix(pixelValues);
			this.features += rlm.glnu(0) + "," + rlm.glnu(45) + "," + rlm.glnu(90) + "," + rlm.glnu(135) + ",";
			this.features += rlm.runPercentage(0) + "," + rlm.runPercentage(45) + "," + rlm.runPercentage(90) + "," + rlm.runPercentage(135) + ",";
			rlm.dispose();
			rlm = null;
	
			//moments
			m = new Moments(pixelValues);
			this.features += m.getGeometricMoment(0, 1) + "," + m.getGeometricMoment(1, 0) + "," + m.getGeometricMoment(1, 1) + "," + m.getGeometricMoment(2, 2) + ",";
			this.features += m.getCentralMoment(0, 1) + "," + m.getCentralMoment(1, 0) + "," + m.getCentralMoment(1, 1) + "," + m.getCentralMoment(2, 2);
			m.dispose();
			m = null;
			*/
		}else{
			this.features += "0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0";
		}
		
		return this.features;
	}
	
	public void dispose(){
		if (this.com != null) this.com.dispose();
		com = null;
		if (this.rlm != null) this.rlm.dispose();
		rlm = null;
		if (this.m != null) this.m.dispose();
		m = null;
		this.features = null;
		System.gc();
	}
	
	
	public String getFeatures(){
		return this.features;
	}
	public String[] getSplittedFeatures(){
		this.splittedFeatures = this.features.split(",");
		return this.splittedFeatures;
	}
	
	
	public double[] getGreenFeatures(){
		double[] out = this.getNumericFeatures();
		double[] newOut = new double[out.length - 1];
		newOut[0] = out[1];
		for (int i=3; i<out.length; i++){
			newOut[i-2] = out[i];
		}
		return newOut;
	}
	public double[] getRedFeatures(){
		double[] out = this.getNumericFeatures();
		double[] newOut = new double[out.length - 1];
		newOut[0] = out[0];
		for (int i=3; i<out.length; i++){
			newOut[i-2] = out[i];
		}
		return newOut;
	}
	public double[] getBlueFeatures(){
		double[] out = this.getNumericFeatures();
		double[] newOut = new double[out.length - 1];
		newOut[0] = out[2];
		for (int i=3; i<out.length; i++){
			newOut[i-2] = out[i];
		}
		return newOut;
	}
	public double[] getLayerFeatures(int layerIndex){
		if (layerIndex == 0) return getRedFeatures();
		else if (layerIndex == 1) return getGreenFeatures();
		else return getBlueFeatures();
	}
	private double[] getNumericFeatures(){
		double[] out = new double[this.getSplittedFeatures().length];
		if (this.getSplittedFeatures()[0].equals("true")) out[0] = 0d; else out[0] = 1d;
		if (this.getSplittedFeatures()[1].equals("true")) out[1] = 0d; else out[1] = 1d;
		if (this.getSplittedFeatures()[2].equals("true")) out[2] = 0d; else out[2] = 1d;
		
		for (int i=3; i<this.getSplittedFeatures().length; i++){
			out[i] = Double.parseDouble(this.getSplittedFeatures()[i]);
		}
		
		return out;
	}
}
