package com.xbcx.core.db;

import java.util.concurrent.locks.ReentrantReadWriteLock;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class DatabaseManager{
	
	private SQLiteOpenHelper 		mDBHelper;
	
	private ReentrantReadWriteLock 	mReadWriteLock;
	
	public DatabaseManager() {
		mReadWriteLock = new ReentrantReadWriteLock();
	}
	
	public SQLiteDatabase 	lockWritableDatabase(){
		synchronized (this) {
			if(mDBHelper == null){
				mDBHelper = onInitDBHelper();
				if(mDBHelper == null){
					return null;
				}
				return null;
			}
		}
		mReadWriteLock.writeLock().lock();
		return mDBHelper.getWritableDatabase();
	}
	
	public void	unlockWritableDatabase(SQLiteDatabase db){
		mReadWriteLock.writeLock().unlock();
		onDBUnlock();
	}
	
	public SQLiteDatabase 	lockReadableDatabase(){
		synchronized (this) {
			if(mDBHelper == null){
				mDBHelper = onInitDBHelper();
				if(mDBHelper == null){
					return null;
				}
			}
		}
		mReadWriteLock.readLock().lock();
		return mDBHelper.getReadableDatabase();
	}
	
	protected abstract SQLiteOpenHelper onInitDBHelper();
	
	public void	unlockReadableDatabase(SQLiteDatabase db){
		mReadWriteLock.readLock().unlock();
		onDBUnlock();
	}
	
	protected void onDBUnlock(){
	}
	
	public void release(){
		synchronized (this) {
			if(mDBHelper != null){
				mDBHelper.close();
				mDBHelper = null;
			}
		}
	}
}
