package org.zoodb.test.index2.performance;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.zoodb.jdo.ZooJdoHelper;
import org.zoodb.tools.ZooHelper;

/* 
 * TODO: include open/close db into time measurement?
 */

public class PerformanceTest {
	
	public static void main(String[] args) {
		new PerformanceTest();
	}

	public class Tuple<X, Y> { 
          public final X x; 
          public final Y y; 
          public Tuple(X x, Y y) { 
            this.x = x; 
            this.y = y; 
          } 
	} 
	
	ArrayList<Tuple<Integer, String>> carOwners = new ArrayList<Tuple<Integer, String>>();

	public PerformanceTest() {
        String dbName = "ex3.zdb";
        String fileName = "./tst/org/zoodb/test/index2/performance/performanceTest.csv";

		ArrayList<ArrayList<Long>> results = new ArrayList<ArrayList<Long>>();

		int maxElementsNotIndexed = 5000;
		Integer[] numElementsArray = {10,20,50,100,200,500,1000,2000,5000,10000,20000,50000,100000,200000};
		ArrayList<Integer> numElements = new ArrayList<Integer>(Arrays.asList(numElementsArray));
		for(Integer elements : numElements) {
			ArrayList<Long> iterationResult = new ArrayList<Long>();
			System.out.println("Elements: "+Integer.toString(elements));
			iterationResult.add(new Long(elements));
			createDB(dbName);
			iterationResult.add(populateDB(dbName,elements));
			System.out.println("\tPopulated database\t" + Long.toString(iterationResult.get(1))  + "ms");
			if(elements <= maxElementsNotIndexed) {
				iterationResult.add(readDB(dbName));
                System.out.println("\tQueried all elements\t" + Long.toString(iterationResult.get(2))+ "ms");
			} else {
				iterationResult.add(-1L);
			}
			iterationResult.add(buildIndex(dbName));
			System.out.println("\tBuilt index\t\t" + Long.toString(iterationResult.get(3)) + "ms");
			iterationResult.add(readDB(dbName));
            System.out.println("\tQueried all elements\t" + Long.toString(iterationResult.get(4))+ "ms");
            
            results.add(iterationResult);

		}

		// write results to file
		try {
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName));
		
			//header
			fileWriter.write("numElements,populate,regularQuery,index,indexedQuery");
			fileWriter.newLine();
			
			for(ArrayList<Long> l : results){ 
				fileWriter.write(Long.toString(l.get(0)) + ","
									+ Long.toString(l.get(1)) + "," 
									+ (l.get(2) > 0 ? Long.toString(l.get(2)) : "NA") + "," 
									+ Long.toString(l.get(3)) + "," 
									+ Long.toString(l.get(4))
									);
				fileWriter.newLine();
			
			}
			fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
    private long buildIndex(String dbName) {
        PersistenceManager pm = ZooJdoHelper.openDB(dbName);
        long startTime = System.nanoTime();

        pm.currentTransaction().begin();
		ZooJdoHelper.createIndex(pm, Car.class, "owner", false);
        pm.currentTransaction().commit();

        long ret = (System.nanoTime()-startTime)/1000000;
        closeDB(pm);
        return ret;
		
	}
	private static void createDB(String dbName) {
        // remove database if it exists
        if (ZooHelper.dbExists(dbName)) {
            ZooHelper.removeDb(dbName);
        }
        ZooHelper.createDb(dbName);
    }
    
    /* 
     * fill with random strings
     */
    private long populateDB(String dbName, int elements) {
        PersistenceManager pm = ZooJdoHelper.openDB(dbName);
        
        for(int i=0; i<elements; i++) {
        	String randStr = UUID.randomUUID().toString();
        	this.carOwners.add(new Tuple<Integer, String>(i, randStr));
        }

        long startTime = System.nanoTime();
        pm.currentTransaction().begin();
        // create instances
        for(Tuple<Integer, String> t : this.carOwners) {
        	Car car = new Car(t.y,t.x);
            pm.makePersistent(car);
        }
        pm.currentTransaction().commit();
        
        long ret = (System.nanoTime()-startTime)/(1000000);
        closeDB(pm);
        return ret;
    } 
    
    @SuppressWarnings("unchecked")
	private long readDB(String dbName) {
        PersistenceManager pm = ZooJdoHelper.openDB(dbName);
        
        Collections.shuffle(this.carOwners, new Random(System.nanoTime()));

        long startTime = System.nanoTime();
        pm.currentTransaction().begin();

        for(Tuple<Integer, String> t : this.carOwners) {
        	Query query = pm.newQuery(Car.class, "owner == '"+t.y+"'");
        	Collection<Car> cars = (Collection<Car>) query.execute();
        	if(cars.size() > 1) throw new RuntimeException("All objects should have different strings.");
        	for (Car c: cars) {
        		if(c.getId()!=t.x || !c.getOwner().equals(t.y)) {
        			throw new RuntimeException("Did not retrieve the correct object."
        											+ "Queried: "+t.x+" "+t.y+", "
        											+ "Received: " + c.getId()+ " " + c.getOwner());
        		}
        	}
        	query.closeAll();
        }
        
        pm.currentTransaction().commit();
        long ret = (System.nanoTime()-startTime)/(1000000);

        closeDB(pm);
        return ret;
    }
    
    private void closeDB(PersistenceManager pm) {
        if (pm.currentTransaction().isActive()) {
            pm.currentTransaction().rollback();
        }
        pm.close();
        pm.getPersistenceManagerFactory().close();
    }

}
