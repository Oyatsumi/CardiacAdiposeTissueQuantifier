package registration;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import globals.ImageOp;

import javax.imageio.ImageIO;

import similarity.Similarity;

import exceptions.NullTransformation;


import globals.BooleanMatrix;
import globals.Vector2;
import globals.Vector3;

public class Registration{
	private String outputImagePath = null, atlasImagePath = "C:\\Users\\Érick\\Documents\\Aura\\CT\\Registro\\combinedatlas3.png";
	private BufferedImage img1 = null, atlas = null;

	
	/**
	 * Creates a new Registration
	 * @param fixedImg - reference image
	 * @param movingImg - moving image or atlas/atlas
	 * @param outputImagePath - path of the img file incluing extension, e.g.: "C:/eg/2.png" - "C:/eg/2.png"
	 * @param interpolation - the interpolation method defined on the Registration class
	 */
	public Registration(BufferedImage fixedImg, BufferedImage movingImg, String outputImagePath){
		this.outputImagePath = outputImagePath;
		this.img1 = fixedImg; this.atlas = movingImg;
		try {
			//set mark
			//setMarkAndExport(img2, "C:\\Users\\Érick\\Documents\\Aura\\CT\\Registro\\atlas.bmp", "C:\\Users\\Érick\\Documents\\Aura\\CT\\Registro\\combined.png");
			
			/*
			Transformation t = new Transformation();
			System.out.println(compare(img1, img2, t));
			BufferedImage img3 = ImageIO.read(new File("C:\\Users\\Érick\\Documents\\Aura\\CT\\Registro\\teste registro random\\afazer2.BMP"));
			System.out.println(compare(img1, img3, t));
			BufferedImage img4 = ImageIO.read(new File("C:\\Users\\Érick\\Documents\\Aura\\CT\\Registro\\teste registro random\\feito1.BMP"));
			System.out.println("lol" +compare(img1, img4, t));
			
			System.out.println(compare(img1, img3, t));
			*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void dispose(){
		if (this.img1.getGraphics() != null)
			this.img1.getGraphics().dispose();
		if (this.atlas.getGraphics() != null)
			this.atlas.getGraphics().dispose();
		this.outputImagePath = null;
	}
	public void setatlas(String atlasImgPath){
		this.atlasImagePath = atlasImgPath;
	}
	public void findatlas() throws IOException{
		if (atlas == null){
			atlas = ImageIO.read(new File(this.atlasImagePath));
		}
		findAtlas(img1, atlas);
	}
	public void findatlasAndExport(String outputImgPath) throws IOException{
		if (atlas == null){
			atlas = ImageIO.read(new File(this.atlasImagePath));
		}
		findatlasAndExport(img1, atlas, outputImgPath);
	}



	
	
	
	
	
	private static void findatlasAndExport(BufferedImage img, String atlasImgPath, String outputImgPath) throws IOException{
		String[] splittedName = atlasImgPath.split("\\.");
		BufferedImage atlasIMG = ImageIO.read(new File(splittedName[splittedName.length - 1] + ".png"));
		findatlasAndExport(img, atlasIMG, outputImgPath);
	}
	private static void findatlasAndExport(BufferedImage img, BufferedImage atlas, String outputImgPath) throws IOException{
		BufferedImage atlasPNG = ImageIO.read(new File("C:\\Users\\Érick\\Documents\\Aura\\CT\\Registro\\combinedatlas3.png")),
		atlasIMG = atlas;
		
		// create the new image, canvas size is the max. of both image sizes
		int w = Math.max(img.getWidth(), atlasIMG.getWidth());
		int h = Math.max(img.getHeight(), atlasIMG.getHeight());
		BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

		//get the right translation
		Marker translation = findAtlas(img, atlasIMG);
		
		// paint both images, preserving the alpha channels
		Graphics2D g2d = (Graphics2D) combined.getGraphics();
		g2d.drawImage(translation.getReferenceImage(), 0, 0, null);
		g2d.drawImage(atlasPNG, (int)translation.getX(), (int)translation.getY(), null);
		g2d.dispose();
		
		// Save as new image
		String[] splittedName = outputImgPath.split("\\.");
		ImageIO.write(combined, splittedName[splittedName.length - 1], new File(outputImgPath));
	}
	

	private static byte redBand = 0, greenBand = 1, blueBand = 2;
	/*
	/**
	 * @param atlas - atlas with the reference image and the atlas image set
	 * @param threshold - Threshold vector representing the respective thresholds of the layers (red, green, blue) in this order.
	 * @return
	 *-/
	private static long getBlueScore(atlas atlas, Raster atlasRaster, Vector2 atlasInternalPos, Vector3 threshold){
		final byte precision = 3;
		final int maxIntensity = (int) Math.pow(2, atlas.getReferenceBitDepth());
		
		int blueSample = (int) (atlasRaster.getSample((int) atlasInternalPos.x, (int) atlasInternalPos.y, blueBand)*
				(maxIntensity/atlas.getatlasMaximumIntensity(blueBand)));

		long blueScore = 1;
		
		int actualX = (int) (atlas.getX() + atlasInternalPos.x), actualY = (int) (atlas.getY() + atlasInternalPos.y);
		
		int delta = atlas.getReferenceImageData().getPixel((int)atlasInternalPos.x, (int)atlasInternalPos.y);
		if (actualX < atlas.getReferenceImageData().getWidth() && actualY < atlas.getReferenceImageData().getHeight()){
			if (blueSample > threshold.z){
				delta -= (1 + maxIntensity)/(1 + blueSample);
				for (int i=0; i<=(precision); i++) blueScore *= delta;
				blueScore = Math.abs(blueScore);
			}else{
				delta -= blueSample;
				for (int i=0; i<=(precision); i++) blueScore *= delta;
				blueScore = -Math.abs(blueScore);
			}
		}else{
			delta = maxIntensity;
			for (int i=0; i<=(precision); i++) blueScore *= delta;
			blueScore -= blueScore;
		}
		
		
		return blueScore;
		
	}
	private static int getRedAndGreenScore(atlas atlas, Raster atlasRaster, Vector2 atlasInternalPos, Vector3 threshold){
		int score = 0;
		
		int actualX = (int) (atlas.getX() + atlasInternalPos.x), actualY = (int) (atlas.getY() + atlasInternalPos.y);
		
		if (atlas.getReferenceImageData().getPixel(actualX, actualY) < atlas.getReferenceImgThreshold()){// if there is no intensity
			if (atlasRaster.getSample((int)atlasInternalPos.x, (int)atlasInternalPos.y, redBand) > threshold.x) //red
				score ++;
		}else{
			if (atlasRaster.getSample((int)atlasInternalPos.x, (int)atlasInternalPos.y, greenBand) > threshold.y) //green
				score ++;
		}
		
		return score;
	}
	*/


	public static Marker findatlas(Marker atlas, Vector2 leftTopCrop, Vector2 rightBottomCrop, boolean doConfirmation){
		int finalX = (int) (atlas.getReferenceImage().getWidth()*(rightBottomCrop.x)),
				finalY = (int) (atlas.getReferenceImage().getHeight()*(rightBottomCrop.y)),
				initialX = (int) (atlas.getReferenceImage().getWidth()*(leftTopCrop.x)),
				initialY = (int) (atlas.getReferenceImage().getHeight()*(leftTopCrop.y));

		
		
		
		
		
	    long bestRGScore = Integer.MIN_VALUE, previousBestRGScore = 0;
		double bestBlueScore = Double.NEGATIVE_INFINITY, previousBestBlueScore = 0;
		Vector2 bestPos = new Vector2(0, 0);
		
		Vector2 dXRange = new Vector2(100, 280); //diminuir para até 250
		
		boolean confirm = false;
		
		Vector2 atlasInitialPos = new Vector2(atlas.getX(), atlas.getY());
		Vector2 previousPos = new Vector2(0, 0);
		
		Similarity.threshold = new Vector3(100, 100, 0);
		atlas.setIntensityLayer(blueBand);
		Similarity.setatlas(atlas);
		int precision = 3;
		Raster atlasRaster = atlas.getatlasImg().getRaster();
		
		
		
		double blueScore = 0;
		
		long blueCounter = 1; //alterar pra long se precision > 3
		
		int RGScore = 0;
		final int convergence = 280;
		
		BooleanMatrix bm = new BooleanMatrix(atlas.getReferenceImage().getWidth(), atlas.getReferenceImage().getHeight());
		double worstBlueScore = Long.MAX_VALUE, worstRGScore = Long.MAX_VALUE;
		
		
		short counter = 0;
		do{
			counter ++;
			//translate the atlas on top of the reference image and find the optimal position
			for (int i=(int) (atlasInitialPos.y + initialY); i<finalY -1*atlasInitialPos.y; i+=3){
				for (int j=(int) (atlasInitialPos.x + initialX); j<finalX -1*atlasInitialPos.x; j+=3){
					blueScore = 0;
					RGScore = 0;
					blueCounter = 1;
					
					boolean skip = false;
					if (counter > 1 && !bm.isChecked(j, i)) skip = true;
					
					if (!skip){
						double test = 0, test2 = 0;
						//images corresponding to the atlas size and position
						short[][] fixedImg, movingImg;
						fixedImg = new short[atlas.getHeight()][atlas.getWidth()];
						movingImg = new short[atlas.getHeight()][atlas.getWidth()];
						//best position through scores
						for (short y=0; y<atlas.getHeight(); y++){
							for (short x=0; x<atlas.getWidth(); x++){
								
								//construct images table
								if (x+j < atlas.getReferenceImage().getWidth() && 
										x+j >= 0 && i+y >= 0 && i+y < atlas.getReferenceImage().getHeight()){
									fixedImg[y][x] = atlas.getReferenceImageData().getPixel(x + j, y + i);
								}else{
									fixedImg[y][x] = -1;
								}
								movingImg[y][x] = (short) atlasRaster.getSample(x, y, blueBand);
								
								
								
								//red and green score
								if (atlas.getReferenceImageData().getPixel(x + j, i + y) < atlas.getReferenceImgThreshold()){// if there is no intensity
									if (atlasRaster.getSample(x, y, redBand) > Similarity.threshold.x){ //red
										if (x+j < atlas.getReferenceImage().getWidth() && x+j >= 0
												&& i+y >= 0 && i+y < atlas.getReferenceImage().getHeight())
											RGScore += 2;
										else
											RGScore += 1;
									}//mudança
								}else{
									if (atlasRaster.getSample(x, y, greenBand) > Similarity.threshold.y) //green
										RGScore += 1;
								}
							}
						}
						
						//blue score
						blueScore = Similarity.hybridSumOfDifferences(fixedImg, movingImg, 1/2f);
						//blueScore = -Similarity.sumOfDifferences(fixedImg, movingImg, 3);
						//blueScore = Similarity.normalizedCrossCorrelation(fixedImg, movingImg);
						//blueScore = Similarity.mutualInformation(fixedImg, movingImg, "10");
						//blueScore = Similarity.weightedMutualInformation(fixedImg, movingImg, "2", true);
						
						if (!confirm){//if not on the confirmation loop
							//initializing
							if (worstBlueScore == Long.MAX_VALUE) {
								worstBlueScore = blueScore;
								worstRGScore = RGScore;
								bestBlueScore = blueScore;
								bestRGScore = RGScore;
							}
							//avail which points will be picked to be computed further on the next iteration
							if ((bestBlueScore + worstBlueScore)*0.65 < blueScore &&
									(bestRGScore + worstRGScore)*0.65 <= RGScore){
								bm.check(j, i);
							}
							//update worst matching
							if (blueScore < worstBlueScore && RGScore <= worstRGScore){
								worstBlueScore = blueScore;
								worstRGScore = RGScore;
							}
						}

						
						//update the position of the best matching
						if (blueScore > bestBlueScore && RGScore >= bestRGScore){
							
							//previous values saving
							previousBestRGScore = bestRGScore;
							previousBestBlueScore = bestBlueScore;
							previousPos.x = bestPos.x;
							previousPos.y = bestPos.y;
							//updating
							bestRGScore = RGScore;
							bestBlueScore = blueScore;
							bestPos.x = j;
							bestPos.y = i;
							
							//confirmation method
							if (confirm && bm.isChecked(j, i)){
								atlas.setPos(bestPos);
								if (!confirm(atlas, dXRange, convergence)){
									bestPos.x = previousPos.x;
									bestPos.y = previousPos.y;
									bestRGScore = previousBestRGScore;
									bestBlueScore = previousBestBlueScore;
									atlas.setPos(bestPos);
								}
							}
						}
					}//end skip
					
				}
			}
		
			//confirmation method
			//reset things
			bestBlueScore = Double.NEGATIVE_INFINITY;
			bestRGScore = Integer.MIN_VALUE;
			//finished reseting
			atlas.setPos(bestPos);
			if (doConfirmation) confirm = !confirm(atlas, dXRange, convergence);
			if (confirm)
				System.out.println("The atlas was thought to be at ("+bestPos.x+", "+bestPos.y+"). " +
						"Nonetheless, the confirmation method failed. The process responsible for recognizing the retrosternal area is trying to recover by running again along with the confirmation method. Therefore, " +
								bm.getChecked()+" points were selected to be availed as possible new placements of the atlas.");
			else{
				System.out.println("The confirmation method was successful. The atlas is now thought to be at ("+bestPos.x+", "+bestPos.y+").");
			}
			System.gc();
			
		}while(doConfirmation && confirm && counter < 2);
		
		
		
		atlas.setPos(bestPos);
		//if (doConfirmation && !confirm) confirmAndDraw(atlas, dXRange, convergence); //retirar dps
		return atlas;
	}
	public static boolean confirmAndDraw(Marker atlas, Vector2 dXRange, int convergence){
		final short xSpacing = 142, ySpacing = 29;
		
		short initialX = (short) (atlas.getX() + xSpacing), initialY = (short) (atlas.getY() + ySpacing);
		
		short maxC = 3,
				toleranceWidth = 150,
				toleranceHeight = 50;
		
		int xL=initialX, yL=initialY, xR=initialX, yR=initialY;
		for (short tY=(short) (-toleranceHeight/2); tY<toleranceHeight/2; tY+=2){
			for (short tX=0; tX<toleranceWidth/2; tX+=2){
				//left
				short x = (short) (initialX - tX), y = (short) (initialY + tY);
				byte idX = -1, idY = 1, dX = idX, dY = idY;
				if (atlas.getReferenceImageData().getPixel(x, y) > atlas.getReferenceImgThreshold()){
					for (int a=0; a<convergence; a++){
						short previousX = x, previousY = y;
						for (byte c=1; c<=maxC; c++){
							dX = (byte) (idX*c); dY = (byte) (idY*c);
							x += dX; //first varying on X-axis
							if (atlas.getReferenceImageData().getPixel(x, y) < atlas.getReferenceImgThreshold()){
								x -= dX; y += dY; //then on the Y-axis
								if (atlas.getReferenceImageData().getPixel(x, y) < atlas.getReferenceImgThreshold()){
									x += dX; //then on the diagonal
									if (atlas.getReferenceImageData().getPixel(x, y) < atlas.getReferenceImgThreshold()){
										x -= 2*dX;
										if (atlas.getReferenceImageData().getPixel(x, y) < atlas.getReferenceImgThreshold()){
											x = previousX; y = previousY;
										}else c=(byte) (maxC+1);
									}else c=(byte) (maxC+1);
								}else c=(byte) (maxC+1);
							}else c=(byte) (maxC+1);
						}
						if (previousX == x && previousY == y) { //finished the loop
							a=convergence;
						}
					}
				}else{
					x += tX;
					y -= tY;
				}
				xL = x; yL = y;
				
				boolean bypass = false;
				if (initialX == x && initialY == y) 
					bypass = true;
				
				for (short tX2=(short) -tX; tX2<toleranceWidth/2 && !bypass; tX2+=2){
					for (short tY2=(short) (-toleranceHeight/2); tY2<toleranceHeight/2; tY2+=2){	
						//right
						x = (short) (initialX + tX2); y = (short) (initialY + tY2);
						idX = 1; idY = 1; dX = idX; dY = idY;
						if (atlas.getReferenceImageData().getPixel(x, y) > atlas.getReferenceImgThreshold()){ //if it is a fat pixel
							for (int a=0; a<convergence; a++){
								short previousX = x, previousY = y;
								for (byte c=1; c<=maxC; c++){
									dX = (byte) (idX*c); dY = (byte) (idY*c);
									x += dX; //first varying on X-axis
									if (atlas.getReferenceImageData().getPixel(x, y) < atlas.getReferenceImgThreshold()){
										x -= dX; y += dY; //then on the Y-axis
										if (atlas.getReferenceImageData().getPixel(x, y) < atlas.getReferenceImgThreshold()){
											x += dX; //then on the diagonal
											if (atlas.getReferenceImageData().getPixel(x, y) < atlas.getReferenceImgThreshold()){
												x -= 2*dX;
												if (atlas.getReferenceImageData().getPixel(x, y) < atlas.getReferenceImgThreshold()){
													x = previousX; y = previousY;
												}else c=(byte) (maxC+1);
											}else c=(byte) (maxC+1);
										}else c=(byte) (maxC+1);
									}else c=(byte) (maxC+1);
								}
								if (previousX == x && previousY == y) { //finished the loop
									a=convergence;
								}
							}
						}else{
							x -= tX2;
							y -= tY2;
						}
						xR = x; yR = y;
						
						if (isFit(dXRange, initialX, initialY, xR, yR, xL, yL)){
							//alterar-sumir-deletar dps
							System.out.println("Pl was thought to be at: ("+xL + ", " + yL + "), whilst Pr at: (" + xR + ", " + yR + ")");
							atlas.setReferenceImage(ImageOp.drawLine(atlas.getReferenceImage(), new Vector2(xL, yL), new Vector2(xR, yR)));
							return true;
						}
					}
				}
			}
		}
		
		
		//alterar-sumir-deletar dps
		System.out.println("Pl was thought to be at: ("+xL + ", " + yL + "), whilst Pr at: (" + xR + ", " + yR + ")");
		atlas.setReferenceImage(ImageOp.drawLine(atlas.getReferenceImage(), new Vector2(xL, yL), new Vector2(xR, yR)));
		

		return isFit(dXRange, initialX, initialY, xR, yR, xL, yL);
	}
	private static boolean confirm(Marker atlas, Vector2 dXRange, int convergence){
		final short xSpacing = 142, ySpacing = 29;
		
		short initialX = (short) (atlas.getX() + xSpacing), initialY = (short) (atlas.getY() + ySpacing);
		
		short maxC = 3,
				toleranceWidth = 150,
				toleranceHeight = 50;
		
		int xL=initialX, yL=initialY, xR=initialX, yR=initialY;
		for (short tY=(short) (-toleranceHeight/2); tY<toleranceHeight/2; tY+=2){
			for (short tX=0; tX<toleranceWidth/2; tX+=2){
				//left
				short x = (short) (initialX - tX), y = (short) (initialY + tY);
				byte idX = -1, idY = 1, dX = idX, dY = idY;
				if (atlas.getReferenceImageData().getPixel(x, y) > atlas.getReferenceImgThreshold()){
					for (int a=0; a<convergence; a++){
						short previousX = x, previousY = y;
						for (byte c=1; c<=maxC; c++){
							dX = (byte) (idX*c); dY = (byte) (idY*c);
							x += dX; //first varying on X-axis
							if (atlas.getReferenceImageData().getPixel(x, y) < atlas.getReferenceImgThreshold()){
								x -= dX; y += dY; //then on the Y-axis
								if (atlas.getReferenceImageData().getPixel(x, y) < atlas.getReferenceImgThreshold()){
									x += dX; //then on the diagonal
									if (atlas.getReferenceImageData().getPixel(x, y) < atlas.getReferenceImgThreshold()){
										x -= 2*dX;
										if (atlas.getReferenceImageData().getPixel(x, y) < atlas.getReferenceImgThreshold()){
											x = previousX; y = previousY;
										}else c=(byte) (maxC+1);
									}else c=(byte) (maxC+1);
								}else c=(byte) (maxC+1);
							}else c=(byte) (maxC+1);
						}
						if (previousX == x && previousY == y) { //finished the loop
							a=convergence;
						}
					}
				}else{
					x += tX;
					y -= tY;
				}
				xL = x; yL = y;
				
				boolean bypass = false;
				if (initialX == x && initialY == y) 
					bypass = true;
				
				for (short tX2=(short) -tX; tX2<toleranceWidth/2 && !bypass; tX2+=2){
					for (short tY2=(short) (-toleranceHeight/2); tY2<toleranceHeight/2; tY2+=2){	
						//right
						x = (short) (initialX + tX2); y = (short) (initialY + tY2);
						idX = 1; idY = 1; dX = idX; dY = idY;
						if (atlas.getReferenceImageData().getPixel(x, y) > atlas.getReferenceImgThreshold()){ //if it is a fat pixel
							for (int a=0; a<convergence; a++){
								short previousX = x, previousY = y;
								for (byte c=1; c<=maxC; c++){
									dX = (byte) (idX*c); dY = (byte) (idY*c);
									x += dX; //first varying on X-axis
									if (atlas.getReferenceImageData().getPixel(x, y) < atlas.getReferenceImgThreshold()){
										x -= dX; y += dY; //then on the Y-axis
										if (atlas.getReferenceImageData().getPixel(x, y) < atlas.getReferenceImgThreshold()){
											x += dX; //then on the diagonal
											if (atlas.getReferenceImageData().getPixel(x, y) < atlas.getReferenceImgThreshold()){
												x -= 2*dX;
												if (atlas.getReferenceImageData().getPixel(x, y) < atlas.getReferenceImgThreshold()){
													x = previousX; y = previousY;
												}else c=(byte) (maxC+1);
											}else c=(byte) (maxC+1);
										}else c=(byte) (maxC+1);
									}else c=(byte) (maxC+1);
								}
								if (previousX == x && previousY == y) { //finished the loop
									a=convergence;
								}
							}
						}else{
							x -= tX2;
							y -= tY2;
						}
						xR = x; yR = y;
						
						if (isFit(dXRange, initialX, initialY, xR, yR, xL, yL)){
							return true;
						}
					}
				}
			}
		}
		
		
					
		return isFit(dXRange, initialX, initialY, xR, yR, xL, yL);
	}
	private static boolean isDXWithinLimit(int dX, Vector2 dXRange){
		return (Math.abs(dX) > dXRange.x && Math.abs(dX) < dXRange.y);
	}
	private static boolean isBalanced(int initialX, int initialY, int xR, int yR, int xL, int yL){
		return  Math.sqrt(Math.pow(xR - initialX, 2) + Math.pow(yR - initialY, 2)) > Math.sqrt(Math.pow(xL - initialX, 2) + Math.pow(yL - initialY, 2))/2 &&
				Math.sqrt(Math.pow(xL - initialX, 2) + Math.pow(yL - initialY, 2)) > Math.sqrt(Math.pow(xR - initialX, 2) + Math.pow(yR - initialY, 2))/2;
	}
	private static boolean isLongEnough(int initialX, int initialY, int xR, int yR, int xL, int yL, Vector2 dXRange){
		return ((Math.sqrt(Math.pow(xR - initialX, 2) + Math.pow(yR - initialY, 2)) > dXRange.x) &&
				((Math.sqrt(Math.pow(xL - initialX, 2) + Math.pow(yL - initialY, 2)) > dXRange.x)));
	}
	//fast method, none pow
	private static boolean isBalancedAndLongEnough(int initialX, int initialY, int xR, int yR, int xL, int yL, Vector2 dXRange){
		short dR = (short) ((Math.abs(xR - initialX) + Math.abs(yR - initialY))/2),
				dL = (short) ((Math.abs(xL - initialX) + Math.abs(yL - initialY))/2);
		boolean x = Math.abs(xR - initialX) > Math.abs(xL - initialX)*0.6 
				&& Math.abs(xL - initialX) > Math.abs(xR - initialX)*0.6,
				/*novo2*/y = Math.abs(yR - initialY) > Math.abs(yL - initialY)*0.3
						&& Math.abs(yL - initialY) > Math.abs(yR - initialY)*0.3;
		return (dR > dL*0.75 && dL > dR*0.75 && dR > dXRange.x && dL > dXRange.x && x && y);
	}
	private static boolean isFit(Vector2 dXRange, int initialX, int initialY, int xR, int yR, int xL, int yL){
		return isDXWithinLimit(xR - xL, dXRange) && isBalancedAndLongEnough(initialX, initialY, xR, yR, xL, yL, dXRange)
				&& xR > xL /*novo*/&& Math.abs(yR - initialY) > Math.abs(xR - initialX)/2.5 &&
				Math.abs(yL - initialY) > Math.abs(xL - initialX)/2.5 && (Math.abs(yR - initialY) > 30 || Math.abs(yL - initialY) > 30);
	}
	
	
	public static Marker findAtlas(BufferedImage img, BufferedImage atlasImg){
		Marker m = new Marker(img, atlasImg);
		m.setPos(-80, -10);
		m.setReferenceImgThrehsold(10);
		return findatlas(m, new Vector2(0, 0), new Vector2(1, 0.6f), true); //antes tava false
	}

	
	
	
	
	
	private static double d0(double n, double limit){
		if (n < limit) return limit;
		else if (n > 1d/limit) return 1d/limit;
		return n;
	}
	private static boolean isOK(double n){
		if (Double.isInfinite(n) || Double.isNaN(n)) return false;
		return true;
	}
	


	


	
	private static double log(double x, double base){
	    return (double) (Math.log(x) / Math.log(base));
	}

	
}
