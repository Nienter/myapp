package com.xbcx.mediarecord;

public class ConcurrentShortQueue{
	
	private short mElement[];
	private int mLength;
	
	public ConcurrentShortQueue(int nCapacity){
		mElement = new short[nCapacity];
	}
	
	public synchronized void put(short buf[],int nOffset,int nLength){
		if(mElement.length < mLength + nLength){
			final short buffer[] = new short[mLength + nLength];
			System.arraycopy(mElement, 0, buffer, 0, mElement.length);
			mElement = buffer;
		}
		System.arraycopy(buf, nOffset, mElement, mLength, nLength);
		mLength += nLength;
	}
	
	public synchronized void get(short buf[]){
		final int nCopyLength = buf.length;
		System.arraycopy(mElement, 0, buf, 0, nCopyLength);
		System.arraycopy(mElement, nCopyLength, mElement, 0, mElement.length - nCopyLength);
		mLength -= nCopyLength;
	}
	
	public synchronized int length(){
		return mLength;
	}
}
