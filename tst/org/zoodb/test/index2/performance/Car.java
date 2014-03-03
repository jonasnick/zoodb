package org.zoodb.test.index2.performance;

import org.zoodb.api.impl.ZooPC;

public class Car extends ZooPC {
	
	private String owner;
	private int id;

    @SuppressWarnings("unused")
    private Car() {
        // All persistent classes need a no-args constructor. 
        // The no-args constructor can be private.
    }

	public Car(String owner, int id) {
		this.owner = owner;
		this.id = id;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String name) {
		this.owner = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int speed) {
		this.id = speed;
	} 
}
