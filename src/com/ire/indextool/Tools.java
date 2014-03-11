package com.ire.indextool;

/**
 * Basic tools to be used for conversion
 * @author gaurav
 *
 */
public class Tools {

	/**
	 * 
	 * @param sb StringBuffer
	 * @return chracter array
	 */
	public static char [] convertSBtoCharArr(StringBuffer sb) {
		int len = sb.length();
		char[] ch = new char[len];
		sb.getChars(0, len, ch, 0);
		return ch;
	}
	
}
