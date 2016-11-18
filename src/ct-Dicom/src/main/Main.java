package main;

import globals.FileComparator;
import globals.Vector2;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.dcm4che2.tool.dcm2xml.Dcm2Xml;

import registration.Marker;
import registration.Registration;

import dicom.DicomXML;




public abstract class Main {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		//folder de um paciente
		//String patientspath = "C:\\Users\\Érick\\Documents\\Aura\\CT\\Novos Pacientes - Calcio3 - PSpacing\\";
		String patientspath = "H:/etapa3";
		
		File newdir;
		File folder = new File(patientspath);

		//fazer para todos os pacientes dentro de uma pasta
		File[] patientfolder = folder.listFiles(); 
		Arrays.sort(patientfolder, new FileComparator());
		for (int f=0; f<patientfolder.length; f++){
			String[] ok = patientfolder[f].getName().split("_");
			boolean skip = false;
			if (ok.length > 1)
				skip = (ok[1].equals("OK")) ? true : false;
			if (!patientfolder[f].isDirectory()) 
				skip = true;
			
			if (!skip){
				File dicomfolder = new File(patientfolder[f].getAbsolutePath() + "\\DICOM\\");
				File[] files = dicomfolder.listFiles();	
				//converter para xml
				
				//continuar
				String[] parsedargs = new String[3];
				if (files != null){
					Arrays.sort(files, new FileComparator());
					
					for (int a=0; a<files.length; a++){
						parsedargs[0] = files[a].getPath();
						parsedargs[1] = "-o"; //write output file
						newdir = new File(patientfolder[f].getAbsolutePath() + "\\xml\\");
						if (!newdir.exists()) newdir.mkdir();
						parsedargs[2] = patientfolder[f].getAbsolutePath() + "\\xml\\" +files[a].getName() + ".xml";
						Dcm2Xml.main(parsedargs);
					}
					
					
					//image convertion and registration
				
					
					//converter para imagem
					BufferedImage outimg = null; WritableRaster raster = null;
					DicomXML newdcm = null;
					String fformat = "PNG"; //output file format
					
					AffineTransform patientAf = null;
					for (int a=0; a<files.length; a++){
						newdcm = new DicomXML(patientfolder[f].getAbsolutePath() + "\\xml\\" + files[a].getName() + ".xml");
						
						//tem que estar antes do loop abaixo
						//newdcm.setPixelSpacing(0.4f, 0.4f);
						
						//primeira entrada no loop
						if (patientAf == null){
							System.out.println("Attempting to recognize the retrosternal area...");
							AffineTransform resizedAf;
							int numSlice = (int) ((files.length > 45) ? files.length - 45: 0);
							//pegar o marcador na quinta imagem do paciente
							DicomXML newdcm2 = new DicomXML(patientfolder[f].getAbsolutePath() + "\\xml\\" + files[numSlice].getName() + ".xml");
							newdcm2.setPixelSpacing(0.35f, 0.35f);
							Vector2 atlasStdPosition = new Vector2(70, 120);
							
							patientAf = newdcm2.getRegisteredFatImageTransformation(atlasStdPosition, 
									ImageIO.read(new File("C:\\Users\\Érick\\Documents\\Aura\\CT\\Registro\\combinedatlas3.png")),
									new File(patientfolder[f] + "\\priorly.png"));
							resizedAf = newdcm2.getResizedFatImageTransformation(atlasStdPosition, 
									ImageIO.read(new File("C:\\Users\\Érick\\Documents\\Aura\\CT\\Registro\\combinedatlas3.png")));
							//retirar dps
							Marker novo = new Marker(newdcm2.getTransformedFatImage(patientAf ),
									ImageIO.read(new File("C:\\Users\\Érick\\Documents\\Aura\\CT\\Registro\\combinedatlas3.png")));
							novo.setReferenceImgThrehsold(10);
							novo.setPos(atlasStdPosition);
							//se o translado for maior que 1/4 da largura ou altura então deixa pra lá
							if (Math.abs(patientAf.getTranslateX() - resizedAf.getTranslateX()) > (newdcm2.getWidth())/3f
									|| Math.abs(patientAf.getTranslateY() - resizedAf.getTranslateY()) > (newdcm2.getHeight())/3f){
								novo.setReferenceImage(newdcm2.getTransformedFatImage(resizedAf));
								patientAf = resizedAf;
							}
							Registration.confirmAndDraw(novo, new Vector2(100, 280), 280);
							//outputting img
								
							ImageIO.write(novo.getReferenceImage(), "PNG", new File(
									patientfolder[f].getAbsolutePath()+"\\posteriorly.png"));
							//fim retirar
							newdcm2 = null;
						}
						
						outimg = newdcm.getTransformedFatImage(patientAf);
						//outimg = newdcm.getTransformedImageOnRange(patientAf, -200, -30 );
						
						/*
						//criar uma nova imagem com o range pré-definido
						outimg = new BufferedImage(newdcm.getWidth(), newdcm.getHeight() );
						raster = outimg.getRaster();
						
						
						
						short[][] fatimg = new short[newdcm.getHeight()][newdcm.getWidth()], fluidimg = new short[newdcm.getHeight()][newdcm.getWidth()];
						double hvalue;
						int count = 0;
						//fluid range = -29 ~ 35
						final short hinf = -200, hsup = -30, minvalue = 0, maxvalue = 255,
								f_hinf = -29, f_hsup = 35;
						for (int i=0; i<newdcm.getHeight(); i++){
							for (int j=0; j<newdcm.getWidth(); j++){
								hvalue = newdcm.getHounsfieldValue(j, i);
								
								/*
								//under [-200,-30] hounsfield range - a shift on the values
								if (hvalue >= -200 && hvalue <= -30)
									raster.setSample(j, i, 0, 260 + hvalue);
								else
									raster.setSample(j, i, 0, 0);
								*-/
								
								
								/* ANTIGO
								//linearly transformation (expanding) - com contraste
								final double a_f = (maxvalue - minvalue)/(hsup - hinf), b_f = maxvalue - hsup*a_f;
								if (hvalue >= hinf && hvalue <= hsup)
									raster.setSample(j, i, 0, (a_f*hvalue) + b_f);
									*-/
								
								//linearly transformation (expanding) - com contraste
								final double a_f = (maxvalue - minvalue)/(hsup - hinf), b_f = maxvalue - hsup*a_f;
								if (hvalue >= hinf && hvalue <= hsup)
									fatimg[i][j] = (short) ((a_f*hvalue) + b_f);
								final double f_a_f = (maxvalue - minvalue)/(f_hsup - f_hinf), f_b_f = maxvalue - f_hsup*f_a_f;
								if (hvalue >= f_hinf && hvalue <= f_hsup)
									fluidimg[i][j] = (short) ((f_a_f*hvalue) + f_b_f);
									
			
							}
					
							//Andamento
							count += newdcm.getWidth();
							if (i % 10 == 0) System.out.println("Conversion of file: " + files[a].getName() + "... " + (count*100)/(newdcm.getHeight() * newdcm.getWidth()) + "% ...");
						}
						*/
						
						
						//criar diretório de imagens se não houver
						newdir = new File(patientfolder[f].getAbsolutePath() + "/imagens/");
						if (!newdir.exists()) newdir.mkdir();
						
						
						//salvando em range maior a imagem, se dentro da pasta do paciente já estiver presente a pasta "segmentadas"
						String segmentedPath = patientfolder[f].getAbsolutePath()  + "/segmentadas/", classifiedPath = patientfolder[f].getAbsolutePath()  + "/classified/";
						if (new File(patientfolder[f].getAbsolutePath() + "/segmentadas/").exists()){
							FatToHigherRange.writeColoredHigherRange(a, patientfolder[f].getAbsolutePath(), segmentedPath, patientAf, null, null);
						}else if (new File(patientfolder[f].getAbsolutePath() + "/classified/").exists()){
							FatToHigherRange.writeColoredHigherRange(a, patientfolder[f].getAbsolutePath(), classifiedPath, patientAf, null, null);
						}
						
						//salvando só o range -200, 500
						for (int k=0; k<files.length; k++){
							DicomXML dicomFile = new DicomXML(patientfolder[f].getAbsolutePath() + "\\xml\\" + files[k].getName() + ".xml");
							newdir = new File(patientfolder[f].getAbsolutePath() + "/imagens2/");
							if (!newdir.exists()) newdir.mkdir();
							ImageIO.write(dicomFile.getTransformedImageOnRange(patientAf, -200, 500), 
									fformat, new File(patientfolder[f].getAbsolutePath() + "/imagens2/" + files[k].getName() + "." + fformat));
						}
	
						final String filename = (files[a].getName().contains(".")) ? files[a].getName().substring(0, files[a].getName().length() - 4) : files[a].getName();
						ImageIO.write(outimg, fformat, new File(patientfolder[f].getAbsolutePath() + "\\imagens\\" + filename + "." + fformat));
						System.out.println("Finished transforming " + patientfolder[f].getName() + "'s file: " +  files[a].getName() + " ...");
					
						
					}
				
				//saving transformation to file
				FileOutputStream fout = new FileOutputStream(patientfolder[f].getAbsolutePath() + "/AffineTransform.obj");
				ObjectOutputStream oos = new ObjectOutputStream(fout);
				oos.writeObject(patientAf);
				oos.close();
				fout.close();
					
				//dar ok no diretório do paciente - não tá funcionando não sei pq
				patientfolder[f].renameTo(new File(patientfolder[f].getAbsolutePath() + "_OK"));
				
				}//fim do se tiver arquivos dentro do diretório
			}
		}
		
	}
	
	
	public static void printFilesForFolder(final File folder) {
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            printFilesForFolder(fileEntry);
	        } else {
	            System.out.println(fileEntry.getName());
	        }
	    }
	}
	
	

		
}
