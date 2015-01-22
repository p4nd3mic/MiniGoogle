package edu.upenn.cis455.storage.data;

import java.util.ArrayList;
import java.util.List;

public class RobotExclusion {

	private final String host;
	private List<Rule> ruleList = new ArrayList<Rule>();
	
	public RobotExclusion(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public List<Rule> getRules() {
		return ruleList;
	}

	public void addRule(Rule rules) {
		ruleList.add(rules);
	}

	public static class Rule {
		
		private String userAgent;
		private int crawlDelay = 0;
		private String[] disallow;
		
		public String getUserAgent() {
			return userAgent;
		}
		public void setUserAgent(String userAgent) {
			this.userAgent = userAgent;
		}
		public int getCrawlDelay() {
			return crawlDelay;
		}
		public void setCrawlDelay(int crawlDelay) {
			this.crawlDelay = crawlDelay;
		}
		public String[] getDisallow() {
			return disallow;
		}
		public void setDisallow(String[] disallow) {
			this.disallow = disallow;
		}
		
		public boolean isDisallowed(String path) {
			boolean disallowed = false;
			if(disallow != null && path != null) {
				for(String d : disallow) {
					if(path.startsWith(d)) {
						disallowed = true;
						break;
					}
				}
			}
			return disallowed;
		}
	}
}
