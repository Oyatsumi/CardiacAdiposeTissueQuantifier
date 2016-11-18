package datamining;

import java.io.Serializable;
import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class AttributesHeader implements Serializable {
	private static final long serialVersionUID = 1L;
	private String baseName = "";
	private ArrayList<Attribute> att = new ArrayList<Attribute>();
	private Instances instancesHeader = null;
	//private ArrayList<ArrayList<String>> attValues = new ArrayList<ArrayList<String>>();
	
	/**
	 * If there is more than one value separate them by comma (",")
	 * @param baseName
	 * @param names
	 * @param types
	 */
	public AttributesHeader(String baseName, String[] names, String[] values){
		this.baseName = baseName;
		for (int a=0; a<names.length; a++){
			this.addAttribute(names[a], values);
		}
	}
	public AttributesHeader(String baseName){
		this.baseName = baseName;
	}
	public AttributesHeader(){}
	
	public void putAttribute(int index, String name, String[] values){
		if (values.length == 1){//numeric attribute
			att.add(new Attribute(name));
		}else{
			ArrayList<String> currentVal = new ArrayList<String>();
			for (int a=0; a<values.length; a++){
				currentVal.add(values[a]);
			}
			att.add(index, new Attribute(name, currentVal));
		}
		
	}
	public void addAttribute(Attribute a){
		this.att.add(a);
	}
	public void putAttribute(int index, Attribute a){
		this.att.add(index, a);
	}
	public void addAttribute(String name, String[] values){
		this.putAttribute(att.size(), name, values);
	}

	public void buildInstances(int instancesNumber){
		this.instancesHeader = new Instances(baseName, att, instancesNumber);
	}
	public void clearInstances(){
		if (instancesHeader != null) {
			for (int a=0; a<this.instancesHeader.numInstances(); a++){
				this.instancesHeader.remove(a);
			}
		}
	}
	public void setClassIndex(int index){
		if (instancesHeader != null) this.instancesHeader.setClassIndex(index);
	}
	public void setDataset(Instance i){
		if (this.instancesHeader == null){
			this.buildInstances(1);
		}
		if (this.instancesHeader.size() > 1){
			this.instancesHeader.remove(0);
		}
		i.setDataset(this.instancesHeader);
	}
	
	public String getArffStructure(){//conferir se está certo
		String type;
		String out = "@RELATION mineddata \n\n";
		for (int a=0; a<att.size(); a++){
			if (att.get(a).isNumeric()) type = "REAL";
			else{
				type = "{";
				int numValues = att.get(a).numValues();
				for (int b=0; b<numValues - 1; b++){
					type += att.get(a).value(b) + ",";
				}
				type = att.get(a).value(numValues) + "}";
			}
			out += "@ATTRIBUTE " + att.get(a).name() + " " + type + "\n";
		}
		out += "\n\n @DATA \n";
		return out;
	}
	
	public void setBaseName(String name){
		this.baseName = name;
	}
	
	public Instances getInstances(){
		return this.instancesHeader;
	}
}
