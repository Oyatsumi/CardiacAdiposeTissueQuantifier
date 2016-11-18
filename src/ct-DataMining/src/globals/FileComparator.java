package globals;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<File>{

	@Override
	public int compare(File f1, File f2) {
		String fileName1 = ((File) f1).getName();
        String fileName2 = ((File) f2).getName();

        String numbers1 = "", letters1 = "", numbers2 = "", letters2 = "";
        
        for (int k=0; k<fileName1.length(); k++){
        	if (Character.isDigit(fileName1.charAt(k))) numbers1 += fileName1.charAt(k);
        	else letters1 += fileName1.charAt(k);
        }
        for (int k=0; k<fileName2.length(); k++){
        	if (Character.isDigit(fileName2.charAt(k))) numbers2 += fileName2.charAt(k);
        	else letters2 += fileName2.charAt(k);
        }
        
        
        int fileId1 = (int) ((numbers1.length() > 0) ? Double.parseDouble(numbers1) : 0);
        int fileId2 = (int) ((numbers2.length() > 0) ? Double.parseDouble(numbers2) : 0);
        int numberScore = (int) (fileId1 - fileId2);
        /*
        int numberScore = 0;
        int size = (numbers1.length() > numbers2.length()) ? numbers1.length() : numbers2.length();
        for (int k=1; k<size;k++){
        	int n1 = (numbers1.length() - k >= 0) ? Integer.parseInt(numbers1.charAt(numbers1.length() - k)+"") : 0,
        	n2 = (numbers2.length() - k >= 0) ? Integer.parseInt(numbers2.charAt(numbers2.length() - k)+"") : 0;
        	numberScore += (n1 - n2)*((k-1)*10);
        }
        */
        
        
        int size = (letters1.length() > letters2.length()) ? letters1.length() : letters2.length();
        int letterScore = 0;
        for (int k=0; k<size; k++){
        	if (k < letters1.length() && k < letters2.length()){
        		if (letters1.charAt(k) > letters2.charAt(k))
        			letterScore --;
        		else if (letters1.charAt(k) == letters2.charAt(k))
        			//do nothing
        		;else
        			letterScore ++;
        	}
        }
        
        
      

        return (letterScore + numberScore);
	}

	
	
}
