package edu.upenn.cis455.mapreduce.worker;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import edu.upenn.cis455.mapreduce.Job;
import edu.upenn.cis455.mapreduce.job.MapContext;

public class WorkerServlet extends HttpServlet {

	static final long serialVersionUID = 455555002;
	String port;
	String master;
	String jobClass;
	String inputDirectory;
	String outputDirectory;
	String storageDirectory;
	String numMapThreads;
	String numReduceThreads;
	// File folder;
	Job wordCount;
	BufferedReader reader;
	MapContext context;
	List<File> mapInputFiles;
	String nextKey = null;
	String value = null;

	String status = "idle";
	int keysRead = 0;
	int keysWritten = 0;
	List<String> workers = new ArrayList<String>();
	Map<String, BigInteger[]> workerRanges = new HashMap<String, BigInteger[]>();
	Map<String, String> workerNames = new HashMap<String, String>();
	Map<String, String> inverseWorkerNames = new HashMap<String, String>();

	List<Thread> mapThreads = new ArrayList<Thread>();
	List<Thread> reduceThreads = new ArrayList<Thread>();
	boolean isNewJob = true;
	//logic for map threads
	public class ReadThread extends Thread {
		String name;
		
		public ReadThread(String name) {
			this.name = name;
		}

		public void run() {
			// going to change to while worker status != waiting?
			while (reader != null) {
				synchronizedRead();
			}
		}
	}
	//logic for reduce threads
	public class ReduceThread extends Thread {
		String name;

		public ReduceThread(String name) {
			this.name = name;
		}

		public void run() {
			// going to change to while worker status != waiting?
			while (reader != null) {
				synchronizedReduce();
			}

			// sendWorkerStatus();
		}
	}

	public void reset() {
		workerRanges = new HashMap<String, BigInteger[]>();
		workerNames = new HashMap<String, String>();
		inverseWorkerNames = new HashMap<String, String>();
		mapThreads = new ArrayList<Thread>();
		reduceThreads = new ArrayList<Thread>();
		nextKey = null;
		value = null;
		workers = new ArrayList<String>();
		status = "idle";
		keysRead = 0;
		keysWritten = 0;
		jobClass = null;
		inputDirectory = null;
		outputDirectory = null;
		numMapThreads = null;
		numReduceThreads = null;
		// File folder;
		wordCount = null;
	}

	//setup
	public void init() {
		ServletConfig config = getServletConfig();
		port = config.getInitParameter("port");
		master = config.getInitParameter("master");
		storageDirectory = config.getInitParameter("storagedir");
		if (!storageDirectory.endsWith("/")) {
			storageDirectory = storageDirectory + "/";
		}
		// createSpoolDir();

		// each worker sends a heartbeat every 10 seconds.
		// get heartbeat working for multiple workers.
		Thread thread = new Thread() {
			public void run() {
				while (true) {
					try {
						sendWorkerStatus();
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		thread.start();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws java.io.IOException {
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws java.io.IOException {
		String path = request.getServletPath();
		if (path == null) {
			System.out.println("path is null///////////////////////////");
		}
		if (path.equals("/runmap")) {
			reset();
			PrintWriter out = response.getWriter();
			response.setContentType("text/html");
			//runmap function for this worker
			runMap(request);
		} else if (path.equals("/runreduce")) {
			runReduce();
			// run reduce then set status to idle
		} else if (path.equals("/pushdata")) {
			if (isNewJob) {
				File spoolIn = new File(storageDirectory + "spool-in");
				if (spoolIn.exists()) {
					deleteDir(spoolIn);
				}
				spoolIn.mkdir();
				isNewJob = false;
			}
			BufferedReader in;
			in = request.getReader();
			String line;
			StringBuffer answer = new StringBuffer();
			while ((line = in.readLine()) != null) {
				answer.append(line + "\n");
			}
			writeToSpoolIn(answer.toString());
			// add to spool-in/reduce
		}

	}
	//handles spool-in logic
	public void writeToSpoolIn(String body) {
		FileWriter fw;
		File file = new File(storageDirectory + "spool-in");
		if (!file.exists()) {
			file.mkdir();
		}
		try {
			File reduceFile = new File(storageDirectory + "spool-in/reduce");
			if (!reduceFile.exists()) {
				reduceFile.createNewFile();
			}
			fw = new FileWriter(reduceFile.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(body);
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	//starts and runs reduce threads.
	public void runReduce() {
		status = "reduce";
		Class<?> job;
		try {
			context.setMapReduce(false);
			context.setFirst(true);
			job = Class.forName(jobClass);
			// load class
			wordCount = (Job) job.newInstance();
			// need to change to spool-in/file
			Process p = Runtime.getRuntime().exec(
					"sort -k1 " + storageDirectory + "spool-in/reduce");
			reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			startThreads(numReduceThreads, "reduce");
			for (int i = 0; i < reduceThreads.size(); i++) {
				reduceThreads.get(i).join();
			}
			status = "idle";

			isNewJob = true;
			File diffOutput = new File(storageDirectory + "diffOutput");
			if (diffOutput.exists()) {
				sendDiff();
			}
			sendWorkerStatus();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	//send the difference to the master. if < convergence then master runs finish. else master runs iter again.
	public void sendDiff() {
		try {
			//System.out.println("sending diff");
			File diff = new File(storageDirectory + "diffOutput/part-r-00000");
			StringBuffer sb = new StringBuffer();
			BufferedReader br = new BufferedReader(new FileReader(diff));
			String strDiff = br.readLine();

			String entireURL = "http://" + master + "/diff?diff=" + strDiff;
			URL url;
			url = new URL(entireURL);
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("GET");
			http.setRequestProperty("User-Agent", "Mozilla/5.0");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					http.getInputStream()));
			if (http != null) {
				String line;
				StringBuffer answer = new StringBuffer();
				while ((line = in.readLine()) != null) {
					answer.append(line);
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println("workerstatus failed. no connection");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//runs map threads
	public void runMap(HttpServletRequest request) {
		status = "mapping";
		context = new MapContext();
		context.setStorageDirectory(storageDirectory);
		context.setMapReduce(true);
		BufferedReader in;
		try {
			// delete spool-out if it exists
			File spoolOut = new File(storageDirectory + "spool-out");
			if (spoolOut.exists()) {
				deleteDir(spoolOut);
			}
			spoolOut.mkdir();

			in = request.getReader();

			String line;
			StringBuffer answer = new StringBuffer();
			while ((line = in.readLine()) != null) {
				answer.append(line);
			}
			parseRunMapVariables(answer.toString());
			context.setOutputDirectory(outputDirectory);
			mapInputFiles = new ArrayList<File>();
			// if diff we need to look at multiple directories
			if (inputDirectory.equals("diff")) {
				File folder = new File(storageDirectory + "interm1");
				File folder2 = new File(storageDirectory + "interm2");
				for (File file : folder.listFiles()) {
					mapInputFiles.add(file);
				}
				for (File file : folder2.listFiles()) {
					mapInputFiles.add(file);
				}
			} else {
				// otherwise we only look at one				
				File folder = new File(storageDirectory + inputDirectory);
				if (!folder.exists()) {
					System.out.println("file:"+folder.toString()+"    does not exist");
				}else {
				for (File file : folder.listFiles()) {
					mapInputFiles.add(file);
				}
				}
			}
			Class<?> job;
			job = Class.forName(jobClass);
			// load class
			wordCount = (Job) job.newInstance();

			reader = new BufferedReader(new FileReader(mapInputFiles.remove(0)));

			startThreads(numMapThreads, "map");
			for (int i = 0; i < mapThreads.size(); i++) {
				mapThreads.get(i).join();
			}
			in.close();
			pushData();
			// send /pushdata post to each worker.
			// set status to waiting
			status = "waiting";
			sendWorkerStatus();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch blocks
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//pushdata to other workers
	public void pushData() {
		File directory = new File(storageDirectory + "spool-out");
		String files[] = directory.list();

		for (String temp : files) {
			File file = new File(directory, temp);
			BufferedReader in = null;
			String worker = inverseWorkerNames.get(temp);
			String entireURL = "http://" + worker + "/pushdata";
			URL url;
			try {
				url = new URL(entireURL);
				in = new BufferedReader(new FileReader(file));
				String line;
				StringBuffer answer = new StringBuffer();
				while ((line = in.readLine()) != null) {
					answer.append(line + "\n");
				}
				HttpURLConnection http = (HttpURLConnection) url
						.openConnection();
				http.setDoOutput(true);
				http.setUseCaches(false);
				http.setRequestMethod("POST");
				http.setRequestProperty("User-Agent", "Mozilla/6.0");
				http.setRequestProperty("Content-Type", "text/html");
				http.setRequestProperty("Content-Length",
						String.valueOf(answer.toString().length()));
				PrintWriter out = new PrintWriter(http.getOutputStream());

				out.write(answer.toString());
				out.flush();

				BufferedReader in1 = new BufferedReader(new InputStreamReader(
						http.getInputStream()));
				String line1;
				StringBuffer answer1 = new StringBuffer();
				while ((line1 = in1.readLine()) != null) {
					answer1.append(line);
				}
				out.close();

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void deleteDir(File directory) {
		if (directory.isDirectory()) {
			if (directory.list().length == 0) {
				directory.delete();
			} else {
				// list all contents
				String files[] = directory.list();

				for (String temp : files) {
					File fileDelete = new File(directory, temp);
					deleteDir(fileDelete);
				}
				if (directory.list().length == 0) {
					directory.delete();
				}
			}
		} else {
			// if file then delete file
			directory.delete();
		}

	}

	public void startThreads(String numThreads, String mapOrReduce) {
		// create and start Mapthreads
		int numMT = Integer.parseInt(numThreads);
		for (int i = 0; i < numMT; i++) {
			if (mapOrReduce.equals("map")) {
				ReadThread thread = new ReadThread("Thread# " + i);
				mapThreads.add(thread);
				thread.start();
			} else if (mapOrReduce.equals("reduce")) {
				ReduceThread thread = new ReduceThread("Thread# " + i);
				reduceThreads.add(thread);
				thread.start();
			}
		}
	}
	//this is the logic for reading during reduce phase
	public synchronized void synchronizedReduce() {
		// synchronized read line by line in file. and transfer to map function
		// firstRead()
		String line;
		String key;
		if (reader != null) {
			if (nextKey == null) {
				try {
					if (reader != null && (line = reader.readLine()) != null) {

						String[] keyVal = line.split("\t");
						key = keyVal[0];
						nextKey = key;
						value = keyVal[1];
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			key = nextKey;
			List<String> values = new ArrayList<String>();
			while (key.equals(nextKey)) {
				values.add(value);
				try {
					if ((line = reader.readLine()) != null) {
						String[] newKeyVal = line.split("\t");
						nextKey = newKeyVal[0];
						value = newKeyVal[1];
					} else {
						reader.close();
						reader = null;
						break;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			String[] strValues = new String[values.size()];
			strValues = values.toArray(strValues);
			wordCount.reduce(key, strValues, context);
		}
	}
	//logic for reading during reduce phase
	public synchronized void synchronizedRead() {
		// synchronized read line by line in file. and transfer to map function
		try {
			String line;
			if (reader != null && (line = reader.readLine()) != null) {
				if (!line.trim().equals("")) {
					String[] keyVal = line.split("\t");
					if (keyVal.length == 2) {
						context.setWorkerRanges(workerRanges);
						context.setWorkerNames(workerNames);
						keysRead += 1;
						// System.out.println("key = " + keyVal[0]+ "val = " +
						// keyVal[1]);
						wordCount.map(keyVal[0], keyVal[1], context);
						keysWritten += context.getWrites();
						context.setKeysWritten(0);
					}
				}
			} else {
				if (mapInputFiles.size() == 0) {
					reader = null;
				} else {
					reader.close();
					reader = new BufferedReader(new FileReader(
							mapInputFiles.remove(0)));
				}
				// add next file here.
			}
			// reader.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	//parses the input from master and sets variables appropriately
	public void parseRunMapVariables(String answer) {
		String[] parameters = answer.toString().split("&");
		for (String p : parameters) {

			String[] keyValue = p.split("=");
			if (keyValue[0].equals("job")) {
				jobClass = keyValue[1];
			} else if (keyValue[0].equals("input")) {
				inputDirectory = keyValue[1];
			} else if (keyValue[0].equals("output")) {
				outputDirectory = keyValue[1];
			} else if (keyValue[0].equals("map")) {
				numMapThreads = keyValue[1];
			} else if (keyValue[0].equals("reduce")) {
				numReduceThreads = keyValue[1];
			} else if (keyValue[0].equals("workers")) {
				String values = keyValue[1];
				String[] workerValues = values.split(";");
				for (String s : workerValues) {
					workers.add(s);
				}
				createHashRange();

			}
		}
	}
	//handles hash function
	public void createHashRange() {
		String size = String.valueOf(workers.size());
		BigInteger bi = new BigInteger(
				"1461501637330902918203684832716283019655932542976");
		BigInteger numWorkers = new BigInteger(size);
		bi = bi.divide(numWorkers);

		BigInteger[] intArr = new BigInteger[2];
		intArr[0] = new BigInteger("0");
		for (String s : workers) {
			BigInteger nextBI = bi.add(intArr[0]);
			intArr[1] = nextBI;
			workerRanges.put(s, intArr);
			intArr = new BigInteger[2];
			intArr[0] = nextBI;
		}
		int count = 0;
		for (String s : workers) {
			workerNames.put(s, String.valueOf(count));
			inverseWorkerNames.put(String.valueOf(count), s);
			count++;
		}
	}
	//sends worker status to the master
	public void sendWorkerStatus() {
		String entireURL = "http://" + master + "/workerstatus?port=" + port
				+ "&status=" + status + "&job=" + jobClass + "&keysWritten="
				+ keysWritten + "&keysRead=" + keysRead;
		URL url;
		try {
			url = new URL(entireURL);
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("GET");
			http.setRequestProperty("User-Agent", "Mozilla/5.0");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					http.getInputStream()));
			if (http != null) {
				String line;
				StringBuffer answer = new StringBuffer();
				while ((line = in.readLine()) != null) {
					answer.append(line);
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println("workerstatus failed. no connection");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
