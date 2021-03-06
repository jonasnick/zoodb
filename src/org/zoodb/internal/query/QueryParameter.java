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
package org.zoodb.internal.query;


/**
 * Query parameter instances are used to assign values to parameters in query after compilation.
 * 
 * @author Tilmann Zaeschke
 */
public final class QueryParameter {
	
	private String type;
	private final String name;
	private Object value;

	public QueryParameter(String type, String name) {
		this.type = type;
		this.name = name;
	}
	
	public void setValue(Object p1) {
		if (p1 != null) {
			value = p1;
		} else {
			value = QueryParser.NULL;
		} 
	}
	
	public Object getValue() {
		return value;
	}
	
	public Object getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String typeName) {
		this.type = typeName;
	}

}