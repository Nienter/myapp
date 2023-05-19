package com.xbcx.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.xbcx.library.R;
import com.xbcx.utils.FileHelper;

import android.annotation.SuppressLint;
import android.os.Environment;

public class LoggerSystemOutHandler extends Handler {
	
	@SuppressLint("SimpleDateFormat")
	public static final SimpleDateFormat df = new SimpleDateFormat("y/M/d HH:mm:ss.sss");
	
	public static boolean DEBUG 		= true;
	
	public static boolean WARNING_FILE 	= false;

	private BufferedWriter 	mBw;
	
	private BufferedWriter	mFineBw;
	
	@Override
	public void close() {
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(LogRecord record) {
//		System.out.println(record.getSourceClassName() + " " + 
//				record.getSourceMethodName() + ":" + 
//				record.getMessage());
		
		if(!DEBUG){
			return;
		}
		
		if(WARNING_FILE){
			if(record.getLevel() == Level.WARNING){
				synchronized (this) {
					if(mBw == null){
						try {
							final String path = Environment.getExternalStorageDirectory().getPath() + 
									File.separator + "xblog" + File.separator +
									XApplication.getApplication().getString(R.string.app_name) + File.separator +
									System.currentTimeMillis() + ".txt";
							if(FileHelper.checkOrCreateDirectory(path)){
								File f = new File(path);
								File parent = f.getParentFile();
								File childs[] = parent.listFiles();
								if(childs != null && childs.length > 50){
									long minTime = Long.MAX_VALUE;
									File delete = null;
									for(File child : childs){
										if(child.lastModified() < minTime){
											minTime = child.lastModified();
											delete = child;
										}
									}
									if(delete != null){
										delete.delete();
									}
								}
								mBw = new BufferedWriter(new FileWriter(path));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if(mBw != null){
						try {
							mBw.newLine();
							mBw.write(df.format(new Date()) + ":");
							mBw.write(record.getMessage());
							mBw.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}else if(record.getLevel() == Level.FINE){
				synchronized (this) {
					if(mFineBw == null){
						try{
							final String path = Environment.getExternalStorageDirectory().getPath() + 
									File.separator + 
									XApplication.getApplication().getString(R.string.app_name) + ".txt";
							mFineBw = new BufferedWriter(new FileWriter(path));
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					if(mFineBw != null){
						try {
							mFineBw.newLine();
							mFineBw.write(df.format(new Date()) + ":");
							mFineBw.write(record.getMessage());
							mFineBw.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		System.out.println(record.getMessage());
	}

}
