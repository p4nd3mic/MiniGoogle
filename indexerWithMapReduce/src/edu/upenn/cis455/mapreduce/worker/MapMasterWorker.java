package edu.upenn.cis455.mapreduce.worker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class MapMasterWorker extends Thread {
	private boolean isStop = false;
	private TaskQueue mapTaskQueue;
	private String num_map;
	private String workinfo;
	private int numWorkers;
	private String spoolOut;
	private Object objectJob;
	private Method mapMethod;
	private MapContext context;

	private ArrayList<TaskQueue> ownTaskQueue;
	private boolean previousDone = false;
	private ReportMessage reportMessage;

	private HashMap<Integer, String> AddressInfo = new HashMap<Integer, String>();
	private ThreadPool threadpool;
	private ArrayList<File> fileArray;

	public MapMasterWorker(TaskQueue mapTaskQueue, String num_map,
			String workinfo, int numberWorker, String spoolOut,
			Object objectJob, Method mapMethod, MapContext context) {
		this.mapTaskQueue = mapTaskQueue;
		this.num_map = num_map;
		this.workinfo = workinfo;
		this.numWorkers = numberWorker;
		this.spoolOut = spoolOut;
		this.objectJob = objectJob;
		this.mapMethod = mapMethod;
		this.context = context;

		ReportSingleton reportSingleton = ReportSingleton.getInstance();
		reportMessage = reportSingleton.getReportMessage();

		parseWorkinfo(workinfo);
		fileArray = createFileEachWorker(numWorkers);
		context.setNumWorkers(numWorkers);

		WriteSpoolOutSingleton.getInstance().createWriterForEachFile(fileArray);

		// For each worker
		int num = Integer.valueOf(num_map);
		this.ownTaskQueue = new ArrayList<TaskQueue>();
		// System.out.println("num_map: " + num);
		for (int i = 0; i < num; i++) {
			ownTaskQueue.add(new TaskQueue());
		}
		threadpool = new ThreadPool(num, ownTaskQueue, objectJob, mapMethod,
				context);

	}

	// HashMap -- key:index(start:1) value:address
	public void parseWorkinfo(String workinfo) {
		String[] info = workinfo.split("%3B");
		int index = 0;
		for (String string : info) {
			index++;
			System.out.println("parseWorkinfo: " + string);
			String address = string.split("%3D")[1];
			address = address.replaceAll("%3A", ":");
			address = address.replaceAll("localhost", "127.0.0.1");
			System.out.println("parseWorkAddress: " + address);
			AddressInfo.put(index, address);
		}
	}

	// Create file -- filename: start:~/1.txt
	public ArrayList<File> createFileEachWorker(int numWorkers) {
		ArrayList<File> fileArray = new ArrayList<File>();

		for (int i = 1; i <= numWorkers; i++) {
			String path = spoolOut + "/" + i + ".txt";
			File temp = new File(path);
			try {
				temp.createNewFile();
			} catch (IOException e) {
				System.out.println("Cannot create file");
				e.printStackTrace();
			}
			fileArray.add(temp);
		}
		return fileArray;
	}

	public void run() {
		while (!isStop()) {
			String content = null;
			try {
				if (mapTaskQueue.getSize() > 0) {
					content = mapTaskQueue.getLine();
					if (content != null) {
						reportMessage.addKeysRead();
					}
				} else {
					if (getPreviousDone()) {
						break;
					} else {
						content = mapTaskQueue.getLine();
						if (content != null) {
							reportMessage.addKeysRead();
						}
					}
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (content == null) {
				continue;
			}

			int index = HashText.whichOneThread(content, num_map);
			try {
				// System.out.println("index: " + index + " - " + "content: "
				// + content);
				ownTaskQueue.get(index).addLine(content);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		threadpool.AllWorkerSetPreviousDone();
		notifyAllWorkers();

		for (Worker worker : threadpool.getPool()) {
			try {
				worker.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		WriteSpoolOutSingleton.getInstance().closeWriterForEachFile();

		try {
			sendFileToEachWorker();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("MapMaster: I am going to die..");

		// while(!threadpool.allWaiting()){
		// }
	}

	private void sendFileToEachWorker() throws IOException {
		System.out.println("sendFileToEachWorker AddressInfo: " + AddressInfo);
		for (Entry<Integer, String> entry : AddressInfo.entrySet()) {
			int index = entry.getKey();
			String address = entry.getValue();
			if (!address.startsWith("http://")) {
				address = "http://" + address;
			}
			String content = FileContent(fileArray.get(index - 1)
					.getAbsolutePath());
			String wholeAddress = address + "/worker/pushdata";
			URL obj = new URL(wholeAddress);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add reuqest header
			con.setRequestMethod("POST");
			// System.out.println("Post content : " + content);
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());

			wr.writeBytes(content);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : "
					+ wholeAddress);
			System.out.println("Response Code : " + responseCode);
		}
	}

	private String FileContent(String filePath) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(filePath));

			while ((sCurrentLine = br.readLine()) != null) {
				sb.append(sCurrentLine + "\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return sb.toString();
	}

	public synchronized void notifyAllWorkers() {
		threadpool.AllWorkerNotify();
	}

	public synchronized boolean isStop() {
		return isStop;
	}

	public synchronized void stopWorker() {
		isStop = true;
	}

	public synchronized void setPreviousDone() {
		previousDone = true;

	}

	public synchronized boolean getPreviousDone() {
		return previousDone;
	}
}
