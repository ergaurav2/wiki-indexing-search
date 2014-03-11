package com.ire.sax;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
/**
 * SAX PArser Service for the xml file
 * @author gaurav
 *
 */
public class SAXParserService {

	public static void runParser(String fileurl) {
		DefaultHandler def = new SAXParserHandler();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		File f = new File(fileurl);
		SAXParser saxParser = null;
		try {
			saxParser = factory.newSAXParser();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		try {
			saxParser.parse(f, def);
			//return saxParser;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		//return null;
	}
}
