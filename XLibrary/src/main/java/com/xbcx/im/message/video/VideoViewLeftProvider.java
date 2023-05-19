package com.xbcx.im.message.video;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xbcx.core.XApplication;
import com.xbcx.im.XMessage;
import com.xbcx.im.ui.messageviewprovider.CommonViewProvider;
import com.xbcx.library.R;

public class VideoViewLeftProvider extends CommonViewProvider<VideoViewLeftProvider.VideoViewHolder> {

	@Override
	public boolean acceptHandle(XMessage message) {
		if(!message.isFromSelf()){
			return message.getType() == XMessage.TYPE_VIDEO;
		}
		return false;
	}

	@Override
	protected VideoViewHolder onCreateViewHolder() {
		return new VideoViewHolder();
	}

	@Override
	protected void onSetViewHolder(View convertView, VideoViewHolder viewHolder,XMessage xm) {
		super.onSetViewHolder(convertView, viewHolder,xm);
		View v = LayoutInflater.from(convertView.getContext()).inflate(R.layout.message_content_video, null);
		viewHolder.mImageViewVideo = (ImageView)v.findViewById(R.id.ivVideo);
		viewHolder.mTextViewTime = (TextView)v.findViewById(R.id.tvTime);
		viewHolder.mProgressBar = (ProgressBar)v.findViewById(R.id.pb);
		viewHolder.mContentView.addView(v);
		viewHolder.mButton.setText(R.string.cancel);
		viewHolder.mButton.setTextColor(0xffa40000);
		viewHolder.mButton.setBackgroundResource(R.drawable.msg_btn_gray);
	}

	@Override
	protected void onUpdateView(VideoViewHolder viewHolder, XMessage m) {
		ProgressBar progressBar = viewHolder.mProgressBar;
		if(m.isThumbDownloading()){
			viewHolder.mButton.setVisibility(View.GONE);
			viewHolder.mImageViewVideo.setImageResource(R.drawable.msg_video_default);
			progressBar.setVisibility(View.VISIBLE);
			progressBar.setProgress(m.getThumbDownloadPercentage());
			viewHolder.mTextViewTime.setVisibility(View.GONE);
		}else if(m.isDownloading()){
			viewHolder.mButton.setVisibility(View.VISIBLE);
			viewHolder.mImageViewVideo.setImageResource(R.drawable.msg_video_default);
			if(m.isThumbFileExists()){
				if(!XApplication.setBitmapFromFile(viewHolder.mImageViewVideo, m.getThumbFilePath())){
					viewHolder.mImageViewVideo.setImageResource(R.drawable.msg_video_default);
				}
			}else{
				viewHolder.mImageViewVideo.setImageResource(R.drawable.msg_video_default);
			}
			progressBar.setVisibility(View.VISIBLE);
			progressBar.setProgress(m.getDownloadPercentage());
			viewHolder.mTextViewTime.setVisibility(View.GONE);
		}else{
			viewHolder.mButton.setVisibility(View.GONE);
			boolean bShowTime = true;
			if(m.isThumbFileExists()){
				if(!XApplication.setBitmapFromFile(viewHolder.mImageViewVideo, m.getThumbFilePath())){
					viewHolder.mImageViewVideo.setImageResource(R.drawable.chat_img_wrong);
					viewHolder.mViewWarning.setVisibility(View.VISIBLE);
					bShowTime = false;
				}
			}else if(m.isDownloaded()){
				viewHolder.mImageViewVideo.setImageResource(R.drawable.chat_img_wrong);
				viewHolder.mViewWarning.setVisibility(View.VISIBLE);
				bShowTime = false;
			}else{
				viewHolder.mImageViewVideo.setImageResource(R.drawable.msg_video_default);
				viewHolder.mViewWarning.setVisibility(View.GONE);
			}
			progressBar.setVisibility(View.GONE);
			if(bShowTime){
				viewHolder.mTextViewTime.setVisibility(View.VISIBLE);
				viewHolder.mTextViewTime.setText(getVideoTimeShow(m.getVideoSeconds()));
			}else{
				viewHolder.mTextViewTime.setVisibility(View.GONE);
			}
		}
	}
	
	protected String getVideoTimeShow(int seconds){
		final int minute = seconds / 60;
		final int second = seconds % 60;
		String strMinute,strSecond;
		if(minute > 9){
			strMinute = String.valueOf(minute);
		}else{
			strMinute = "0" + minute;
		}
		if(second > 9){
			strSecond = String.valueOf(second);
		}else{
			strSecond = "0" + second;
		}
		return strMinute + ":" + strSecond;
	}

	protected static class VideoViewHolder extends CommonViewProvider.CommonViewHolder{
		public ImageView	mImageViewVideo;
		public TextView		mTextViewTime;
		public ProgressBar	mProgressBar;
	}
}
