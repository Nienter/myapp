package com.xbcx.adapter;

import java.util.Collection;

public interface SelectableAdapter<E extends Object> {
	
	public void				addSelectItem(E item);
	
	public void 			addSelectItem(int pos);
	
	public void 			removeSelectItem(E item);
	
	public void 			removeSelectItem(int pos);
	
	public void 			clearSelectItem();
	
	public boolean 			isSelected(E item);
		
	public boolean 			containSelect();
	
	public Collection<E> 	getAllSelectItem();
}
