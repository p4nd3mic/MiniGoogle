package edu.upenn.cis455.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;

public class ResponseReader {
	
	public static final String TAG = ResponseReader.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	
	private LineReader mReader = new LineReader(null);

	/**
	 * Read text content from response
	 * @param response
	 * @return
	 * @throws HttpException
	 */
	public String readText(HttpResponse response) throws IOException {
		String text = null;
		String contentType = response.getHeader("Content-Type");
		String charset = getCharset(contentType);
		Charset cs = null;
		if(charset != null) {
			try {
				cs = Charset.forName(charset);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		
		byte[] bytes = readContent(response);
		if(cs != null) {
			text = new String(bytes, cs);
		} else {
			text = new String(bytes);
		}
		return text;
	}
	
	/**
	 * Read raw content from response
	 * @param response
	 * @return
	 * @throws HttpException
	 */
	public byte[] readContent(HttpResponse response) throws IOException {
		byte[] content = null;
		int contentLength = -1;
		String contentLengthStr = response.getHeader("Content-Length");
		if(contentLengthStr != null) {
			contentLength = Integer.parseInt(contentLengthStr);
		}
		InputStream input = response.getContent();
		if(contentLength != -1) {
			byte[] buf = new byte[contentLength];
			if(readContent(input, buf, contentLength) != contentLength) {
				throw new IOException("Cannot read the content");
			}
			content = buf;
		} else { // Maybe chunked
			String transEncoding = response.getHeader("Transfer-Encoding");
			if(transEncoding != null && transEncoding.equalsIgnoreCase("chunked")) {
				byte[] bytes = readChunkedContent(input);
				content = bytes;
			} else {
				byte[] bytes = readWholeContent(input);
				content = bytes;
			}
		}
		return content;
	}
	
	private static int readContent(InputStream input, byte[] buf, int len)
			throws IOException {
		int offset = 0;
		do {
			int inputLen = input.read(buf, offset, len - offset);
			if(inputLen < 0) {
				return offset;
			}
			offset += inputLen;
		} while(len - offset > 0);
		return len;
	}
	
	private static byte[] readWholeContent(InputStream input)
			throws IOException {
		byte[] buf = new byte[8192];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int len;
		while((len = input.read(buf)) != -1) {
			baos.write(buf, 0, len);
		}
		return baos.toByteArray();
	}
	
	private byte[] readChunkedContent(InputStream input) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		mReader.setInputStream(input);
		boolean end = false;
		while(!end) {
			String sizeStr = mReader.readLine();
			int semicolon = sizeStr.indexOf(';');
			if(semicolon >= 0) {
				sizeStr = sizeStr.substring(0, semicolon);
			}
			int size = Integer.parseInt(sizeStr, 16);
			if(size == 0) {
				end = true;
			} else {
				byte[] buf = new byte[size];
				readContent(input, buf, size);
				buffer.write(buf);
				mReader.readLine(); // next line
			}
		}
		return buffer.toByteArray();
	}
	
	public static String getCharset(String contentType) {
		String charset = null;
		if(contentType != null) {
			String[] types = contentType.split(";");
			if(types.length > 1) {
				for(int i = 1; i < types.length; i++) {
					String type = types[i].trim();
					int equals = type.indexOf('=');
					if(equals >= 0) {
						String name = type.substring(0, equals);
						String value = type.substring(equals + 1);
						if("charset".equalsIgnoreCase(name)) {
							charset = value;
							break;
						}
					}
				}
			}
		}
		return charset;
	}
}
