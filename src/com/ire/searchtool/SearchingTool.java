package com.ire.searchtool;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import com.ire.entity.StopWords;
import com.ire.indextool.IndexingTools;
import com.ire.tag.TagName;

/**
 * Searching tool to search in the index
 * @author gaurav
 *
 */
public class SearchingTool {
	
	public static String indexfolderpath = "C://gaurav/index";
	
	public static String filesec = "";
	public static String fileindex = "";
	
	public static String titleFile = indexfolderpath + "/titles.txt";
	
	public static HashMap<String, Integer> mapField = new HashMap<String, Integer>();
	
	static {
		mapField.put("t", 15);//"00001111"
		mapField.put("i", 23);//"00010111");
		mapField.put("c", 29);//"11111101");
		mapField.put("l", 27);//"//11111011");
		mapField.put("b", 30);//"11101110");
	}
	public static String searchword(String word) throws Exception {
		long startcount = 1;
		long endcount = 1;
		
		//	BufferedReader br = new BufferedReader(new FileReader(new File(
			//		filesec)));
			String line = "";
			String line2 = "";
			StringBuffer sb = new StringBuffer();
			char ch[] = word.toCharArray();
			int len = ch.length;
			for (int i = 0; i < len; i++) {
				ch[i] = Character.toLowerCase(ch[i]);
				int chin = (int) ch[i];
				if (chin >= 97 && chin <= 122) {
					sb.append((ch[i]));
				}
			}
			word = sb.toString();
			if(StopWords.stopwordsarrList.contains(word)) {
				return "";
			}
			word = IndexingTools.doStemming(word);
			char startchar = word.charAt(0);
			/*String outfile = 	indexfolderpath+"/temp"+startchar+"first.txt";
			System.out.println("Output file "+outfile);
			char secchar = word.charAt(1);
			BufferedReader brfirst = new BufferedReader(new FileReader(new File(outfile)));
			int linetoread = secchar-97+2;
			int readedline = 0;
			while(readedline<linetoread) {
				 line = brfirst.readLine();
				 readedline++;
			} 
			System.out.println("Output line "+line);
			
			int index = line.indexOf('-');
			int seek = Integer.parseInt(line.substring(1, index));
			int lineno = Integer.parseInt(line.substring(index+1)); */
			String outfile = 	indexfolderpath+"/temp"+startchar+".txt";
			 File out = new File(outfile);
			RandomAccessFile raf = new RandomAccessFile(new File(outfile), "r");
			String fileline = IndexingTools.binarySearch(raf, word, 0, out.length());
			/*raf.seek(seek);
			 readedline = 0;
			 String fileline = "";
			 String foundline = "";
			while(readedline<lineno) {
				fileline = raf.readLine();
				System.out.println("File line "+fileline);
				int charpos = fileline.indexOf(TagName.equalstring);
				String fileword = fileline.substring(0, charpos);
				if(word.equals(fileword)) {
					foundline = fileline.substring(charpos+1);
					break;
				}
			} */
			//System.out.println("file line "+fileline);
		return fileline;	
	}

	public static String sort(String line) {
		String [] sarr = line.split(",");
		TreeSet<String> idlist = new TreeSet<String>();
		for(String s: sarr) {
			idlist.add(s);
		}
		StringBuffer sb = new StringBuffer();
		for(String id: idlist) {
			sb.append(id);
			sb.append(TagName.comma);
		}
		return sb.toString().substring(0, sb.length()-1);
	}
	
	
	public static void main(String[] args)  {
		try {
			//indexfolderpath = args[0];
			//indexfolderpath = "index";
			filesec = indexfolderpath+File.separatorChar+"filesec.txt";
			fileindex = indexfolderpath+File.separatorChar+"index.txt";
//	String word = args[1];
//	String word = "bye";
	long time1 = System.currentTimeMillis();
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line = "";
			while(!line.equals("1")) {
				line = br.readLine();
			String inputString = line;
			String words[] = inputString.split(" ");
			ArrayList<LinkedHashSet<String>> searchedlist = new ArrayList<LinkedHashSet<String>>();
			for(String word: words) {
				Integer fieldvalue = 0;
				String s = "";
				LinkedHashSet<String> listofpage = new LinkedHashSet();
				if(word.indexOf(':')!=-1) {
					String newword = word.substring(2);
					String field = word.substring(0, 1);
					 fieldvalue = mapField.get(field);
					  listofpage = new LinkedHashSet<String>();
						 try {
							s = searchword(newword);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}
						 if(s==null || s == "") {
							 continue;
						 }
						String firstsplit[] = s.split(",");
						for(int i=0;i<firstsplit.length;i++) {
						//	System.out.println("First "+firstsplit[i]);
							String secondsplit[] = firstsplit[i].split("_");
							//System.out.println("Second split "+secondsplit[0]);
							String [] thirdsplit = secondsplit[1].split("-");
							for (int j = 0; j < thirdsplit.length; j++) {
								String fourthsplit [] = thirdsplit[j].split(":");
								Integer value = Integer.parseInt(fourthsplit[1]);
								int val = value | fieldvalue;
							//	System.out.println("Val "+val+"FIled value "+fieldvalue);
								if(val == 31) {
									listofpage.add(fourthsplit[0]);
									//listofpage.append(',');
								}
							}
				}
				//s = listofpage.toString();
				//System.out.println(" Print -->"+s);
				} else {
					 try {
						s = searchword(word);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
					 if(s==null || s == "") {
						 continue;
					 }
					// System.out.println("Line Found "+s);
						String firstsplit[] = s.split(",");
						for(int i=0;i<firstsplit.length;i++) {
							String secondsplit[] = firstsplit[i].split("_");
							String [] thirdsplit = secondsplit[1].split("-");
							for (int j = 0; j < thirdsplit.length; j++) {
								String fourthsplit [] = thirdsplit[j].split(":");
								Integer value = Integer.parseInt(fourthsplit[1]);
									listofpage.add(fourthsplit[0]);
									//listofpage.append(',');
							}
				}
				//s = listofpage.toString();
				//System.out.println("Print "+s);
				}
				searchedlist.add(listofpage);
			}
			//System.out.println("Searched list "+searchedlist);
			//System.out.println("Seached List "+searchedlist.size());
			Iterator<LinkedHashSet<String>> list = searchedlist.iterator();
			LinkedHashSet<String> intersecthashset = searchedlist.get(0);
			LinkedHashSet<String> unionhashset = searchedlist.get(0);
		
			
			for(int i=1;i<searchedlist.size();i++) {
				LinkedHashSet<String> newhashset = searchedlist.get(i);
				intersecthashset.retainAll(newhashset);
				unionhashset.addAll(newhashset);
			}
			ArrayList<String> finalpages = new ArrayList<String>();
			//System.out.println("Intersect Hashset "+intersecthashset);
			//System.out.println("Union Hashset "+unionhashset);
			int count = 10;
			Iterator<String> it1 = intersecthashset.iterator();
			int i= 1;
			for (i=1;i<=count;i++) {
				if(it1.hasNext()) {
				finalpages.add(it1.next());
				}
				//it1.next();
			}
			Iterator<String> it2 = intersecthashset.iterator();
			for(int j=i;j<=count;j++) {
				String page  = "";
				if(it2.hasNext()) {
				 page = it2.next();
				} else {
					break;
				}
				if(!finalpages.contains(page)) {
					finalpages.add(page);
				}
			}
			System.out.println("Final Pages "+finalpages);
			File titlef = new File(titleFile);
			RandomAccessFile raf = new RandomAccessFile(titlef, "r");
			int length = 9;
			for(i=length;i>=0;i--) {
				System.out.print(finalpages.get(i));
				String fileline = IndexingTools.binarySearchinTitle(raf, Integer.parseInt(finalpages.get(i)), 0, titlef.length());
				System.out.println(" "+fileline);
			}
			StringBuffer sb = new StringBuffer();
			//System.out.println("Final Pages "+finalpages);
			
			for(String pageno: finalpages) {
				sb.append(pageno);
				sb.append(TagName.comma);
			}
			//System.out.println(sb.toString());
	long time2 = System.currentTimeMillis();
	System.out.println("TIme consumed "+(time2-time1)/1000);
			}
			//searchword("Age!");
			//searchword("!bye");
			//searchword("$$$bye!$$$");
			//searchword("$$$Bye!$$$");
			//searchword("$$$b$ye!$$$");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
		
}
