package com.ire.indextool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.ire.entity.Page;
import com.ire.entity.StopWords;
import com.ire.tag.TagName;

/**
 * The class to do the indexing of the wikipedia data
 * @author gaurav
 *
 */
public class IndexingTools {
	public static int maxsize = 0;
	
	public static int pagereaded=0;
	
	public static long currenttime = System.currentTimeMillis();
	
	public static int totalpages = pagereaded;
	

	public static Map<String, long[]> maparrindex = new HashMap<String, long[]>();
	// public static Map<String, StringBuffer> mapfinalindex = new
	// HashMap<String, StringBuffer>();
	public static Map<String, ArrayList<String>> mapfinalindex = new HashMap<String, ArrayList<String>>();
	// public static Map<String, int[]> sortedmapindex = new TreeMap<String,
	// int[]>();

	// public static HashSet<String> maparrindex = new HashSet<String>();
	// public static Map<String, StringBuffer> mapfinalindex = new
	// HashMap<String, StringBuffer>();
	// public static Map<String, StringBuffer> mapfinalindex = new
	// HashMap<String, StringBuffer>();
	// public static Map<String, int[]> sortedmapindex = new TreeMap<String,
	// int[]>();
	public static String indexoutputfolder = "index";
	private static boolean isExtlinkfound;
	private static Stemmer stem = new Stemmer();
	private static int MAX_SIZE = 80000;
	public static int pagecount = 0;
	public static String outtempfile = "temp";
	public static int fileno = 1;
	// static File f = new File("out.txt");
	static FileWriter fw = null;
	static boolean isFirst = true;
	public static File outfile =  new File(outtempfile+"final.txt");;
	public static TreeMap<String, ArrayList<String>> sortedmap = new TreeMap<String, ArrayList<String>>();
	public static String filein = "";
	public static String fileoutname = "out";
	public static String fileextn = ".txt";
	public static String filesec = "";
	public static String fileindex = "";

	public static List<File> filelist = new ArrayList<File>();

	static  {
		indexoutputfolder = "C:\\gaurav\\index_test";
		filein = indexoutputfolder + File.separatorChar + "temp1.txt";
		// fileout = indexoutputfolder + File.separatorChar + "temp2.txt";
		filesec = indexoutputfolder + File.separatorChar + "filesec.txt";
		outtempfile = indexoutputfolder + File.separatorChar + "temp";

	}

	/**
	 * This method represent a treatment on a Page and updating the index for that page
	 * @param p @Page
	 */
	public static void treatPage(Page p) {
		//System.out.println("Treat page");
		try {
			pagereaded++;
			 //System.out.println(pagereaded);
			char[] ch = Tools.convertSBtoCharArr(p.getBodytext());
			extractDatafromBody(ch, p, p.getBodytext().length());

		} catch (Exception e) {
			//System.out.println("Error in Page count " + pagereaded +" with page id"+p.getId());
			e.printStackTrace();
		}
		pagecount++;
		// System.out.println("Remove noise -->");
		try {
			removeNoisefromPage(p);
		} catch (Exception e) {
			//System.out.println("Error in Page count " + pagereaded +" with page id"+p.getId());
			e.printStackTrace();
		}
		try {
			convertArrtoStringMap(maparrindex, p);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (mapfinalindex.size() > MAX_SIZE) {
				StringBuffer sb = new StringBuffer();
				sb.append(outtempfile);
				fileno++;
				sb.append(fileno);
				sb.append(fileextn);
				File f = new File(sb.toString());
				filelist.add(f);
				try {
					f.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//;
				}
				sortedmap.putAll(mapfinalindex);
				System.out.println("Time elapsed "+(System.currentTimeMillis()-currenttime)/60000+" min");
				System.out.println("Page readed " +pagereaded+" Writing..."+f.getName());
				writeToFile(f, sortedmap);
				// mapfinalindex.clear();
				sortedmap.clear();
				mapfinalindex.clear();
				isFirst = false;
				pagecount = 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println("Index "+maparrindex);
		maparrindex.clear();
	}

	public static void fileend() {
		try {
			if (mapfinalindex.size() >= 1) {
				StringBuffer sb = new StringBuffer();
				sb.append(outtempfile);
				fileno++;
				sb.append(fileno);
				sb.append(fileextn);
				File f = new File(sb.toString());
				filelist.add(f);
				sortedmap.putAll(mapfinalindex);
				writeToFile(f, sortedmap);
				sortedmap.clear();
				// mapfinalindex.clear();
			}
			totalpages = pagereaded;
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			narraymerge(filelist);
			createfirstlevelindex(outfile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			//generatealpaheticindex(fileindex, filesec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// int size = sortedmapindex.size();
	// System.out.println("Size"+size);
	// mapindex.clear();
	/*
	 * try { if(fw == null) { fw = new FileWriter(f); } // fw = new
	 * FileWriter(f); // fw.append(" "+sortedmapindex); // fw.flush(); }
	 * catch(IOException e) { //; } // sortedmapindex.clear();
	 * }
	 */

	/**
	 * To remove the noise from the page data
	 * 2- T 3 - I 4 - L 5 - C 6 - B
	 * 
	 * @param p
	 */
	private static void removeNoisefromPage(Page p) {
		String pageid = p.getId();
		StringBuffer sb = p.getTitle();
		char[] ch = null;
		try {
			ch = Tools.convertSBtoCharArr(sb);
			// System.out.println("Title "+sb.toString());
		} catch (Exception e) {
			// Eat the error
		}
		try {

			int tcount = removeNoiseCaseStopWord(ch, 0, sb.length(), pageid, 2);
			p.setTcount(tcount);
		} catch (Exception e) {
			// Eat the error
		}
		try {
			sb = p.getInfobox();
			if (sb != null) {
				ch = Tools.convertSBtoCharArr(sb);
				// System.out.println("infobox "+sb); //tokens =
				int icount = removeNoiseCaseStopWord(ch, 0, ch.length, pageid,
						3);
				p.setIcount(icount);
			}//
				// createIndexElement(pageid, tokens, 3);
			sb = p.getExternallinks();
			if (sb != null) {
				ch = Tools.convertSBtoCharArr(sb);
				// System.out.println("external "+p.getExternallinks());
				int ecount = removeNoiseCaseStopWord(ch, 0, ch.length, pageid,
						4);
				p.setEcount(ecount);
			}
			//
			// createIndexElement(pageid, tokens, 4); ch =
			sb = p.getCategory();
			if (sb != null) {
				ch = Tools.convertSBtoCharArr(p.getCategory());
				// System.out.println("category "+p.getCategory());
				int ccount = removeNoiseCaseStopWord(ch, 0, ch.length, pageid,
						5);
				p.setCcount(ccount);
				//
				// createIndexElement(pageid, tokens, 5);
			}
			ch = p.getText();
			// System.out.println("Text "+new String(ch));
			if (ch != null) {
				int bcount = removeNoiseCaseStopWord(ch, 0, ch.length, pageid,
						6);
				p.setBcount(bcount);
			}
			//
			// createIndexElement(pageid, tokens, 6);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//;
		}
	}

/**
 *  To remove the noise including the conversion of upper case to lower case and removal of stop words
 * @param ch array of characters
 * @param start start index
 * @param end end index
 * @param pageid id of the page
 */
	public static void removeNoiseCaseStopWord(char[] ch, int start, int end,
			String pageid) {
		// List<String> finalsb = new ArrayList<String>();
		// System.out.println(" To treat"+new String(ch));
		StringBuffer sb = new StringBuffer();
		int len = start + end;
		for (int i = start; i < len; i++) {
			int chin = (int) ch[i];
			if (chin >= 97 && chin <= 122) {
				sb.append((ch[i]));
			} else {
				try {
					if (sb.length() > 0
							&& !StopWords.stopwordsarrList.contains(sb
									.toString())) {
						String token = doStemming(sb.toString());
						addtoIndex(token, pageid);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//;
				}
				sb.setLength(0);
			}
		}
		// System.out.println("Tokens "+tokens);
	}

	/**
	 *  To remove the noise including the conversion of upper case to lower case and removal of stop words
	 * @param ch array of characters
	 * @param start start index
	 * @param end end index
	 * @param pageid id of the page
	 * @param loc location index
	 */
	public static int removeNoiseCaseStopWord(char[] ch, int start, int end,
			String pageid, int loc) {
		int wordcount = 0;
		// List<String> finalsb = new ArrayList<String>();
		// System.out.println(" To treat"+new String(ch));
		StringBuffer sb = new StringBuffer();
		for (int i = start; i < start + end; i++) {
			int chin = (int) ch[i];
			if (chin >= 97 && chin <= 122) {
				sb.append((ch[i]));
			} else if (Character.isLetterOrDigit(ch[i])) {
				continue;
			} else {
				if (sb.length() > 0
						&& !StopWords.stopwordsarrList.contains(sb.toString())) {
					// System.out.println("Before Stemming "+sb.toString());
					String token = doStemming(sb.toString());
					// System.out.println("After Stemming "+token);
					// if (!StopWords.stopwordsarrList.contains(token)) {
					addtoIndex(token, pageid, loc);
					wordcount++;
					// }
				}
				sb.setLength(0);
			}
		}
		return wordcount;
		// System.out.println("Tokens "+tokens);
	}

	/**
	 * To create a index element
	 * @param pageid Page id
	 * @param tokens list of token
	 * @param loc location index
	 */

	public static void createIndexElement(String pageid, List<String> tokens,
			int loc) {
		for (String token : tokens) {
			addtoIndex(token, pageid, loc);
		}
	}

	public static void addtoIndex(String token, String pageid, int loc) {
		if (maparrindex.containsKey(token)) {
			long[] counts = maparrindex.get(token);
			counts[1]++;
			counts[loc]++;
		} else {
			// System.out.println("Token not found"+mapindex);
			long[] counts = new long[7];
			// System.out.println("Pageid "+pageid);
			counts[0] = Long.parseLong(pageid);
			counts[1]++;
			counts[loc]++;
			maparrindex.put(token, counts);
		}
	}

	/**
	 * Adding a Token to the index
	 * @param token Token value
	 * @param pageid Id of the page
	 */
	public static void addtoIndex(String token, String pageid) {
		if (maparrindex.containsKey(token)) {
			long[] counts = maparrindex.get(token);
			counts[1]++;
			// counts[loc]++;
		} else {
			// System.out.println("Token not found"+mapindex);
			long[] counts = new long[7];
			// System.out.println("Pageid "+pageid);
			counts[0] = Integer.parseInt(pageid);
			counts[1]++;
			// counts[loc]++;
			maparrindex.put(token, counts);
		}
	}

	/**
	 * Adding a Token to the index
	 * @param token Token
	 * @param value value of the token
	 */
	public static void addtoFinalIndex(String token, StringBuffer value) {
		if (mapfinalindex.containsKey(token)) {
			ArrayList<String> list = mapfinalindex.get(token);
			list.add(value.toString());
		} else {
			// System.out.println("Token not found"+mapindex);
			ArrayList<String> list = new ArrayList<String>();
			list.add(value.toString());
			mapfinalindex.put(token, list);

		}
	}

	/**
	 * * 2- T 3 - I 4 - L 5 - C 6 - B
	 * To create a string map
	 * @param maparrindex Map of the Token and list of locaton it is present
	 * @param p @Page
	 */
	
	public static void convertArrtoStringMap(Map<String, long[]> maparrindex,
			Page p) {
		int ccount = p.getCcount();
		int bcount = p.getBcount();
		int icount = p.getIcount();
		int ecount = p.getEcount();
		int tcount = p.getTcount();
		// System.out.println(" ccount "+ccount+" bcount "+bcount+" icount "+icount+" ecount "+ecount+" tcount "+tcount);
		Set<String> keys = maparrindex.keySet();
		StringBuffer sb = new StringBuffer();
		for (String key : keys) {
			long[] values = maparrindex.get(key);
			// System.out.println("Page no "+values[0]);
			sb.append(values[0]);
			sb.append(TagName.colon);
			// sb.append(values[1]);
			// sb.append(TagName.hypen);
			double tvalue = 0f;
			double ivalue = 0f;
			double evalue = 0f;
			double cvalue = 0f;
			double bvalue = 0f;
			if (tcount != 0) {
				tvalue = ((double) values[2] / tcount) * 0.5f * 100000;
				// System.out.println(" value [2] "+values[2]+" Tvalue "+tvalue);
			}
			if (icount != 0) {
				ivalue = ((double) values[3] / icount) * 0.12f * 100000;
				// System.out.println(" value [3] "+values[3]+" Ivalue "+ivalue);

			}
			if (ecount != 0) {
				evalue = ((double) values[4] / ecount) * 0.8f * 100000;
				// System.out.println(" value [4] "+values[4]+" evalue "+evalue);

			}
			if (ccount != 0) {
				cvalue = ((double) values[5] / ccount) * 0.1f * 100000;
				// System.out.println(" value [5] "+values[5]+" cvalue "+cvalue);

			}
			if (bcount != 0) {
				bvalue = ((double) values[6] / bcount) * 0.2f * 100000;
				// System.out.println(" value [6] "+values[6]+" Bvalue "+bvalue);

			}
			double tf = tvalue + ivalue + evalue + cvalue + bvalue;
			sb.append((int)tf);
			sb.append(TagName.colon);
			StringBuffer available = new StringBuffer();
			available.append("000");
				if (values[2] != 0) {
					available.append(TagName.ONE);
				} else {
					available.append(TagName.ZERO);
				}
				if (values[3] != 0) {
					available.append(TagName.ONE);
				} else {
					available.append(TagName.ZERO);
				}
				if (values[4] != 0) {
					available.append(TagName.ONE);
				} else {
					available.append(TagName.ZERO);
				}
				if (values[5] != 0) {
					available.append(TagName.ONE);
				} else {
					available.append(TagName.ZERO);
				}
				if (values[6] != 0) {
					available.append(TagName.ONE);
				} else {
					available.append(TagName.ZERO);
				}
				byte b = Byte.parseByte(available.toString(), 2);
				sb.append(b);
				addtoFinalIndex(key, sb);
			sb.setLength(0);
			}
		} 

	public static void main(String[] args) {
		//indexoutputfolder = "/media/Local Disk/Sem2/IRE/workspace/MiniProject_2/index";
		List<File> listFile = new ArrayList<File>();
		for(int i=2;i<=296;i++) {
			File f = new File(outtempfile+i+".txt");
			listFile.add(f);
			System.out.println("Adding "+f.getAbsolutePath());
		}
		narraymerge(listFile);
	}

	/**
	 * Performing stemming
	 * @param s String to be stemmed
	 * @return Stemmed result
	 */
	public static String doStemming(String s) {
		stem = new Stemmer();
		stem.add(s.toCharArray(), s.length());
		stem.stem();
		return stem.toString();
	}

	/**
	 * Merging a Map into the File
	 * @param f File
	 * @param map Map to be merged
	 */
	public static void merge(File f, TreeMap<String, ArrayList<String>> map) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			 e.printStackTrace();
		}
		try {
			File outfile = new File(fileoutname);
			BufferedWriter bw = new BufferedWriter(new FileWriter(outfile,
					false));

			Set<String> mapwords = map.keySet();
			Iterator<String> mapit = mapwords.iterator();
			String line = "";
			String fileword = "";
			String mapword = mapit.next();
			while (mapit.hasNext()) {
				line = br.readLine();
				if (line == null) {
					break;
				}
				// System.out.println("Line "+line);
				int loc = line.indexOf(TagName.equal);
				fileword = line.substring(0, loc);
				int flag = mapword.compareTo(fileword);
				if (flag < 0) {
					StringBuffer sb = createValueString(mapword,
							map.get(mapword));
					sb.append(TagName.newline);
					bw.append(sb);
					mapword = mapit.next();
				} else if (flag > 0) {
					bw.append(line);
					line = br.readLine();
				} else if (flag == 0) {
					StringBuffer sb = new StringBuffer();
					sb.append(line);
					sb.append(createValue(map.get(mapword)));
					sb.append(TagName.newline);
					bw.append(sb);
					line = br.readLine();
					mapword = mapit.next();
				}
			}
			while (mapit.hasNext()) {
				mapword = mapit.next();
				StringBuffer sb = createValueString(mapword, map.get(mapword));
				sb.append(TagName.newline);
				bw.append(sb);
				// mapit.next();
			}
			line = br.readLine();
			while (line != null) {
				bw.append(line);
				bw.newLine();
				line = br.readLine();
			}
			bw.close();
			String temp = filein;
			// filein = fileout;
			// fileout = temp;
		} catch (Exception e) {
			 e.printStackTrace();
		}

	}

	/**
	 * Writing the Sorted Map into the file
	 * @param f Output file
	 * @param sortedmap Sorted Map of the data
	 */
	public static void writeToFile(File f,
			TreeMap<String, ArrayList<String>> sortedmap) {
		BufferedWriter bw = null;
		//System.out.println("Write to File "+f.getAbsolutePath());
		try {
			bw = new BufferedWriter(new FileWriter(f));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			Set<String> words = sortedmap.keySet();
			for (String word : words) {
				StringBuffer sb = createValueString(word, sortedmap.get(word));
				sb.append(TagName.newline);
				bw.append(sb);
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * TO generated alphabetic index 
	 * @param indexfile Index file
	 * @param outfile Output file
	 */
	public static void generatealpaheticindex(String indexfile, String outfile) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(
					indexfile)));
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(new File(outfile)));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				long linenumber = 0;
				String line = "";
				char current = '$';
				char readchar;
				while ((line = br.readLine()) != null) {
					linenumber++;
					if ((readchar = line.charAt(0)) != current) {
						try {
							addentryforalpha(readchar, linenumber, bw);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							 //;
						}
						current = readchar;
					} else {
						continue;
					}
				}
				try {
					addentryforalpha('$', linenumber, bw);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//;
				}
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				 //;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			 //;
		}

	}

	/**
	 * Adding an entry for a alphabet
	 * @param alpha
	 * @param linenumber
	 * @param bw
	 */
	public static void addentryforalpha(char alpha, long linenumber,
			BufferedWriter bw) {
		StringBuffer sb = new StringBuffer();
		sb.append(alpha);
		sb.append(TagName.equal);
		sb.append(linenumber);
		sb.append(TagName.newline);
		try {
			bw.write(sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// //;
		}
	}

	public static StringBuffer createValueString(String word,
			ArrayList<String> list) {
		StringBuffer sb = new StringBuffer();
		sb.append(word);
		sb.append(TagName.equal);
		for (String doc : list) {
			sb.append(doc);
			sb.append(TagName.hypen);
		}
		return sb;
	}

	public static StringBuffer createValue(ArrayList<String> list) {
		StringBuffer sb = new StringBuffer();
		for (String doc : list) {
			sb.append(doc);
			sb.append(TagName.hypen);
		}
		return sb;
	}

	public static void extractDatafromBody(char[] ch, Page p, int len) {
		// System.out.println(""+new String(ch));
		int countopen = 0;
		int infoboxstart = 0;
		int infoboxend = 0;
		StringBuffer sb = new StringBuffer();
		// String s = new String(ch);
		for (int i = 0; i < len - 12; i++) {
			if (ch[i] == '{' && ch[i + 1] == '{' && ch[i + 2] == 'I'
					&& ch[i + 3] == 'n' && ch[i + 4] == 'f' && ch[i + 5] == 'o'
					&& ch[i + 6] == 'b' && ch[i + 7] == 'o' && ch[i + 8] == 'x') {
				infoboxstart = i + 9;
				countopen = 1;
				i = i + 9;
			}
		}
		for (int i = infoboxstart; i < len - 1; i++) {
			// System.out.println("char "+ch[i]);
			if ((ch[i] == '{') && ch[i + 1] == '{') {
				countopen++;
			} else if (ch[i] == '}' && ch[i + 1] == '}') {
				// System.out.println("content"+sb.toString());
				countopen--;
			} else {
				sb.append(ch[i]);
				ch[i] = ' ';
			}
			if (countopen == 0) {
				infoboxend = i + 2;
				p.setInfobox(sb);
				// System.out.println("Infobox " + sb.toString());
				break;
			}
		}
		// System.out.println("Infoboxxxxx"+sb.toString());
		sb = new StringBuffer();
		int extlinkstart = infoboxend;
		int exlinkend = infoboxend;
		for (int i = infoboxend; i + 17 < len; i++) {
			if (ch[i] == '=' && ch[i + 1] == '=' && ch[i + 2] == 'e'
					&& ch[i + 3] == 'x' && ch[i + 4] == 't' && ch[i + 5] == 'e'
					&& ch[i + 6] == 'r' && ch[i + 7] == 'n' && ch[i + 8] == 'a'
					&& ch[i + 9] == 'l' && ch[i + 10] == ' '
					&& ch[i + 11] == 'l' && ch[i + 12] == 'i'
					&& ch[i + 13] == 'n' && ch[i + 14] == 'k'
					&& ch[i + 15] == 's' && ch[i + 16] == '='
					&& ch[i + 17] == '=') {
				isExtlinkfound = true;
				extlinkstart = i + 17;
				break;
			}
		}
		if (isExtlinkfound) {
			for (int i = extlinkstart; i < len; i++) {
				// System.out.println("character"+ch[i]);
				if (ch[i] == '[') {
					i++;
					if (i < len && ch[i] == '[') {
						break;
					}
					while (i< len && ch[i] != ']') {
						sb.append(ch[i]);
						ch[i] = ' ';
						i++;
					}
					exlinkend = i;
				}
			}
			p.setExternallinks(sb);
			sb = new StringBuffer();
		}
		// int catstart = 0;
		for (int i = exlinkend; i < len - 12; i++) {
			if (ch[i] == '[' && ch[i + 1] == '[' && ch[i + 2] == 'c'
					&& ch[i + 3] == 'a' && ch[i + 4] == 't' && ch[i + 5] == 'e'
					&& ch[i + 6] == 'g' && ch[i + 7] == 'o' && ch[i + 8] == 'r'
					&& ch[i + 9] == 'y') {
				i = i + 10;
				// catstart = i + 10;
				while (i < len && ch[i] != ']') {
					sb.append(ch[i]);
					ch[i] = ' ';

					i++;
				}
				i = i + 2;
			}
		}
		// System.out.println("Cateogry"+sb.toString());
		p.setCategory(sb);
		// sb = new StringBuffer();
		p.setText(ch);
		// System.out.println("Character "+new String(ch));
	}

	public static void narraymerge(List<File> files) {
		System.out.println("Merging files..... ");
		int nooffiles = files.size();
		List<BufferedReader> listbr = new ArrayList<BufferedReader>();
		for (int i = 0; i < nooffiles; i++) {
			try {
				listbr.add(new BufferedReader(new FileReader(files.get(i))));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//;
			}
		}
		try {
			 outfile = new File(outtempfile+"final.txt");
			BufferedWriter bw = new BufferedWriter(new FileWriter(outfile,
					false));
			String fileword = "";
			List<String> words = new ArrayList<String>();
			List<String> lines = new ArrayList<String>();
			for (BufferedReader br : listbr) {
				lines.add(br.readLine());
			}
			while (!listbr.isEmpty()) {
				words.clear();
				// System.out.println("Lines"+lines);
				for (String line : lines) {
					String[] splits = line.split(TagName.equalstring);
					fileword = splits[0];
					words.add(fileword);
				}
				// System.out.println("Words"+words);
				String minword = words.get(0);
				int minindex = 0;
				for (int i = 1; i < words.size(); i++) {
					String currentword = words.get(i);
					int flag = minword.compareTo(currentword);
					if (flag > 0) {
						minword = currentword;
						minindex = i;
					}
				}
				// System.out.println("Min Word"+minword);
				List<String> newlines = new ArrayList<String>();
				newlines.addAll(lines);
				newlines.remove(minindex);
				String newline = listbr.get(minindex).readLine();
				newlines.add(minindex, newline);
				StringBuffer towriteline = new StringBuffer(lines.get(minindex));
				for (int i = 1; i < words.size(); i++) {
					String currentword = words.get(i);
					if (currentword.equals(minword) && i != minindex) {
						newlines.remove(i);
						newline = listbr.get(i).readLine();
						newlines.add(i, newline);
						String[] splits = lines.get(i).split(
								TagName.equalstring);
						// towriteline.append(TagName.sep);
						towriteline.append(splits[1]);
					}
				}
				//System.out.println(towriteline.toString());
				String linewithidf = calculateidf(towriteline.toString());
				bw.write(linewithidf);
				//bw.write("\n");
				for (int i = 0; i < newlines.size(); i++) {
					if (newlines.get(i) == null) {
						listbr.remove(i);
						newlines.remove(i);
					}
				}
				lines = newlines;
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String calculateidf(String string) {
		//System.out.println("String to be converted "+string);
		String firstsplit[] = string.split(TagName.equalstring);
		String secondsplit[] = firstsplit[1].split(""+TagName.hypen);
		int noofpages = secondsplit.length;
		if(noofpages == 0 || noofpages < 0);
		double idf = Math.log10(totalpages/noofpages);
		if(idf < 0) {
		//	System.out.println("total pages "+totalpages+"noofpages "+noofpages);
		//	System.out.println("idf "+idf);
		}
	//	int scaledtfidf=(int) (y0+(y1-y0)*(tfidf-x0)/(x1-x0));
		List<String> list = new ArrayList<String>();
		String thirdsplit[] = secondsplit[0].split(""+TagName.colon);
		int refpageid = Integer.parseInt(thirdsplit[0]);
		//System.out.println("Refid "+refpageid);
		 HashMap<Integer, StringBuffer> tfidfpageno = new HashMap<Integer, StringBuffer>();
		for(int i=0;i<noofpages;i++) {
			 thirdsplit = secondsplit[i].split(""+TagName.colon);
			 int pageid = Integer.parseInt(thirdsplit[0]) ;//- refpageid;
			 int tfidf = (int)idf * (Integer.parseInt(thirdsplit[1]));
			 if(tfidfpageno.get(tfidf)==null) {
				 StringBuffer sb = new StringBuffer();
				 sb.append(pageid);
				 sb.append(TagName.colon);
				 sb.append(thirdsplit[2]);
				 sb.append(TagName.hypen);
				 tfidfpageno.put(tfidf, sb);
			//	 System.out.println("Adding "+tfidf+" "+sb);
			 } else {
				 StringBuffer sb = new StringBuffer();
				 sb.append(pageid);
				 sb.append(TagName.colon);
				 sb.append(thirdsplit[2]);
				 sb.append(TagName.hypen);
				 tfidfpageno.get(tfidf).append(sb);
			 }
			// System.out.println("TfIds "+tfidfpageno);
			// sb.setLength(0);
		}
		//System.out.println("After List "+list);
		StringBuffer finalString = new StringBuffer(); 
		finalString.append(firstsplit[0]);
		finalString.append(TagName.equalstring);
		Set<Integer> tfids = tfidfpageno.keySet();
		List<Integer> list1 = new ArrayList<Integer>();
		list1.addAll(tfids);
		Collections.sort(list1, new Comparator<Integer>() {
			@Override
			public int compare(Integer tfidf1, Integer tfidf2) {
				return -(tfidf1.compareTo(tfidf2));
			}
		});
	
		//System.out.println("tf   "+tfidfpageno);
		Iterator<Integer> it = list1.iterator();
		int pagelimit = 90000;
		int count = 0;
		while(it.hasNext()) {
			if(count>pagelimit) {
				break;
			}
			Integer tfid = it.next();
			finalString.append(tfid);
			finalString.append(TagName.sep);
			//System.out.println("Tf id "+tfid+" "+tfidfpageno.get(tfid));
			//12222_14440000-1- 18*5000 = 90000
			String newS = tfidfpageno.get(tfid).toString();
			finalString.append(newS);
			count = count + newS.length();
			finalString.append(",");
		}
		finalString.append('\n');
		//System.out.println("Conveted "+finalString.toString());
		return finalString.toString();
	}

	static void createfirstlevelindex(File f) {
		System.out.println("Reading "+f.getAbsolutePath());
		try {
			BufferedReader bf = new BufferedReader(new FileReader(f));
		String line = "";
		//char [] currentchar = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
		//		'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
		int i = 0;
		int j = 0;
		char currentchar = 'a';
		char secondchar = '$';
		File currentcharfile = new File(outtempfile+currentchar+".txt");
		File currentcharfirstfile = new File(outtempfile+currentchar+"first"+".txt");
		
		System.out.println("File "+currentcharfile.getAbsolutePath());
		currentcharfile.createNewFile();
		currentcharfirstfile.createNewFile();
		BufferedWriter bwchar = new BufferedWriter(new FileWriter(currentcharfile));
		BufferedWriter bwfirstlevel = new BufferedWriter(new FileWriter(currentcharfirstfile));
		int seekpoint = 0;
		int linecount=0;
		while((line = bf.readLine())!=null) {
			char linechar = line.charAt(0);
		if(linechar == currentchar) {
			//
			bwchar.write(line);
			bwchar.write('\n');
			if(line.charAt(1) == secondchar) {
				linecount++;
			} else {
				bwfirstlevel.write("-");
				bwfirstlevel.write(""+linecount);
				bwfirstlevel.write('\n');
				secondchar = line.charAt(1);
				linecount=1;
				bwfirstlevel.write(""+secondchar);
				bwfirstlevel.write(""+seekpoint);
				
			}
		} else {
			bwchar.close();
			bwfirstlevel.close();
			currentchar = linechar;
			secondchar = '$';
			currentcharfile = new File(outtempfile+currentchar+".txt");
			currentcharfirstfile = new File(outtempfile+currentchar+"first"+".txt");
			currentcharfirstfile.createNewFile();
			System.out.println("File "+currentcharfile.getAbsolutePath());
			currentcharfile.createNewFile();
			 bwchar = new BufferedWriter(new FileWriter(currentcharfile));
			 bwchar.write(line);
			 bwfirstlevel = new BufferedWriter(new FileWriter(currentcharfirstfile));
				if(line.charAt(1) == secondchar) {
					linecount++;
					continue;
				} else {
					bwfirstlevel.write("-");
					bwfirstlevel.write(""+linecount);
					bwfirstlevel.write('\n');
					secondchar = line.charAt(1);
					linecount=1;
					bwfirstlevel.write(secondchar);
					bwfirstlevel.write(""+seekpoint);
				}
		}
		seekpoint = seekpoint+line.length()+1;
		//System.out.println("Seek point "+seekpoint);
		} 
		}catch (Exception e) {
			//;			
	}
	}
		
		public static String binarySearch(RandomAccessFile raf, String keyword, long startseek, long endseek) {
			//RandomAccessFile raf = new RandomAccessFile(file, "r");
			//System.out.println("Binary Search "+keyword);
			if(startseek>=endseek) {
				//System.out.println("Start is more now");
				return "";
			}
			long mid = (startseek+endseek)/2;
			//System.out.println("Start seek "+startseek+"Endseek "+endseek+" Mid "+mid);
			try {
				raf.seek(mid);
				//while(raf.read)
				String line = raf.readLine();
				line = raf.readLine();
				//System.out.println("Line Readed 1 "+line);
				if(line == null  || line.length()==0) {
					//System.out.println("Null");
					return "";
				}
				char chin = line.charAt(0);
				if (!(chin >= 97 && chin <= 122)) {
					 line = raf.readLine();
				}
				//System.out.println("Line Readed 2 "+line);
				int charpos = line.indexOf(TagName.equalstring);
				String word = line.substring(0, charpos);
				int comp = keyword.compareTo(word); 
			//	System.out.println("Comp "+comp);
				if(comp == 0) {
				//System.out.println("found --> "+line);
					return line.substring(charpos+1);
				} else if(comp < 0) {
					endseek = mid-1;
				} else {
					startseek = mid+1;
				}	
			} catch (IOException e) {
				//;
			}
			return binarySearch(raf, keyword, startseek, endseek);
}

		
		
		public static String binarySearchinTitle(RandomAccessFile raf, Integer keyword, long startseek, long endseek) {
			//RandomAccessFile raf = new RandomAccessFile(file, "r");
			//System.out.println("Binary Search "+keyword);
			if(startseek>=endseek) {
				//System.out.println("Start is more now");
				return "";
			}
			long mid = (startseek+endseek)/2;
		//	System.out.println("Start seek "+startseek+"Endseek "+endseek+" Mid "+mid);
			try {
				raf.seek(mid);
				//while(raf.read)
				String line = raf.readLine();
				line = raf.readLine();
			//	System.out.println("Line Readed 1 "+line);
				if(line == null  || line.length()==0) {
					//System.out.println("Null");
					return "";
				}
				//System.out.println("Line Readed 2 "+line);
				int charpos = line.indexOf(TagName.colon);
				Integer word = Integer.parseInt(line.substring(0, charpos));
				int comp = keyword.compareTo(word); 
			//	System.out.println("Comp "+comp);
				if(comp == 0) {
				//System.out.println("found --> "+line);
					return line.substring(charpos+1);
				} else if(comp < 0) {
					endseek = mid-1;
				} else {
					startseek = mid+1;
				}	
			} catch (IOException e) {
				//;
			}
			return binarySearchinTitle(raf, keyword, startseek, endseek);
}

		
		
		
}