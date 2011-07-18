package org.zoodb.test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.zoodb.jdo.internal.Config;
import org.zoodb.jdo.internal.Util;

public class Test_100_FreeSpaceManager {

	private static final String DB_NAME = "TestDb";
	
	@Before
	public void before() {
		//Config.setFileManager(Config.FILE_MGR_IN_MEMORY);
		//Config.setFileProcessor(Config.FILE_PAF_BB_MAPPED_PAGE);
		//Config.setFilePageSize(Config.FILE_PAGE_SIZE_DEFAULT * 4);
		TestTools.createDb();
		TestTools.defineSchema(TestClass.class);
		//TestTools.defineSchema(TestClassTiny.class);
	}

	@After
	public void after() {
		TestTools.closePM();
	}
	
	@Test
	public void testObjectsRollback() {
		final int MAX = 1000;
		
		File f = new File(TestTools.getDbFileName());
		long len1 = f.length();

		//create some object
		PersistenceManager pm = TestTools.openPM();
		pm.currentTransaction().begin();
		for (int i = 0; i < MAX; i++) {
			TestClass tc = new TestClass();
		
			pm.makePersistent(tc);
			//Object oidP = pm.getObjectId(tc);
		}
		pm.currentTransaction().rollback();
		TestTools.closePM();

		assertEquals(len1, f.length());
	}
	
	@Test
	public void testObjectsReusePagesDeleted() {
		final int MAX = 10000;
		
		File f = new File(TestTools.getDbFileName());

		//First, create objects
		PersistenceManager pm = TestTools.openPM();
		pm.currentTransaction().begin();
		for (int i = 0; i < MAX; i++) {
			TestClass tc = new TestClass();
			pm.makePersistent(tc);
		}
		pm.currentTransaction().commit();
		
		
		pm.currentTransaction().begin();
		//now delete them
		Collection<TestClass> col = (Collection<TestClass>) pm.newQuery(TestClass.class).execute();
		for (TestClass tc: col) {
			pm.deletePersistent(tc);
		}
		pm.currentTransaction().commit();
		TestTools.closePM();


		//check length
		long len1 = f.length();

		//create objects
		pm = TestTools.openPM();
		pm.currentTransaction().begin();
		for (int i = 0; i < MAX; i++) {
			TestClass tc = new TestClass();
		
			pm.makePersistent(tc);
			//Object oidP = pm.getObjectId(tc);
		}
		pm.currentTransaction().commit();
		TestTools.closePM();
		
		//check that the new Objects reused previous pages
		//w/o FSM, the values were 274713 vs 401689
		//w/o #2 380920 / 524280
		assertTrue("l1=" + len1/1024 + " l2=" + f.length()/1024, len1*1.1 > f.length());
	}

	@Test
	public void testObjectsReusePagesDirtyObjects() {
		final int MAX = 1000;
		
		File f = new File(TestTools.getDbFileName());

		//First, create objects
		PersistenceManager pm = TestTools.openPM();
		pm.currentTransaction().begin();
		for (int i = 0; i < MAX; i++) {
			TestClass tc = new TestClass();
			pm.makePersistent(tc);
		}
		pm.currentTransaction().commit();
		TestTools.closePM();

		//now make them dirty
		pm = TestTools.openPM();
		pm.currentTransaction().begin();
		Collection<TestClass> col1 = (Collection<TestClass>) pm.newQuery(TestClass.class).execute();
		for (TestClass tc: col1) {
			JDOHelper.makeDirty(tc, null);
		}
		pm.currentTransaction().commit();
		TestTools.closePM();
		
		//check length
		long len1 = f.length();

		//now make them dirty again, this should reuse pages of the original objects
		pm = TestTools.openPM();
		pm.currentTransaction().begin();
		Collection<TestClass> col = (Collection<TestClass>) pm.newQuery(TestClass.class).execute();
		for (TestClass tc: col) {
			JDOHelper.makeDirty(tc, null);
		}
		pm.currentTransaction().commit();
		TestTools.closePM();
		
		//check that the new Objects reused previous pages
		//w/o FSM, the values were 274713 vs 401689
		//w/o #2 380920 / 524280
//		assertEquals(len1/1024, f.length()/1024);
		assertTrue("l1=" + len1/1024 + " l2=" + f.length()/1024, len1*1.1 > f.length());
	}

	@Test
	public void testObjectsReusePagesAfterCommitOnly() {
		final int MAX = 1000;
		
		File f = new File(TestTools.getDbFileName());

		//First, create objects
		PersistenceManager pm = TestTools.openPM();
		pm.currentTransaction().begin();
		for (int i = 0; i < MAX; i++) {
			TestClass tc = new TestClass();
			tc.setInt(14);
			pm.makePersistent(tc);
			//Object oidP = pm.getObjectId(tc);
		}
		pm.currentTransaction().commit();
		

		//check length
		long len1 = f.length();

		//now delete them and create new ones.
		pm.currentTransaction().begin();
		Collection<TestClass> col = (Collection<TestClass>) pm.newQuery(TestClass.class).execute();
		for (TestClass tc: col) {
			pm.deletePersistent(tc);
		}

		//create objects
		for (int i = 0; i < MAX; i++) {
			TestClass tc = new TestClass();
			tc.setInt(18);
			pm.makePersistent(tc);
			//Object oidP = pm.getObjectId(tc);
		}
		pm.currentTransaction().commit();

		//ensure that the new object got written and the previous one disappeared from the indices
		pm.currentTransaction().begin();
		col = (Collection<TestClass>) pm.newQuery(TestClass.class).execute();
		for (TestClass tc: col) {
			assertEquals(18, tc.getInt());
			pm.deletePersistent(tc);
		}
		pm.currentTransaction().rollback();
		
		
		TestTools.closePM();
		
		//check that the new Objects did NOT reuse previous pages
		//w/o FSM, the values were 258329 vs 381209
		assertTrue("l1=" + len1/1024 + " l2=" + f.length()/1024, len1*1.4 < f.length());
	}

	
	/**
	 * Test with multi-page objects
	 */
	@Test
	public void testObjectsReusePagesWithLargeObjects() {
		final int MAX = 10;
		final int SIZE = 100000;
		byte[] ba = new byte[SIZE];
		
		
		File f = new File(TestTools.getDbFileName());
		//First, create objects
		PersistenceManager pm = TestTools.openPM();
		pm.currentTransaction().begin();
		for (int i = 0; i < MAX; i++) {
			TestClass tc = new TestClass();
			tc.setByteArray(ba);
			pm.makePersistent(tc);
			//Object oidP = pm.getObjectId(tc);
		}
		pm.currentTransaction().commit();
		
		
		pm.currentTransaction().begin();
		//now delete them
		Collection<TestClass> col = (Collection<TestClass>) pm.newQuery(TestClass.class).execute();
		for (TestClass tc: col) {
			pm.deletePersistent(tc);
		}
		pm.currentTransaction().commit();
		TestTools.closePM();


		//check length
		long len1 = f.length();

		//create objects
		pm = TestTools.openPM();
		pm.currentTransaction().begin();
		for (int i = 0; i < MAX; i++) {
			TestClass tc = new TestClass();
			tc.setByteArray(ba);
			pm.makePersistent(tc);
			//Object oidP = pm.getObjectId(tc);
		}
		pm.currentTransaction().commit();
		TestTools.closePM();
		
		//check that the new Objects reused previous pages
		//w/o FSM, the values were 1179929 vs 2212121
		//assertEquals(len1/1024, f.length()/1024);
		assertTrue("l1=" + len1/1024 + " l2=" + f.length()/1024, len1*1.1 > f.length());
	}


	/**
	 * Test with multi-page objects
	 */
	@Test
	public void testObjectsDoNotReusePagesWithOverlappingObjects() {
		final int MAX = 100;
		final int SIZE = 10000;  //multi-page object must be likely
		int nTotal = 0;
		byte[] ba1 = new byte[SIZE];
		byte[] ba2 = new byte[SIZE];
		for (int i = 0; i < SIZE; i++) {
			ba1[i] = 11;
			ba2[i] = 13;
		}
		
		
		//First, create objects
		PersistenceManager pm = TestTools.openPM();
		pm.currentTransaction().begin();
		for (int i = 0; i < MAX; i++) {
			TestClass tc = new TestClass();
			tc.setByteArray(ba1);
			pm.makePersistent(tc);
			nTotal++;
			//Object oidP = pm.getObjectId(tc);
		}
		pm.currentTransaction().commit();
		
		pm.currentTransaction().begin();
		//now delete them
		Collection<TestClass> col = (Collection<TestClass>) pm.newQuery(TestClass.class).execute();
		int n = 0;
		for (TestClass tc: col) {
			if ((n++ % 2) == 0) {
				pm.deletePersistent(tc);
				nTotal--;
			}
		}
		pm.currentTransaction().commit();
		TestTools.closePM();

		//create objects
		pm = TestTools.openPM();
		pm.currentTransaction().begin();
		for (int i = 0; i < MAX; i++) {
			TestClass tc = new TestClass();
			tc.setByteArray(ba2);
			pm.makePersistent(tc);
			nTotal++;
		}
		pm.currentTransaction().commit();
		TestTools.closePM();

		// now check objects
		pm = TestTools.openPM();
		pm.currentTransaction().begin();
		col = (Collection<TestClass>) pm.newQuery(TestClass.class).execute();
		int i = 0;
		n = 0;
		for (TestClass tc: col) {
			n++;
			byte[] ba = tc.getBytaArray();
			int b0 = ba[0];
			for (byte b2: ba) {
				assertEquals(b0, b2);
			}
		}
		assertEquals(nTotal, n);
		pm.currentTransaction().commit();
		TestTools.closePM();
	}
	
	@AfterClass
	public static void tearDown() {
		TestTools.removeDb(DB_NAME);
		Config.setFilePageSize(Config.FILE_PAGE_SIZE_DEFAULT);
	}
	
}
