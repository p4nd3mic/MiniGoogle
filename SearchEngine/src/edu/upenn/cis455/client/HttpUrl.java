package edu.upenn.cis455.client;

public class HttpUrl {
	
	public static final String SCHEME_DELIM = "://";
	
	private String url;
	private String scheme;
	private String host;
	private int port;
	private String path;
	
	public HttpUrl() {}
	
	public HttpUrl(HttpUrl url) {
		if(url != null) {
			this.url = url.getUrl();
			this.scheme = url.getScheme();
			this.host = url.getHost();
			this.port = url.getPort();
			this.path = url.getPath();
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getCanonicalUrl() {
		StringBuilder sb = new StringBuilder();
		sb.append(scheme)
		.append(SCHEME_DELIM)
		.append(host);
		if(port != 80) {
			sb.append(':').append(port);
		}
		sb.append(path);
		return sb.toString();
	}
	
	public String getHostPort() {
		if(port == 80) {
			return host;
		} else {
			return host + ":" + port;
		}
	}
	
	public static HttpUrl parseUrl(String url) {
		HttpUrl httpUrl = new HttpUrl();
		httpUrl.doParseUrl(url);
		return httpUrl;
	}
	
	private void doParseUrl(String url) {
		this.url = url;
		// Parse scheme
		String scheme = "http";
		String uri = url;
		int schemeInd = url.indexOf(SCHEME_DELIM);
		if(schemeInd != -1) {
			scheme = url.substring(0, schemeInd);
			uri = url.substring(schemeInd + SCHEME_DELIM.length());
		}
		this.scheme = scheme;
		
		// Parse path
		String host = uri;
		String path = "/";
		int slash = host.indexOf('/');
		if(slash != -1) {
			host = uri.substring(0, slash);
			path = uri.substring(slash);
		}
		this.path = path;
		
		// Parse port
		int port = 80;
		int colon = host.indexOf(':');
		if(colon != -1) {
			try {
				port = Integer.parseInt(host.substring(colon + 1));
			} catch (NumberFormatException e) {
			}
			host = host.substring(0, colon);
		}
		this.host = host;
		this.port = port;
	}

	@Override
	public String toString() {
		return getCanonicalUrl();
	}

	@Override
	public boolean equals(Object obj) {
		HttpUrl otherUrl = (HttpUrl) obj;
		return getCanonicalUrl().equals(otherUrl.getCanonicalUrl());
	}

	@Override
	public int hashCode() {
		return getCanonicalUrl().hashCode();
	}
}
