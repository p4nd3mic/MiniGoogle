//package edu.upenn.cis455.indexTest;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.Socket;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Properties;
//import java.util.TreeMap;
//import java.util.TreeSet;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import edu.upenn.cis455.indexStorage.DBSingleton;
//import edu.upenn.cis455.indexStorage.DatabaseWrapper;
//import edu.upenn.cis455.utility.GenerateHitListDocument;
//import edu.upenn.cis455.utility.HashHelper;
//import edu.upenn.cis455.utility.JsoupHelper;
//import edu.upenn.cis455.utility.Stemmer;
//
//
//public class Test {
//	public static void getGeolocation() {
//		Pattern phonePattern = Pattern
//				.compile("(?:\\([2-9]\\d{2}\\)\\ ?|[2-9]\\d{2}(?:\\-?|\\ ?))[2-9]\\d{2}[- ]?\\d{4}");
//		Pattern codePattern = Pattern.compile("\\d{5}");
//		
//		Matcher matcher = codePattern
//				.matcher("d 215-778-4235 fsdf  309-778-9999 (215) 222 3711");
//		System.out.println("Start...");
//		while (matcher.find()) {
//			String one = matcher.group();
//			System.out.println("one: " + one);
//		}
//	}
//
//	public static void main(String[] args) throws Exception {
//		Stemmer s = new Stemmer();
//		String word = "running";
//		s.add(word.toCharArray(), word.length());
//		s.stem();
//		word = s.toString();
//		System.out.println("change to... " + word);
//
//		 DBSingleton.setDbPath("/Users/xuxu/Desktop/db");
//		 DatabaseWrapper wrapper = new
//		 DatabaseWrapper(DBSingleton.getInstance().getEntityStore());
//		 String sCurrentLine;
//		 BufferedReader br = new BufferedReader(new FileReader(
//		 "/Users/xuxu/Desktop/US.txt"));
//		 while ((sCurrentLine = br.readLine()) != null) {
//		 String[] parts = sCurrentLine.split("\t");
//		 String postCode = parts[1];
//		 String cityName = parts[2].toLowerCase();
//		 String stateName = parts[3].toLowerCase();
//		 String stateAbbr = parts[4].toLowerCase();
//		 String latitude = parts[8];
//		 String longitude = parts[9];
//		 wrapper.addLocDateEntity(postCode, cityName, stateName, stateAbbr,
//		 latitude, longitude);
//		 }
//		 br.close();
//		
//		 System.out.println(wrapper.getLocDateInfo("19104"));
//		
//		 DBSingleton.getInstance().closeBDBstore();
//		 System.exit(0);
//
//		getGeolocation();
//		
//		
//		
//		String urlTest = "http://bananaleafphilly.com/";
//
//		URL url = new URL(urlTest);
//
//		PrintWriter writer = null;
//		BufferedReader reader = null;
//
//		Socket client = new Socket(url.getHost(), 80);
//		if (client != null) {
//			writer = new PrintWriter(client.getOutputStream());
//			reader = new BufferedReader(new InputStreamReader(
//					client.getInputStream()));
//			client.setSoTimeout(20000);
//		}
//
//		StringBuilder requestString = new StringBuilder();
//		if (url.getPath().isEmpty()) {
//			requestString.append("GET " + "/" + " HTTP/1.1\r\n");
//		} else {
//			requestString.append("GET " + url.getPath() + " HTTP/1.1\r\n");
//		}
//		requestString.append("Host:" + url.getHost() + "\r\n");
//		requestString.append("User-Agent: cis455crawler\r\n");
//		requestString.append("Connection: close\r\n\r\n");
//		writer.print(requestString.toString());
//		writer.flush();
//
//		String line = reader.readLine();
//		while (line != null && !line.isEmpty()) {
//			// System.out.println(line);
//			line = reader.readLine();
//		}
//
//		StringBuilder resBody = new StringBuilder();
//
//		// BODY INFOMATION
//		while (line != null) {
//			// System.out.println(line);
//			resBody.append(line);
//			line = reader.readLine();
//		}
//
//		// System.out.println("resBody: " + resBody);
//		String html = resBody.toString();
//		reader.close();
//		writer.close();
//		client.close();
//
//		GenerateHitListDocument generateHitList = new GenerateHitListDocument(html, urlTest);
//		System.out.println("docId: " + HashHelper.generateDocId(urlTest)
//				+ " : " + generateHitList.serialize());
//
//		JsoupHelper jsoup = new JsoupHelper(html);
//		System.out.println("getUrl: " + jsoup.getUrl(urlTest));
//		System.out.println("getAnchor: " + jsoup.getAnchor());
//		System.out.println("getMeta: " + jsoup.getMeta());
//		System.out.println("getHtag: " + jsoup.getHtag());
//		System.out.println("getTitle: " + jsoup.getTitle());
//	}
//}
