package org.zoodb.test.util;

import java.util.Properties;

import javax.jdo.JDOException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.spi.PersistenceCapable;

import org.zoodb.jdo.api.ZooHelper;
import org.zoodb.jdo.api.ZooJdoProperties;
import org.zoodb.jdo.api.ZooSchema;

public class TestTools {

	private static final String DB_NAME = "TestDb";
	private static PersistenceManager pm;

	public static void createDb(String dbName) {
		if (ZooHelper.getDataStoreManager().dbExists(dbName)) {
			removeDb(dbName);
		}
		ZooHelper.getDataStoreManager().createDb(dbName);
	}
	

	/**
	 * Create the default database.
	 */
	public static void createDb() {
		createDb(DB_NAME);
	}
	
	
	/**
	 * Remove the default database.
	 */
	public static void removeDb() {
		removeDb(DB_NAME);
	}
	
	
	public static void removeDb(String dbName) {
		if (!ZooHelper.getDataStoreManager().dbExists(dbName)) {
			return;
		}
		
		try {
			closePM();
		} catch (JDOException e) {
			e.printStackTrace();
		}
        //TODO implement loop ala http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4724038 ?
        //-> Entry from 23-MARCH-2005 #2
//      for (int i = 0; i < 100; i++) {
//          System.gc();
//          System.runFinalization();
//          //Thread.sleep(100);
//      }
		try {
			ZooHelper.getDataStoreManager().removeDb(dbName);
		} catch (JDOUserException e) {
			//ignore
		}
	}

	/**
	 * Varargs does not seem to work with generics.
	 * @param classes
	 */
    public static void defineSchema(Class<?> ... classes) {
        defineSchema(DB_NAME, classes);
    }
	
    
	public static void defineSchema(String dbName, Class<?> ... classes) {
		Properties props = new ZooJdoProperties(dbName);
		PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(props);
		PersistenceManager pm = null;
		try {
		    pm = pmf.getPersistenceManager();

	        pm.currentTransaction().begin();
	        
	        for (Class<?> cls: classes) {
	        	ZooSchema.create(pm, cls);
	        }

	        pm.currentTransaction().commit();
		} finally {
			safeClose(pmf, pm);
		}
	}

	public static void removeSchema(String dbName, Class<? extends PersistenceCapable> cls) {
		Properties props = new ZooJdoProperties(dbName);
		PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(props);
        PersistenceManager pm = null;
        try {
            pm = pmf.getPersistenceManager();
            ZooSchema.locate(pm, cls, dbName).remove();
            pm.currentTransaction().commit();
        } finally {
			safeClose(pmf, pm);
        }
	}


	public static PersistenceManager openPM() {
		Properties props = new ZooJdoProperties(DB_NAME);
		PersistenceManagerFactory pmf = 
			JDOHelper.getPersistenceManagerFactory(props);
		pm = pmf.getPersistenceManager();
		return pm;
	}


	public static void closePM(PersistenceManager pm) {
		PersistenceManagerFactory pmf = pm.getPersistenceManagerFactory();
		if (!pm.isClosed()) {
			if (pm.currentTransaction().isActive()) {
				pm.currentTransaction().rollback();
			}
			pm.close();
		}
		pmf.close();
		TestTools.pm = null;
	}


	public static void closePM() {
		if (pm != null)
			closePM(pm);
	}


	public static String getDbFileName() {
		return ZooHelper.getDataStoreManager().getDbPath(DB_NAME);
	}


	public static void dropInstances(Class<?> ... classes) {
		Properties props = new ZooJdoProperties(DB_NAME);
		PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(props);
		PersistenceManager pm = null;
		try {
		    pm = pmf.getPersistenceManager();

	        pm.currentTransaction().begin();
	        
	        for (Class<?> cls: classes) {
	        	ZooSchema.locate(pm, cls).dropInstances();
	        }

	        pm.currentTransaction().commit();
		} finally {
			safeClose(pmf, pm);
		}
	}
	
	private static void safeClose(PersistenceManagerFactory pmf, PersistenceManager pm) {
	    if (pm != null) {
	    	if (pm.currentTransaction().isActive()) {
	    		pm.currentTransaction().rollback();
	    	}
	        pm.close();
	    }
        if (pmf != null) {
            pmf.close();
        }
	}
}
