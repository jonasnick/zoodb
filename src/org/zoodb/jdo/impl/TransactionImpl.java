/*
 * Copyright 2009-2014 Tilmann Zaeschke. All rights reserved.
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
package org.zoodb.jdo.impl;

import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.zoodb.internal.DataStoreHandler;
import org.zoodb.internal.Session;

/**
 *
 * @author Tilmann Zaeschke
 */
public class TransactionImpl implements Transaction {

    //The final would possibly avoid garbage collection
    private final PersistenceManagerImpl pm;
    private volatile Synchronization sync = null;
    private volatile boolean retainValues = false;
    
    private final Session connection;

    /**
     * @param arg0
     * @param pm
     * @param i 
     */
    TransactionImpl(PersistenceManagerImpl pm, 
            boolean retainValues, boolean isOptimistic, Session con) {
        DataStoreHandler.connect(null);
        this.retainValues = retainValues;
        this.pm = pm;
        this.connection = con;
    }

    /**
     * @see Transaction#begin()
     */
    @Override
	public synchronized void begin() {
    	connection.begin();
    }

    /**
     * @see Transaction#commit()
     */
    @Override
	public synchronized void commit() {
    	if (!connection.isActive()) {
    		throw new JDOUserException("Can't commit closed transaction. Missing 'begin()'?");
    	}

    	//synchronisation #1
    	if (sync != null) {
    		sync.beforeCompletion();
    	}

    	//commit
    	connection.commit(retainValues);

    	//synchronization #2
    	if (sync != null) {
    		sync.afterCompletion(Status.STATUS_COMMITTED);
    	}
    }

    /**
     * @see Transaction#commit()
     */
    @Override
	public synchronized void rollback() {
    	if (!connection.isActive()) {
    		throw new JDOUserException("Can't rollback closed transaction. Missing 'begin()'?");
    	}
    	//Don't call beforeCompletion() here. (JDO 3.0, p153)
    	connection.rollback();
    	if (sync != null) {
    		sync.afterCompletion(Status.STATUS_ROLLEDBACK);
    	}
    }

    /**
     * @see Transaction#getPersistenceManager()
     */
    @Override
	public PersistenceManager getPersistenceManager() {
        //Not synchronised, field is final
        return pm;
    }

    /**
     * @see Transaction#isActive()
     */
    @Override
	public boolean isActive() {
        //Not synchronised, field is volatile
        return connection.isActive();
    }
    
    /**
     * @see Transaction#getSynchronization()
     */@Override
	synchronized 
    public Synchronization getSynchronization() {
        return sync;
    }

    /**
     * @see Transaction#setSynchronization(Synchronization)
     */
    @Override
	public synchronized void setSynchronization(Synchronization sync) {
        this.sync = sync;
    }

	@Override
	public String getIsolationLevel() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getNontransactionalRead() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getNontransactionalWrite() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getOptimistic() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getRestoreValues() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getRetainValues() {
		return retainValues;
	}

	@Override
	public boolean getRollbackOnly() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIsolationLevel(String arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNontransactionalRead(boolean arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void setNontransactionalWrite(boolean arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void setOptimistic(boolean arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRestoreValues(boolean arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRetainValues(boolean arg0) {
		retainValues = arg0;
	}

	@Override
	public void setRollbackOnly() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSerializeRead(Boolean serialize) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
		//
	}

	@Override
	public Boolean getSerializeRead() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
		//return null;
	}
}
