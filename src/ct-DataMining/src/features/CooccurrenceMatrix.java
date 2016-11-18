package features;

import globals.ImageData;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


public class CooccurrenceMatrix {
	private ImageData img;
	private TreeMap<Short, TreeMap<Short, Short>> coMatrix; //coocorrencia, tom1 e tom2, depois a quantidade de vezes que co-ocorrem
	private int probtotal = 0;	
	private short dx, dy;
	
	
	public CooccurrenceMatrix(short[][] img){
		this.img = new ImageData(img);
	}
	public CooccurrenceMatrix(short[][] img, int dx, int dy){
		this.img = new ImageData(img);
		buildCOM (dx, dy);
	}
	public CooccurrenceMatrix (int[][] img, int dx, int dy){
		this.img = new ImageData(img);
		buildCOM (dx, dy);
	}
	public void buildCOM (int dx, int dy){
		this.dx = (short) dx; this.dy = (short) dy;
		//int ix = 0xFFF, iy = 0xFFF, fx = 0, fy = 0;
		short value, value2;
		short auxi;
		coMatrix = new TreeMap<Short, TreeMap<Short, Short>>();

		for (int i=0; i<img.getHeight(); i++){
			for (int j=0; j<img.getWidth(); j++){
				value = img.getPixel(j, i);;
				
				//se o TreeMap da coMatrix não contém o tom, inicializar
				if (!coMatrix.containsKey(value)) coMatrix.put(value, new TreeMap<Short, Short>());
				value2 = 0;
				
				//para não estourar os limites da imagem
				if ((j + dx >= 0) && (i + dy >= 0) && (j + dx < img.getWidth()) && (i + dy < img.getHeight())){
					
					value2 = img.getPixel(j + dx, i + dy);
				
					if (!coMatrix.get(value).containsKey(value2)) coMatrix.get(value).put(value2, (short) 1);
					else{
						auxi = (short) (coMatrix.get(value).get(value2) + 1);
						coMatrix.get(value).remove(value2);
						coMatrix.get(value).put(value2, auxi);
					}
					
					//scores every cooccurrence
					this.probtotal ++;
						
				}
			}
		}
		
		//colocar 0 nas não ocorrências
		Iterator<Map.Entry<Short, TreeMap<Short, Short>>> it = coMatrix.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Short, TreeMap<Short, Short>> entry = it.next();
			for (int i=0; i<img.getHues().size(); i++){
				if (!entry.getValue().containsKey(img.getHues().get(i))){
					entry.getValue().put(img.getHues().get(i), (short) 0);
				}
			}
		}

		
	}
	
	
	public void printCOM(){ //na tela
		Iterator<Map.Entry<Short, TreeMap<Short, Short>>> it = coMatrix.entrySet().iterator();
		Iterator<Map.Entry<Short, Short>> it2;
		
		for (int i=0; i<this.img.getHues().size(); i++){
			System.out.print(this.img.getHues().get(i) + " ");
		}
		System.out.println();
		System.out.println("____________");
		
		while (it.hasNext()) {
			Map.Entry<Short, TreeMap<Short, Short>> entry = it.next();
			it2 = entry.getValue().entrySet().iterator();
			while (it2.hasNext()){
				Map.Entry<Short, Short> entry2 = it2.next();
				System.out.print(entry2.getValue() + " ");
			}
			System.out.println();
		}
	}
	
	public void printImage(){
		for (int i=0; i<img.getHeight(); i++){
			for (int j=0; j<img.getWidth(); j++){
				System.out.print(img.getPixel(j, i) + " ");
			}
			System.out.println();
		}
	}
	
	
	public float cooccurrenceProbability(short i, short j){
		if (probtotal == 0)
			return 0;
		return (float) coMatrix.get(i).get(j)/probtotal;
	}
	
	private Iterator<Map.Entry<Short, Short>> it;
	private Map.Entry<Short, Short> entry;
	public float cooccurrenceProbabilityPerHue(short i, short j){
		if (coMatrix.get(i).get(j) == 0) return 0;
		
		int probhuei = 0;
		
		
		it = coMatrix.get(i).entrySet().iterator();
		while (it.hasNext()){
			entry = it.next();
			probhuei += coMatrix.get(i).get(entry.getKey());
		}
		
		if (probhuei == 0) return Float.POSITIVE_INFINITY; //Infinity - NaN
		return (float) coMatrix.get(i).get(j)/probhuei;
	}
	
	
	//função duplicada com metodologia diferente
	public double getCOMMoment(int g){
		double total = 0;
		Iterator<Map.Entry<Short, TreeMap<Short, Short>>> it = coMatrix.entrySet().iterator();
		Iterator<Map.Entry<Short, Short>> it2;
		
		while (it.hasNext()) {
			Map.Entry<Short, TreeMap<Short, Short>> entry = it.next();
			it2 = entry.getValue().entrySet().iterator();
			while (it2.hasNext()){
				Map.Entry<Short, Short> entry2 = it2.next();
				total += Math.pow(entry.getKey() - entry2.getKey(), g) *
						cooccurrenceProbability(entry.getKey(), entry2.getKey());
			}
		}
		
		return total;
	}
	public double getCOMMomentPerHue(int g){
		double total = 0;
		Iterator<Map.Entry<Short, TreeMap<Short, Short>>> it = coMatrix.entrySet().iterator();
		Iterator<Map.Entry<Short, Short>> it2;
		
		while (it.hasNext()) {
			Map.Entry<Short, TreeMap<Short, Short>> entry = it.next();
			it2 = entry.getValue().entrySet().iterator();
			while (it2.hasNext()){
				Map.Entry<Short, Short> entry2 = it2.next();
				total += Math.pow(entry.getKey() - entry2.getKey(), g) *
						cooccurrenceProbabilityPerHue(entry.getKey(), entry2.getKey());
			}
		}
		
		return total;
	}
	
	
	
	
	
	public void dispose(){
		this.coMatrix.clear();
		this.coMatrix = null;
		this.img = null;
	}

}
