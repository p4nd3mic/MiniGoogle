package edu.upenn.cis455.mapreduce.master;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class SendMessage extends Thread {
	private String class_name;
	private String input_path;
	private String num_map;
	private String workerInfoSerial;
	private int numWorkers;
	private static ConcurrentHashMap<String, HashMap<String, String>> workerStatus = workerInfo
			.getInstance().getWorkerStatus();

	public SendMessage(String class_name, String input_path, String num_map,
			String workerInfoSerial, int numWorkers) {
		this.class_name = class_name;
		this.input_path = input_path;
		this.num_map = num_map;
		this.workerInfoSerial = workerInfoSerial;
		System.out.println("SendMessage - workerInfoSerial: "
				+ workerInfoSerial);
		this.numWorkers = numWorkers;
	}

	public void mapMessage(String url) throws Exception {
		if(!url.startsWith("http://")){
			url = "http://" + url;
		}
		url = url + "/worker/runmap";
		System.out.println("SendMessage - mapMessage url: " + url);
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// add reuqest header
		con.setRequestMethod("POST");

		String urlParameters = "class_name=" + class_name + "&" + "input_path="
				+ input_path + "&" + "num_map=" + num_map + "&" + "numWorkers="
				+ numWorkers + "&" + "workinfo=" + workerInfoSerial;

		System.out.println("Post parameters : " + urlParameters);
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		// BufferedReader in = new BufferedReader(new InputStreamReader(
		// con.getInputStream()));
		// String inputLine;
		// StringBuffer result = new StringBuffer();
		//
		// while ((inputLine = in.readLine()) != null) {
		// result.append(inputLine);
		// }
		// in.close();
		// System.out.println(result.toString());
	}

	public void run() {

		for (String addressPort : workerStatus.keySet()) {
			try {
				this.mapMessage(addressPort);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
