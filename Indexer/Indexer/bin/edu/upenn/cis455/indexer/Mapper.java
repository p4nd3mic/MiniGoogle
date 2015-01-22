package edu.upenn.cis455.indexer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;


public class Mapper {
	public void mapper(String url, String html) {
		DBSingleton.setDbPath("/Users/xuxu/Desktop/db");
		DatabaseWrapper wrapper = DBSingleton.getInstance().getWrapper();
		
		
		JsoupHelper soup = new JsoupHelper(html);

		// Change to docId
		int docId = HashHelper.generateDocId(url);
		

		// Store into DocumentEntity (docId; url; title; location)
		String title = soup.getHtmlTitle();
		ArrayList<String> paragraphText = soup.getParagraphText();
		String location = soup.getGeolocation();
		
		
		if(location != null){
			wrapper.addDocumentEntity(docId, url, title, paragraphText, location);
		}else{
			wrapper.addDocumentEntity(docId, url, title, paragraphText);
		}
		
		

		// Get serialize String
		GenerateHitList generateHitList = new GenerateHitList(html, url, docId);
		String serialStr = generateHitList.serialize();

		
		System.out.println("docId: " + docId);
		System.out.println("location: " + location);
//		System.out.println("generateHitList: " + serialStr);
		
		// Ready for reduce
		String[] pieces = serialStr.split(";");
		for (String piece : pieces) {
//			System.out.println("piece: " + piece);
			String[] parts = piece.split("\\|");
			System.out.println("key: " + parts[0] + "  " + "value: " + parts[1]);
		}
		
		DBSingleton.getInstance().closeBDBstore();
	}

	public static void main(String[] args) throws Exception {
		String url = "http://www.sangkeechinatown.com/";
		Mapper m = new Mapper();
		String html = m.getHtml(url);
		m.mapper(url, html);
	}
	
	public String getHtml(String urlStr) throws Exception {
		URL url = new URL(urlStr);

		PrintWriter writer = null;
		BufferedReader reader = null;

		Socket client = new Socket(url.getHost(), 80);
		if (client != null) {
			writer = new PrintWriter(client.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
			client.setSoTimeout(20000);
		}

		StringBuilder requestString = new StringBuilder();
		if (url.getPath().isEmpty()) {
			requestString.append("GET " + "/" + " HTTP/1.1\r\n");
		} else {
			requestString.append("GET " + url.getPath() + " HTTP/1.1\r\n");
		}
		requestString.append("Host:" + url.getHost() + "\r\n");
		requestString.append("User-Agent: cis455crawler\r\n");
		requestString.append("Connection: close\r\n\r\n");
		writer.print(requestString.toString());
		writer.flush();

		String line = reader.readLine();
		while (line != null && !line.isEmpty()) {
			// System.out.println(line);
			line = reader.readLine();
		}

		StringBuilder resBody = new StringBuilder();

		// BODY INFOMATION
		while (line != null) {
			// System.out.println(line);
			resBody.append(line);
			line = reader.readLine();
		}

		// System.out.println("resBody: " + resBody);
		String html = resBody.toString();
		reader.close();
		writer.close();
		client.close();
		return html;
	}
}
