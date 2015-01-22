package edu.upenn.cis455.storage.data;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class StringListBinding extends TupleBinding<List<String>> {

	@Override
	public List<String> entryToObject(TupleInput ti) {
		int length = ti.readInt();
		List<String> set = new ArrayList<String>();
		for(int i = 0; i < length; i++) {
			set.add(ti.readString());
		}
		return set;
	}

	@Override
	public void objectToEntry(List<String> object, TupleOutput to) {
		int length = object.size();
		to.writeInt(length);
		for(String str : object) {
			to.writeString(str);
		}
	}

}
