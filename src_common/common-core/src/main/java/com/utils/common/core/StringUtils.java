package com.utils.common.core;

/**
 * Some Sting utils
 */
public class StringUtils {

	/**
	 * Checks if null or empty or just spaces
	 * @param str the string
	 * @return true if at least one "real" character inside the String
	 */
	public static boolean isEmpty(String str) {
		
	    if(str != null) {
	    	
	        int len = str.length();
	        
	        for(int x = 0; x < len; ++x) {
	        	
	            if(str.charAt(x) > ' ') {
	            	
	                return false;
	            }
	        }
	    }
	    
	    return true;
	}	
}
