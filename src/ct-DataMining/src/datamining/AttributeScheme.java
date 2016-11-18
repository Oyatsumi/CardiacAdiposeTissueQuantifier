package datamining;

import java.util.ArrayList;
import java.util.HashMap;

public class AttributeScheme {
	private short classIndex = -1;
	private HashMap<Short, ArrayList<String>> nominalLabels = new HashMap<Short, ArrayList<String>>();
	private HashMap<Short, Boolean> isNumeric = new HashMap<Short, Boolean>();
	private HashMap<Short, String> attName = new HashMap<Short, String>();
	
	AttributeScheme(){
	}
	AttributeScheme(int classIndex){
		this.classIndex = (short) classIndex;
	}

	/**
	 * Name a attribute/column
	 * @param index - index of the attribute/column
	 * @param attributeName - name of the attribute
	 */
	public void name(int index, String attributeName){
		attName.put((short) index, attributeName);
	}
	public void addLabel(int index, String value){
		/*
		if (value.matches("-?\\d+(\\.\\d+)?")){//is numeric
			isNumeric.put((short) index, true);
			return false;
		}
		*/
		if (nominalLabels.get((short) index) == null) 
			nominalLabels.put((short) index, new ArrayList<String>());
		
		nominalLabels.get(index).add(value);
		isNumeric.put((short) index, false);
	}
	public void addLabel(int index, double value){
		isNumeric.put((short) index, true);
	}
	public void addLabels(int index, String[] values){
		isNumeric.put((short) index, false);
		if (nominalLabels.get((short) index) == null) 
			nominalLabels.put((short) index, new ArrayList<String>());
		for (int i=0; i<values.length; i++){
			nominalLabels.get(index).add(values[i]);
		}
	}
	
	public boolean isNumeric(int attributeIndex){return isNumeric.get(attributeIndex);}
	public int getIndexOfValue(int attributeIndex, String value){
		if (!this.isNumeric(attributeIndex)){
			ArrayList<String> as = nominalLabels.get(attributeIndex);
			for (int i=0; i<as.size(); i++){
				if (as.get(i).equals(value)) return i;
			}
		}
		return 0;
	}
	public String getValueOfIndex(int attributeIndex, int index){
		if (!this.isNumeric(attributeIndex)){
			return nominalLabels.get(attributeIndex).get(index);
			}
		return null;
	}
	
	public short getClassIndex(){
		return this.classIndex;
	}
	
	
	/**
	 * Converts the attribute scheme to the weka-pattern attribute relation.
	 * @return - attribute relation
	 */
	public String toWekaAttributeRelation(){
		String output = "";
		for (int i=0; i<attName.size(); i++){
			if (this.isNumeric(i)){
				output.concat("@attribute " + this.attName.get(i) + " numeric");
			}else{
				output.concat("@attribute " + this.attName.get(i) + " {");
				for (int j=0; j<this.nominalLabels.get(i).size(); j++){
					output.concat(this.nominalLabels.get(i).get(j) + ",");
				}
				output.concat("} \n");
			}
		}
		return output;
	}
}
