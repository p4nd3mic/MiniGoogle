package edu.upenn.cis455.storage.data;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import edu.upenn.cis455.storage.data.RobotExclusion.Rule;

public class RobotExclusionBinding extends TupleBinding<RobotExclusion> {

	@Override
	public RobotExclusion entryToObject(TupleInput ti) {
		String host = ti.readString();
		RobotExclusion object = new RobotExclusion(host);
		int length = ti.readInt();
		for(int i = 0; i < length; i++) {
			object.addRule(readRule(ti));
		}
		return object;
	}

	@Override
	public void objectToEntry(RobotExclusion object, TupleOutput to) {
		to.writeString(object.getHost());
		to.writeInt(object.getRules().size());
		for(Rule rule : object.getRules()) {
			writeRule(rule, to);
		}
	}
	
	private Rule readRule(TupleInput ti) {
		Rule rule = new Rule();
		rule.setUserAgent(ti.readString());
		int length = ti.readInt();
		if(length > 0) {
			String[] disallow = new String[length];
			for(int i = 0; i < length; i++) {
				disallow[i] = ti.readString();
			}
			rule.setDisallow(disallow);
		}
		rule.setCrawlDelay(ti.readInt());
		return rule;
	}
	
	private void writeRule(Rule rule, TupleOutput to) {
		to.writeString(rule.getUserAgent());
		int length = -1;
		String[] disallow = rule.getDisallow();
		if(disallow != null) {
			length = disallow.length;
		}
		to.writeInt(length);
		if(length != -1) {
			for(int i = 0; i < length; i++) {
				to.writeString(disallow[i]);
			}
		}
		to.writeInt(rule.getCrawlDelay());
	}
}
