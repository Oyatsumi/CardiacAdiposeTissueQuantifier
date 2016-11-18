package datamining;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

public class Classifier {
	private String arffPath = null,
			classifierParameters = null,
			modelName = null;
	private AbstractClassifier model = null;
	private Class<? extends AbstractClassifier> classifierClass = null;
	private AttributesHeader attributesHeader = null;
	private String classifierName;
	private Filter filter = null;
	private int classIndex = 0;
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractClassifier> void Constructor(
			String modelName, String arffPath, Class<T> classifierClass, String classifierParameters) throws Exception{
		this.arffPath = arffPath;
		this.classifierParameters = classifierParameters;
		this.classifierClass = classifierClass;
		this.classifierName = classifierClass.getSimpleName();
		this.modelName = modelName;
		
		File treeModelFile = new File(this.getModelPath());
		if (treeModelFile.exists()){
			//model
			FileInputStream fis = new FileInputStream(this.getModelPath());
		    ObjectInputStream in = new ObjectInputStream(fis);
		    this.model = (T) in.readObject(); //old
		    in.close();
		    fis.close();
		    
		    //attributesHeader
		    fis = new FileInputStream(this.getAttributesHeaderPath());
		    in = new ObjectInputStream(fis);
		    this.attributesHeader = (AttributesHeader) in.readObject();
		    in.close();
		    fis.close();
		}else{
			createClassifyingModel(true);
		}
		
	}
	public <T extends AbstractClassifier> Classifier(
			String modelName, String arffPath, Class<T> classifierClass, String classifierParameters) throws Exception{
		Constructor(modelName, arffPath, classifierClass, classifierParameters);
	}
	public <T extends AbstractClassifier> Classifier(
			String modelName, String arffPath, Class<T> classifierClass, String classifierParameters, Filter filter) throws Exception{
		this.setFilter(filter);
		Constructor(modelName, arffPath, classifierClass, classifierParameters);
	}
	public <T extends AbstractClassifier> Classifier(
			String modelName, String arffPath, Class<T> classifierClass, int classIndex, String classifierParameters, Filter filter) throws Exception{
		this.setFilter(filter);
		this.classIndex = classIndex;
		Constructor(modelName, arffPath, classifierClass, classifierParameters);
	}
	public <T extends AbstractClassifier> Classifier(
			String modelName, String arffPath, Class<T> classifierClass, int classIndex, String classifierParameters) throws Exception{
		this.classIndex = classIndex;
		Constructor(modelName, arffPath, classifierClass, classifierParameters);
	}
	
	
	/**
	 * Classifies the instance, if it is a nominal attribute then the index of the attribute is returned.
	 * Convert the value to its index first with the AttributeScheme class.
	 * @param i - instance to classify
	 * @return - class of the instance (or the index of the class for nominal attributes)
	 * @throws Exception - weka exception
	 */
	public double classifyInstance(Instance i) throws Exception{
		return model.classifyInstance(i);
	}


	
	private void createClassifyingModel(boolean setAttributesHeader) throws Exception{
		//split the options passed as parameter to the class/classifier
		String[] options = weka.core.Utils.splitOptions(classifierParameters);

		

		//read arff and put that on the Instances class
		Instances labeled = new Instances(new BufferedReader(
                  new FileReader(arffPath)));
		
		//apply filter if set
		if (filter != null){
			this.filter.setInputFormat(labeled);
			labeled = Filter.useFilter(labeled, this.filter);
		}
		
		//create the header internally
		if (setAttributesHeader){
			this.attributesHeader = new AttributesHeader();
			for (int a=0; a<labeled.numAttributes(); a++){
				this.attributesHeader.addAttribute(labeled.attribute(a));
			}
		}

		
		labeled.setClassIndex(this.classIndex);
	    
		this.model = classifierClass.newInstance();
		//this.model = classifierClass.newInstance();
		this.model.setOptions(options);
		this.model.buildClassifier(labeled);
		
		this.saveClassifyingModel();
		this.saveAttributesHeader();
	}
	private void saveClassifyingModel() throws IOException{
	    FileOutputStream fos = null;
	    ObjectOutputStream out = null;
	    fos = new FileOutputStream(this.getModelPath());
	    out = new ObjectOutputStream(fos);
	    out.writeObject(this.model);
	    out.close();
	    fos.close();
	}
	private void saveAttributesHeader() throws IOException{
	    FileOutputStream fos = null;
	    ObjectOutputStream out = null;
	    fos = new FileOutputStream(this.getAttributesHeaderPath());
	    out = new ObjectOutputStream(fos);
	    out.writeObject(this.attributesHeader);
	    out.close();
	    fos.close();
	}
	private String getModelPath(){
		File f = new File(arffPath),
	    		f2 = f.getParentFile();
	    //return (f2.getAbsolutePath() + "/" + f.getName().split(".arff")[0] + ".model");
		return (f2.getAbsolutePath() + "/" + this.modelName + "_" + f.getName().split(".arff")[0] + "_" + this.classifierName + ".model");
	}
	private String getAttributesHeaderPath(){
		File f = new File(arffPath),
	    		f2 = f.getParentFile();
	    //return (f2.getAbsolutePath() + "/" + f.getName().split(".arff")[0] + ".attHeader");
		return (f2.getAbsolutePath() + "/" + this.modelName + "_" + f.getName().split(".arff")[0] + "_" + this.classifierName + ".attHeader");
	}
	
	
	
	/*public void removeAttribute(int index){
		Remove r = new Remove();
		r.setAttributeIndices(Integer.toString(index+1));
		this.filter = r;
	}*/
	private void setFilter(Filter f){
		this.filter = f;
	}
	public void setAttributesHeader(AttributesHeader ah){
		this.attributesHeader = ah;
	}
	public AttributesHeader getAttributesHeader(){return this.attributesHeader;}

}
