package edu.upenn.cis455.mapreduce.job;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class FormatFile {
	public static void main(String[] args) {
	  	try {

		File f = new File("/home/cis455/storage1/pagerank/1");
	  	BufferedReader br = new BufferedReader(new FileReader(f));
	  	String line;
	  	StringBuffer sb = new StringBuffer();
		Gson gson = new Gson();
		String[] keyVal;
		Type listType = new TypeToken<List<String>>() {}.getType();
		File newFile = new File("/home/cis455/storage1/input/input.txt");
		if (newFile.exists()) {
			newFile.delete();
		}
		newFile.createNewFile();
			while ((line = br.readLine())!=null) {
				keyVal = line.split("\t");
				List<String> yourList = new Gson().fromJson(keyVal[1], listType);
				
				for (String s : yourList) {
					System.out.println("key: " + keyVal[0]+" val: " + s);

					FileWriter fw = new FileWriter(newFile.getAbsoluteFile(),true);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(keyVal[0]+"\t"+s+"\n");
					bw.close();
				}
			}
		} catch (JsonSyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
