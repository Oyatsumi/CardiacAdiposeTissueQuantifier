package main;

import globals.Vector2;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.imageio.ImageIO;

import org.dcm4che2.tool.dcm2xml.Dcm2Xml;

import registration.Marker;
import registration.Registration;

import dicom.DicomXML;

public class TConvert extends Thread{
	private static File movingImg;
	private boolean convertDicom = true;
	private File dicomFile = null;
	private String patientPath = "", imageFormat = "";
	
	public static void setMovingImage(File movingImage){
		TConvert.movingImg = movingImage;
	}
	
	public TConvert(){
		
	}
	public TConvert(String patientPath, String imageFormat){
		convertDicom = false;
		this.patientPath = patientPath;
		this.imageFormat = imageFormat;
	}
	public TConvert(File dicomFile, String patientPath){
		this.setDicomFile(dicomFile);
		this.patientPath = patientPath;
		convertDicom = true;
	}
	public void setDicomFile(File dicomFile){
		this.dicomFile = dicomFile;
	}
	public void setPatientFolderPath(String patientPath){
		this.patientPath = patientPath;
	}

	@SuppressWarnings("finally")
	public void run (){
		if (convertDicom){//convert dicom to xml
			try{
				String extension = (dicomFile.getPath().substring(dicomFile.getPath().length()-4, dicomFile.getPath().length())).toLowerCase();
				if (!extension.equals(".ima") && !extension.equals(".dcm") && extension.contains(".")) return;
				
				System.out.println("Thread "+this.getName() + ": converting DICOM file " + dicomFile.getName());
				String[] parsedArgs = new String[3];
				parsedArgs[0] = dicomFile.getPath();
				parsedArgs[1] = "-o"; //write output file
				File newdir = new File(patientPath + "/xml/");
				if (!newdir.exists()) newdir.mkdir();
				newdir = null;
				parsedArgs[2] = patientPath + "/xml/" + dicomFile.getName() + ".xml";
				Dcm2Xml.main(parsedArgs);
			}catch(Exception e){
				System.out.println("Error reading DICOM when trying to convert to XML");
			}
		}else{
			String patientName = new File(patientPath).getName();
			
			//converter para imagem
			BufferedImage outImg = null;
			DicomXML newDcm = null;
			
			AffineTransform patientAf = null;
			File xmlFolder = new File(patientPath + "/xml/"), newdir;
			File[] xmlFiles = xmlFolder.listFiles();
			xmlFor:
			for (int a=0; a<xmlFiles.length; a++){
				newDcm = new DicomXML(xmlFiles[a].getAbsolutePath());
				
				
				String fileName = (xmlFiles[a].getName().contains(".")) ? xmlFiles[a].getName().substring(0, xmlFiles[a].getName().length() - 4) : xmlFiles[a].getName();
				DicomXML auxDcm2 = new DicomXML(xmlFiles[a].getAbsolutePath());
				boolean convertedExists = false;
				String fileName2 = "";
				try {
					fileName2 = auxDcm2.getTagValue("00200013");
					if (fileName2 == null) continue;
					while (fileName2.length() < 4) fileName2 = "0" + fileName2;
				} catch (Exception e1) {
					//e1.printStackTrace();
					continue xmlFor;
				}
				convertedExists = new File(patientPath + "/imagens/" + fileName2 + "." + imageFormat).exists();
				
				try{
					//primeira entrada no loop
					if (patientAf == null){
						System.out.println("Thread " + this.getName() + ": Attempting to recognize the retrosternal area of " + patientName + "...");
						AffineTransform resizedAf;
						int numSlice = (int) ((xmlFiles.length > 45) ? Math.pow((xmlFiles.length - 40),1/2f): 2);
						if (numSlice < 2) numSlice = 2;
						//pegar o marcador na quinta imagem do paciente
						//String xmlName = (xmlFiles[numSlice].getName().contains("xml"))? xmlFiles[numSlice].getName() : xmlFiles[numSlice].getName() + ".xml";
						String xmlName = xmlFiles[numSlice].getName();
						DicomXML auxDcm = null;
						int previousNum = Integer.MAX_VALUE, currentNum;
						forT:
						for (int t=0; t<2; t++){
							//if (ok) break;
							forK:
							for (int k=0; k<xmlFiles.length; k++){
								auxDcm = new DicomXML(xmlFiles[k].getAbsolutePath());
								if (auxDcm.getTagValue("00200013") == null ||
										auxDcm.getTagValue("00281053") == null || auxDcm.getTagValue("00281053").length() > 100 || 
										auxDcm.getTagValue("00280030") == null ) 
									continue forK;
								currentNum = Integer.parseInt(auxDcm.getTagValue("00200013"));
								if (currentNum == numSlice
										|| (t == 1)){//pega o primeiro possivel
									if (currentNum < previousNum && currentNum >= 2) {
										xmlName = xmlFiles[k].getName();
										previousNum = currentNum;
										if (t == 0) break forT;
									}
									//ok = true;
									//break;
								}
							}
						}
						if (auxDcm != null) auxDcm.dispose(); auxDcm = null;
						DicomXML newdcm2 = new DicomXML(patientPath + "/xml/" + xmlName);
						newdcm2.setPixelSpacing(0.35f, 0.35f);
						fileName = newdcm2.getTagValue("00200013"); //instance number name
						Vector2 atlasStdPosition = new Vector2(70, 120);
						
						
						BufferedImage atlas;
						//internal png
						if (!movingImg.getAbsolutePath().contains("!")) 
							atlas = ImageIO.read(movingImg);
						else
							atlas = ImageIO.read(this.getClass().getClassLoader().getResource(movingImg.getName()));
						
						//save images previously and after the registration
						patientAf = newdcm2.getRegisteredFatImageTransformation(atlasStdPosition, 	atlas,
								new File(patientPath + "/priorly." + imageFormat));
						resizedAf = newdcm2.getResizedFatImageTransformation(atlasStdPosition, atlas);
						//retirar dps se quiser - outputa a imagem posteriorly
						Marker novo = new Marker(newdcm2.getTransformedFatImage(patientAf),	atlas);
						novo.setReferenceImgThrehsold(10);
						novo.setPos(atlasStdPosition);
						//se o translado for maior que 1/3 da largura ou altura então deixa pra lá
						if (Math.abs(patientAf.getTranslateX() - resizedAf.getTranslateX()) > (newdcm2.getWidth())/3f
								|| Math.abs(patientAf.getTranslateY() - resizedAf.getTranslateY()) > (newdcm2.getHeight())/3f){
							novo.setReferenceImage(newdcm2.getTransformedFatImage(resizedAf));
							patientAf = resizedAf;
						}
						Registration.confirmAndDraw(novo, new Vector2(95, 280), 280);
						//outputting img
							
						ImageIO.write(novo.getReferenceImage(), imageFormat, new File(
								patientPath+"/posteriorly."+imageFormat));
						//fim retirar
						newdcm2.dispose();
						newdcm2 = null;
					}
					
  					if (!convertedExists)
  						outImg = newDcm.getTransformedFatImage(patientAf);
					newDcm.dispose();
					
				}catch (Exception e){
					System.out.println("Thread " + this.getName() + ": Error when reading or writing images or DICOM files.");
					e.printStackTrace(System.out);
					try{newDcm.dispose();}catch(Exception e2){}
					convertedExists = true;
					continue xmlFor;
				}
				
				//criar diretório de imagens se não houver
				newdir = new File(patientPath + "/imagens/");
				if (!newdir.exists()) newdir.mkdir();
				
				
				//salvando em range maior a imagem, se dentro da pasta do paciente já estiver presente a pasta "segmentadas" ou "classified"
				String segmentedPath = patientPath + "/segmentadas/", classifiedPath = patientPath + "/classified/";
				if (new File(segmentedPath).exists()){
					FatToHigherRange.writeColoredHigherRange(a, patientPath, segmentedPath, patientAf, null, null);
				}else if (new File(classifiedPath).exists()){
					FatToHigherRange.writeColoredHigherRange(a, patientPath, classifiedPath, patientAf, null, null);
				}

				if (!convertedExists){//if not converted already
					//Save transformed image
					try {
						fileName = auxDcm2.getTagValue("00200013");
						auxDcm2.dispose(); auxDcm2 = null;
						//if (fileName == null) fileName = "Z" + a;
						while (fileName.length() < 4 && fileName != null) fileName = "0" + fileName;
						if (outImg != null && fileName != null)
							ImageIO.write(outImg, imageFormat, new File(patientPath + "/imagens/" + fileName + "." + imageFormat));
					} catch (Exception e) {
						System.out.println("Thread " + this.getName() + ": Error when saving registered image.");
						System.out.println(e.getMessage());
						e.printStackTrace();
						continue;
					}
					System.out.println("Thread " + this.getName() + ": Finished transforming " + patientName + "'s file: " +  xmlFiles[a].getName() + " ...");
				}
				
			}
		
			//saving transformation to file
			try{
				if (new File(patientPath + "/AffineTransform.obj").exists()) return;
				FileOutputStream fout = new FileOutputStream(patientPath + "/AffineTransform.obj");
				ObjectOutputStream oos = new ObjectOutputStream(fout);
				oos.writeObject(patientAf);
				oos.close();
				fout.close();
			}catch(Exception e){
				System.out.println("Thread " + this.getName() + ": Error when saving the transformation object on " + patientName + "'s folder.");
				System.out.println(e.getMessage());
			}
		}
		
	}
	
	
	
	
}
