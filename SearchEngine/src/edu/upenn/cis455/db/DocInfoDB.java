package edu.upenn.cis455.db;

import java.io.Closeable;

import org.apache.log4j.Logger;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import edu.upenn.cis455.db.data.DocInfo;
import edu.upenn.cis455.db.data.DocInfoBinding;
import edu.upenn.cis455.db.data.ImageInfo;
import edu.upenn.cis455.db.data.VideoInfo;

public class DocInfoDB implements Closeable {

	public static final String TAG = DocInfoDB.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	public static final String UTF_8 = "UTF-8";
	
	private static final String DB_DOCUMENT = "docs_db";
	private static final String DB_IMAGE = "image_db";
	private static final String DB_VIDEO = "video_db";
//	private static final String DB_FINGERPRINTS = "docs_fp_db";
//	private static final String DB_DOCUMENT_CHECK_DATE = "docs_check_date_db";
//	private static final String FILE_FINGERPRINTS = "docs_fp";
//	private static final int MAX_FINGERPRINTS = 10000;
	
	private Database mDocDb;
	private Database mImageDb;
	private Database mVideoDb;
	private DocInfoBinding mDocBinding = new DocInfoBinding();
	private ImageInfo.Binding mImageBinding = new ImageInfo.Binding();
	private VideoInfo.Binding mVideoBinding = new VideoInfo.Binding();
	
	public DocInfoDB(Environment env, DatabaseConfig config) {
//		mEnv = env;
		mDocDb = env.openDatabase(null, DB_DOCUMENT, config);
		mImageDb = env.openDatabase(null, DB_IMAGE, config);
		mVideoDb = env.openDatabase(null, DB_VIDEO, config);
	}
	
	/**
	 * Get document object by its url
	 * @param url
	 * @return
	 */
	public DocInfo getDocument(String url) {
		DocInfo document = null;
		try {
			DatabaseEntry key = new DatabaseEntry(url.getBytes(UTF_8));
			DatabaseEntry data = new DatabaseEntry();
			if(mDocDb.get(null, key, data, LockMode.DEFAULT)
					== OperationStatus.SUCCESS) {
				document = mDocBinding.entryToObject(data);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return document;
	}

	/**
	 * Save document object into database, create if not exist
	 * @param document
	 * @return
	 */
	public boolean saveDocument(DocInfo document) {
		boolean result = false;
		try {
			DatabaseEntry key = new DatabaseEntry(document.getUrl().getBytes(UTF_8));
			DatabaseEntry data = new DatabaseEntry();
			mDocBinding.objectToEntry(document, data);
			if(mDocDb.put(null, key, data) == OperationStatus.SUCCESS) {
				result = true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}
	
	public int getDocumentCount() {
		int count = 0;
		Cursor cur = null;
		try {
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			cur = mDocDb.openCursor(null, null);
			while(cur.getNext(key, data, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
				count++;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if(cur != null) {
				cur.close();
			}
		}
		return count;
	}
	
	public boolean saveImage(ImageInfo imageInfo) {
		boolean result = false;
		try {
			DatabaseEntry key = new DatabaseEntry(imageInfo.getUrl().getBytes(UTF_8));
			DatabaseEntry data = new DatabaseEntry();
			mImageBinding.objectToEntry(imageInfo, data);
			if(mImageDb.put(null, key, data)
					== OperationStatus.SUCCESS) {
				result = true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}
	
	public int getImageCount() {
		int count = 0;
		Cursor cur = null;
		try {
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			cur = mImageDb.openCursor(null, null);
			while(cur.getNext(key, data, LockMode.READ_UNCOMMITTED)
					== OperationStatus.SUCCESS) {
				count++;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if(cur != null) {
				cur.close();
			}
		}
		return count;
	}
	
	public boolean saveVideo(VideoInfo videoInfo) {
		boolean result = false;
		try {
			DatabaseEntry key = new DatabaseEntry(videoInfo.getUrl().getBytes(UTF_8));
			DatabaseEntry data = new DatabaseEntry();
			mVideoBinding.objectToEntry(videoInfo, data);
			if(mVideoDb.put(null, key, data) == OperationStatus.SUCCESS) {
				result = true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}
	
	public int getVideoCount() {
		int count = 0;
		Cursor cur = null;
		try {
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			cur = mVideoDb.openCursor(null, null);
			while(cur.getNext(key, data, LockMode.READ_UNCOMMITTED)
					== OperationStatus.SUCCESS) {
				count++;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if(cur != null) {
				cur.close();
			}
		}
		return count;
	}
	
	@Override
	public void close() {
		mDocDb.close();
		mImageDb.close();
		mVideoDb.close();
	}
}
