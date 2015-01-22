package edu.upenn.cis455.storage;

import java.io.Closeable;

import org.apache.log4j.Logger;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import edu.upenn.cis455.storage.data.DocInfo;
import edu.upenn.cis455.storage.data.DocInfoBinding;

public class DocumentStorage implements Closeable {

	public static final String TAG = DocumentStorage.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);
	public static final String UTF_8 = "UTF-8";
	private static final String DB_DOCUMENT = "docs_db";
//	private static final String DB_FINGERPRINTS = "docs_fp_db";
//	private static final String DB_DOCUMENT_CHECK_DATE = "docs_check_date_db";
//	private static final String FILE_FINGERPRINTS = "docs_fp";
//	private static final int MAX_FINGERPRINTS = 10000;
	
	private Environment mEnv;
	private Database mDocDb;
//	private Database mDocCheckDateDb;
	private Database mDocFpDb;
	private DocInfoBinding mBinding = new DocInfoBinding();
//	private TupleBinding<Long> mDateBinding = TupleBinding.getPrimitiveBinding(Long.class);
//	private Set<String> mFingerPrints = new LinkedHashSet<String>(MAX_FINGERPRINTS);
//	private File mFpFile = null; // Fingerprint file
//	private boolean mMerged = false;
	
	public DocumentStorage(Environment env, DatabaseConfig config) {
		mEnv = env;
		mDocDb = env.openDatabase(null, DB_DOCUMENT, config);
//		mDocFpDb = env.openDatabase(null, DB_FINGERPRINTS, config);
//		mDocCheckDateDb = env.openDatabase(null, DB_DOCUMENT_CHECK_DATE, config);
//		setupFingerPrintFile(env.getHome());
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
				document = mBinding.entryToObject(data);
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
			mBinding.objectToEntry(document, data);
			if(mDocDb.put(null, key, data) == OperationStatus.SUCCESS) {
				result = true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}
	
//	public long getLastCheckDate(String docUrl) {
//		long date = -1;
//		try {
//			DatabaseEntry key = new DatabaseEntry(docUrl.getBytes(UTF_8));
//			DatabaseEntry data = new DatabaseEntry();
//			if(mDocCheckDateDb.get(null, key, data, LockMode.DEFAULT)
//					== OperationStatus.SUCCESS) {
//				date = mDateBinding.entryToObject(data);
//			}
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		}
//		return date;
//	}
//	
//	/**
//	 * Update the document's last checked date
//	 * @param docUrl
//	 * @param date
//	 * @return
//	 */
//	public boolean updateLastCheckDate(String docUrl, long date) {
//		boolean result = false;
//		if(getDocument(docUrl) != null) {
//			try {
//				DatabaseEntry key = new DatabaseEntry(docUrl.getBytes(UTF_8));
//				DatabaseEntry data = new DatabaseEntry();
//				mDateBinding.objectToEntry(date, data);
//				if(mDocCheckDateDb.put(null, key, data) == OperationStatus.SUCCESS) {
//					result = true;
//				}
//			} catch (Exception e) {
//				logger.error(e.getMessage(), e);
//			}
//		}
//		return result;
//	}

	
	
	@Override
	public void close() {
		mDocDb.close();
//		mDocCheckDateDb.close();
//		try { // Delete finger print file
//			if(mFpFile != null && mFpFile.exists()) {
//				mFpFile.delete();
//			}
//		} catch (Exception e) {
//			logger.error(e.getMessage());
//		}
	}
	
//	private void setupFingerPrintFile(File envHome) {
//		mFpFile = new File(envHome, FILE_FINGERPRINTS);
//		try {
//			if(mFpFile.exists()) { // Delete if exists
//				mFpFile.delete();
//			}
//			mFpFile.createNewFile();
//		} catch (IOException e) {
//			logger.error(e.getMessage(), e);
//		}
//	}
	
	// Find fingerprint in file
//	private boolean findFingerPrint(String fp) {
//		if(fp == null) {
//			return false;
//		}
//		boolean result = false;
//		BufferedReader reader = null;
//		try {
//			if(mFpFile.exists()) {
//				reader = new BufferedReader(new FileReader(mFpFile));
//				String line = null;
//				while((line = reader.readLine()) != null) { // Exhaustive search 
//					if(fp.equals(line)) {
//						result = true;
//						break;
//					}
//				}
//			}
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		} finally {
//			closeObject(reader);
//		}
//		return result;
//	}
	
//	// Merge in-memory fingerprints into file
//	private void mergeFingerPrintsIntoFile() {
//		mMerged = true;
//		PrintWriter writer = null;
//		try { // Append to file
//			writer = new PrintWriter(new FileWriter(mFpFile, true));
//			for(String fp : mFingerPrints) {
//				writer.println(fp);
//			}
//			writer.flush();
//		} catch (IOException e) {
//			logger.error(e.getMessage(), e);
//		} finally {
//			closeObject(writer);
//		}
//	}
}
