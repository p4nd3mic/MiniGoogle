package edu.upenn.cis455.mapreduce.worker;

import java.net.HttpURLConnection;
import java.net.URL;

public class ReportMessage extends Thread {
	private String remoteAddr;
	private String port;
	private String status;
	private String job;
	private int keysRead;
	private int keysWritten;

	public ReportMessage(String remoteAddr, String port) {
		this.remoteAddr = remoteAddr;
		this.status = "idle";
		this.job = "NoJob";
		this.keysRead = 0;
		this.keysWritten = 0;
		this.port = port;
	}
	
	
	private void sendReport(String url) throws Exception{
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");
		int responseCode = con.getResponseCode();
		System.out.println("Response Code : " + responseCode);
	}

	public void run() {
		while (true) {
			// Send message
			String url = "http://" + remoteAddr + "/workerstatus";
			String queryPart = "?port=" + port + "&status=" + status + "&job="
					+ job + "&keysRead=" + getKeysRead() + "&keysWritten="
					+ getKeysWritten();
			url = url + queryPart;
			System.out.println("url:" + url);
			try {
				sendReport(url);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// Sleep
			try {
				Thread.interrupted();
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				System.out.println("I was interrupted");
				continue;
			}
		}
	}

	public synchronized String getStatus() {
		return status;
	}

	public synchronized void setStatus(String status) {
		this.status = status;
	}

	public synchronized String getJob() {
		return job;
	}

	public synchronized void setJob(String job) {
		this.job = job;
	}

	public synchronized void addKeysRead(){
		keysRead = keysRead + 1;
	}
	
	public synchronized int getKeysRead() {
		return keysRead;
	}

	public synchronized void addKeysWritten(){
		keysWritten = keysWritten + 1;
	}
	
	public synchronized int getKeysWritten() {
		return keysWritten;
	}

	public synchronized void setKeysRead(int keysRead) {
		this.keysRead = keysRead;
	}
	
	public synchronized void setKeysWritten(int keysWritten) {
		this.keysWritten = keysWritten;
	}
}
