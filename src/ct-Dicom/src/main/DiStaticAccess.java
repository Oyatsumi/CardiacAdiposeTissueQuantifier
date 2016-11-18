package main;

import globals.FileComparator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import java.util.Arrays;


public abstract class DiStaticAccess {
	private static boolean ended = false;
	
	public static boolean hasEnded(){return ended;}
	
	public static void convertAndRegister(String patientsFolderPath, int availableCores, File movingImage) throws Exception{
		
		
		File patientsFolder = new File(patientsFolderPath), dicomDir;
		
		//fazer para todos os pacientes dentro de uma pasta
		File[] patientFolder = patientsFolder.listFiles(), patientFiles; 
		Arrays.sort(patientFolder, new FileComparator());
		
		

		
		//create threads
		TConvert[] cm = new TConvert[availableCores];
		
		//set the atlas
		TConvert.setMovingImage(movingImage);
		
		File dicomFolder, alreadyProcessed;
		File[] dicomFiles = null, xmlFiles;
		
		
		//CONVERT TO XML
		for (int f=0; f<patientFolder.length; f++){
			
			dicomFolder = new File(patientFolder[f].getAbsolutePath() + "/DICOM/");
			dicomFiles = dicomFolder.listFiles();	
			
			xmlFiles = new File(patientFolder[f].getAbsolutePath() + "/xml/").listFiles();
			boolean convertXML = (xmlFiles == null) ? true : !(dicomFiles.length <= xmlFiles.length);
			if (new File(patientFolder[f].getAbsolutePath() + "/REGISTRATION_OK").exists()) convertXML = false;
			
			//Arrays.sort(dicomFiles, new FileComparator());
			if (dicomFiles != null && convertXML){
				
				//for each Dicom
				int length = dicomFiles.length;
				for (int a=0; a<length; a++){
					boolean bounded = false;
					while (!bounded){
						for (int c=0; c<availableCores; c++){
							boolean dontBypass = cm[c] == null ? true : !cm[c].isAlive();
							if (dontBypass){
								cm[c] = new TConvert(dicomFiles[a], patientFolder[f].getAbsolutePath());
								cm[c].setName(Integer.toString(c));
								cm[c].start();
								bounded = true;
								c=availableCores;
							}
						}
					}
				}
			}
				
		}
		boolean ok = false;
		while (!ok){
			ok = true;
			for (int c=0; c<cm.length; c++) ok &= (cm[c] == null) ? true : !cm[c].isAlive();
		}
		
		for (int c=0; c<cm.length; c++) cm[c] = null;
		System.gc();
		
		
		//converting to image
		for (int f=0; f<patientFolder.length; f++){
			boolean skip = false;

			if (!patientFolder[f].isDirectory()) {
				skip = true;
			}else{
				alreadyProcessed = new File(patientFolder[f].getAbsolutePath() + "/imagens/"); //images folder
				if (alreadyProcessed.exists()){
					//skip = true;
					if (new File(patientFolder[f].getAbsolutePath() + "/REGISTRATION_OK").exists()) skip = true;
					if (alreadyProcessed.listFiles().length >= 
							new File(patientFolder[f].getAbsolutePath() + "/xml/").listFiles().length - 2)
							skip = true;
				}
			}
			
			
			if (!skip){
				
				//IMAGE CONVERSION AND REGISTRATION
				boolean bounded = false;
				while (!bounded){
					for (int c=0; c<availableCores; c++){
						boolean dontBypass = cm[c] == null ? true : !cm[c].isAlive();
						if (dontBypass){
							cm[c] = new TConvert(patientFolder[f].getAbsolutePath(), "PNG");
							cm[c].setName(Integer.toString(c));
							cm[c].start();
							bounded = true;
							c=availableCores;
						}
					}
					System.gc();
				}
				
				
				
				}//fim do se tiver arquivos dentro do diretório
			if (f == patientFolder.length - 1) 
				new File(patientFolder[f].getAbsolutePath() + "/REGISTRATION_OK").createNewFile();
		}
		
		ok = false;
		while (!ok){
			ok = true;
			for (int c=0; c<cm.length; c++) ok &= (cm[c] == null) ? true : !cm[c].isAlive();
		}
		for (int c=0; c<cm.length; c++) cm[c] = null;
		System.gc();
		
		
	}
	
	
}
