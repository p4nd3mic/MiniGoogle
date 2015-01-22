package edu.upenn.cis455.webserver.exception;

/**
 * Exception when something wrong with socket conneciton
 * @author Ziyi Yang
 *
 */
public class HttpConnectionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6528627404400412094L;

	public HttpConnectionException() {
		super();
	}

	public HttpConnectionException(String message) {
		super(message);
	}
}
