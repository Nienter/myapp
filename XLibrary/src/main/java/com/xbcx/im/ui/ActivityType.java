package com.xbcx.im.ui;

import java.util.ArrayList;

import com.xbcx.im.IMKernel;
import com.xbcx.im.XMessage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;

public class ActivityType {
	
	public static final int SingleChat 			= 1;
	public static final int GroupChat			= 2;
	public static final int DiscussionChat		= 3;
	public static final int FriendVerify		= 4;
	public static final int ChatRoom			= 5;
	
	public static final int UserDetailActivity	= 6;
	public static final int SelfDetailActivity	= 7;
	public static final int ChooseFileActivity	= 8;
	
	public static final int LocationMsgActivity = 10;
	public static final int ForwardActivity		= 11;
	
	public static final int ConflictActivity	= 20;
	public static final int ConflictJumpActivity= 21;
	public static final int PwdErrorActivity	= 22;
	public static final int PwdErrorJumpActivity= 23;

	private static SparseArray<Class<?>> sMapActivityTypeToActivityClass = new SparseArray<Class<?>>();
	
	public static Class<?>	getActivityClass(int activity){
		return sMapActivityTypeToActivityClass.get(activity);
	}
	
	public static void		registerActivityClass(int activity,Class<?> cls){
		sMapActivityTypeToActivityClass.put(activity, cls);
	}
	
	public static void		launchChatActivity(Activity activity,
			int activityType,String id,String name){
		launchChatActivity(activity, activityType, id, name, null,true);
	}
	
	public static void		launchChatActivity(Activity activity,
			int activityType,String id,String name,boolean bClearTop){
		launchChatActivity(activity, activityType, id, name, null,bClearTop);
	}
	
	public static void		launchChatActivity(Activity activity,
			int activityType,String id,String name,Bundle b){
		launchChatActivity(activity, activityType, id, name, b,true);
	}
	
	public static void		launchChatActivity(Activity activity,
			int activityType,String id,String name,Bundle b,boolean bClearTop){
		if(TextUtils.isEmpty(id)){
			return;
		}
		Class<?> cls = getActivityClass(activityType);
		if(cls != null){
			try{
				Intent intent = new Intent(activity, cls);
				intent.putExtra("id", id);
				intent.putExtra("name", name);
				if(bClearTop){
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				}
				if(b != null){
					intent.putExtras(b);
				}
				activity.startActivity(intent);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static void		launchLocationMessageActivity(Activity activity,XMessage xm){
		Class<?> cls = getActivityClass(LocationMsgActivity);
		if(cls != null){
			try{
				final String location[] = xm.getLocation();
				Intent i = new Intent(activity, cls);
				i.putExtra("lat", Double.parseDouble(location[0]));
				i.putExtra("lng", Double.parseDouble(location[1]));
				if(xm.isFromSelf()){
					i.putExtra("id", IMKernel.getLocalUser());
				}else{
					i.putExtra("id", xm.getUserId());
					i.putExtra("name", xm.getUserName());
				}
				activity.startActivity(i);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static void 		launchForwardMessageActivity(Activity activity,XMessage xm){
		launchForwardMessageActivity(activity, xm, null);
	}
	
	public static void 		launchForwardMessageActivity(Activity activity,XMessage xm,
			Bundle b){
		Class<?> cls = getActivityClass(ForwardActivity);
		if(cls != null){
			try{
				Intent i = new Intent(activity, cls);
				if(xm.isStoraged()){
					i.putExtra("id", xm.getOtherSideId());
					i.putExtra("fromtype", xm.getFromType());
					i.putExtra("message_id", xm.getId());
				}else{
					i.putExtra("data", xm);
				}
				if(b != null){
					i.putExtras(b);
				}
				activity.startActivity(i);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static void 		launchForwardMessageActivity(Activity activity,ArrayList<String> filePath,int msgType){
		launchForwardMessageActivity(activity, filePath, msgType, null);
	}
	
	public static void 		launchForwardMessageActivity(Activity activity,ArrayList<String> filePath,int msgType,Bundle b){
		Class<?> cls = getActivityClass(ForwardActivity);
		if(cls != null){
			try{
				Intent i = new Intent(activity, cls);
				if(msgType == XMessage.TYPE_PHOTO){
					i.putExtra("pics", filePath);
					if(b != null){
						i.putExtras(b);
					}
					activity.startActivity(i);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static boolean	launchConflictActivity(Context context){
		Class<?> cls = getActivityClass(ConflictActivity);
		if(cls != null){
			try{
				Intent i = new Intent(context, cls);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(i);
				return true;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static boolean	launchPwdErrorActivity(Context context){
		Class<?> cls = getActivityClass(PwdErrorActivity);
		if(cls != null){
			try{
				Intent i = new Intent(context, cls);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("pwderror", true);
				context.startActivity(i);
				return true;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static boolean	launchLoginFailureActivity(Context context){
		Class<?> cls = getActivityClass(PwdErrorActivity);
		if(cls != null){
			try{
				Intent i = new Intent(context, cls);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(i);
				return true;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return false;
	}
}
