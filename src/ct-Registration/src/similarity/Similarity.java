package similarity;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import registration.Marker;

import globals.Vector3;


public abstract class Similarity {
	private static Marker atlas;
	public static int movingImgMaxIntensity = 0, fixedImgMaxIntensity = 0, movingImgMeanIntensity = 0, fixedImgMeanIntensity = 0;
	public static Vector3 threshold = new Vector3(100, 100, 0);
	
	public static void setatlas(Marker m){
		Similarity.atlas = m;
		Similarity.movingImgMaxIntensity = atlas.getatlasMaximumIntensity(atlas.getIntensityLayer()); //2 = blue band
		Similarity.fixedImgMaxIntensity = (int) Math.pow(2, atlas.getReferenceBitDepth()) - 1;
		Similarity.movingImgMeanIntensity = atlas.getatlasMeanIntensity(atlas.getIntensityLayer());
		Similarity.fixedImgMeanIntensity = atlas.getMeanIntensity();
	}
	
	//the higher the better (maximization)
	public static long hybridSumOfDifferences(short[][] fixedImg, short[][] movingImg, float g){
		long blueScore = 0;
		for (int i=0; i<fixedImg.length; i++){
			for (int j=0; j<fixedImg[0].length; j++){
				int delta = fixedImg[i][j],
						movingSample = (int) (movingImg[i][j]*(fixedImgMaxIntensity/movingImgMaxIntensity));
				long blueCounter = 1;
				if (fixedImg[i][j] > -1){// != -1 equals within fixed image boundaries
					if (movingSample > threshold.z){
						delta -= (1 + fixedImgMaxIntensity)/(1 + movingSample);
						if (delta < 0) delta = 0;
						for (byte a=0; a<g; a++) blueCounter *= delta;
						//blueCounter = delta*delta*delta;
						blueCounter = Math.abs(blueCounter);
					}else{
						delta -= movingSample;
						for (byte a=0; a<g; a++) blueCounter *= delta;
						//blueCounter = delta*delta*delta;
						blueCounter = -Math.abs(blueCounter);
					}
				}else{//if the atlas is out of the fixed image boundaries
					for (byte a=0; a<g; a++) blueCounter *= fixedImgMaxIntensity;
					//blueCounter = maxIntensity*maxIntensity*maxIntensity;
					blueCounter = -blueCounter;
				}
				
				//hybric mean error
				blueScore += blueCounter;

			}
		}
		
		return blueScore;
	}
	//maximization
	public static double normalizedCrossCorrelation(short[][] fixedImg, short[][] movingImg){
		double numerator = 0,
				denominator1 = 0, denominator2 = 0;
		for (int i=0; i<fixedImg.length; i++){
			for (int j=0; j<fixedImg[0].length; j++){
				numerator += (fixedImg[i][j]-fixedImgMeanIntensity)*(movingImg[i][j]-movingImgMeanIntensity);
				denominator1 += Math.pow((fixedImg[i][j]-fixedImgMeanIntensity), 2);
				denominator2 += Math.pow((movingImg[i][j]-movingImgMeanIntensity), 2);
			}
		}
		
		return Math.abs(numerator/(Math.pow(denominator1*denominator2,1/2f)));
	}
	
	public static long sumOfDifferences(short[][] fixedImg, short[][] movingImg, float g){
		long sum = 0;
		for (int i=0; i<fixedImg.length; i++){
			for (int j=0; j<fixedImg[0].length; j++){
				sum += Math.abs(Math.pow(fixedImg[i][j]-movingImg[i][j], g));
			}
		}
		return sum;
	}
	
	public static double mutualInformation(short[][] fixedImg, short[][] movingImg, String logBase){
		return weightedMutualInformation(fixedImg, movingImg, logBase, false);
	}
	private static TreeMap<Short, TreeMap<Short, Double>> mutualOccurrence = null;
	private static TreeMap<Short, Double> fixedOccurrence = null, movingOccurrence = null;
	public static double weightedMutualInformation(short[][] fixedImg, short[][] movingImg, String logBase, boolean weighted){
		mutualOccurrence = new TreeMap<Short, TreeMap<Short, Double>>();
		fixedOccurrence = new TreeMap<Short, Double>();
		movingOccurrence = new TreeMap<Short, Double>();
		TreeMap<Short, Double> aux = new TreeMap<Short, Double>();
		double count = 0;
		for (int i=0; i<fixedImg.length; i++){
			for (int j=0; j<fixedImg[0].length; j++){
				//mutual occurrence
				if (!mutualOccurrence.containsKey(fixedImg[i][j])){
					aux = new TreeMap<Short, Double>();
					mutualOccurrence.put(fixedImg[i][j], aux);
				}
				count = 0;
				aux = mutualOccurrence.get(fixedImg[i][j]);
				if (aux.containsKey(movingImg[i][j])){
					count = aux.get(movingImg[i][j]);
					aux.remove(movingImg[i][j]);
				}
				aux.put(movingImg[i][j], (count + 1));
				
				
				//fixed occurrence
				if (!fixedOccurrence.containsKey(fixedImg[i][j])){
					fixedOccurrence.put(fixedImg[i][j], (double) 0);
				}
				double value = fixedOccurrence.get(fixedImg[i][j]);
				fixedOccurrence.remove(fixedImg[i][j]);
				fixedOccurrence.put(fixedImg[i][j], (value + 1));
				
				//moving occurrence
				if (!movingOccurrence.containsKey(movingImg[i][j])){
					movingOccurrence.put(movingImg[i][j], (double) 0);
				}
				value = movingOccurrence.get(movingImg[i][j]);
				movingOccurrence.remove(movingImg[i][j]);
				movingOccurrence.put(movingImg[i][j], (value + 1));
				
			}
		}
		

		//divide by the overall number of occurrences to get the probability
		ArrayList<Short> firstValues = new ArrayList<Short>();
		ArrayList<ArrayList<Short>> secondValues = new ArrayList<ArrayList<Short>>();
		for (Entry <Short, TreeMap<Short, Double>> entry : mutualOccurrence.entrySet()){
			firstValues.add(entry.getKey());
			secondValues.add(new ArrayList<Short>());
		}
		int totalMutualOccurrencies = 0;
		for (int k=0; k<firstValues.size(); k++){
			for (Entry <Short, Double> entry : mutualOccurrence.get((short)firstValues.get(k)).entrySet()){
				secondValues.get(k).add(entry.getKey());
				totalMutualOccurrencies += entry.getValue();
			}
		}
		for (int l=0; l<firstValues.size(); l++){
			for (int m=0; m<secondValues.get(l).size(); m++){
				double value = mutualOccurrence.get(firstValues.get(l)).get(secondValues.get(l).get(m));
				mutualOccurrence.get(firstValues.get(l)).remove(secondValues.get(l).get(m));
				mutualOccurrence.get(firstValues.get(l)).put(secondValues.get(l).get(m), (double)value/(totalMutualOccurrencies));
			}
		}
		ArrayList<Short> fixedHues = new ArrayList<Short>();
		int totalFixedOccurrencies = 0;
		for (Entry<Short, Double> entry : fixedOccurrence.entrySet()){
			 fixedHues.add(entry.getKey());
			 totalFixedOccurrencies += entry.getValue();
		}
		for (int k=0; k<fixedHues.size(); k++){
			double value = fixedOccurrence.get(fixedHues.get(k));
			fixedOccurrence.remove(fixedHues.get(k));
			fixedOccurrence.put(fixedHues.get(k), (double)value/(totalFixedOccurrencies));
		}
		ArrayList<Short> movingHues = new ArrayList<Short>();
		int totalMovingOccurrencies = 0;
		for (Entry<Short, Double> entry : movingOccurrence.entrySet()){
			movingHues.add(entry.getKey());
			totalMovingOccurrencies += entry.getValue();
		}
		for (int k=0; k<movingHues.size(); k++){
			double value = movingOccurrence.get(movingHues.get(k));
			movingOccurrence.remove(movingHues.get(k));
			movingOccurrence.put(movingHues.get(k), (double)value/(totalMovingOccurrencies));
		}
		
		
		//computing result
		double mutualInf = 0;
		for (Entry<Short, Double> fixed : fixedOccurrence.entrySet()){
			for (Entry<Short, Double> moving : movingOccurrence.entrySet()){
				if (mutualOccurrence.get(fixed.getKey()).containsKey(moving.getKey())){
					double weight = 1;
					if (weighted)
						weight = (double) 1/(Math.abs(moving.getKey() - fixed.getKey())+1);
					double result = mutualOccurrence.get(fixed.getKey()).get(moving.getKey()),
							resultAux = (double)result/(fixedOccurrence.get(fixed.getKey())*movingOccurrence.get(moving.getKey()));
					if (logBase.equals("e"))
						result *= Math.log(resultAux);
					else if (logBase.equals("2"))
						result *= Math.log(resultAux)/Math.log(2);
					else if (logBase.equals("10"))
						result *= Math.log10(resultAux);
					result *= weight;
					mutualInf += result;
				}
			}
		}

		return mutualInf;
	}
	
	
	//dice similarity (colors-segmentation)
	public static double diceColorSimilarity(BufferedImage img1, BufferedImage img2){
		Raster r1 = img1.getRaster(), r2 = img2.getRaster();
		int n1 = 0, n2 = 0, nS = 0;
		for (int i=0; i<img1.getHeight(); i++){
			for (int j=0; j< img1.getWidth(); j++){
				boolean grey1 = true, grey2 = true;
				if (!(r1.getSample(j, i, 0) == r1.getSample(j, i, 1) && r1.getSample(j, i, 0) == r1.getSample(j, i, 2))){//if not grey
					grey1 = false;
				}
				if (!(r2.getSample(j, i, 0) == r2.getSample(j, i, 1) && r2.getSample(j, i, 0) == r2.getSample(j, i, 2))){
					grey2 = false;
				}
				//if (!grey1 || !grey2){
				if (r1.getSample(j, i, 0) > 0 && r2.getSample(j, i, 0) > 0 ){
					for (int k=0; k<2; k++){//band amount
						final int variance = 10;
						if ((r1.getSample(j, i, k) > r2.getSample(j, i, k) - variance 
								&& r1.getSample(j, i, k) < r2.getSample(j, i, k) + variance)
								//&& (!grey1 && !grey2)){
								){
							nS ++;
							n1 ++;
							n2 ++;
						}else{
							if (!grey1) n1++;
							if (!grey2) n2++;
						}
					}
				}
			}
				
		}
		return (2d*nS/(n1 + n2));
	}
	
	
}
