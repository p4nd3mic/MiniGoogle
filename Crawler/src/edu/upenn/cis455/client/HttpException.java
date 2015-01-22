package edu.upenn.cis455.client;

/**
 * Exception when something wrong with socket conneciton
 * @author Ziyi Yang
 *
 */
public class HttpException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6528627404400412094L;

	public HttpException() {
		super();
	}

	public HttpException(String message) {
		super(message);
	}

	public HttpException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public HttpException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
}
