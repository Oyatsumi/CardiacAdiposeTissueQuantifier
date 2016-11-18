package main;


import globals.FileComparator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import weka.filters.unsupervised.attribute.Remove;
import datamining.Classifier;



public abstract class DmStaticAccess {
	private static String folderPath = "";
	//private static final String folderpath = "C:\\Users\\Érick\\Documents\\Aura\\CT\\NoiseReduction\\Novo teste - Features\\NoiseDataMining\\apenassegmentadas";
	private static File folder = null;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void extractDatabase() throws IOException {
		
		//keyboard IO
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Insira o tamanho da janela:");
		int windowSize = Integer.parseInt(br.readLine());
		
		//janela para extração das features
		System.out.println("Quantidade de pixels para dar skip (quando não achar pixels em cinza):");
		String currentInput = br.readLine();
		int jumpWindowSize = (currentInput.length() != 0) ? Integer.parseInt(currentInput): 1;
		
		System.out.println("Quantidade de pixels para dar skip (quando achar pixels em cinza):");
		currentInput = br.readLine();
		int jumpWindowSize2 = (currentInput.length() != 0) ? Integer.parseInt(currentInput) : 1;
		
		System.out.println("Quantidade de slices pra dar skip:");
		currentInput = br.readLine();
		int jumpSlicesSize = (currentInput.length() != 0) ? Integer.parseInt(currentInput) : 1;
		
		System.out.println("Speed boost? 0=false, 1=true:");
		int tof = Integer.parseInt(br.readLine());
		boolean speedBoost;
		if (tof == 1) speedBoost = true;
		else speedBoost = false;
		
		System.out.println("Diretorio do arquivo:");
		folderPath = br.readLine();
		
		System.out.println("Nome do arquivo:");
		String nomeArq = br.readLine();
		
				
		br.close();
		
		
		folder = new File(new File(folderPath).getAbsolutePath());
		

		
		
		TBuildClassifyingModel.setParameters(folder, nomeArq, windowSize, jumpWindowSize, jumpWindowSize2, speedBoost);
		short cores = (short) Runtime.getRuntime().availableProcessors();
		File[] patients = folder.listFiles();
		Arrays.sort(patients, new FileComparator());
		System.out.println("Processing patients at folder " + folder.getAbsolutePath());

		
		//for each patient
		System.out.println("A total of " + cores + " threads have been created.");
		for (int p=0; p<patients.length; p++){
			File sFolder = new File(patients[p].getAbsolutePath() + "/segmentadas/");
			if (sFolder != null){
				File[] files = sFolder.listFiles();
				if (files != null ) 
					Arrays.sort(files, new FileComparator());
				int length = 0;
				if (!sFolder.isDirectory()) length = 0;
				else length = files.length;
				
				//for each image
				TBuildClassifyingModel[] cm = new TBuildClassifyingModel[cores];
				for (int a=0; a<length; a+=jumpSlicesSize){
					boolean bounded = false;
					while (!bounded){
						for (int c=0; c<cores; c++){
							boolean dontBypass = cm[c] == null ? true : !cm[c].isAlive();
							if (dontBypass){
								int sliceId = Integer.parseInt(files[a].getName().split("\\.")[0]);
								if (sliceId != a) sliceId = a; 
								cm[c] = new TBuildClassifyingModel(sliceId
										, patients[p], files[a]);
								cm[c].setName(Integer.toString(c));
								cm[c].start();
								bounded = true;
								c=cores;
							}
						}
					}
				}
			}
		}
		if (TBuildClassifyingModel.out != null) 
			TBuildClassifyingModel.out.close();
		
		br.close();
		
	}
	
	
	
	public static void classify() throws Exception{
		
		//keyboard IO
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Insira o tamanho da janela:");
		int windowSize = Integer.parseInt(br.readLine());

		
		//janela para extração das features
		System.out.println("Quantidade de pixels para dar skip:");
		String currentInput = br.readLine();
		int jumpWindowSize = (currentInput.length() != 0) ? Integer.parseInt(currentInput): 1;
		
		//pular quantos slices
		System.out.println("Quantidade de slices para dar skip:");
		currentInput = br.readLine();
		int sliceJump = (currentInput.length() != 0) ? Integer.parseInt(currentInput): 1;
		
		System.out.println("Diretorio dos pacientes:");
		folderPath = br.readLine();
		
		System.out.println("Caminho do arff:");
		String arffPath = br.readLine();
				
		br.close();
		
		classify(windowSize, jumpWindowSize, sliceJump, folderPath, arffPath);

	}
	

	public static void classify(int windowSize, int pixelJump, int sliceJump, String patientsFolder, String arffPath) throws Exception{
		folder = new File(new File(patientsFolder).getAbsolutePath());
		File[] patients = folder.listFiles();
		Arrays.sort(patients, new FileComparator());
	
		
		short cores = (short)(Runtime.getRuntime().availableProcessors() * 2);
		System.out.println("Processing patients at folder " + folder.getAbsolutePath());

		
		//red filter options
		String[] redOp = new String[2];
		redOp[0] = "-R";
		redOp[1] = "2,3";
		Remove redR = new Remove();
		redR.setOptions(redOp);
		//green filter op
		String[] greenOp = new String[2];
		greenOp[0] = "-R";
		greenOp[1] = "1,3";
		Remove greenR = new Remove();
		greenR.setOptions(greenOp);
		//blue filter op
		String[] blueOp = new String[2];
		blueOp[0] = "-R";
		blueOp[1] = "1,2";
		Remove blueR = new Remove();
		blueR.setOptions(blueOp);


		
		//novo classificador
		//final String parameters = "-M 2 -V 0.001 -N 3 -S 1 -L -1"; //REP TREE
		//final String parameters = "-F 0 -L 1.0E-4 -E 500"; //SPegasos
		final String parameters = "-I 10 -K 0 -S 1"; //random Forest
		//final String parameters = "-C 0.25 -M 2"; //J48
		//final String parameters = "-C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\""; //SMO
		//final String parameters = "-S 1 -M 2.0 -N 5 -C 1.0"; //simple cart
		//final String parameters = "-B 2 -S 1 -R 1.0E-8 -M -1 -W 0.1"; //RBFNet
		//final String parameters = "-C 0.25 -M 2"; //J48Graf
		Classifier[] classifiers = new Classifier[3];
		classifiers[0] = new Classifier("redModel", arffPath, weka.classifiers.trees.RandomForest.class, parameters, redR);
		classifiers[1] = new Classifier("greenModel", arffPath, weka.classifiers.trees.RandomForest.class, parameters, greenR);
		classifiers[2] = new Classifier("blueModel", arffPath, weka.classifiers.trees.RandomForest.class, parameters, blueR);

		
		TClassify.setParameters(classifiers, new File(arffPath), windowSize, pixelJump);
		
		//how many patients
		short count = 0;
		for (int p=0; p<patients.length; p++){
			File imagesFolder = new File(patients[p].getAbsolutePath() + "/imagens/");
			if (imagesFolder != null){
				count ++;	
			}
		}
			
		//for each patient
		short availableQntd = 0;
		System.out.println("A total of " + cores + " threads have been created.");
		TClassify[] cm = new TClassify[cores];
		File imagesFolder, xmlFolder;
		File[] files;
		forP:
		for (int p=0; p<patients.length; p++){
			imagesFolder = new File(patients[p].getAbsolutePath() + "/imagens/");
			xmlFolder = new File(patients[p].getAbsolutePath() + "/xml/");
			if (imagesFolder != null && xmlFolder != null){
				if (imagesFolder.listFiles() == null || xmlFolder.listFiles() == null) continue forP;
				if (imagesFolder.listFiles().length >= xmlFolder.listFiles().length
						|| new File(patients[p].getAbsolutePath() + "/REGISTRATION_OK").exists()){//if already processed
					System.out.println("Total progress... " + (int) ((availableQntd/count) * 100));
					availableQntd ++;
					files = imagesFolder.listFiles();
					if (files != null) 
						Arrays.sort(files, new FileComparator());
					int length = 0;
					if (!imagesFolder.isDirectory()) length = 0;
					else length = files.length;
					
					String sliceNumber = "";
					//for each image
					for (int a=0; a<length; a+=sliceJump){
						//concurrent image processing flags
						//flag = new File(files[a].getAbsolutePath() + ".flag");
						//if (flag.exists())
							//continue;
						//
						sliceNumber = "";
						for (int k=0; k<files[a].getName().length(); k++)
							if (Character.isDigit(files[a].getName().charAt(k))) sliceNumber += files[a].getName().charAt(k);
						
						boolean bounded = false;
						while (!bounded){
							for (int c=0; c<cores; c++){
								boolean dontBypass = cm[c] == null ? true : !cm[c].isAlive();
								if (dontBypass){
									bounded = true;
									cm[c] = new TClassify(Integer.parseInt(sliceNumber), files[a]);
									cm[c].setName(Integer.toString(c));
									cm[c].start();
									c=cores;
								}
							}
						}
						System.gc();
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
		
	}
	
	
	private static boolean deleteDirectory(File directory) {
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
	            for(int i=0; i<files.length; i++) {
	                if(files[i].isDirectory()) {
	                    deleteDirectory(files[i]);
	                }
	                else {
	                    files[i].delete();
	                }
	            }
	        }
	    }
	    return(directory.delete());
	}


}
