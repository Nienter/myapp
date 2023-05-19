package com.xbcx.im.message.voice;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xbcx.im.XMessage;
import com.xbcx.im.messageprocessor.VoicePlayProcessor;
import com.xbcx.im.ui.messageviewprovider.CommonViewProvider;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

public class VoiceViewLeftProvider extends CommonViewProvider<VoiceViewLeftProvider.VoiceViewHolder> {

	protected int mTextViewMinWidth;
	protected int mTextViewWidthIncrement;
	
	public VoiceViewLeftProvider(Context context){
		mTextViewMinWidth = SystemUtils.dipToPixel(context, 30);
		mTextViewWidthIncrement = SystemUtils.dipToPixel(context, 2);
	}
	
	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.getType() == XMessage.TYPE_VOICE){
			XMessage hm = (XMessage)message;
			return !hm.isFromSelf();
		}
		return false;
	}
	
	@Override
	protected VoiceViewHolder onCreateViewHolder() {
		return new VoiceViewHolder();
	}

	@Override
	protected void onSetViewHolder(View convertView, VoiceViewHolder viewHolder,XMessage xm) {
		super.onSetViewHolder(convertView, viewHolder,xm);
		final View voiceView = onCreateVoiceView(convertView.getContext());
		voiceView.setClickable(false);
		viewHolder.mImageViewVoice = (ImageView)voiceView.findViewById(R.id.ivVoice);
		viewHolder.mTextViewVoice = (TextView)voiceView.findViewById(R.id.tvVoice);
		viewHolder.mProgressBar = (ProgressBar)voiceView.findViewById(R.id.pbDownload);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT, 
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER_VERTICAL);
		viewHolder.mContentView.addView(voiceView,lp);
	}
	
	protected View onCreateVoiceView(Context context){
		return LayoutInflater.from(context).inflate(R.layout.message_content_voice_left, null);
	}

	@Override
	protected void onUpdateView(VoiceViewHolder viewHolder, XMessage m) {
		if(m.isDownloading()){
			viewHolder.mProgressBar.setVisibility(View.VISIBLE);
			viewHolder.mImageViewVoice.setVisibility(View.GONE);
		}else{
			final ImageView imageViewVoice = viewHolder.mImageViewVoice;
			viewHolder.mProgressBar.setVisibility(View.GONE);
			imageViewVoice.setVisibility(View.VISIBLE);
			if(VoicePlayProcessor.getInstance().isPlaying(m)){
				imageViewVoice.setImageResource(R.drawable.animlist_play_voice);
				AnimationDrawable ad = (AnimationDrawable)imageViewVoice.getDrawable();
				ad.start();
			}else if(m.isPlayed()){
				imageViewVoice.setImageResource(R.drawable.voice_played);
			}else if(m.isFileExists()){
				imageViewVoice.setImageResource(R.drawable.voice_playing_unplay);
			}else if(m.isDownloaded()){
				imageViewVoice.setImageResource(R.drawable.voice_playing_unplay);
				setShowWarningView(viewHolder.mViewWarning, true);
			}else{
				imageViewVoice.setImageResource(R.drawable.voice_playing_unplay);
			}
		}
		
		showSeconds(viewHolder.mTextViewVoice, m);
	}
	
	protected void showSeconds(TextView textView,XMessage m){
		final int seconds = m.getVoiceSeconds();
		textView.setMinimumWidth(mTextViewMinWidth + (seconds - 1) * mTextViewWidthIncrement);
		textView.setText(seconds + "\"");
	}
	
	protected void showFail(TextView textView,int nResId){
		textView.setMinimumWidth(mTextViewMinWidth);
		textView.setText(nResId);
	}

	protected static class VoiceViewHolder extends CommonViewProvider.CommonViewHolder{
		public ImageView 	mImageViewVoice;
		
		public ProgressBar 	mProgressBar;
		
		public TextView		mTextViewVoice;
	}
}
