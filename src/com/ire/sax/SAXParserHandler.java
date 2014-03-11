package com.ire.sax;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ire.entity.Page;
import com.ire.indextool.IndexingTools;
import com.ire.tag.TagName;
/**
 * SAX Handler for the XML file
 * @author gaurav
 *
 */
public class SAXParserHandler extends DefaultHandler {

	boolean istitle = false;
	String currenttag = "";
	private boolean starttag = false;
	private StringBuffer sb = new StringBuffer();
	private HashMap<String, StringBuffer> tagConent = new HashMap<String, StringBuffer>();
	private boolean isFirst = true;
	private String pageid;
	private Page currentPage = null;

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		try {
			currenttag = qName;
			starttag = true;
			if (currenttag.equals(TagName.PAGE)) {
				currentPage = new Page();
				isFirst = true;
				pageid = new String();
			}
			sb = new StringBuffer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		try {
			for (int i = start; i < start + length; i++) {
				sb.append(Character.toLowerCase(ch[i]));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
	//	System.out.println("Ending -->" + qName);
		try {
			if (currenttag.equals(TagName.ID) && isFirst) {
				pageid = sb.toString();
				currentPage.setId(pageid);
				isFirst = false;
				//IndexingTools.init();
			} else if (qName.equals(TagName.TITLE)) {
				currentPage.setTitle(sb);
			} else if (qName.equals(TagName.TEXT)) {
				currentPage.setBodytext(sb);
			} else if (qName.equals(TagName.PAGE)) {
			//	System.out.println("Page end");
				// sb.setLength(0);
				IndexingTools.treatPage(currentPage);
			} else if(qName.equalsIgnoreCase(TagName.MEDIAWIKI) || qName.equalsIgnoreCase(TagName.FILE)) {
				System.out.println("End of media file Tag");
				IndexingTools.fileend();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
 		// System.out.println("String "+sb.toString());
		// super.pendElement(uri, localName, qName);
	}

}
