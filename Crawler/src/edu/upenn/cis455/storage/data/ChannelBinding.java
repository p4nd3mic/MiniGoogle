package edu.upenn.cis455.storage.data;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ChannelBinding extends TupleBinding<Channel> {

	@Override
	public Channel entryToObject(TupleInput ti) {
		String name = ti.readString();
		Channel channel = new Channel(name);
		channel.setXslUrl(ti.readString());
		channel.setUsername(ti.readString());
		
		int length = ti.readInt();
		if(length > 0) {
			String[] xpaths = new String[length];
			for(int i = 0; i < length; i++) {
				xpaths[i] = ti.readString();
			}
			channel.setXpaths(xpaths);
		}
		return channel;
	}

	@Override
	public void objectToEntry(Channel object, TupleOutput to) {
		to.writeString(object.getName());
		to.writeString(object.getXslUrl());
		to.writeString(object.getUsername());
		
		String[] xpaths = object.getXpaths();
		int length = 0;
		if(xpaths != null) {
			length = xpaths.length;
		}
		to.writeInt(length);
		for(int i = 0; i < length; i++) {
			to.writeString(xpaths[i]);
		}
	}
}
