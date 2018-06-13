package org.coredata.core.data;

public interface Record {

	public void add(Object object);

	public void add(int index, Object object);

	public Object get(int index);

	public void set(int index, Object object);

	public int size();

	public void remove(int index);
	
}
