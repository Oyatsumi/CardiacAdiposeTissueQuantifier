package main;

import globals.FileComparator;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import dicom.DicomXML;


public abstract class FatToHigherRange {
	private final static int minRange = -200, maxRange = 500;
	
	public static void writeColoredHigherRange(int imageIndex, String patientFolder, String coloredFolder, AffineTransform at, DicomXML dFile, BufferedImage coloredFatImg){

		File coloredFatFolder = new File(coloredFolder);
		File[] coloredFatFiles = coloredFatFolder.listFiles();
		Arrays.sort(coloredFatFiles, new FileComparator());
		
		BufferedImage coloredFatImage2 = null;
		if (coloredFatImg == null){
			try {
				if (imageIndex < coloredFatFiles.length)
					coloredFatImage2 = ImageIO.read(coloredFatFiles[imageIndex]);
				else
					return;
			} catch (IOException e) {
				System.out.println("Skipping the color in higher ranges conversion. No classified folder found.");
				//e.printStackTrace(System.out);
			}
			if (!coloredFatFiles[imageIndex].exists()) return;
		}else
			coloredFatImage2 = coloredFatImg;
		Raster fRaster = coloredFatImage2.getRaster(); //null
		
		
		
		
		File xmlFolder = new File(patientFolder + "/xml/");
		File[] xmlFiles = xmlFolder.listFiles();
		Arrays.sort(xmlFiles, new FileComparator());
		
		BufferedImage higherRangeImg = null;
		DicomXML dicomFile = null;
		if (dFile == null)
			dicomFile = new DicomXML(xmlFiles[imageIndex].getAbsolutePath());
		else
			dicomFile = dFile;
		try {
			higherRangeImg = dicomFile.getTransformedImageOnRange(at, minRange, maxRange);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Raster hRaster = higherRangeImg.getRaster();
		
		BufferedImage segImg2 = null, segImg3 = null;
		segImg2 = new BufferedImage(hRaster.getWidth(), hRaster.getHeight(), BufferedImage.TYPE_INT_RGB);
		segImg3 = new BufferedImage(hRaster.getWidth(), hRaster.getHeight(), BufferedImage.TYPE_INT_RGB);
		WritableRaster wRaster2 = segImg2.getRaster(),
				wRaster3 = segImg3.getRaster();
		
		File segmentadas2, segmentadas3;
		//read and write the new img
		for (int i=0; i<fRaster.getHeight(); i++){
			for (int j=0; j<fRaster.getWidth(); j++){
				int r = fRaster.getSample(j, i, 0), g = fRaster.getSample(j, i, 1), b = fRaster.getSample(j, i, 2);
				
				int previousV = hRaster.getSample(j, i, 0);
				boolean painted = false;
				//wRaster.setSample(j, i, 0, 0); wRaster.setSample(j, i, 1, 0); wRaster.setSample(j, i, 2, 0);
				if (r > g || r > b){
					wRaster2.setSample(j, i, 0, previousV);
					wRaster3.setSample(j, i, 0, r);
					painted = true;
				}
				if (g > r || g > b){
					wRaster2.setSample(j, i, 1, previousV);
					wRaster3.setSample(j, i, 1, g);
					painted = true;
				}
				if ((b > r || b > g) || (painted && b > 0)){
					wRaster2.setSample(j, i, 2, previousV);
					wRaster3.setSample(j, i, 2, b);
					painted = true;
				}
				
				if (!painted){
					wRaster2.setSample(j, i, 0, previousV); 
					wRaster2.setSample(j, i, 1, previousV); 
					wRaster2.setSample(j, i, 2, previousV);
					wRaster3.setSample(j, i, 0, previousV); 
					wRaster3.setSample(j, i, 1, previousV); 
					wRaster3.setSample(j, i, 2, previousV);
				}
					
					
			}
		}
		
		segmentadas2 = new File(patientFolder + "/segmentadas2/");
		if (!segmentadas2.exists()){
			segmentadas2.mkdir();
		}
		
		segmentadas3 = new File(patientFolder + "/segmentadas3/");
		if (!segmentadas3.exists()){
			segmentadas3.mkdir();
		}
		
		int length = coloredFatFiles[imageIndex].getName().split("\\.").length;
		String type = coloredFatFiles[imageIndex].getName().split("\\.")[length - 1];
		try {
			String name = "";
			String[] splittedName = xmlFiles[imageIndex].getName().split("\\.");
			for (int k=0; k<splittedName.length - 2; k++)
				name += splittedName[k];
			ImageIO.write(segImg2, type, new File(segmentadas2.getAbsolutePath() + "/" 
					+ name + "." + type));
			ImageIO.write(segImg3, type, new File(segmentadas3.getAbsolutePath() + "/" 
					+ name + "." + type));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	
	
	
	
	
	
	
	
	//acessar a pasta segmentadas e dicom e criar a pasta segmentadas2 (com range maior)
	public void main(){
		
	}
	
}
