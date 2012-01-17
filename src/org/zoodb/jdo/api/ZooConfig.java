package org.zoodb.jdo.api;

public class ZooConfig {
	
	public static final int MODEL_1P = 1; 
	public static final int MODEL_2P = 2;
	public static final int MODEL = MODEL_1P;

	private static final String preServer = "org.zoodb.jdo.internal.server.";
	private static final String preJdo = "org.zoodb.jdo.";
	
	public static final String FILE_PAF_BB = preServer + "PageAccessFile_BB";
	//public static final String FILE_PAF_BB = preServer + "PageAccessFileInMemory";
	public static final String FILE_PAF_BB_MAPPED_PAGE = preServer + "PageAccessFile_BBMappedPage";
	public static final String FILE_PAF_MAPPED_BB = preServer + "PageAccessFile_MappedBB";
	public static final String FILE_PAF_IN_MEMORY = preServer + "PageAccessFileInMemory";

	public static final String FILE_MGR_IN_MEMORY = preJdo + "api.impl.DataStoreManagerInMemory";
	public static final String FILE_MGR_ONE_FILE = preJdo + "api.impl.DataStoreManagerOneFile";
	//public static final String FILE_MGR_ONE_FILE = FILE_MGR_IN_MEMORY; 

	public static final int FILE_PAGE_SIZE_DEFAULT = 1024*4;  //bytes

	
	private static String fileDefault = FILE_PAF_BB;
	private static String fileManagerDefault = FILE_MGR_ONE_FILE;
	private static int defaultPageSize = FILE_PAGE_SIZE_DEFAULT;

	public static void setDefaults() {
		fileDefault = FILE_PAF_BB;
		fileManagerDefault = FILE_MGR_ONE_FILE;
		defaultPageSize = FILE_PAGE_SIZE_DEFAULT;
	}
	
	public static void setFileManager(String className) {
		fileManagerDefault = className;
		if (className.equals(FILE_MGR_ONE_FILE)) {
			fileDefault = FILE_PAF_BB;
		} else if (className.equals(FILE_MGR_IN_MEMORY)) {
			fileDefault = FILE_PAF_IN_MEMORY;
		}
	}

	public static String getFileProcessor() {
		return fileDefault;
	}

	public static String getFileManager() {
		return fileManagerDefault;
	}

	public static int getFilePageSize() {
		return defaultPageSize;
	}

	/**
	 * 
	 * @param i page size in bytes.
	 */
	public static void setFilePageSize(int pageSize) {
		defaultPageSize = pageSize;
	}
}