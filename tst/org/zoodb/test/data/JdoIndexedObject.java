/* 
This file is part of the PolePosition database benchmark
http://www.polepos.org

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA  02111-1307, USA. */


package org.zoodb.test.data;

import org.zoodb.jdo.spi.PersistenceCapableImpl;

public class JdoIndexedObject extends PersistenceCapableImpl implements CheckSummable{
	
	public int _int;
	
	public String _string;
	
	public JdoIndexedObject(){
		
	}
	
	public JdoIndexedObject(int int_, String str){
		_int = int_;
		_string = str;
	}
	
	public JdoIndexedObject(int int_){
		this(int_, IndexedObject.queryString(int_));
	}

	@Override
	public long checkSum() {
        activateRead("_string");
		return _string.length();
	}

	public void updateString() {
    	activateWrite("_string");
		_string = _string.toUpperCase();
	}
	
	@Override
	public String toString() {
        activateRead("_string");
		return "JdoIndexedObject _int:" + _int + " _string:" + _string;
	}
	
}
