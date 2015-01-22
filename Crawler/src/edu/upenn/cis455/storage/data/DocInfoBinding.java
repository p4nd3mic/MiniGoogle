package edu.upenn.cis455.storage.data;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class DocInfoBinding extends TupleBinding<DocInfo> {

	@Override
	public DocInfo entryToObject(TupleInput ti) {
		String url = ti.readString();
		DocInfo document = new DocInfo(url);
		document.setCharsetName(ti.readString());
		document.setLastCheckDate(ti.readLong());
		String type = ti.readString();
		if(type != null && !type.isEmpty()) {
			document.setType(type);
		}
		return document;
	}

	@Override
	public void objectToEntry(DocInfo object, TupleOutput to) {
		to.writeString(object.getUrl());
		to.writeString(object.getCharsetName());
		to.writeLong(object.getLastCheckDate());
		String type = object.getType();
		if(type == null) {
			type = "";
		}
		to.writeString(type);
	}
}
