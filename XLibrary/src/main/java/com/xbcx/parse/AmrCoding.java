package com.xbcx.parse;

public class AmrCoding {
	
	static{
		try{
			System.loadLibrary("AmrCoding");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public native static int EncodeWAVEFileToAMRFile(String strWaveFilePath,String strAmrFilePath);
	
	public native static int DecodeAmrFileToWAVEFile(String strAmrFilePath,String strWaveFilePath);
	
	public native static int EncodeInit(String strAmrOutputPath);
	
	public native static int EncodeDo(short speech[]);
	
	public native static int EncodeExit();
}
