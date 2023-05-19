package com.xbcx.im.message.video;

import android.view.View;
import android.widget.ProgressBar;

import com.xbcx.core.XApplication;
import com.xbcx.im.XMessage;

public class VideoViewRightProvider extends VideoViewLeftProvider {

	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.isFromSelf()){
			return message.getType() == XMessage.TYPE_VIDEO;
		}
		return false;
	}

	@Override
	protected void onUpdateView(VideoViewHolder viewHolder, XMessage m) {
		XApplication.setBitmapFromFile(viewHolder.mImageViewVideo, m.getThumbFilePath());
		if(m.isUploading()){
			final ProgressBar pb = viewHolder.mProgressBar;
			pb.setVisibility(View.VISIBLE);
			pb.setProgress(m.getUploadPercentage());
			viewHolder.mTextViewTime.setVisibility(View.GONE);
			viewHolder.mButton.setVisibility(View.VISIBLE);
		}else if(m.isDownloading()){
			viewHolder.mProgressBar.setVisibility(View.VISIBLE);
			viewHolder.mProgressBar.setProgress(m.getDownloadPercentage());
			viewHolder.mTextViewTime.setVisibility(View.GONE);
			viewHolder.mButton.setVisibility(View.VISIBLE);
		}else if(m.isUploadSuccess()){
			viewHolder.mProgressBar.setVisibility(View.GONE);
			viewHolder.mTextViewTime.setVisibility(View.VISIBLE);
			viewHolder.mTextViewTime.setText(getVideoTimeShow(m.getVideoSeconds()));
			viewHolder.mButton.setVisibility(View.GONE);
		}else{
			viewHolder.mProgressBar.setVisibility(View.GONE);
			setShowWarningView(viewHolder.mViewWarning, true);
			viewHolder.mTextViewTime.setVisibility(View.VISIBLE);
			viewHolder.mTextViewTime.setText(getVideoTimeShow(m.getVideoSeconds()));
			viewHolder.mButton.setVisibility(View.GONE);
		}
	}

}
