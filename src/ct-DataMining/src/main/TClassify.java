package main;

import globals.FeaturesImage;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import others.DynamicFeatures;

import weka.core.DenseInstance;

import datamining.Classifier;

public class TClassify extends Thread{
	private static final String folderpath = "C:/Users/Érick/Documents/Aura/CT/Novos Pacientes - Calcio3 - PSpacing/Z-Adenildes Araujo/imagens/";
	private static File folder = new File(folderpath), arffFile = null;
	private static final String arffPath = "C:/Users/Érick/Documents/Aura/CT/Novos Pacientes - Calcio3 - PSpacing/";
	private static String redArffPath = arffPath + "mineddata.arff",
			greenArffPath = arffPath + "mineddata.arff";
	private static int windowSize, jumpPixel;
	private File imageFile = null;
	private int sliceId = 0;
	private static Classifier[] classifiers;
	
	
	//static parameters
	public static void setParameters(Classifier[] classifiers, File arffFile, int windowSize, int jumpPixel){
		TClassify.arffFile = arffFile;
		TClassify.windowSize = windowSize;
		TClassify.jumpPixel = jumpPixel;
		TClassify.classifiers = classifiers;
	}
	
	public TClassify(int sliceId, File imageFile){
		this.setInput(imageFile, sliceId);
	}
	public TClassify(){}
	
	public void setImage(File imageFile){
		this.imageFile = imageFile;
	}
	public void setSliceId(int sliceId){
		this.sliceId = sliceId;
	}
	public void setInput(File imageFile, int sliceId){
		this.setImage(imageFile);
		this.setSliceId(sliceId);
		//declaring window
		window = new short[windowSize*2+1][windowSize*2+1];
	}
	
	
	private BufferedImage outImg = null;
	private WritableRaster outRaster = null, inRaster = null;
	private DenseInstance di = null;
	private short[][] image, window;
	private int[] rgb;
	private FeaturesImage currentImg = null;
	/**
	 * @param args
	 * @throws Exception 
	 */
	public void run() {
		//for each image
		String status = "Thread " + this.getName() + ": Processing " + imageFile.getParentFile().getParentFile().getName() + "'s image " + imageFile.getName() + "... ";
	
		//if the image does not exist already
		if (!new File(imageFile.getParentFile().getParentFile().getAbsolutePath() + "/classified/" + imageFile.getName()).exists()){

			//create concurrent flag
			//try {
			//	new File(imageFile.getAbsolutePath() + ".flag").createNewFile();
			//} catch (IOException e1) {
			//	e1.printStackTrace(System.out);
			//}
			
			DynamicFeatures ef = new DynamicFeatures();
			BufferedImage img = null;
			try {
				img = ImageIO.read(imageFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Error during reading file " + imageFile.getAbsolutePath() + " by thread " + this.getName());
				e.printStackTrace(System.out);
			}
			int bitdepth = img.getColorModel().getPixelSize();
			inRaster = img.getRaster();
			image = new short[inRaster.getHeight()][inRaster.getWidth()];
			
			outImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
			outRaster = outImg.getRaster();
			
			//loading image and -center of gravity-
			if (currentImg != null) currentImg.dispose();
			for (int i=0; i<inRaster.getHeight(); i++){
				for (int j=0; j<inRaster.getWidth(); j++){
					rgb = inRaster.getPixel(j, i, rgb);
					image[i][j] = (short) rgb[0];
				}
			}
			img = null;
			currentImg = new FeaturesImage(image);
			image = null;
			
			
			//windowed processing
			boolean criticalarea = false;
			boolean painted = false;
			double[] instance;
			short currentPixel;
			double result;
			for (int i=0; i<currentImg.getHeight(); i+=jumpPixel){
				if (i%20==0) System.out.println("Classifying... " + status + i*100/(currentImg.getHeight()) + "%...");
				for (int j=0; j<currentImg.getWidth(); j+=jumpPixel){
					
					rgb = inRaster.getPixel(j, i, rgb);
					currentPixel = (short) rgb[0];
					
					if (currentPixel > 30){
					
						//window loop/construction
						for (int i2=i-windowSize; i2<=i+windowSize; i2++){
							for (int j2=j-windowSize; j2<=j+windowSize; j2++){
								window[i2 - (i - windowSize)][j2 - (j - windowSize)] = currentImg.getPixel(j2, i2);
							}
						}
						
						ef.extractFeatures(sliceId, currentPixel, j, i, (byte)0, currentImg, window);
						
			
						//red classification
						instance = ef.getRedFeatures();
						di = new DenseInstance(1.0, instance);
						classifiers[0].getAttributesHeader().setDataset(di);
						classifiers[0].getAttributesHeader().setClassIndex(0);
						di.setClassMissing();
						result = 0;
						try {result = classifiers[0].classifyInstance(di);} catch (Exception e) {}
						if (result != 1.0) {//true
							outRaster.setSample(j, i, 0, currentPixel);
							painted = true;
						}
	
						
						//green classification
						instance = ef.getGreenFeatures();
					    di = new DenseInstance(1.0, instance);
						classifiers[1].getAttributesHeader().setDataset(di);
						classifiers[1].getAttributesHeader().setClassIndex(0);
						di.setClassMissing();
						result = 0;
						try {result = classifiers[1].classifyInstance(di);} catch (Exception e) {}
						if (result != 1.0) {//true
							 outRaster.setSample(j, i, 1, currentPixel); //paint green
							 painted = true;
						}
						
						
						if (!painted){
							outRaster.setSample(j, i, 0, currentPixel);
							outRaster.setSample(j, i, 1, currentPixel);
							outRaster.setSample(j, i, 2, currentPixel);
						}
						painted = false;
					}
				}
			}
			ef.dispose();
			
			
			//copying raster
			BufferedImage classifiedImage = new BufferedImage(currentImg.getWidth(), currentImg.getHeight(), BufferedImage.TYPE_INT_RGB);
			WritableRaster classRaster = classifiedImage.getRaster();
			for (int i=0; i<currentImg.getHeight(); i++){
				for (int j=0; j<currentImg.getWidth(); j++){
					for (int k=0; k<3; k++)
						classRaster.setSample(j, i, k, outRaster.getSample(j, i, k));
				}
			}
			
			//box-blur like classification-filter
			System.out.println("Box-blurring classification... " + status);
			short[] layerScore;
			for (int i=0; i<currentImg.getHeight(); i++){
				for (int j=0; j<currentImg.getWidth(); j++){
					//process the iterated pixel
					rgb = new int[3];
					for (int k=0; k<3; k++)
						rgb[k] = classRaster.getSample(j, i, k);
					byte layerNumber = 3, itLayerNumber = 0;
					if (rgb[0] != rgb[1] && rgb[1] == rgb[2]) layerNumber = 0;
					else if (rgb[1] != rgb[0] && rgb[0] == rgb[2]) layerNumber = 1;
					else if (rgb[2] != rgb[1] && rgb[0] == rgb[1]) layerNumber = 2;
					else if(rgb[0] == rgb[1] && rgb[1] == rgb[2] && rgb[0] > 0) layerNumber = 3; //grey
					else layerNumber = 4; //black
					
					if (layerNumber == 3 || layerNumber == 4){//if it is still grey or black
						
						//calculate the score of colored pixels on the surroundings
						layerScore = new short[5];
						for (int i2=(int) (i-Math.ceil(jumpPixel/2)); i2<=i+Math.ceil(jumpPixel/2); i2++){
							for (int j2=(int) (j-jumpPixel); j2<=j+jumpPixel; j2++){
								if (i2 >= 0 && j2 >=0 && i2 < currentImg.getHeight() && j2 < currentImg.getWidth()){
									for (int k=0; k<3; k++)
										rgb[k] = classRaster.getSample(j2, i2, k);
									
									if (rgb[0] > rgb[1] && rgb[1] == rgb[2]) itLayerNumber = 0;
									else if (rgb[1] > rgb[0] && rgb[0] == rgb[2]) itLayerNumber = 1;
									else if (rgb[2] > rgb[1] && rgb[0] == rgb[1]) itLayerNumber = 2;
									else if(rgb[0] == rgb[1] && rgb[1] == rgb[2] && rgb[0] > 0) itLayerNumber = 3; //grey
									else itLayerNumber = 4;
									
									layerScore[itLayerNumber] ++;
								}
							}
						}
						
						
						boolean paintRed = false, paintGreen = false, paintBlue = false, paintGrey = false;
						if ((layerScore[3] > layerScore[2] && layerScore[3] >= layerScore[1] && layerScore[3] >= layerScore[0]) ||
								(layerScore[3] >= layerScore[2] && layerScore[3] > layerScore[1] && layerScore[3] >= layerScore[0]) ||
								(layerScore[3] >= layerScore[2] && layerScore[3] >= layerScore[1] && layerScore[3] > layerScore[0]))
							paintGrey = true;
						else if (layerScore[1] > layerScore[0] && layerScore[1] >= layerScore[2] && layerScore[1] >= layerScore[3] ||
								layerScore[1] >= layerScore[0] && layerScore[1] > layerScore[2] && layerScore[1] >= layerScore[3] ||
								layerScore[1] >= layerScore[0] && layerScore[1] >= layerScore[2] && layerScore[1] > layerScore[3])
							paintGreen = true;
						else if (layerScore[0] > layerScore[1] && layerScore[0] >= layerScore[2] && layerScore[0] >= layerScore[3] ||
								layerScore[0] >= layerScore[1] && layerScore[0] > layerScore[2] && layerScore[0] >= layerScore[3] ||
								layerScore[0] >= layerScore[1] && layerScore[0] >= layerScore[2] && layerScore[0] > layerScore[3])
							paintRed = true;
						else if (layerScore[2] > layerScore[1] && layerScore[2] >= layerScore[0] && layerScore[2] >= layerScore[3] ||
								layerScore[2] >= layerScore[1] && layerScore[2] > layerScore[0] && layerScore[2] >= layerScore[3] ||
								layerScore[2] >= layerScore[1] && layerScore[2] >= layerScore[0] && layerScore[2] > layerScore[3])
							paintBlue = true;
						else
							paintGrey = true;
						
						
						
						/*
						boolean paintRed = false, paintGreen = false, paintBlue = false, paintGrey = false;
						if (layerScore[3] >= layerScore[2] && layerScore[3] >= layerScore[1] && layerScore[3] >= layerScore[0])
							paintGrey = true;
						else if (layerScore[1] >= layerScore[0] && layerScore[1] >= layerScore[2] && layerScore[1] >= layerScore[3])
							paintGreen = true;
						else if (layerScore[0] >= layerScore[1] && layerScore[0] >= layerScore[2] && layerScore[0] >= layerScore[3])
							paintRed = true;
						else if (layerScore[2] >= layerScore[1] && layerScore[2] >= layerScore[0] && layerScore[2] >= layerScore[3])
							paintBlue = true;
						else
							paintGrey = true;
						
						/*
						boolean paintRed = false, paintGreen = false, paintBlue = false, paintGrey = false;
						paintGrey = (layerScore[0] == layerScore[1] && layerScore[0] == layerScore[2] && layerScore[0] == layerScore[3]);
						//layerScore[3] = (short) (layerScore[3]*0.8f); //pesar os pixels cinza, precisa ter pelo menos 2 vezes mais do que os outros pra pintar de cinza
						paintGrey = !paintGrey && (layerScore[3] >= layerScore[0] && layerScore[3] >= layerScore[1] && layerScore[3] >= layerScore[2]);
						paintRed = !paintGrey && layerScore[0] >= layerScore[1] && layerScore[0] >= layerScore[2] && layerScore[0] >= layerScore[3];
						paintGreen = !paintRed && layerScore[1] >= layerScore[0] && layerScore[1] >= layerScore[2] && layerScore[1] > layerScore[3];
						paintBlue = !paintGreen && layerScore[2] >= layerScore[1] && layerScore[2] >= layerScore[0] && layerScore[2] >= layerScore[3];
						paintGrey = paintGrey || (!paintRed && !paintGreen && !paintBlue);
						*/
						
						
						if (paintGrey) {outRaster.setSample(j, i, 2, currentImg.getPixel(j, i)); outRaster.setSample(j, i, 1, currentImg.getPixel(j, i)); outRaster.setSample(j, i, 0, currentImg.getPixel(j, i));}
						else if (paintRed) {outRaster.setSample(j, i, 0, currentImg.getPixel(j, i)); outRaster.setSample(j, i, 1, 0); outRaster.setSample(j, i, 2, 0);}
						else if(paintGreen) {outRaster.setSample(j, i, 1, currentImg.getPixel(j, i)); outRaster.setSample(j, i, 0, 0); outRaster.setSample(j, i, 2, 0);}
						else if (paintBlue) {outRaster.setSample(j, i, 2, currentImg.getPixel(j, i)); outRaster.setSample(j, i, 1, 0); outRaster.setSample(j, i, 0, 0);}
						
					}
				}
			}
			
			
			int length = imageFile.getName().split("\\.").length;
			
			File newdir = new File(imageFile.getParentFile().getParentFile().getAbsolutePath() + "/classified");
			if (!newdir.exists()) newdir.mkdir();
			try {
				ImageIO.write(outImg, imageFile.getName().split("\\.")[length - 1], new File(newdir.getAbsolutePath() + "/" + imageFile.getName()));
			} catch (IOException e) {
				System.out.println("Error during writing output image " + imageFile.getName());
				e.printStackTrace();
			}
			
			outImg = null;
			outRaster = null;
			currentImg= null;
			System.gc();
		
		
		}
	}
	
		
	

	
}
