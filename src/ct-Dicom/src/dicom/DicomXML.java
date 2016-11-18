package dicom;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.BitSet;
import java.util.HashMap;

import javax.imageio.ImageIO;

import registration.Marker;
import registration.Registration;


import exceptions.DicomDataFormat;
import exceptions.ReadFileException;
import exceptions.WrongDicomFileException;
import globals.ImageOp;
import globals.Vector2;

/**
 * @author Érick Oliveira Rodrigues (erickr@id.uff.br)
 * Accesses DICOM data that is converted to a XML-file, respecting the
 *  data format distributed by dcm4che library website as the binary 'dcm2xml'
 */
public class DicomXML {
	private final static int bufferedImageType = BufferedImage.TYPE_BYTE_GRAY;
	private String filepath = null;
	private HashMap<String, String> tag;
	private PixelDataMap pixelrawdata = null; //first: x-axis, second: y-axis

	public DicomXML(String filepath) {
		this.filepath = filepath;
	}
	
	public void dispose() throws IOException{
		if (br != null) br.close();
		filepath = null;
		if (tag != null) tag.clear();
		tag = null;
		pixelrawdata = null;
		fs = null;
	}

	
	private final String TAG_FINAL_FORMAT = "</attr>";
	private final byte MAX_LINES_FOR_VALUE = 20;
	private final byte TAG_INITIAL_POS = 11, TAG_FINAL_POS = 19; //on the archieve
	private BufferedReader br = null;
	private String currentline, currenttag, tagcontent = null;
	/*
	/**
	 * Receives a tag (XXXX,XXXX) both numbers in hexadecimal format and returns the file tag
	 * @param id1 - First id in hexadecimal format
	 * @param id2 - Second id in hexadecimal format
	 * @return - String file tag
	 * @throws Exception - error reading file
	 *-/
	public String getTagValue(String id1, String id2) throws Exception{
		return getTagValue(id1+id2);
		
	}
	*/
	private FileInputStream fs = null;
	/**
	 * Receives a tag (XXXX,YYYY) both numbers in hexadecimal format and returns the file tag, 
	 * yet collapsed in one single parameter (XXXXYYYY)
	 * @throws Exception - error reading file
	 */
	public String getTagValue(String tagid) throws Exception{
		if (br == null){
			fs = new FileInputStream(filepath);
			br = new BufferedReader(new InputStreamReader(fs));
		}else{
			fs.getChannel().position(0);
			br = new BufferedReader(new InputStreamReader(fs));
		}
		
		boolean found = false;
		
		//halt condition (previously stored on memory)
		if (tag != null) {
			if (tag.containsKey(tagid)) return tag.get(tagid);
		}else{
			tag = new HashMap<String, String>();
		}
		
		while (((currentline = br.readLine()) != null) && !found) {
			//verify the tag indexes on each file of the archieve, respecting the xml format <attr tag="F2150010" ...
			
			if (currentline.length() > TAG_FINAL_POS){
				currenttag = currentline.substring(TAG_INITIAL_POS, TAG_FINAL_POS);
				if (currenttag.equals(tagid)){
					short cont = 0;
					while (!currentline.endsWith(TAG_FINAL_FORMAT)) {
						currentline = currentline.concat(br.readLine());
						cont ++;
						if (cont > MAX_LINES_FOR_VALUE) throw new ReadFileException("Tag format doesn't match " + TAG_FINAL_FORMAT);
					}
					tagcontent = currentline.split(">", 2)[1];
					tagcontent = tagcontent.substring(0, tagcontent.length() - TAG_FINAL_FORMAT.length());
					found = true;
					tag.put(tagid, tagcontent);
				}
			}
		}
		//br.close();
		return tagcontent;
	}
	
	private short height = -1, width = - 1;
	public short getHeight() throws NumberFormatException, Exception{
		if (height == -1)
			height = Short.parseShort(this.getTagValue("00280010"));
		return height;
	}
	public short getWidth() throws NumberFormatException, Exception{
		if (width == -1)
			width = Short.parseShort(this.getTagValue("00280011"));
		return width;
	}
	private boolean unsigned = false;
	private final boolean firsttimeunsigned = true;
	public boolean isUnsigned() throws Exception{
		if (firsttimeunsigned){
			unsigned = this.getTagValue("00280103").charAt(0) == '0';
			if (this.getTagValue("00280103").charAt(0) != '0' && this.getTagValue("00280103").charAt(0) != '1')
				throw new DicomDataFormat("Pixel format tag not present on file or it has an invalid value.");
		}
		return unsigned;
	}
	private boolean modalitylut, firsttimelut;
	public boolean hasModalityLUT() throws Exception{
		if (firsttimelut){
			modalitylut = (this.getTagValue("00283000") != null);
		}
		return modalitylut;
	}


	
	/**
	 * Returns the raw value of a given pixel at coordinate (x, y). 
	 * (0,0) is the upper left pixel, standard orientation.
	 * @param x - x coordinate of the pixel
	 * @param y - y coordinate of the pixel
	 * @return - raw data value of a pixel at (x, y)
	 * @throws NumberFormatException 
	 * @throws Exception
	 */
	private byte bitdepth = -1;
	public byte getAllocatedPixelBitDepth() throws NumberFormatException, Exception{
		if (bitdepth == -1)
			bitdepth = Byte.parseByte(this.getTagValue("00280100"));
		return bitdepth;
	}
	public byte getStoredPixelBitDepth() throws NumberFormatException, Exception{
		if (bitdepth == -1)
			bitdepth = Byte.parseByte(this.getTagValue("00280101"));
		return bitdepth;
	}
	public long getPixelRawData(int x, int y) throws Exception{
		if (this.pixelrawdata != null){
			return pixelrawdata.getValue(x, y);
		}else{
			this.pixelrawdata = new PixelDataMap(this.getStoredPixelBitDepth(), this.getWidth(), this.getHeight());
		}
		
		
		//split at the '\' separator
		//String[] splittedrawdata = this.getTagValue("7FE00010").split("\\\\");
		String data = this.getTagValue("7FE00010");
		String conc = "";
		int counter = 0;
		for (int k=0; k<data.length(); k++){
			if (data.charAt(k) == '\\'){
				this.pixelrawdata.putValue(counter % this.getWidth(), (int) counter / this.getWidth(), Long.parseLong(conc));	
				conc = ""; counter ++; continue;
			}
			conc += data.charAt(k);
		}

		/*
		for (int i=0; i<splittedrawdata.length; i++){
			this.pixelrawdata.putValue(i % this.getWidth(), (int) i / this.getWidth(), Long.parseLong(splittedrawdata[i]));
		}
		splittedrawdata = null;
		*/
		data = null;
		System.gc();
		
		return this.pixelrawdata.getValue(x, y);
	}
	
	
	/**
	 * Returns the Hounsfield value (CT DICOM) of a pixel for some cases (not supporting every 'encoding')
	 * @param x - pixel's coordinate x
	 * @param y - pixel's coordinate y
	 * @return - Housnfield value
	 * @throws Exception
	 */

	//private static BitSet bs;
	public double getHounsfieldValue(int x, int y) throws Exception{
		//Modality LUT tag found, not converting
		if (this.hasModalityLUT()){
			throw new DicomDataFormat("Modality LUT tag found in the file, conversion not yet supported.");
		}

		
		double decodedvalue;
		
		//for signed values
		if (!this.isUnsigned()){
			
			//complement of 2
			//STRING APPROACH 
			String value, value2;
			byte signal = 1;
			value = Long.toBinaryString(this.getPixelRawData(x, y));
			//for hounsfield: 16 bits *practically always
			byte bitDepth = this.getStoredPixelBitDepth();
			while (value.length() < bitDepth) 
				value = "0" + value;
			if (value.charAt(0) == '1'){//if its a negative number
				signal = -1;
				value = Long.toBinaryString((this.getPixelRawData(x, y) - 1));
				value2 = Long.toBinaryString(-(this.getPixelRawData(x, y) - 1) - 1);
				value2 = value2.substring(value2.length() - value.length());
				value = value2;
				//value = value.replaceAll("0", "X");
				//value = value.replaceAll("1", "0");
				//value = value.replaceAll("X", "1");
			} 
			decodedvalue = (double) Long.parseLong(value, 2) * signal;
			
			
			/*
			//complement of 2
			//JAVA 7 - NÃO TÁ FUNCIONANDO PERFEITAMENTE
			byte signal = 1;
			long value;
			bs = BitSet.valueOf(new long[] {this.getPixelRawData(x, y)});
			if (bs.get(0)){
				signal = -1;
				value = ~(this.getPixelRawData(x, y) - 1);
			}
			decodedvalue = value * signal;
			*/
			
		//for unsigned values
		}else{
			decodedvalue = this.getPixelRawData(x, y);
		}
		
		
		//Rescale (slope and intercept)
		final float slope = this.getRescaleSlope(),
				intercept = this.getRescaleIntercept();
		//hounsfield = raw data pixel * slope + intercept
		return (decodedvalue * slope) + intercept;
	}
	
	
	private Vector2 pixelSpacing = null;
	public Vector2 getPixelSpacing() throws Exception{
		if (pixelSpacing == null) {
			String tag = this.getTagValue("00280030");
			pixelSpacing = new Vector2(Float.parseFloat(tag.split("\\\\", 2)[0]), Float.parseFloat(tag.split("\\\\", 2)[1]));
			firstslope = false;
		}
		return pixelSpacing;
	}
	
	boolean firstslope = true; float slope = 0;
	public float getRescaleSlope() throws NumberFormatException, Exception{
		if (firstslope) {
			firstslope = false;
			String slopeStr = this.getTagValue("00281053");
			if (slopeStr == null || slopeStr.length() > 100) throw new WrongDicomFileException("The dicom file contains some error on the slope tag. The tag may not be present (0028,1053).");
			slope = Float.parseFloat(slopeStr);
		}
		return slope;
	}
	boolean firstintercept = true; float intercept = 0;
	public float getRescaleIntercept() throws NumberFormatException, Exception{
		if (firstintercept) {
			intercept = Float.parseFloat(this.getTagValue("00281052"));
			firstintercept = false;
		}
		return intercept;
	}
	
	/*
	/**
	 * Retrieves an image based from the housnfield data present on the DICOM file.
	 * The transformation is linear.
	 * E.g.: Assuming we want to retrieve the hounsfield values from 0 to 10 and to cover an entire output 8-bit depth image (from 0 to 255),
	 * we would call this method like: "getHounsfieldImage(0, 10, 0, 255)".
	 * The value 0 of the hounsfield scale will be 0 on the image and the value 10 will be expanded to be equal to 255.
	 * 
	 * If we want to do a shift of values then the difference between the two limits must be equal.
	 * @param hounsfieldInfLimit - The inferior limit on the hounsfield scale (inclusive)
	 * @param hounsfieldSupLimit - The superior limit on the hounsfield scale (inclusive)
	 * @param infLimit - The inferior limit of the retrieved image (inclusive)
	 * @param supLimit - The superior limit of the retrieved image (inclusive)
	 * @return
	 * @throws Exception 
	 *-/
	public BufferedImage getHounsfieldImage(int hounsfieldInfLimit, int hounsfieldSupLimit, int infLimit, int supLimit, int bufferedImageType) throws Exception{
		double hvalue = 0;
		final double a_f = (supLimit - infLimit)/(hounsfieldSupLimit - hounsfieldInfLimit), b_f = supLimit - hounsfieldSupLimit*a_f;
		BufferedImage bi = new BufferedImage(this.getHeight(), this.getWidth(), bufferedImageType);
		WritableRaster r = bi.getRaster();
		int result = 0;
		for (int i=0; i<this.getHeight(); i++){
			for (int j=0; j<this.getWidth(); j++){
				hvalue = this.getHounsfieldValue(j, i);
				if (hvalue >= hounsfieldInfLimit && hvalue <= hounsfieldSupLimit){
					result = (int)((a_f*hvalue) + b_f);
					r.setSample(j, i, 0, result);
				}
			}
		}
		

		return bi;
	}
	*/
	
	/**
	 * Retrieves an image based from the housnfield data present on the DICOM file.
	 * The transformation is linear.
	 * E.g.: Assuming we want to retrieve the hounsfield values from 0 to 10 and to cover an entire output 8-bit depth image (from 0 to 255),
	 * we would call this method like: "getHounsfieldImage(0, 10, 0, 255)".
	 * The value 0 of the hounsfield scale will be 0 on the image and the value 10 will be expanded to be equal to 255.
	 * 
	 * If we want to do a shift of values then the difference between the two limits must be equal.
	 * @param hounsfieldInfLimit - The inferior limit on the hounsfield scale (inclusive)
	 * @param hounsfieldSupLimit - The superior limit on the hounsfield scale (inclusive)
	 * @param infLimit - The inferior limit of the retrieved image (inclusive)
	 * @param supLimit - The superior limit of the retrieved image (inclusive)
	 * @return
	 * @throws Exception 
	 */
	public BufferedImage getHounsfieldImage(int hounsfieldInfLimit, int hounsfieldSupLimit, int infLimit, int supLimit, boolean superiorToWhite) throws Exception{
		double hvalue = 0;
		final double a_f = (double)(supLimit - infLimit)/(hounsfieldSupLimit - hounsfieldInfLimit), 
						b_f = supLimit - hounsfieldSupLimit*a_f;

		BufferedImage bi = new BufferedImage(this.getHeight(), this.getWidth(), bufferedImageType);
		WritableRaster r = bi.getRaster();
		double result = 0;
		for (int i=0; i<this.getHeight(); i++){
			for (int j=0; j<this.getWidth(); j++){
				hvalue = this.getHounsfieldValue(j, i);
				if (hvalue >= hounsfieldInfLimit && hvalue <= hounsfieldSupLimit){
					result = a_f*hvalue + b_f;
					r.setSample(j, i, 0, result);
				}else if (hvalue > hounsfieldSupLimit && superiorToWhite)
					r.setSample(j, i, 0, supLimit);

			}
		}
		

		return bi;
	}
	/*
	public BufferedImage getHounsfieldImage(int hounsfieldInfLimit, int hounsfieldSupLimit, int infLimit, int supLimit, boolean superiorToWhite, int bufferedImageType) throws Exception{
		double hvalue = 0;
		final double aI = ((double)(hounsfieldSupLimit - hounsfieldInfLimit)/(supLimit - infLimit)),
				a_f = (double)(supLimit - infLimit)/((hounsfieldSupLimit/aI) - (hounsfieldInfLimit/aI)), 
						b_f = supLimit - (hounsfieldSupLimit/aI)*a_f;

		BufferedImage bi = new BufferedImage(this.getHeight(), this.getWidth(), bufferedImageType);
		WritableRaster r = bi.getRaster();
		double result = 0;
		for (int i=0; i<this.getHeight(); i++){
			for (int j=0; j<this.getWidth(); j++){
				hvalue = this.getHounsfieldValue(j, i);
				if (hvalue >= hounsfieldInfLimit && hvalue <= hounsfieldSupLimit){
					result = a_f*hvalue/aI + b_f;
					r.setSample(j, i, 0, result);
				}else if (hvalue > hounsfieldSupLimit && superiorToWhite)
					r.setSample(j, i, 0, supLimit);

			}
		}
		

		return bi;
	}
	*/
	
	
	private final Object transfInterpolation = ImageOp.BICUBIC;
	/**
	 * Returns the registered fat image based on hounsfield values of CT-scans
	 * @param bufferedImageType
	 * @return
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public BufferedImage getRegisteredFatImage(Vector2 atlasStandardPosition, BufferedImage atlasImg) throws NumberFormatException, Exception{
		return 
			ImageOp.transform(this.getFatImage(), 
					this.getRegisteredFatImageTransformation(atlasStandardPosition, atlasImg, null),
					transfInterpolation);
	}
	public BufferedImage getRegisteredFatImage(Vector2 atlasStandardPosition, BufferedImage atlasImg, File debugImg) throws NumberFormatException, Exception{
		return 
			ImageOp.transform(this.getFatImage(), 
					this.getRegisteredFatImageTransformation(atlasStandardPosition, atlasImg, debugImg),
					transfInterpolation);
	}
	public AffineTransform getRegisteredFatImageTransformation(Vector2 atlasStandardPosition, BufferedImage atlasImg) throws NumberFormatException, Exception{
		return this.getRegisteredFatImageTransformation(atlasStandardPosition, atlasImg, null);
	}
	public AffineTransform getRegisteredFatImageTransformation(Vector2 atlasStandardPosition, BufferedImage atlasImg, File debugImg) throws NumberFormatException, Exception{
		BufferedImage bi = this.getFatImage();
		BufferedImage bi2 = ImageOp.cloneImage(bi);
		AffineTransform at = this.getResizedFatImageTransformation(atlasStandardPosition, atlasImg);
		
		//Do the registration - a translation using the atlas (cross)
		bi2 = ImageOp.transform(bi, at, transfInterpolation); //scaled
		
		Marker m = Registration.findAtlas(bi2, atlasImg);
		//Write the image priorly to the final transformation and after the initial transformation
		if (debugImg != null){
			String[] splitted = debugImg.getAbsolutePath().split("\\.");
			ImageIO.write(bi2, splitted[splitted.length - 1], debugImg);
		}
		
		int xOffset = (int) (atlasStandardPosition.x - m.getX()),
				yOffset = (int) (atlasStandardPosition.y - m.getY());
		m.dispose();
		at.translate(xOffset, yOffset);
		
		
		return at;
	}
	public AffineTransform getResizedFatImageTransformation(Vector2 atlasStandardPosition, BufferedImage atlasImg) throws NumberFormatException, Exception{
		AffineTransform at = new AffineTransform();
		
		float cX = 1, cY = 1;
		//check to see if there's a need to change the image to match the desired pixelSpacing
		if (this.getDesiredPixelSpacing() != null){
			//double dX = Math.abs(this.getDesiredPixelSpacing().x - this.getPixelSpacing().x),
				//	dY = Math.abs(this.getDesiredPixelSpacing().y - this.getPixelSpacing().y);
			//if (dX > 0.001f || dY > 0.001f){// only if the spacing is significant
				cX = (float) (1f/(getDesiredPixelSpacing().x/this.getPixelSpacing().x));
				cY = (float) (1f/(getDesiredPixelSpacing().y/this.getPixelSpacing().y));
				
				int rXOffset = (int) (this.getWidth() - (this.getWidth()*cX))/4, 
						rYOffset = (int) (this.getHeight() - (this.getHeight()*cY))/4;
				
				at.scale(cX, cY);
				at.translate(rXOffset, rYOffset);
			//}
		}
		
		return at;
	}
	public BufferedImage getTransformedFatImage(AffineTransform at) throws Exception{
		 return ImageOp.transform(this.getFatImage(), at, transfInterpolation);
	}

	
	private BufferedImage fatImg = null;
	private BufferedImage getFatImage() throws Exception{
		//range interno de gordura
		if (fatImg == null) 
			fatImg = this.getHounsfieldImage(-200, -30, 0, 255, false); 
		return fatImg;
	}
	
	public BufferedImage getTransformedImageOnRange(AffineTransform at, int minRange, int maxRange) throws Exception {
		return ImageOp.transform(this.getImageOnRange(minRange, maxRange), at, transfInterpolation);
	}
	public BufferedImage getImageOnRange(int minRange, int maxRange) throws Exception{
		return this.getHounsfieldImage(minRange, maxRange, 0, 255, true);
	}
	

	private Vector2 getDesiredPixelSpacing(){return this.desiredPixelSpacing;}
	private Vector2 desiredPixelSpacing = null;
	/**
	 * Sets the desired real spacing in 'mm' between two consecutives pixels.
	 * If null is passed as parameter then the pixelspacing on the DICOM file will be regarded.
	 * @param spacingX - Pixel spacing in mm on the X-axis
	 * @param spacingY - Pixel spacing in mm on the Y-axis
	 */
	public void setPixelSpacing(float spacingX, float spacingY){
		desiredPixelSpacing = new Vector2(spacingX, spacingY);
	}
	
}
