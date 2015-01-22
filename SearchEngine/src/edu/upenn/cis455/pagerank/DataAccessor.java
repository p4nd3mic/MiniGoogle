package edu.upenn.cis455.pagerank;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

	public class DataAccessor {
		PrimaryIndex<String,Ranks> RanksPIdx;
		PrimaryIndex<String,Ranks> SinksPIdx;


		public DataAccessor(EntityStore store) throws DatabaseException {
			RanksPIdx = store.getPrimaryIndex(String.class, Ranks.class);
			SinksPIdx = store.getPrimaryIndex(String.class, Ranks.class);
		}
}
