package edu.upenn.cis455.spellchecker;

public class WordDis implements Comparable<WordDis> {
	private WordTemp temp;
	private int distance;

	public WordDis(WordTemp temp, int distance) {
		this.temp = temp;
		this.distance = distance;
	}

	public WordTemp getWordTemp() {
		return temp;
	}

	public int getDistance() {
		return distance;
	}

	// IMPORTANT: Descend
	@Override
	public int compareTo(WordDis o) {
		double oDistance = o.getDistance();
		double oHit = o.getWordTemp().getHit();
		double oResult = oDistance / oHit;

		double tDistance = getDistance();
		double tHit = getWordTemp().getHit();
		double tResult = tDistance / tHit;

		if (tResult > oResult) {
			return 1;
		} else if (tResult < oResult) {
			return -1;
		} else {
			return 0;
		}
	}
}
