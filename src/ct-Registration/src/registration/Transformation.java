package registration;


import globals.ImageOp;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Transformation extends AffineTransform{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double score = Double.MAX_VALUE;
	public Object interpolation = ImageOp.NEAREST_NEIGHBOR;
	public double xBias = 1, yBias = 1, sBias = 1;
	



	public Transformation doMean(AffineTransform af){
		AffineTransform aux = new AffineTransform();
		aux.translate((this.getTranslateX() + af.getTranslateX())/2f, 
				(this.getTranslateY() + af.getTranslateY())/2f);
		aux.scale((this.getScaleX() + af.getScaleX())/2f, 
				(this.getScaleY() + af.getScaleY())/2f);
		this.setTransform(aux);
		return this;
	}
	
	public void setBias(double x, double y, double s){
		this.xBias = x;
		this.yBias = y;
		this.sBias = s;
	}
	public void meanBias(double x, double y, double s){
		this.xBias = (this.xBias + x)/2d;
		this.yBias = (this.yBias + y)/2d;
		this.sBias = (this.sBias + s)/2d;
	}

	

	public void setInterpolation(Object i){this.interpolation = i;}

	
	
	public Transformation(int interpolation){
		super();
		this.interpolation = interpolation;
	}
	public Transformation(Transformation t){
		super(t);
		this.interpolation = t.interpolation;
		this.score = t.score;
	}
	public Transformation(){
		super();
	}
	

	
	
	/*
	private static final long serialVersionUID = 1L;
	private short tx, ty, sy, sx;
	private double dissimilarityScore = -1;
	
	Transformation(double similarityScore){
		this.dissimilarityScore = similarityScore;
	}
	Transformation(double similarityScore, double tx, double ty, double sx, double sy){
		this.dissimilarityScore = similarityScore;
		this.ty = (short) ty;
		this.tx = (short) tx;
		this.sx = (short) sx;
		this.sy = (short) sy;
	}
	Transformation(double tx, double ty, double sx, double sy){
		this.ty = (short) ty;
		this.tx = (short) tx;
		this.sx = (short) sx;
		this.sy = (short) sy;
	}
	Transformation(double similarityScore, AffineTransform at){
		this.tx = (short) at.getTranslateX();
		this.ty = (short) at.getTranslateY();
		this.sx = (short) at.getScaleX();
		this.sy = (short) at.getScaleY();
	}
	
	protected void setTranslationX(int tx){
		this.tx = (short) tx;
	}
	protected void setTranslationY(int ty){
		this.ty = (short) ty;
	}
	protected void setScaleX(int sx){
		this.sx = (short) sx;
	}
	protected void setScaleY(int sy){
		this.sy = (short) sy;
	}
	protected void setDissimilarityScore(double similarityScore){
		this.dissimilarityScore = similarityScore;
	}
	
	
	protected void incrementDissimilarityScore(int i){this.dissimilarityScore += i;}
	protected void incrementTranslationX(int i){this.tx += i;}
	protected void incrementTranslationY(int i){this.ty += i;}
	protected void incrementScaleX(int i){this.sx *= i;}
	protected void incrementScaleY(int i){this.sy *= i;}
	protected void incrementParameters(double dissimilarityScore, int tx, int ty, int sx, int sy){
		this.dissimilarityScore = dissimilarityScore;
		this.tx += tx;
		this.ty += ty;
		this.sx *= sx;
		this.sy *= sy;
	}
	
	protected double getDissimilarityScore(){return this.dissimilarityScore;}
	protected short getTranslationX(){return this.tx;}
	protected short getTranslationY(){return this.ty;}
	protected short getScaleX(){return this.sx;}
	protected short getScaleY(){return this.sy;}
	
	*/
}
