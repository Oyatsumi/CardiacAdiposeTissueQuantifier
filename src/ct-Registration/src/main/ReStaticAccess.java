package main;

import globals.FileComparator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

public abstract class ReStaticAccess {
	public static int AVAILABLE_CORES = 1;
	public static String patientsFolderPath = "";
	public static BufferedImage movingImg = null;
	
	public static void set(String patientsFolderPath, BufferedImage movingImg, int availableCores){
		AVAILABLE_CORES = availableCores;
		ReStaticAccess.patientsFolderPath = patientsFolderPath;
		ReStaticAccess.movingImg = movingImg;
	}

	public static void register() throws IOException{
		
		File patientFolder = new File(new File(patientsFolderPath).getAbsolutePath());
		File[] patients = patientFolder.listFiles();
		Arrays.sort(patients, new FileComparator());
		System.out.println("Registering patients at folder " + patientFolder.getAbsolutePath() + "...");
		
		
		//for each patient
		System.out.println("A total of " + AVAILABLE_CORES + " threads have been created.");
		TRegistration[] r = new TRegistration[AVAILABLE_CORES];
		for (int p=0; p<patients.length; p++){
			if (patients[p].isDirectory()){
				File imagesFolder = new File(patients[p].getAbsolutePath() + "/imagens/");
				if (imagesFolder != null){
					File[] imageFiles = imagesFolder.listFiles();
					if (imageFiles != null) 
						Arrays.sort(imageFiles, new FileComparator());
					int length = 0;
					if (!imagesFolder.isDirectory()) length = 0;
					else length = imageFiles.length;
					
					//for each image
					for (int a=0; a<length; a++){
						boolean bounded = false;
						while (!bounded){
							for (int c=0; c<AVAILABLE_CORES; c++){
								boolean dontBypass = r[c] == null ? true : !r[c].isAlive();
								if (dontBypass){
									bounded = true;
									r[c] = new TRegistration(imageFiles[a].getName(), imageFiles[a].getParentFile().getAbsolutePath() + "/imagens/",
											ImageIO.read(imageFiles[a]), movingImg);
									r[c].setName(Integer.toString(c));
									r[c].start();
									c=AVAILABLE_CORES;
								}
							}
						}
					}
				}
			}
		}
	}
}
