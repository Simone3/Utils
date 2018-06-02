package com.utils.common.xml;

/**
 * A helper exception
 */
public class XmlParserException extends Exception {

	private static final long serialVersionUID = 1L;

	public XmlParserException(Throwable e) {
		
		super(e);
	}

	public XmlParserException(String message) {
		
		super(message);
	}
	
	@Override
	public String toString() {
		
		String message = getMessage();
		Throwable cause = getCause();
		
		if(cause == null) {
			
			return "Message: " + message;
		}
		else {
			
			return "Cause: " + cause.getClass().getSimpleName() + " - " + getMessage();
		}
	}
}