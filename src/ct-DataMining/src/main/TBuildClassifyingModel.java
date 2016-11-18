package main;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import globals.FeaturesImage;

import javax.imageio.ImageIO;

import others.DynamicFeatures;

public class TBuildClassifyingModel extends Thread{
	private static final short min = 10;
	protected static int windowSize = 2, jumpWindowSize = 10, jumpWindowSize2 = 10;
	private int sliceId = 0;
	protected static boolean speedBoost = false;
	private File imageFile = null, patientFile = null;
	private static File folder = null;
	private String lines = "";
	private static String outputFileName = "";
	protected static volatile PrintWriter out = null;
	private DynamicFeatures ef = new DynamicFeatures();
	
	public static void setParameters(File outputFolder, String outputFileName, int windowSize, int jumpWindowSize, int jumpWindowSize2, boolean speedBoost){
		TBuildClassifyingModel.windowSize = windowSize;
		TBuildClassifyingModel.jumpWindowSize = jumpWindowSize;
		TBuildClassifyingModel.jumpWindowSize2 = jumpWindowSize2;
		TBuildClassifyingModel.speedBoost = speedBoost;
		TBuildClassifyingModel.folder = outputFolder;
		TBuildClassifyingModel.outputFileName = outputFileName;
	}
	
	public TBuildClassifyingModel(int sliceId, File patientFile, File imageFile){
		this.sliceId = sliceId;
		this.patientFile = patientFile;
		this.imageFile = imageFile;
	}
	
	public void run(){
		//for each image
		String status = "Thread " + this.getName() + ": Processing " + patientFile.getName() + "'s image " + imageFile.getName() + "... ";
		BufferedImage img = null;
		try {
			img = ImageIO.read(imageFile.getAbsoluteFile());
		} catch (IOException e1) {
			System.out.println("Error during reading file " + imageFile.getAbsolutePath() + " by thread " + this.getName());
			e1.printStackTrace();
		}
		Raster r = img.getRaster();
		short[][] image = new short[r.getHeight()][r.getWidth()];
		byte[][] type = new byte[r.getHeight()][r.getWidth()];
		int[] rgb = null;
		short[][] window = new short[2*windowSize+1][2*windowSize+1];
		
		//loading image and -center of gravity-
		for (int i=0; i<r.getHeight(); i++){
			for (int j=0; j<r.getWidth(); j++){
				rgb = r.getPixel(j, i, rgb);
				//color of the fat window-centered pixel
				if (rgb[0] == rgb[1] && rgb[1] == rgb[2] && rgb[0] > min) {type[i][j] = 3; image[i][j] = (short) rgb[0];}
				else if (rgb[2] > min) {type[i][j] = 2; image[i][j] = (short) rgb[2];}
				else if (rgb[0] > min) {type[i][j] = 0; image[i][j] = (short) rgb[0];}
				else if (rgb[1] > min) {type[i][j] = 1; image[i][j] = (short) rgb[1];}
				else {type[i][j] = 3; image[i][j] = (short) (rgb[0]+rgb[1]+rgb[2]/3);}
				
			}
		}
		img = null;
		FeaturesImage currentFeaturesImg = new FeaturesImage(image);
		r = null;
		
		//windowed processing
		for (int i=windowSize; i<currentFeaturesImg.getHeight() - windowSize; i++){
			System.out.println(status + i*100/(currentFeaturesImg.getHeight()-windowSize) + "%...");
			for (int j=windowSize; j<currentFeaturesImg.getWidth() - windowSize; j++){
				
				boolean criticalBypass = type[i][j] == 3 ? false : j % jumpWindowSize2 == 0 && i % jumpWindowSize2 == 0;
				
				//if its red or green (important pixels)
				if (!speedBoost || ((j % jumpWindowSize == 0 && i % jumpWindowSize == 0) || (criticalBypass))){
					
					//window loop/construction
					for (int i2=i-windowSize; i2<=i+windowSize; i2++){
						for (int j2=j-windowSize; j2<=j+windowSize; j2++){
							window[i2 - (i - windowSize)][j2 - (j - windowSize)] = currentFeaturesImg.getPixel(j2, i2);
						}
					}

					lineFeatures(this.sliceId, currentFeaturesImg.getPixel(j, i), j, i, type[i][j], currentFeaturesImg, window);
				}
				
			}
		    try {
				if (!this.lines.equals("")) 
					write(this.lines);
			} catch (IOException e) {
				System.out.println("Error during concatenating the features from of the file: " + imageFile.getAbsolutePath());
				e.printStackTrace();
			}
		    this.lines = "";
		}
		currentFeaturesImg.dispose();
		currentFeaturesImg = null;
		try {
			this.finalize();
		} catch (Throwable e) {
			System.out.println("Error during the finalization of thread number " + this.getName());
			e.printStackTrace();
		}
	}
	
	private void lineFeatures(int sliceid, short pixelvalue, int posx, int posy, byte type, FeaturesImage img, short[][] pixelValues){
		
		/*String[] f = ExtractedFeatures.extractFeatures(sliceid, pixelvalue, posx, posy, type, img, pixelValues);
		for (int i=0; i<f.length-1; i++){
			lines += f[i] + ",";
		}
		lines += f[f.length - 1] + "\n";
		*/
		lines += ef.extractFeatures(sliceid, pixelvalue, posx, posy, type, img, pixelValues) + "\r\n";
	}
	
	private synchronized static void write(String s) throws IOException{
		if (out == null) {
			System.out.println("Results are being outputted to: " + folder.getAbsolutePath() + "/" + outputFileName);
			//apagar arquivo
			out = new PrintWriter(new BufferedWriter(new FileWriter(folder.getAbsolutePath() + "/" + outputFileName, false)));
			out.print("");
			out.close();
			//iniciar e escrever
			out = new PrintWriter(new BufferedWriter(new FileWriter(folder.getAbsolutePath() + "/" + outputFileName, true)));
			//adicionar conteúdo do cabeçalho à saída
			File test = new File(folder.getAbsolutePath()+"/cabecalho.txt");
			BufferedReader br = new BufferedReader(new FileReader(test));
			String cLine = "";
			while ((cLine = br.readLine()) != null) {out.print(cLine + "\r\n");}
		}
		out.print(s);
	}
}
	