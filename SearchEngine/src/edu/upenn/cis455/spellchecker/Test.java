package edu.upenn.cis455.spellchecker;

import java.util.Scanner;

public class Test {
	public static void main(String[] args) {
		DBSingleton.setDbPath("/home/cis455/info/checkerDB");
		DatabaseWrapper wrapper = DBSingleton.getInstance().getWrapper();
//		wrapper.getHit("however");
		wrapper.increaseHit("pennsylvania");
		wrapper.increaseHit("pennsylvania");
		DBSingleton.getInstance().closeBDBstore();
//		
//		
		
		/*SuggestionWords suggest = new SuggestionWords();
		Scanner scanner = new Scanner(System.in);
		while(true){
			String str = scanner.nextLine();
			if(str != null)
				System.out.println(suggest.getWordsResult(str));
		}*/
	}
}
