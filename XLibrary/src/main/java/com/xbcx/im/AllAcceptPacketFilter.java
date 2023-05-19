package com.xbcx.im;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

public class AllAcceptPacketFilter implements PacketFilter {
	
	private static AllAcceptPacketFilter sInstance;
	
	static{
		sInstance = new AllAcceptPacketFilter();
	}
	
	public static AllAcceptPacketFilter getInstance(){
		return sInstance;
	}
	
	private AllAcceptPacketFilter(){
	}

	@Override
	public boolean accept(Packet packet) {
		return true;
	}

}
