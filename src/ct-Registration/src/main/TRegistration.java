package main;


import java.awt.image.BufferedImage;
import java.io.IOException;


import registration.Registration;

public class TRegistration extends Thread{
	private BufferedImage fixedImg = null, movingImg = null;
	private String fileName = null;
	private String outputRegisteredFolder = null;
	
	TRegistration(String fileName, String outputRegisteredFolder, BufferedImage fixedImage, BufferedImage movingImage){
		this.fixedImg = fixedImage;
		this.movingImg = movingImage;
		this.fileName = fileName;
		this.outputRegisteredFolder = outputRegisteredFolder;
	}
	
	public void run(){
			System.out.println("Thread " + this.getName() + ": Registering file " + this.fileName + "... ");
			Registration r = new Registration(fixedImg, movingImg, outputRegisteredFolder);
			try {
				r.findatlasAndExport(outputRegisteredFolder + fileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
	}
	
}
