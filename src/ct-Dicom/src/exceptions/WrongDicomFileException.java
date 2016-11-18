package exceptions;

public class WrongDicomFileException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public WrongDicomFileException(String msg){
		super(msg);
	}
	
}
