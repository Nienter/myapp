package com.xbcx.im.message.video;

import android.app.Activity;

import com.xbcx.common.choose.ChooseVideoProvider.ChooseVideoCallback;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.XApplication;
import com.xbcx.im.ui.ChatActivity;
import com.xbcx.im.ui.SendPlugin;

public class VideoSendPlugin extends SendPlugin implements
												ChooseVideoCallback{

	public VideoSendPlugin(String id, int icon) {
		super(id, icon);
	}

	@Override
	public void onSend(ChatActivity activity) {
		if(XApplication.checkExternalStorageAvailable()){
			activity.chooseProvider(BaseActivity.RequestCode_LaunchChooseVideo, 
					this);
		}
	}

	@Override
	public int getSendType() {
		return 0;
	}

	@Override
	public void onVideoChoosed(Activity activity, String videoPath,
			long duration) {
		((ChatActivity)activity).sendVideo(videoPath, duration);
	}

}
