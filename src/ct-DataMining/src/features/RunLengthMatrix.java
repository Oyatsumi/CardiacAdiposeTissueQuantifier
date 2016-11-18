package features;

import globals.ImageData;
import globals.Vector2;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class RunLengthMatrix { //OTIMIZAR
	private ImageData img;
	private TreeMap<Short,TreeMap<Short, TreeMap<Short, Short>>> rlm; //angulação(delta), tom, length, quantidade de vezes que ocorre
	private TreeMap<Short, Short> maiorlength;
	private boolean[][] accountedpixels;
	
	public RunLengthMatrix(ImageData img){this.img = img;}
	public RunLengthMatrix(short[][] img){this.img = new ImageData(img);}
	
	public int getRLMValue(short i, short j, short delta){//tom, length, delta referente à direção de avaliação da imagem
		Vector2 rleatual; TreeMap<Short, Short> aux;
		accountedpixels = new boolean[img.getHeight()][img.getWidth()];
		
		//no caso desse método ser chamado antes do biggestLength
		if (maiorlength == null) maiorlength = new TreeMap<Short, Short>();
		
			if (rlm == null) rlm = new TreeMap<Short,TreeMap<Short, TreeMap<Short, Short>>>();
			
			//gerar a RLM se não tiver o ângulo gerado ainda
			if (!rlm.containsKey(delta)){
				rlm.put((short) delta, new TreeMap<Short, TreeMap<Short,Short>>());
				for (int i2=0; i2<img.getHeight(); i2++){
					for (int j2=0; j2<img.getWidth(); j2++){
						if (!accountedpixels[i2][j2]){
							rleatual = nextHue(i2, j2, delta, true); //esse true é convenção
							
							if (rlm.get(delta).get((short) rleatual.x) != null){//se tem o tom
								if (rlm.get(delta).get((short) rleatual.x).get((short) rleatual.y) != null){//se tem o tom mas não tem o length
									rlm.get(delta).get((short) rleatual.x).put((short) rleatual.y, (short)(rlm.get(delta).get((short) rleatual.x).get((short) rleatual.y) + 1));
								}else{
									rlm.get(delta).get((short) rleatual.x).put((short) rleatual.y, (short) 1);
								}
							}else{//se não tem nem tom nem length
								aux = new TreeMap<Short, Short>();
								aux.put((short) rleatual.y, (short) 1);
								rlm.get(delta).put((short) rleatual.x, aux);
							}
							//atualizar maior length
							if (maiorlength.containsKey(delta)){
								if (rleatual.y /*length*/ > maiorlength.get(delta)) {
									maiorlength.remove(delta);
									maiorlength.put(delta, (short) rleatual.y);
								}
							}else{
								maiorlength.put(delta, (short) rleatual.y);
							}
									
						}
					}
				}
			}
			
			
			//caso não haja nenhuma repetição para o delta associado
			if (!maiorlength.containsKey(delta)) maiorlength.put(delta, (short) 1);
		
		
		if (rlm.get(delta).get(i) != null) {
			if (rlm.get(delta).get(i).get(j) != null) return rlm.get(delta).get(i).get(j);
			else return 0;
		}else return 0;
			
	}
	public int biggestLength(int delta){
		if (maiorlength == null){
			maiorlength = new TreeMap<Short, Short>();
		}
		if (!maiorlength.containsKey((short)delta)){
			for (int i=0; i<img.getHues().size(); i++){
				for (int j=0; j<=(int)Math.sqrt((Math.pow(img.getWidth(), 2) + Math.pow(img.getHeight(), 2))); j++){ //percorrer todos os lengths possíveis
					this.getRLMValue((short)img.getHues().get(i), (short)j, (short)delta);
				}
			}
		}
		return maiorlength.get((short)delta);
	}
	
	//Vector2 proximo;
	private Vector2 nextHue(int i, int j, int delta, boolean anteriorigual){//i e j são a posição dos pixels, delta o ângulo da RLM, se a chamada recursiva anterior foi chamada internamente (se foi verdadeiramente recursiva), se os hues foram iguais
		int x = 0, y = 0;
		boolean flag = false;
		if (delta == 0){
			x = i; y = j + 1;
			if (j != img.getWidth() - 1) {
				flag = true;
			}
		}
		else if (delta == 90){
			x = i + 1; y = j;
			if (i >= 0 && i != (img.getHeight() - 1) && j != img.getWidth() - 1) {
				flag = true;
			}
		}
		else if (delta == 45){
			x = i + 1; y = j + 1;
			if (i >= 0 && i != (img.getHeight() - 1) && j != img.getWidth() - 1) {
				flag = true;
			}
		}
		else if (delta == 135){
			x = i + 1; y = j - 1;
			if (i >= 0 && i != (img.getHeight() - 1)  && j > 0) {
				flag = true;
			}
		}


		if (anteriorigual && flag && img.getPixel(j, i) == nextHue(x, y, delta, false).x){//se hues são iguais
			accountedpixels[i][j] = true;
			accountedpixels[x][y] = true;
			return new Vector2(img.getPixel(j, i), nextHue(x, y, delta, true).y + 1);
		}
		
		return new Vector2(img.getPixel(j, i), 1);
	}//retorna tom e length associado
	
	
	
	
	public double glnu(int delta){
		double[] parcial = new double[img.getHues().size()];
		double total = 0, parcialfinal = 0;
		int maiorlength = this.biggestLength(delta);
		for (short i=0; i<img.getHues().size(); i++){
			for (short j=1; j<=maiorlength; j++){
				int aux = this.getRLMValue((short)img.getHues().get(i), (short)j, (short)delta);
				total += aux;
				parcial[i] += aux;
			}
		}
		for (short i=0; i<img.getHues().size(); i++){
			parcialfinal += Math.pow(parcial[i], 2);
		}
		
		//tava sem esse /total antes
		return parcialfinal/total;
	}
	
	public double runPercentage(int delta){
		double total =0;
		int maiorlength = this.biggestLength(delta);
		for (short i=0; i<img.getHues().size(); i++){
			for (short j=1; j<=maiorlength; j++){
				total += this.getRLMValue((short)img.getHues().get(i), (short)j, (short)delta);
			}
		}
		
		return total/(img.getHeight() * img.getWidth());
	}
	
	public void printRLM(short delta){
		if (rlm == null) this.getRLMValue((short)0, (short)0, delta);
		Iterator<Map.Entry<Short, TreeMap<Short, Short>>> it = rlm.get(delta).entrySet().iterator();
		Iterator<Map.Entry<Short, Short>> it2;
		
		for (int i=1; i<=this.biggestLength(delta); i++){
			System.out.print(i + " ");
		}
		System.out.println();
		System.out.println("____________");
		
		for (int i=0; i<img.getHues().size(); i++){
			System.out.print(img.getHues().get(i) + ": ");
			for (int j=1; j<=this.biggestLength(delta); j++){
				System.out.print(this.getRLMValue((short)img.getHues().get(i), (short)j, (short)delta) + " ");
			}
			System.out.println();
		}
		
		System.out.println(rlm);
	}
	
	public void printImage(){
		for (int i=0; i<img.getHeight(); i++){
			for (int j=0; j<img.getWidth(); j++){
				System.out.print(this.img.getPixel(j, i) + " ");
			}
			System.out.println();
		}
	}
	
	
	
	public void dispose(int i){
		rlm.get((short)i).clear();
		rlm.remove((short)i);
		System.gc();
	}
	public void dispose(){
		if (rlm != null) rlm.clear();
		this.rlm = null;
		this.img = null;
		accountedpixels = null;
		if (maiorlength != null) maiorlength.clear();
		maiorlength = null;
		System.gc();
	}
	
}













/* ANTIGO
 * package features;

import globals.ImageData;
import globals.Vector2;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class RunLengthMatrix { //OTIMIZAR
	private ImageData img;
	private TreeMap<Short,TreeMap<Short, TreeMap<Short, Integer>>> rlm; //angulação(delta), tom, length, quantidade de vezes que ocorre
	private TreeMap<Short, Integer> maiorlength;
	private boolean[][] accountedpixels;
	
	public RunLengthMatrix(ImageData img){this.img = img;}
	public RunLengthMatrix(short[][] img){this.img = new ImageData(img);}
	
	public int getRLMValue(short i, short j, short delta){//tom, length, delta referente à direção de avaliação da imagem
		Vector2 rleatual; TreeMap<Short, Integer> aux;
		accountedpixels = new boolean[img.getHeight()][img.getWidth()];
		
		//no caso desse método ser chamado antes do biggestLength
		if (maiorlength == null) maiorlength = new TreeMap<Short, Integer>();
		
			if (rlm == null) rlm = new TreeMap<Short,TreeMap<Short, TreeMap<Short, Integer>>>();
			
			//gerar a RLM se não tiver o ângulo gerado ainda
			if (!rlm.containsKey(delta)){
				rlm.put((short) delta, new TreeMap<Short, TreeMap<Short,Integer>>());
				for (int i2=0; i2<img.getHeight(); i2++){
					for (int j2=0; j2<img.getWidth(); j2++){
						if (!accountedpixels[i2][j2]){
							rleatual = nextHue(i2, j2, delta, true); //esse true é convenção
							
							if (rlm.get(delta).get((short) rleatual.x) != null){//se tem o tom
								if (rlm.get(delta).get((short) rleatual.x).get((short) rleatual.y) != null){//se tem o tom mas não tem o length
									rlm.get(delta).get((short) rleatual.x).put((short) rleatual.y, rlm.get(delta).get((short) rleatual.x).get((short) rleatual.y) + 1);
								}else{
									rlm.get(delta).get((short) rleatual.x).put((short) rleatual.y, 1);
								}
							}else{//se não tem nem tom nem length
								aux = new TreeMap<Short, Integer>();
								aux.put((short) rleatual.y, 1);
								rlm.get(delta).put((short) rleatual.x, aux);
							}
							//atualizar maior length
							if (maiorlength.containsKey(delta)){
								if (rleatual.y /*length*-/ > maiorlength.get(delta)) {
									maiorlength.remove(delta);
									maiorlength.put(delta, (int) rleatual.y);
								}
							}else{
								maiorlength.put(delta, (int) rleatual.y);
							}
									
						}
					}
				}
			}
			
			
			//caso não haja nenhuma repetição para o delta associado
			if (!maiorlength.containsKey(delta)) maiorlength.put(delta, 1);
		
		
		if (rlm.get(delta).get(i) != null) {
			if (rlm.get(delta).get(i).get(j) != null) return rlm.get(delta).get(i).get(j);
			else return 0;
		}else return 0;
			
	}
	public int biggestLength(int delta){
		if (maiorlength == null){
			maiorlength = new TreeMap<Short, Integer>();
		}
		if (!maiorlength.containsKey((short)delta)){
			for (int i=0; i<img.getHues().size(); i++){
				for (int j=0; j<=(int)Math.sqrt((Math.pow(img.getWidth(), 2) + Math.pow(img.getHeight(), 2))); j++){ //percorrer todos os lengths possíveis
					this.getRLMValue((short)img.getHues().get(i), (short)j, (short)delta);
				}
			}
		}
		return maiorlength.get((short)delta);
	}
	
	//Vector2 proximo;
	private Vector2 nextHue(int i, int j, int delta, boolean anteriorigual){//i e j são a posição dos pixels, delta o ângulo da RLM, se a chamada recursiva anterior foi chamada internamente (se foi verdadeiramente recursiva), se os hues foram iguais
		int x = 0, y = 0;
		boolean flag = false;
		if (delta == 0){
			x = i; y = j + 1;
			if (j != img.getWidth() - 1) {
				flag = true;
			}
		}
		else if (delta == 90){
			x = i + 1; y = j;
			if (i >= 0 && i != (img.getHeight() - 1) && j != img.getWidth() - 1) {
				flag = true;
			}
		}
		else if (delta == 45){
			x = i + 1; y = j + 1;
			if (i >= 0 && i != (img.getHeight() - 1) && j != img.getWidth() - 1) {
				flag = true;
			}
		}
		else if (delta == 135){
			x = i + 1; y = j - 1;
			if (i >= 0 && i != (img.getHeight() - 1)  && j > 0) {
				flag = true;
			}
		}


		if (anteriorigual && flag && img.getPixel(j, i) == nextHue(x, y, delta, false).x){//se hues são iguais
			accountedpixels[i][j] = true;
			accountedpixels[x][y] = true;
			return new Vector2(img.getPixel(j, i), nextHue(x, y, delta, true).y + 1);
		}
		
		return new Vector2(img.getPixel(j, i), 1);
	}//retorna tom e length associado
	
	
	
	
	public double glnu(int delta){
		double[] parcial = new double[img.getHues().size()];
		double total = 0, parcialfinal = 0;
		int maiorlength = this.biggestLength(delta);
		for (int i=0; i<img.getHues().size(); i++){
			for (int j=1; j<=maiorlength; j++){
				int aux = this.getRLMValue((short)img.getHues().get(i), (short)j, (short)delta);
				total += aux;
				parcial[i] += aux;
			}
		}
		for (int i=0; i<img.getHues().size(); i++){
			parcialfinal += Math.pow(parcial[i], 2);
		}
		
		//tava sem esse /total antes
		return parcialfinal/total;
	}
	
	public double runPercentage(int delta){
		double total =0;
		int maiorlength = this.biggestLength(delta);
		for (int i=0; i<img.getHues().size(); i++){
			for (int j=1; j<=maiorlength; j++){
				total += this.getRLMValue((short)img.getHues().get(i), (short)j, (short)delta);
			}
		}
		
		return total/(img.getHeight() * img.getWidth());
	}
	
	public void printRLM(short delta){
		if (rlm == null) this.getRLMValue((short)0, (short)0, delta);
		Iterator<Map.Entry<Short, TreeMap<Short, Integer>>> it = rlm.get(delta).entrySet().iterator();
		Iterator<Map.Entry<Short, Integer>> it2;
		
		for (int i=1; i<=this.biggestLength(delta); i++){
			System.out.print(i + " ");
		}
		System.out.println();
		System.out.println("____________");
		
		for (int i=0; i<img.getHues().size(); i++){
			System.out.print(img.getHues().get(i) + ": ");
			for (int j=1; j<=this.biggestLength(delta); j++){
				System.out.print(this.getRLMValue((short)img.getHues().get(i), (short)j, delta) + " ");
			}
			System.out.println();
		}
		
		System.out.println(rlm);
	}
	
	public void printImage(){
		for (int i=0; i<img.getHeight(); i++){
			for (int j=0; j<img.getWidth(); j++){
				System.out.print(this.img.getPixel(j, i) + " ");
			}
			System.out.println();
		}
	}
	
	
	
	public void dispose(int i){
		rlm.get((short)i).clear();
		rlm.remove((short)i);
		this.img = null;
		System.gc();
	}
	public void dispose(){
		rlm.clear();
		this.img = null;
		System.gc();
	}
	
}
*/

