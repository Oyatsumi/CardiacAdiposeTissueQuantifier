

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

import dicom.DicomXML;

import main.DiStaticAccess;
import main.DmStaticAccess;




public class Main {
	
	private static String patientsFolderPath = "", baseAndModelPath = "";
	private static int windowSize = 12, pixelJump = 1, sliceJump = 1, availableProcessors = 1;
	private static File flagsFolder;

	
	private static void moveDicomFiles(){
		File patientsFolder = new File(patientsFolderPath), dicomDir;
		//fazer para todos os pacientes dentro de uma pasta
		File[] patientFolder = patientsFolder.listFiles(), patientFiles; 
		//Arrays.sort(patientFolder, new FileComparator());
		//System.out.println("Moving files to DICOM folder...");
		
		/*
		File moveFlag = new File(flagsFolder.getAbsolutePath() + "/dicomMove");
		if (moveFlag.exists()) {
			while (moveFlag.exists());
			return;
		}
		*/
		
		//se nao tem o dir DICOM, criar e mover tudo
		String name = null, number = null, extension = null;
		for (int k=0; k<patientFolder.length; k++){
			if (!patientFolder[k].isDirectory()) continue; //tb
			patientFiles = patientFolder[k].listFiles();
			if (patientFiles == null) continue; //nao é dir
			dicomDir = new File(patientFolder[k].getAbsolutePath() + "/DICOM/");
			if (!dicomDir.exists()){
				dicomDir.mkdir();
				for (int l=0; l<patientFiles.length; l++){
					if (dicomDir.getAbsolutePath().equals(patientFiles[l].getAbsolutePath())) continue;
					number = Integer.toString(l);
					while (number.length() < 3) number = "0" + number;
					//name = (patientFiles[l].getName().length() > 10) ?
					//		patientFiles[l].getName().substring(0, 10) + number : patientFiles[l].getName();
					//extension = patientFiles[l].getName().substring(patientFiles[l].getName().length() - 3, patientFiles[l].getName().length());
					name = patientFiles[l].getName(); extension = "";
					patientFiles[l].renameTo(new File(dicomDir.getAbsolutePath() + "/" + name + extension));
					//patientFiles[l].delete();
				}
			}else
				return;
		}
		//if (moveFlag.exists()) moveFlag.delete();
	}
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		//arguments: 
		windowSize = Integer.parseInt(args[0]);
		pixelJump = Integer.parseInt(args[1]);
		sliceJump = Integer.parseInt(args[2]);
		patientsFolderPath = args[3];
		baseAndModelPath = args[4];
		availableProcessors = (short) (Runtime.getRuntime().availableProcessors() * 2);
		
		
		/*
		//SERIAL
		Preferences prefs = Preferences.userNodeForPackage(SerialKey.class);
		if (!prefs.getBoolean("isSet", false)){//if not set
			prefs.put("machineId", SerialKey.getRandomSerial());
			prefs.putBoolean("isSet", true);
		}
		//verify 
		
		if (!SerialKey.encode(prefs.get("machineId", "-")).equals(prefs.get("serialKey", "-"))){
			System.out.println("The program is currently locked. Please visit 'http://goo.gl/kysxdn' and place the following id there:");
			System.out.println("+---------------------+");
			System.out.println(prefs.get("machineId", "-"));
			System.out.println("+---------------------+");
			System.out.println("After that, take the serial key returned by the website and place here to unlock the program.");
			Scanner in = new Scanner(System.in);
 			boolean right = false;
			while(!right){
				String inputKey = in.next();
				//String[] split = inputKey.split("-");
				//if (split.length > 2) inputKey = split[0] + "-" + split[1] + "-" + split[2]; //removing the last parcel
				inputKey.toUpperCase();
				if (!SerialKey.encode(prefs.get("machineId", "-")).equals(inputKey)){
					System.out.println("The serial key is wrong, please try again.");
				}else{
					right = true;
					prefs.put("serialKey", inputKey);
				}
			}
		}
		*/
		
		//creating flags folder (concurrency)
		//flagsFolder = new File(patientsFolderPath + "/flags/");
		//if (!flagsFolder.exists()) flagsFolder.mkdir();
		
		//moving files to a dicom folder
		moveDicomFiles();
		
		
		
		System.out.println("Converting and registering patients...");
		
		//REGISTRATION
		File fp = new File("atlas.png");
		if (!fp.isFile()){
		  fp = new File(Main.class.getClassLoader().getResource("atlas.png").getPath());
		}
		DiStaticAccess.convertAndRegister(patientsFolderPath, availableProcessors, fp);
		
		System.gc();
		
		System.out.println("Creating and/or loading the predictive model. Please wait.");
		
		//CLASSIFICATION
		DmStaticAccess.classify(windowSize, pixelJump, sliceJump, patientsFolderPath, baseAndModelPath);

		System.gc();
		
		//DILATION
		dilation(patientsFolderPath);
		
		//COUNT AND OUTPUT THE VOLUME
		countVolume(patientsFolderPath);
		
	}
	
	
	
	public static void dilation(String patientsFolderPath) throws IOException{
		
		BufferedImage inputImg = null, outImg = null;
		Raster r;
		WritableRaster outRaster;
		int timesToDo = 3;
		
		File[] patients = new File(patientsFolderPath).listFiles();
		for (int p=0; p<patients.length; p++){//for each patient
			if (new File(patients[p].getAbsolutePath() + "/DILATION_OK").exists()) continue;
			else if (!new File(patients[p].getAbsolutePath() + "/REGISTRATION_OK").exists()) continue;
			
			if (patients[p].isDirectory()){
				//read classified images
				File classifiedFolder = new File(patients[p].getAbsolutePath() + "/classified");
				if (classifiedFolder.exists()){
					File[] classified = classifiedFolder.listFiles();
					for (int k=0; k<classified.length; k++){
						int iteration = 0;
						while (iteration < timesToDo-1){
							System.out.println("Dilating " + classified[k].getParentFile().getParentFile().getName() + "'s " + classified[k].getName());
							inputImg = ImageIO.read(classified[k]);
							r = inputImg.getRaster();
							outImg = new BufferedImage(r.getWidth(), r.getHeight(), BufferedImage.TYPE_INT_RGB);
							outRaster = outImg.getRaster();
							for (int i=0; i<r.getHeight(); i++){
								for (int j=0; j<r.getWidth(); j++){
									if (i==0 || j ==0 || i==r.getHeight()-1 || j==r.getWidth()-1){
										for (int a=0; a<3; a++)
											outRaster.setSample(j, i, a, r.getSample(j, i, a));
									}else{
										boolean red = false, green = false, grey = false, yellow = false;
										int redValue = r.getSample(j, i, 0),
												greenValue = r.getSample(j, i, 1),
												blueValue = r.getSample(j, i, 2);
										red = redValue >= greenValue && redValue > blueValue;
										green = greenValue >= redValue && greenValue > blueValue;
										grey = (!red && !green);
										yellow = red && green;
										if (grey){
											//for the red fat
											int epiValue[] = new int[3];
											for (int a=0; a<3; a++)
												epiValue[a] = r.getSample(j-1, i, a) + r.getSample(j, i+1, a);
											
											red = epiValue[0] >= epiValue[1] && epiValue[0] > epiValue[2];
											green = epiValue[1] >= epiValue[0] && epiValue[1] > epiValue[2];
											grey = (!red && !green);
											
											if (red){
												outRaster.setSample(j, i, 0, r.getSample(j, i, 0));
											}else{
												//for the green fat
												epiValue = new int[3];
												for (int a=0; a<3; a++)
													epiValue[a] = r.getSample(j+1, i, a) + r.getSample(j, i+1, a);
												
												red = epiValue[0] >= epiValue[1] && epiValue[0] > epiValue[2];
												green = epiValue[1] >= epiValue[0] && epiValue[1] > epiValue[2];
												grey = (!red && !green);
												
												if (green){
													outRaster.setSample(j, i, 1, r.getSample(j, i, 1));
												}else{
													for (int a=0; a<3; a++)
														outRaster.setSample(j, i, a, r.getSample(j, i, a));
												}
											
											}
													
										}else{
											if (yellow){
												outRaster = correctYellow(r, outRaster, j, i);
											}else{
												for (int a=0; a<3; a++)
													outRaster.setSample(j, i, a, r.getSample(j, i, a));
											}
										}
									}
								}
							}
							ImageIO.write(outImg, "PNG", classified[k]);
							iteration ++;
						}
					}
				}
			}
			new File(patients[p].getAbsolutePath() + "/DILATION_OK").createNewFile();
		}
			
		
	}
	private static WritableRaster correctYellow(Raster original, WritableRaster out, int x, int y){
		int size = 6, red = 0, green = 0, grey = 0;
		for (int i=-size/2+y; i<size/2+y; i++){
			for (int j=-size/2+x; j<size/2+x; j++){
				if (j >= 0 && i >= 0 && j < original.getWidth() && i < original.getHeight()){
					if (original.getSample(j, i, 0) > original.getSample(j, i, 1) &&
							original.getSample(j, i, 0) > original.getSample(j, i, 2)){
						red ++;
					}
					else if (original.getSample(j, i, 1) > original.getSample(j, i, 0)
							&& original.getSample(j, i, 1) > original.getSample(j, i, 2)){
						green++;
					}else if (original.getSample(j, i, 0) == original.getSample(j, i, 1) &&
							original.getSample(j, i, 0) == original.getSample(j, i, 2)){
						grey ++;	
					}
				}
			}
		}
		if (red > (Math.pow(grey+green, 2))*0.9f && red > green)
			out.setSample(x, y, 0, original.getSample(x, y, 0));
		else if (green > (Math.pow(grey+red, 2))*0.9f && green > red)
			out.setSample(x, y, 1, original.getSample(x, y, 1));
		else{
			for (int a=0; a<3; a++)
				out.setSample(x, y, a, original.getSample(x, y, a));
		}
		
		return out;
	}
	
	
	
	
	public static void countVolume(String patientsFolderPath) throws Exception{
		
		if (generalWriter == null){
			generalWriter = new PrintWriter(new File(patientsFolderPath + "/generalResults.txt"));
		}
		
		File[] patients = new File(patientsFolderPath).listFiles();
		forP:
		for (int p=0; p<patients.length; p++){//for each patient
			
			//get the z, in mm
			File xmlFolder = new File(patients[p].getAbsolutePath() + "/xml");
			if (xmlFolder.exists() && patients[p].isDirectory()){
				DicomXML xml = new DicomXML(xmlFolder.listFiles()[0].getAbsolutePath());
				
				//taking a single slice
				String sliceThickness = null, spaceBetweenSlices = null;
				forK:
				for (int k=0; k<xmlFolder.listFiles().length; k++){
					xml = new DicomXML(xmlFolder.listFiles()[k].getAbsolutePath());
					boolean skip = false;
					spaceBetweenSlices = xml.getTagValue("00180088");
					sliceThickness = xml.getTagValue("00180050");
					if (spaceBetweenSlices == null && sliceThickness == null) {
						xml.dispose();
						continue forK;
					}
					if (sliceThickness != null && sliceThickness.length() > 100) {
						sliceThickness = null;
						skip = true;
					}
					if (spaceBetweenSlices != null && spaceBetweenSlices.length() > 100) {
						if (skip) continue forK;
					}
					
					break forK;
				}
				
				System.out.println("Counting volume for patient: " + patients[p].getName());
				
				String sliceSpacing = null;
				try{
					float sBS = -1, sT = -1;
					sBS = (spaceBetweenSlices != null && spaceBetweenSlices.length() < 100) ? Float.parseFloat(spaceBetweenSlices) : -1;
					sT = (sliceThickness != null && sliceThickness.length() < 100) ? Float.parseFloat(sliceThickness) : -1;
					sliceSpacing = (sT > 0 && sBS > 0) ? ((sBS > sT) ? spaceBetweenSlices : sliceThickness) : 
						((sT > 0) ? sliceThickness : spaceBetweenSlices);
					if (sliceSpacing == null) sliceSpacing = xml.getTagValue("00180050");
					if (sT == -1 && sBS == -1) throw new Exception();
				}catch (Exception e){
					System.out.println("Error when computing the Z space on patient " + patients[p].getName() + ", skipping.");
					continue forP;
				}
				
				/*
				if (xml.getTagValue("00180088") != null && xml.getTagValue("00180050") != null) {
					float space = Float.parseFloat(xml.getTagValue("00180088")),
							thick = Float.parseFloat(xml.getTagValue("00180050"));
					sliceSpacing = (space > thick) ? Float.toString(space) : Float.toString(thick);
				}*/
				//if (sliceSpacing == null) sliceSpacing = xml.getTagValue("3004000C");
				//String sliceSpacing
				float z = Float.parseFloat(sliceSpacing)/10f;
				//x e y = 0.35, padrão
				float x = 0.35f/10, y = 0.35f/10; //in cm
				
				
				//read classified images
				File[] classified = new File(patients[p].getAbsolutePath() + "/classified").listFiles();
				if (classified != null){
					BufferedImage inputImg = null;
					Raster r;
					PrintWriter writer;
					int red = 0, green = 0, grey = 0;
					int[] redPerSlice = new int[classified.length], greenPerSlice = new int[classified.length];
					for (int c=0; c<classified.length; c++){
						inputImg = ImageIO.read(classified[c]);
						r = inputImg.getRaster();
						for (int i=0; i<inputImg.getHeight(); i++){
							for (int j=0; j<inputImg.getWidth(); j++){
								if (r.getSample(j, i, 0) == r.getSample(j, i, 1) && r.getSample(j, i, 1) != r.getSample(j, i, 2)){//yellow
									red++;
									green++;
									redPerSlice[c] ++;
									greenPerSlice[c] ++;
								}else if (r.getSample(j, i, 0) > r.getSample(j, i, 1) && r.getSample(j, i, 0) > r.getSample(j, i, 2)){//red
									red++;
									redPerSlice[c] ++;
								}else if (r.getSample(j, i, 1) > r.getSample(j, i, 0) && r.getSample(j, i, 1) > r.getSample(j, i, 2)){//green
									green++;
									greenPerSlice[c] ++;
								}
								
								if (r.getSample(j, i, 0) == 0 && r.getSample(j, i, 1) == 0 && r.getSample(j, i, 2) == 0){
									grey ++;
								}
							}
						}
					}
					
					
					writer = new PrintWriter(new File(patients[p].getAbsolutePath() + "/results.txt"));
					println(writer, patients[p].getName() + "...");
					println(writer, "+----------------------------------+");
					println(writer, "Epicardial");
					println(writer, "+ Pixels amount: " + red);
					println(writer, "+ Normalized pixels amount: " + (float)red/(inputImg.getWidth()*inputImg.getHeight()*classified.length));
					println(writer, "+ Total volume (ml|cm³): " + red*x*y*z);
					println(writer, "+ Mean volume per slice (ml|cm³): " + red*x*y*z/classified.length);
					println(writer, "+ Normalized volume: " + red*x*y*z/(inputImg.getWidth()*inputImg.getHeight()*classified.length*x*y*z));
					println(writer, "+ Normalized volume (regarding only fat): " + red*x*y*z/(grey*x*y*z));
					println(writer, "+----------------------------------+");
					println(writer, "Mediastinal");
					println(writer, "+ Pixels amount: " + green);
					println(writer, "+ Normalized pixels amount: " + (float)green/(inputImg.getWidth()*inputImg.getHeight()*classified.length));
					println(writer, "+ Total volume (ml|cm³): " + green*x*y*z);
					println(writer, "+ Mean volume per slice (ml|cm³): " + green*x*y*z/classified.length);
					println(writer, "+ Normalized volume: " + green*x*y*z/(inputImg.getWidth()*inputImg.getHeight()*classified.length*x*y*z));
					println(writer, "+ Normalized volume (regarding only fat): " + green*x*y*z/(grey*x*y*z));
					println(writer, "+----------------------------------+");
					println(writer, "");
					writer.close();
				}
			}
		}
		
		generalWriter.close();
	}
	
	private static PrintWriter generalWriter = null;
	private static void println(PrintWriter writer, String s){
		generalWriter.println(s);
		writer.println(s);
		System.out.println(s);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
}