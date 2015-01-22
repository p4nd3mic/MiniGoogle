package edu.upenn.cis455.webserver;

/**
 * Common constants for HTTP protocol
 * @author Ziyi Yang
 *
 */
public class Const {
	
	public static final String CRLF = "\r\n";
	public static final String LF = "\n";
	public static final String CR = "\r";
	public static final String VERSION_1_0 = "HTTP/1.0";
	public static final String VERSION_1_1 = "HTTP/1.1";
	
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";
	public static final String METHOD_HEAD = "HEAD";
	
	public static final String[] SUPPORTED_METHODS = {
		METHOD_GET,
		METHOD_POST,
		METHOD_HEAD
	};
	
	// 2XX: generally "OK"
    // 3XX: relocation/redirect
    // 4XX: client error
    // 5XX: server error
	public static final int HTTP_CONTINUE = 100;
    /**
     * Numeric status code, 202: Accepted
     */
    public static final int HTTP_ACCEPTED = 202;

    /**
     * Numeric status code, 502: Bad Gateway
     */
    public static final int HTTP_BAD_GATEWAY = 502;

    /**
     * Numeric status code, 405: Bad Method
     */
    public static final int HTTP_BAD_METHOD = 405;

    /**
     * Numeric status code, 400: Bad Request
     */
    public static final int HTTP_BAD_REQUEST = 400;

    /**
     * Numeric status code, 408: Client Timeout
     */
    public static final int HTTP_CLIENT_TIMEOUT = 408;

    /**
     * Numeric status code, 409: Conflict
     */
    public static final int HTTP_CONFLICT = 409;

    /**
     * Numeric status code, 201: Created
     */
    public static final int HTTP_CREATED = 201;

    /**
     * Numeric status code, 413: Entity too large
     */
    public static final int HTTP_ENTITY_TOO_LARGE = 413;

    /**
     * Numeric status code, 403: Forbidden
     */
    public static final int HTTP_FORBIDDEN = 403;

    /**
     * Numeric status code, 504: Gateway timeout
     */
    public static final int HTTP_GATEWAY_TIMEOUT = 504;

    /**
     * Numeric status code, 410: Gone
     */
    public static final int HTTP_GONE = 410;

    /**
     * Numeric status code, 500: Internal error
     */
    public static final int HTTP_INTERNAL_ERROR = 500;

    /**
     * Numeric status code, 411: Length required
     */
    public static final int HTTP_LENGTH_REQUIRED = 411;

    /**
     * Numeric status code, 301 Moved permanently
     */
    public static final int HTTP_MOVED_PERM = 301;

    /**
     * Numeric status code, 302: Moved temporarily
     */
    public static final int HTTP_MOVED_TEMP = 302;

    /**
     * Numeric status code, 300: Multiple choices
     */
    public static final int HTTP_MULT_CHOICE = 300;

    /**
     * Numeric status code, 204: No content
     */
    public static final int HTTP_NO_CONTENT = 204;

    /**
     * Numeric status code, 406: Not acceptable
     */
    public static final int HTTP_NOT_ACCEPTABLE = 406;

    /**
     * Numeric status code, 203: Not authoritative
     */
    public static final int HTTP_NOT_AUTHORITATIVE = 203;

    /**
     * Numeric status code, 404: Not found
     */
    public static final int HTTP_NOT_FOUND = 404;

    /**
     * Numeric status code, 501: Not implemented
     */
    public static final int HTTP_NOT_IMPLEMENTED = 501;

    /**
     * Numeric status code, 304: Not modified
     */
    public static final int HTTP_NOT_MODIFIED = 304;

    /**
     * Numeric status code, 200: OK
     */
    public static final int HTTP_OK = 200;

    /**
     * Numeric status code, 206: Partial
     */
    public static final int HTTP_PARTIAL = 206;

    /**
     * Numeric status code, 402: Payment required
     */
    public static final int HTTP_PAYMENT_REQUIRED = 402;

    /**
     * Numeric status code, 412: Precondition failed
     */
    public static final int HTTP_PRECONDITION_FAILED = 412;

    /**
     * Numeric status code, 407: Proxy authentication required
     */
    public static final int HTTP_PROXY_AUTH = 407;

    /**
     * Numeric status code, 414: Request too long
     */
    public static final int HTTP_REQ_TOO_LONG = 414;

    /**
     * Numeric status code, 205: Reset
     */
    public static final int HTTP_RESET = 205;

    /**
     * Numeric status code, 303: See other
     */
    public static final int HTTP_SEE_OTHER = 303;
    
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505;
    
    /*********** Http Headers ************/
    public static final String HEADER_DATE = "Date";
    public static final String HEADER_EXPECT = "Expect";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_SERVER = "Server";
    public static final String HEADER_CONNECTION = "Connection";
    public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
    public static final String HEADER_IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";
    public static final String HEADER_HOST = "Host";
    public static final String HEADER_TRANSFER_ENCODING = "Transfer-Encoding";
    
	public static String getStatusMessage(int statusCode) {
		switch(statusCode) {
		case 100: return "Continue";
		case 200: return "OK";
		case 304: return "Not Modified";
		case 404: return "Not Found";
		case 400: return "Bad Request";
		case 405: return "Unsupported Method";
		case 412: return "Precondition Failed";
		case 505: return "HTTP Version Not Supported";
		case 500: return "Internal Server Error";
		case 501: return "Not Implemented";
		default: return "";
		}
	}
}
