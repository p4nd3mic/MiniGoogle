package edu.upenn.cis455.spellchecker;

import java.util.Scanner;

public class Test {
	public static void main(String[] args) {
		DBSingleton.setDbPath("/Users/xuxu/Desktop/db");
//		DatabaseWrapper wrapper = DBSingleton.getInstance().getWrapper();
//		wrapper.getHit("however");
//		wrapper.increaseHit("however");
//		wrapper.increaseHit("however");
//		DBSingleton.getInstance().closeBDBstore();
		
		
		
		SuggestionWords suggest = new SuggestionWords();
		Scanner scanner = new Scanner(System.in);
		while(true){
			String str = scanner.nextLine();
			if(str != null)
				System.out.println(suggest.getWordsResult(str));
		}
	}
}
