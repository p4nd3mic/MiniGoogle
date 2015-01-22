package edu.upenn.cis455.indexTest;

import java.util.HashSet;

import org.apache.hadoop.io.Text;

import edu.upenn.cis455.utility.GenerateHitListForImage;
import edu.upenn.cis455.utility.Stemmer;

public class TestForImage {
	public static void main(String[] args) {
		GenerateHitListForImage list = new GenerateHitListForImage(1, "1320video");
		
		String serialStr = list.serialize();
		
		// Ready for reduce
		String[] pieces = serialStr.split(";");

		for (String piece : pieces) {
			// System.out.println("piece: " + piece);
			String[] parts = piece.split("\\|");

			Stemmer stemmer = null;
			char chs[] = parts[0].toCharArray();
			stemmer = new Stemmer();
			stemmer.add(chs, chs.length);
			stemmer.stem();
			String modifiedWord = stemmer.toString();

			HashSet<String> candidates = new HashSet<String>();
			candidates.add(parts[0]);
			candidates.add(modifiedWord);

			for (String str : candidates) {
				System.out.println(str + " : " + parts[1]);
			}
		}
	}
}
