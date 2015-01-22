package edu.upenn.cis455.mapreduce.master;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class SendMessageAgain extends Thread {
	private String class_name;
	private String output_path;
	private String num_reduce;
	private static ConcurrentHashMap<String, HashMap<String, String>> workerStatus = workerInfo
			.getInstance().getWorkerStatus();
	
	public SendMessageAgain(String class_name, 
			String output_path,  String num_reduce) {
		this.class_name = class_name;
		this.output_path = output_path;
		this.num_reduce = num_reduce;
	}

	public void reduceMessage(String url) throws Exception {
		if(!url.startsWith("http://")){
			url = "http://" + url;
		}
		url = url + "/worker/runreduce";
		System.out.println("SendMessage - mapMessage url: " + url);
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// add reuqest header
		con.setRequestMethod("POST");

		String urlParameters = "class_name=" + class_name + "&" + "output_path="
				+ output_path + "&" + "num_reduce=" + num_reduce;

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
				this.reduceMessage(addressPort);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("sending....");
		}
	}
}
