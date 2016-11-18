package main;



import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import registration.Registration;
import similarity.Similarity;

public class Main {

	public static void main(String args[]) throws IOException{
		diceSimilarity();
		//oldMain();
		
	}
	public static void oldMain(){

		String img1Path = "C:\\Users\\Érick\\Documents\\Aura\\CT\\Registro\\referencia.png",
				img2Path = "C:\\Users\\Érick\\Documents\\Aura\\CT\\Registro\\afazer.png",
				outputImagePath = "C:\\Users\\Érick\\Documents\\Aura\\CT\\Registro\\atlas teste registro\\com mean na similaridade\\teste10\\feito.png";
		Registration r = null;
		try {
			/*
			r = new Registration(ImageIO.read(new File(img1Path)),
					ImageIO.read(new File(img2Path)), outputImagePath, 10, TransformationOp.BILINEAR);
					r.registerAndExport(true);
					*/
			File folder = new File("C:\\Users\\Érick\\Documents\\Aura\\CT\\Registro\\atlas\\instancias");
			File[] files = folder.listFiles();
			for (int i=0; i<files.length; i++){
				System.out.println("Fazendo " + i + "... " + files[i].getName());
				r = new Registration(ImageIO.read(files[i]), ImageIO.read(new File(img2Path)), outputImagePath);
				r.findatlasAndExport("C:\\Users\\Érick\\Documents\\Aura\\CT\\Registro\\atlas\\com marcador(redAndGreen)\\semConfirmationMethod\\sem sigmoid\\hybridG=meio\\" + files[i].getName());
			}
					
					
			/*
			//fazer o mean das imagens pequenas
			String p ="C:\\Users\\Érick\\Documents\\Aura\\CT\\Registro\\similaridade por média\\Diagonal Mean\\instancias 25x41\\";
			File folder = new File(p + "corretas\\");
			File[] files = folder.listFiles();
			for (int i=0; i<files.length; i++){
				FeaturesImage fi = new FeaturesImage(ImageIO.read(files[i]));
				String dp = files[i].getParentFile().getAbsolutePath();
				ImageIO.write(ImageIO.read(files[i]), "PNG",
						new File(p + "\\corretas numeradas\\" + files[i].getName().split("\\.")[0] +"_mean~" + fi.getReducedIntensityMean() + ".png"));
			}
			*/
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
	
	
	public void extractImagesMean(String imagesFolderPath, String outputImagePath) throws IOException{
		
		File folder = new File(imagesFolderPath);

		//fazer para todos os pacientes dentro de uma pasta
		File[] files = folder.listFiles(); 
		BufferedImage outimg = null; WritableRaster inputRaster, outputRaster = null;
		BufferedImage img = null;
		double[][][] finalImg = null;
		for (int f=0; f<files.length; f++){
			img = ImageIO.read(files[f].getAbsoluteFile());
			if (finalImg == null) {//first time within the loop
				finalImg = new double[img.getHeight()][img.getWidth()][img.getColorModel().getNumComponents()];
				outimg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
				outputRaster = outimg.getRaster();
			}
			inputRaster = img.getRaster();
			for (int i=0; i<img.getHeight(); i++){
				for (int j=0; j<img.getWidth(); j++){
					for (int c=0; c<img.getColorModel().getNumComponents(); c++)
						finalImg[i][j][c] += inputRaster.getSampleDouble(j, i, c);
				}
			}
		}
		//write to outputraster
		for (int i=0; i<outputRaster.getHeight(); i++){
			for (int j=0; j<outputRaster.getWidth(); j++){
				for (int c=0; c<img.getColorModel().getNumComponents(); c++)
					outputRaster.setSample(j, i, c, finalImg[i][j][c]/files.length);
			}
		}
		finalImg = null;
		
		ImageIO.write(outimg, outputImagePath.split(".")[1], new File(outputImagePath));
	}
	
	
	
	public static void diceSimilarity() throws IOException{
		String dir = "C:/Users/Érick/Documents/Aura/CT/classificacaoFinal/comparacao/";
		BufferedImage ground = ImageIO.read(new File(dir + "g2.png"));
		BufferedImage input = ImageIO.read(new File(dir + "IM-0002-0008.png"));
		
		System.out.println(Similarity.diceColorSimilarity(ground, input));
	}
}
