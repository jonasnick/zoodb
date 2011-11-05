/*
 * Copyright 2009-2011 Tilmann Z�schke. All rights reserved.
 * 
 * This file is part of ZooDB.
 * 
 * ZooDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ZooDB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ZooDB.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * See the README and COPYING files for further information. 
 */
package org.zoodb.jdo.api;

import java.util.Properties;

import javax.jdo.Constants;

import org.zoodb.jdo.PersistenceManagerFactoryImpl;
import org.zoodb.jdo.ZooConstants;

/**
 * Properties to be used for creating JDO session.
 * <p>
 * <code>
 * ZooJdoProperties props = new ZooJdoProperties("MyDatabase");  <br> 
 * JDOHelper.getPersistenceManagerFactory(props);    <br>
 * </code>
 * 
 * 
 * @author Tilmann Zaeschke
 */
public class ZooJdoProperties extends Properties implements Constants {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new set of properties for creating a new persistence manager
	 * with 
	 * @param dbName
	 */
	public ZooJdoProperties(String dbName) {
		super();
		setProperty(Constants.PROPERTY_PERSISTENCE_MANAGER_FACTORY_CLASS,
				PersistenceManagerFactoryImpl.class.getName());
		setProperty(Constants.PROPERTY_CONNECTION_URL, dbName);
	}
	
	
	public ZooJdoProperties setUserName(String userName) {
		put(Constants.PROPERTY_CONNECTION_USER_NAME, userName);
		return this;
	}
	
	public ZooJdoProperties setUserPass(String userName, String password) {
		put(Constants.PROPERTY_CONNECTION_USER_NAME, userName);
		put(Constants.PROPERTY_CONNECTION_PASSWORD, password);
		return this;
	}
	
	
	public ZooJdoProperties setSessionName(String name) {
		put(Constants.PROPERTY_NAME, name);
		return this;
	}

	
	public ZooJdoProperties setOptimisticLocking(boolean flag) {
		put(Constants.PROPERTY_OPTIMISTIC, Boolean.toString(flag));
		return this;
	}


	/**
	 * Whether queries should ignore objects in the cache. Default is 'false'.
	 * @param flag
	 * @return this
	 * @see Constants#PROPERTY_IGNORE_CACHE
	 */
	public ZooJdoProperties setIgnoreCache(boolean flag) {
		put(Constants.PROPERTY_IGNORE_CACHE, Boolean.toString(flag));
		return this;
	}
	
	
	/**
	 * Property that defines whether schemata should be created as necessary or need explicit 
	 * creation. Default is false.
	 * @param flag
	 * @return this
	 * @see ZooConstants#PROPERTY_AUTO_CREATE_SCHEMA
	 */
	public ZooJdoProperties setZooAutoCreateSchema(boolean flag) {
		put(ZooConstants.PROPERTY_AUTO_CREATE_SCHEMA, Boolean.toString(flag));
		return this;
	}
	
	
	/**
	 * Property that defines whether evict() should also reset primitive values. By default, 
	 * ZooDB only resets references to objects, even though the JDO spec states that all fields
	 * should be evicted. 
	 * In a properly enhanced/activated class, the difference should no be noticeable, because
	 * access to primitive fields of evicted objects should always trigger a reload. Because of 
	 * this, ZooDB by default avoids the effort of resetting primitive fields.
	 * Default is false.
	 * @param flag
	 * @return this
	 * @see ZooConstants#PROPERTY_EVICT_PRIMITIVES
	 */
	public ZooJdoProperties setZooEvictPrimitives(boolean flag) {
		put(ZooConstants.PROPERTY_EVICT_PRIMITIVES, Boolean.toString(flag));
		return this;
	}
	
	

}
