package com.xbcx.im.ui;

import java.util.ArrayList;
import java.util.List;

import android.util.SparseIntArray;

import com.xbcx.im.MessageFactoryImpl;
import com.xbcx.im.XMessage;
import com.xbcx.im.XMessageFactory;
import com.xbcx.im.ui.messageviewprovider.CommonViewProvider;
import com.xbcx.im.ui.simpleimpl.CameraActivity;

public class IMGlobalSetting {
	
	public static boolean					photoCrop 						= false;
	
	public static boolean					photoSendPreview 				= false;
	
	@SuppressWarnings("rawtypes")
	public static Class						photoSendPreviewActivityClass 	= null;
	
	public static boolean					viewPictureActivityShowTitle	= false;
	
	public static boolean					viewPictureActivityShowSaveBtn	= true;
	
	public static boolean					msgReSendDialog					= false;
	
	public static boolean					sendPhotoUseMultiChoose			= false;
	
	public static List<SendPlugin> 			publicSendPlugins				= new ArrayList<SendPlugin>();
	
	@SuppressWarnings("rawtypes")
	public static Class						videoCaptureActivityClass		= CameraActivity.class;
	
	public static XMessageFactory			msgFactory						= new MessageFactoryImpl();
	
	public static List<Class<? extends EditViewExpressionProvider>> editViewExpProviders 	
								= new ArrayList<Class<? extends EditViewExpressionProvider>>();
	
	public static int						msgViewInfoShowType				= CommonViewProvider.ViewInfo_DEFAULT;
	
	public static List<TextMessageImageCoder> textMsgImageCodeces			= new ArrayList<TextMessageImageCoder>();
	
	public static boolean					showChatRoomBar					= false;
	
	public static Class<?>					textMsgUrlJumpActivity;
	
	private static SparseIntArray			mapCopyableMessageType			= new SparseIntArray();
	
	public static void setMessageTypeForwardable(int msgType){
		mapCopyableMessageType.put(msgType, msgType);
	}
	
	public static void removeMessageTypeForwardable(int msgType){
		mapCopyableMessageType.delete(msgType);
	}
	
	public static boolean isMessageTypeForwardable(int msgType){
		return mapCopyableMessageType.get(msgType,XMessage.TYPE_UNKNOW) 
				!= XMessage.TYPE_UNKNOW;
	}
}
