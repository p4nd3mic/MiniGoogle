package edu.upenn.cis455.indexer;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class InvertedIndexEntity {
	@PrimaryKey(sequence = "ID")
	private int invertedIndex;

	@SecondaryKey(relate = Relationship.MANY_TO_ONE, relatedEntity = DocumentEntity.class)
	private int docId;

	private ArrayList<Hit> hitList;

	private double TF;
	
	
	private InvertedIndexEntity() {
	}

	public InvertedIndexEntity(int docId, double TF) {
		this.docId = docId;
		this.TF = TF;
		this.hitList = new ArrayList<Hit>();
	}

	public InvertedIndexEntity(int docId, ArrayList<Hit> hitList, double TF) {
		this.docId = docId;
		this.TF = TF;
		this.hitList = new ArrayList<Hit>(hitList);
	}

	public int getInvertedIndex() {
		return invertedIndex;
	}

	public int getDocId() {
		return docId;
	}

	public void setHitList(ArrayList<Hit> hitList) {
		this.hitList.clear();
		this.hitList = hitList;
	}

	public ArrayList<Hit> gethitList() {
		return new ArrayList<Hit>(hitList);
	}

	public int gethitListSize() {
		return hitList.size();
	}

	public double getTF() {
		return TF;
	}

	public void setTF(double tF) {
		TF = tF;
	}

}
