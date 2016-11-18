package features;

public abstract class Intensity {
	final static int da = 10000000;
	
	
	public static double getLGI(short[][] img){
		double intensity = 0;
		int dx = 0, dy = 0;
		int biggest = 0;
		for (int i=0; i<img.length; i++){
			for (int j=0; j<img[0].length; j++){
				dy = (int) Math.abs(i - Math.floor(img.length/2));
				dx = (int) Math.abs(j - Math.floor(img[0].length/2));
				biggest = (dy > dx) ? dy : dx;
				intensity += Math.pow(da, 1d/(biggest + 1))*img[i][j];
			}
		}
		
		return intensity;
	}
	
	
	public static double getMean(short[][] img){
		double mean = 0;
		for (int i=0; i<img.length; i++){
			for (int j=0; j<img[0].length; j++){
				mean += img[i][j];
			}
		}
		mean /= img.length * img[0].length;
		return mean;
	}
	public static int getRoundedMean(short[][] img){
		return (int) getMean(img);
	}
	public static int getReducedMean(short[][] img){
		int count = 1, mean = 0;
		for (int j=img[0].length/3; j<img[0].length; j+=img[0].length/3){
			for (int i=0; i<img.length; i++){//3 stripes on the horizontal
				mean += img[i][j];
				count ++;
			}
		}

		return (int) mean/count;
	}
}
