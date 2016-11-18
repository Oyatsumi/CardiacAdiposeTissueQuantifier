package globals;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import registration.Transformation;

public abstract class ImageOp {
	/*
	public static final int BICUBIC = AffineTransformOp.TYPE_BICUBIC,
			BILINEAR = AffineTransformOp.TYPE_BILINEAR,
			NEAREST_NEIGHBOR = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
			*/
	public static final Object BICUBIC = RenderingHints.VALUE_INTERPOLATION_BICUBIC,
			BILINEAR = RenderingHints.VALUE_INTERPOLATION_BILINEAR,
			NEAREST_NEIGHBOR = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

	public static BufferedImage cloneImage(BufferedImage img){
		BufferedImage outputImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		Graphics2D g2d = outputImg.createGraphics();
		g2d.drawImage(img, null, 0, 0);
		g2d.dispose();
		return outputImg;
	}
	
	public static  BufferedImage translate(int x, int y, BufferedImage img) {
	    BufferedImage outputImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
	    Graphics2D g2d = outputImg.createGraphics();
	    g2d.translate(x, y);
	    g2d.drawImage(img, null, 0, 0);
	    g2d.dispose();
	    return outputImg;
	}
	public static BufferedImage scale(double x, double y, BufferedImage img){
		BufferedImage outputImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		Graphics2D g2d = outputImg.createGraphics();
		g2d.scale(x, y);
		g2d.drawImage(img, null, 0, 0);
		g2d.dispose();
		return outputImg;
	}
	public static BufferedImage transform(BufferedImage img, Transformation t){
		return transform(img, t, t.interpolation);
	}
	public static BufferedImage transform(BufferedImage img, AffineTransform t, Object interpolation){
		BufferedImage outputImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		Graphics2D g2d = outputImg.createGraphics();
		//AffineTransformOp atop = new AffineTransformOp(t, interpolation); JRE7
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.transform(t);
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();
		return outputImg;
	}
	
	public static BufferedImage drawLine(BufferedImage img, Vector2 p1, Vector2 p2){
		BufferedImage outputImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		Graphics2D g2d = outputImg.createGraphics();
		g2d.drawImage(img, null, 0, 0);
		g2d.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
		g2d.dispose();
		return outputImg;
	}
	
	public static BufferedImage blendImages(BufferedImage backgroundImg, BufferedImage topImg, Point2D topImgPosition){
		BufferedImage combined = new BufferedImage(backgroundImg.getWidth(), backgroundImg.getHeight(), topImg.getType());
		// paint both images, preserving the alpha channels
		Graphics2D g2d = (Graphics2D) combined.getGraphics();
		g2d.drawImage(backgroundImg, 0, 0, null);
		g2d.drawImage(topImg, (int)topImgPosition.getX(), (int)topImgPosition.getY(), null);
		g2d.dispose();
		
		return combined;
	}
}
