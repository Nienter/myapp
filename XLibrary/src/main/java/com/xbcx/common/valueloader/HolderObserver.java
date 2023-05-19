package com.xbcx.common.valueloader;

public interface HolderObserver<Holder,Item> {
	public void onPutHolder(Holder holder,Item item);
	
	public void onRemoveHolder(Holder holder);
}
