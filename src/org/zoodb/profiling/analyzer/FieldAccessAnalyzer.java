package org.zoodb.profiling.analyzer;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zoodb.profiling.api.IDataProvider;
import org.zoodb.profiling.api.IFieldAccess;
import org.zoodb.profiling.api.ObjectFieldStats;
import org.zoodb.profiling.api.impl.ProfilingManager;
import org.zoodb.profiling.api.tree.impl.ObjectNode;
import org.zoodb.profiling.suggestion.SuggestionFactory;

import ch.ethz.globis.profiling.commons.suggestion.AbstractSuggestion;
import ch.ethz.globis.profiling.commons.suggestion.FieldRemovalSuggestion;
import ch.ethz.globis.profiling.commons.suggestion.FieldSuggestion;

public class FieldAccessAnalyzer {
	
	private Logger logger = LogManager.getLogger("allLogger");
	
	private IDataProvider dp;
	
	
	public FieldAccessAnalyzer(IDataProvider dp) {
		this.dp = dp;
	}
	
	
	
	/**
	 * Checks classes for disjunct access to attribute sets  
	 */
	public void splitByDisjunctAtt() {
		Iterator<Class<?>> iter = dp.getClasses().iterator();
		
		while (iter.hasNext()) {
			Class<?> currentClass = iter.next();
			
			/*
			 * Go through all attributes of this class, get its trx set
			 * check for trx set "congruency"
			 * 
			 */
			Field[] fields = currentClass.getDeclaredFields();
			
			//Map<Field,TrxSet> attrToTrx = new HashMap<Field,TrxSet>();
			
			for (Field f : fields) {
				//attrToTrx.put(key, value)
			}
			
			
		}
	}
	
	
	/**
	 * Reports fields of class 'clazzName' that are never accessed (neither read nor write)
	 * @param clazzName
	 * @return
	 */
	public Collection<AbstractSuggestion> getUnaccessedFieldsByClassSuggestion(Class<?> c) {
		Collection<AbstractSuggestion> suggestionsByClass = new LinkedList<AbstractSuggestion>();
		
		Collection<String> fieldsUsed = getFieldsUsedByClass(c);
		FieldSuggestion  fs = null;
		
		try {
			Field[] fieldsDeclared = c.getDeclaredFields();
			
			//fieldsDeclared is an array, cannot use 'retainAll'...
			for (Field f : fieldsDeclared) {
				if ( !fieldsUsed.contains(f.getName()) ) {
					
					FieldRemovalSuggestion frs = SuggestionFactory.getFRS(new Object[] {c.getName(), f.getName()});
					
					//fs.setFieldAccesses(dp.getByClassAndField(c, f.getName()));
					logger.info(frs.getText());
					suggestionsByClass.add(frs);
				}
			}
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} 
		return suggestionsByClass;
	
	}
	
	/**
	 * Reports fields accessed by any object of class 'clazzName'
	 * @param clazzName
	 * @return
	 */
	private Collection<String> getFieldsUsedByClass(Class<?> c) {
		Set<IFieldAccess> allAccesses =dp.getByClass(c);
		
		Collection<String> usedFields = new HashSet<String>();
		
		for (IFieldAccess ac : allAccesses) {
			if (ac.isActive()) {
				usedFields.add(ac.getFieldName());
			}
		}
		return usedFields;
	}
	
	public Collection<AbstractSuggestion> getCollectionSizeSuggestions() {
		CollectionAnalyzer ca = new CollectionAnalyzer();
		return ca.analyzeUnused();
	}
	
	public Collection<AbstractSuggestion> getCollectionAggregSuggestions() {
		CollectionAggregAnalyzer ca = new CollectionAggregAnalyzer();
		return ca.analyzeAggregations();
	}
}
