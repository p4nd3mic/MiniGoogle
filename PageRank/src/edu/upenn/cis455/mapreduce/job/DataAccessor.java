package edu.upenn.cis455.mapreduce.job;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

	public class DataAccessor {
		PrimaryIndex<String,Ranks> RanksPIdx;

		public DataAccessor(EntityStore store) throws DatabaseException {
			RanksPIdx = store.getPrimaryIndex(String.class, Ranks.class);

		}
}
