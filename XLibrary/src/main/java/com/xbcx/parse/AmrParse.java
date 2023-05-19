package com.xbcx.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AmrParse {
	
	public static long parseDuration(String strPath){
		long lDuration = 0;
		FileInputStream fis = null;
		try {
			File file = new File(strPath);
			long nLength = file.length();
			fis = new FileInputStream(file);
			fis.skip(6);
			int nBuf = fis.read();
			int nAudioFrameSize = parseFrameSize(nBuf);
			
			if(nAudioFrameSize > 0){
				lDuration = ((nLength - 6) / nAudioFrameSize) * 20;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return lDuration;
	}
	
	public static int parseFrameCount(String strPath){
		int nFrameCount = 0;
		FileInputStream fis = null;
		try {
			File file = new File(strPath);
			long nLength = file.length();
			fis = new FileInputStream(file);
			fis.skip(6);
			int nBuf = fis.read();
			int nAudioFrameSize = parseFrameSize(nBuf);
			
			if(nAudioFrameSize > 0){
				nFrameCount = (int)((nLength - 6) / nAudioFrameSize);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return nFrameCount;
	}
	
	public static boolean isAmrFile(String strPath){
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(strPath));
			if(fis.read() == 0x23 && 
					fis.read() == 0x21 && 
					fis.read() == 0x41 && 
					fis.read() == 0x4d && 
					fis.read() == 0x52 &&
					fis.read() == 0x0a){
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	public static void combineAmrFile(String strPathDst,String strPathSrc){
		FileOutputStream fos = null;
		FileInputStream fis = null;
		try{
			fos = new FileOutputStream(strPathDst,true);
			fis = new FileInputStream(strPathSrc);
			final long nSkipBytes = fis.skip(6);
			if(nSkipBytes != 6){
				long nRemain = 6 - nSkipBytes;
				while(nRemain-- > 0){
					fis.read();
				}
			}
			final byte buf[] = new byte[4096];
			int nReadLength = -1;
			while((nReadLength = fis.read(buf)) != -1){
				fos.write(buf, 0, nReadLength);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(fos != null){
				try{
					fos.close();
				}catch(Exception e){
					
				}
			}
			if(fis != null){
				try{
					fis.close();
				}catch(Exception e){
					
				}
			}
		}
	}
	
	private static int parseFrameSize(int nFrameHeader){
		if(nFrameHeader == 0x3c){
			return 32;
		}else if(nFrameHeader == 0x34){
			return 27;
		}else if(nFrameHeader == 0x2c){
			return 21;
		}else if(nFrameHeader == 0x24){
			return 20;
		}else if(nFrameHeader == 0x1c){
			return 18;
		}else if(nFrameHeader == 0x14){
			return 16;
		}else if(nFrameHeader == 0x0c){
			return 14;
		}else if(nFrameHeader == 0x04){
			return 13;
		}
		return 0;
	}
}
