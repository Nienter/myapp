package com.xbcx.im.ui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.core.XApplication;
import com.xbcx.core.db.XDB;
import com.xbcx.core.module.UserReleaseListener;
import com.xbcx.im.IMConfigManager;
import com.xbcx.im.IMKernel;
import com.xbcx.im.XMessage;
import com.xbcx.im.db.DBColumns;
import com.xbcx.im.db.MessageBaseRunner;
import com.xbcx.im.db.MessageCreator;
import com.xbcx.im.recentchat.RecentChat;
import com.xbcx.im.recentchat.RecentChatManager;
import com.xbcx.library.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StatusBarManager implements OnEventListener,
								UserReleaseListener{
	
	public static StatusBarManager getInstance(){
		return sInstance;
	}
	
	static{
		sInstance = new StatusBarManager();
	}
	
	private static StatusBarManager sInstance;
	
	private static final int NOTIFY_ID_SINGLECHAT 		= 1;
	private static final int NOTIFY_ID_GROUPCHAT 		= 2;
	private static final int NOTIFY_ID_DISCUSSIONCHAT	= 3;
	
	private NotificationManager mNotificationManager;
	
	private Context				mContext;
	
	private boolean				mIsMerge = true;
	
	private int					mIconResId = R.drawable.ic_launcher;
	
	@SuppressWarnings("rawtypes")
	private Class				mJumpActivityClass;
	
	private String 				mTickerLast;
	
	private List<RecentChat> mSingleRcs = new ArrayList<RecentChat>();
	private List<RecentChat> mGroupRcs = new ArrayList<RecentChat>();
	private List<RecentChat> mDiscussionRcs = new ArrayList<RecentChat>();
	private int				 mSingleTotalUnreadCount = 0;
	private int				 mGroupTotalUnreadCount = 0;
	private int				 mDiscussionTotalUnreadCount = 0;
	private RecentChat		 mRecentChatUnreadChange;
	
	protected StatusBarManager(){
		XApplication.addManager(this);
		
		mContext = XApplication.getApplication();
		mNotificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		
		AndroidEventManager.getInstance().addEventListener(
				EventCode.UnreadMessageCountChanged,this);
	}
	
	@SuppressWarnings("rawtypes")
	public StatusBarManager setJumpActivityClass(Class c){
		mJumpActivityClass = c;
		return this;
	}
	
	public StatusBarManager setIconResId(int resId){
		mIconResId = resId;
		return this;
	}
	
	@Override
	public void onUserRelease(String user) {
		clearStatusBar();
	}

	@Override
	public void onEventRunEnd(Event event) {
		final int code = event.getEventCode();
		if(code == EventCode.UnreadMessageCountChanged){
			if(IMKernel.isInBackground(mContext)){
				Collection<RecentChat> collection = RecentChatManager.getInstance()
						.getAllHasUnreadRecentChat();
				final int recentChatSize = collection.size();
				if(recentChatSize > 0){
					if(!IMConfigManager.getInstance().isReceiveNewMessageNotify()){
						clearStatusBar();
						return;
					}
					
					final RecentChat rc = (RecentChat)event.getParamAtIndex(0);
					if(mIsMerge){
						processMergeNotify(collection, recentChatSize,rc);
					}else{
						mRecentChatUnreadChange = rc;
						String strTicker = null;
						if(rc != null){
							if(rc.getActivityType() == ActivityType.SingleChat ||
									rc.getActivityType() == ActivityType.GroupChat ||
									rc.getActivityType() == ActivityType.DiscussionChat){
								if(rc.getUnreadMessageCount() > 0){
									final String strNickname = rc.getName();
									strTicker = mContext.getString(
										R.string.statusbar_single_contact_text_notify, strNickname == null ? "" : strNickname);
									if(strTicker.equals(mTickerLast)){
										strTicker = strTicker + " ";
									}
									mTickerLast = strTicker;
								}
							}
						}
							
						classify(collection);
						if(rc == null){
							processSingleNotify(strTicker,false);
							processGroupNotify(strTicker,false);
							processDiscussionNotify(strTicker,false);
						}else{
							if(rc.getActivityType() == ActivityType.SingleChat){
								processSingleNotify(strTicker,false);
							}else if(rc.getActivityType() == ActivityType.GroupChat){
								processGroupNotify(strTicker,false);
							}else if(rc.getActivityType() == ActivityType.DiscussionChat){
								processDiscussionNotify(strTicker,false);
							}
						}
					}
				}else{
					clearStatusBar();
				}
			}else{
				clearStatusBar();
			}
		}
	}
	
	protected void processMergeNotify(Collection<RecentChat> rcs,int recentChatSize,RecentChat rc){
		String strTicker = null;
		if(rc != null){
			if(rc.getActivityType() == ActivityType.SingleChat ||
					rc.getActivityType() == ActivityType.GroupChat ||
					rc.getActivityType() == ActivityType.DiscussionChat){
				if(rc.getUnreadMessageCount() > 0){
					final String strNickname = rc.getName();
					strTicker = mContext.getString(
						R.string.statusbar_single_contact_text_notify, strNickname == null ? "" : strNickname);
					if(strTicker.equals(mTickerLast)){
						strTicker = strTicker + " ";
					}
					mTickerLast = strTicker;
				}
			}
		}
		
		int unreadTotalCount = calculateUnreadTotalCount(rcs);
		
		if(recentChatSize > 1){
			NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
					.setAutoCancel(true)
					.setSmallIcon(mIconResId)
					.setTicker(strTicker)
					.setNumber(unreadTotalCount)
					.setContentTitle(mContext.getString(R.string.app_name))
					.setContentText(mContext.getString(R.string.statusbar_multi_contact_notify, recentChatSize, unreadTotalCount))
					.setContentIntent(getPendingIntent(null, null, 0));

			mNotificationManager.notify(NOTIFY_ID_SINGLECHAT, builder.build());
		}else{
			RecentChat rcSingle = rcs.iterator().next();
			NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
					.setAutoCancel(true)
					.setSmallIcon(mIconResId)
					.setTicker(strTicker)
					.setNumber(unreadTotalCount);
			
			if (unreadTotalCount > 1) {
				builder.setContentTitle(rcSingle.getName())
						.setContentText(mContext.getString(R.string.statusbar_single_contact_multimsg_notify, unreadTotalCount))
						.setContentIntent(getPendingIntent(rcSingle.getId(), rcSingle.getName(), rcSingle.getActivityType()));
			} else {
				XMessage m = readLastMessage(rcSingle.getId());
				final int nMessageType = m == null ? XMessage.TYPE_TEXT : m.getType();
				builder.setContentTitle(mContext.getString(nMessageType == XMessage.TYPE_VOICE ?
						R.string.statusbar_single_contact_voice_notify
						: R.string.statusbar_single_contact_text_notify, rcSingle.getName()))
						.setContentText(rcSingle.getContent())
						.setContentIntent(getPendingIntent(rcSingle.getId(), rcSingle.getName(), rcSingle.getActivityType()));
			}
			mNotificationManager.notify(NOTIFY_ID_SINGLECHAT, builder.build());
		}
	}
	
	protected int calculateUnreadTotalCount(Collection<RecentChat> rcs){
		int count = 0;
		for(RecentChat rc : rcs){
			count += rc.getUnreadMessageCount();
		}
		return count;
	}
	
	protected void classify(Collection<RecentChat> rcs){
		mSingleRcs.clear();
		mGroupRcs.clear();
		mDiscussionRcs.clear();
		mSingleTotalUnreadCount = 0;
		mGroupTotalUnreadCount = 0;
		mDiscussionTotalUnreadCount = 0;
		for(RecentChat rc : rcs){
			final int activity = rc.getActivityType();
			if(activity == ActivityType.SingleChat){
				mSingleRcs.add(rc);
				mSingleTotalUnreadCount += rc.getUnreadMessageCount();
			}else if(activity == ActivityType.GroupChat){
				mGroupRcs.add(rc);
				mGroupTotalUnreadCount = rc.getUnreadMessageCount();
			}else if(activity == ActivityType.DiscussionChat){
				mDiscussionRcs.add(rc);
				mDiscussionTotalUnreadCount = rc.getUnreadMessageCount();
			}
		}
	}
	
	protected void processSingleNotify(String ticker,boolean bSound) {
		final int size = mSingleRcs.size();
		if (mSingleRcs.size() > 0) {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
					.setAutoCancel(true)
					.setSmallIcon(mIconResId)
					.setTicker(ticker)
					.setNumber(mSingleTotalUnreadCount);
			if(bSound){
				final boolean bReduce = mRecentChatUnreadChange == null || mRecentChatUnreadChange.getUnreadMessageCount() == 0;
				if(IMConfigManager.getInstance().isReceiveNewMessageSoundNotify() && !bReduce){
					builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
				}
				if(IMConfigManager.getInstance().isReceiveNewMessageVibrateNotify() && !bReduce){
					builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
				}
			}
			
			if (mSingleRcs.size() > 1) {
				builder.setContentTitle(mContext.getString(R.string.app_name))
						.setContentText(mContext.getString(R.string.statusbar_multi_contact_notify, size, mSingleTotalUnreadCount))
						.setContentIntent(getPendingIntent(null, null, 0));
			} else {
				final RecentChat recentChat = mSingleRcs.iterator().next();
				if (mSingleTotalUnreadCount > 1) {
					builder.setContentTitle(recentChat.getName())
							.setContentText(mContext.getString(R.string.statusbar_single_contact_multimsg_notify, mSingleTotalUnreadCount))
							.setContentIntent(getPendingIntent(recentChat.getId(), recentChat.getName(), recentChat.getActivityType()));

				} else {
					XMessage m = readLastMessage(recentChat.getId());
					final int nMessageType = m == null ? XMessage.TYPE_TEXT : m.getType();
					builder.setContentTitle(mContext.getString(nMessageType == XMessage.TYPE_VOICE ? R.string.statusbar_single_contact_voice_notify
							: R.string.statusbar_single_contact_text_notify, recentChat.getName()))
							.setContentText(nMessageType == XMessage.TYPE_VOICE ?
									mContext.getString(R.string.voice) : nMessageType == XMessage.TYPE_PHOTO ?
									mContext.getString(R.string.photo) : m == null ? "" : m.getContent())
							.setContentIntent(getPendingIntent(recentChat.getId(), recentChat.getName(), recentChat.getActivityType()));
				}
			}

			mNotificationManager.notify(NOTIFY_ID_SINGLECHAT, builder.build());
		} else {
			clearSingleNotify();
		}
	}
	
	protected void processGroupNotify(String ticker,boolean bSound){
		final int size = mGroupRcs.size();
		if (mGroupRcs.size() > 0) {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
					.setAutoCancel(true)
					.setSmallIcon(mIconResId)
					.setTicker(ticker)
					.setNumber(mGroupTotalUnreadCount);
			if(bSound){
				final boolean bReduce = mRecentChatUnreadChange == null || mRecentChatUnreadChange.getUnreadMessageCount() == 0;
				if(IMConfigManager.getInstance().isReceiveNewMessageSoundNotify() && !bReduce){
					builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
				}
				if(IMConfigManager.getInstance().isReceiveNewMessageVibrateNotify() && !bReduce){
					builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
				}
			}

			if (mGroupRcs.size() > 1) {
				builder.setContentTitle(mContext.getString(R.string.app_name))
						.setContentText(mContext.getString(R.string.statusbar_multigroupnotify, size, mGroupTotalUnreadCount))
						.setContentIntent(getPendingIntent(null, null, 0));
			} else {
				final RecentChat recentChat = mGroupRcs.iterator().next();
				if (mGroupTotalUnreadCount > 1) {
					builder.setContentTitle(recentChat.getName())
							.setContentText(mContext.getString(R.string.statusbar_single_contact_multimsg_notify, mGroupTotalUnreadCount))
							.setContentIntent(getPendingIntent(recentChat.getId(), recentChat.getName(), recentChat.getActivityType()));

				} else {
					XMessage m = readLastMessage(recentChat.getId());
					final int nMessageType = m == null ? XMessage.TYPE_TEXT : m.getType();
					builder.setContentTitle(recentChat.getName())
							.setContentText(nMessageType == XMessage.TYPE_VOICE ?
									mContext.getString(R.string.voice) : nMessageType == XMessage.TYPE_PHOTO ?
									mContext.getString(R.string.photo) : m == null ? "" : m.getContent())
							.setContentIntent(getPendingIntent(recentChat.getId(), recentChat.getName(), recentChat.getActivityType()));
				}
			}

			mNotificationManager.notify(NOTIFY_ID_GROUPCHAT, builder.build());
		} else {
			clearGroupNotify();
		}
	}
	
	protected void processDiscussionNotify(String ticker,boolean bSound){
		final int size = mDiscussionRcs.size();
		if (mDiscussionRcs.size() > 0) {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
					.setAutoCancel(true)
					.setSmallIcon(mIconResId)
					.setTicker(ticker)
					.setNumber(mDiscussionTotalUnreadCount);
			
			if(bSound){
				final boolean bReduce = mRecentChatUnreadChange == null || mRecentChatUnreadChange.getUnreadMessageCount() == 0;
				if(IMConfigManager.getInstance().isReceiveNewMessageSoundNotify() && !bReduce){
					builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
				}
				if(IMConfigManager.getInstance().isReceiveNewMessageVibrateNotify() && !bReduce){
					builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
				}
			}

			if (mDiscussionRcs.size() > 1) {
				builder.setContentTitle(mContext.getString(R.string.app_name))
						.setContentText(mContext.getString(R.string.statusbar_multidiscussionnotify, size, mDiscussionTotalUnreadCount))
						.setContentIntent(getPendingIntent(null, null, 0));
			} else {
				final Context app = mContext;
				final RecentChat recentChat = mDiscussionRcs.iterator().next();
				if (mDiscussionTotalUnreadCount > 1) {
					builder.setContentTitle(recentChat.getName())
							.setContentText(mContext.getString(R.string.statusbar_single_contact_multimsg_notify, mDiscussionTotalUnreadCount))
							.setContentIntent(getPendingIntent(recentChat.getId(), recentChat.getName(), recentChat.getActivityType()));

				} else {
					XMessage m = readLastMessage(recentChat.getId());
					final int nMessageType = m == null ? XMessage.TYPE_TEXT : m.getType();
					builder.setContentTitle(recentChat.getName())
							.setContentText(nMessageType == XMessage.TYPE_VOICE ?
									mContext.getString(R.string.voice) : nMessageType == XMessage.TYPE_PHOTO ?
									mContext.getString(R.string.photo) : m == null ? "" : m.getContent())
							.setContentIntent(getPendingIntent(recentChat.getId(), recentChat.getName(), recentChat.getActivityType()));
				}
			}
			mNotificationManager.notify(NOTIFY_ID_DISCUSSIONCHAT, builder.build());
		} else {
			clearDiscussionNotify();
		}
	}
	
	public void clearStatusBar(){
		mNotificationManager.cancel(NOTIFY_ID_SINGLECHAT);
		mNotificationManager.cancel(NOTIFY_ID_GROUPCHAT);
		mNotificationManager.cancel(NOTIFY_ID_DISCUSSIONCHAT);
	}
	
	public void clearSingleNotify(){
		mNotificationManager.cancel(NOTIFY_ID_SINGLECHAT);
	}
	
	public void clearGroupNotify(){
		mNotificationManager.cancel(NOTIFY_ID_GROUPCHAT);
	}
	
	public void clearDiscussionNotify(){
		mNotificationManager.cancel(NOTIFY_ID_DISCUSSIONCHAT);
	}
	
	protected XMessage readLastMessage(String id){
		return XDB.getInstance().readLast(MessageBaseRunner.getTableName(id), 
					DBColumns.Message.COLUMN_AUTOID, 
					new MessageCreator(id, XMessage.FROMTYPE_SINGLE));
	}
	
	protected PendingIntent getPendingIntent(String id,String name,int activity){
		if(mJumpActivityClass == null){
			return null;
		}
		Intent intent = new Intent(IMKernel.getInstance().getContext(), mJumpActivityClass);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		Bundle b = new Bundle();
		Class<?> cls = ActivityType.getActivityClass(activity);
		if(cls != null){
			b.putString("class_name", cls.getName());
		}
		b.putString("id", id);
		b.putString("name", name);
		intent.putExtra("jump", b);
		return PendingIntent.getActivity(IMKernel.getInstance().getContext()
				, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
