package com.xbcx.im.message.voice;

import com.xbcx.im.XMessage;
import com.xbcx.im.messageprocessor.VoicePlayProcessor;
import com.xbcx.library.R;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class VoiceViewRightProvider extends VoiceViewLeftProvider {

	public VoiceViewRightProvider(Context context) {
		super(context);
	}

	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.getType() == XMessage.TYPE_VOICE){
			XMessage hm = (XMessage)message;
			return hm.isFromSelf();
		}
		return false;
	}
	
	@Override
	protected void onUpdateView(VoiceViewHolder viewHolder, XMessage m) {
		if(m.isUploading() ||
				m.isDownloading()){
			viewHolder.mProgressBar.setVisibility(View.VISIBLE);
			viewHolder.mImageViewVoice.setVisibility(View.GONE);
		}else{
			ImageView imageViewVoice = viewHolder.mImageViewVoice;
			viewHolder.mProgressBar.setVisibility(View.GONE);
			imageViewVoice.setVisibility(View.VISIBLE);
			if (m.isUploadSuccess()) {
				if (VoicePlayProcessor.getInstance().isPlaying(m)) {
					imageViewVoice.setImageResource(R.drawable.animlist_play_voice);
					AnimationDrawable ad = (AnimationDrawable)imageViewVoice.getDrawable();
					ad.start();
				} else{
					imageViewVoice.setImageResource(R.drawable.voice_played);
				}
			}else{
				imageViewVoice.setImageResource(R.drawable.voice_played);
				setShowWarningView(viewHolder.mViewWarning, true);
			}
		}
		
		showSeconds(viewHolder.mTextViewVoice, m);
	}

	@Override
	protected void onSetViewHolder(View convertView, VoiceViewHolder viewHolder,XMessage xm) {
		super.onSetViewHolder(convertView, viewHolder,xm);
		viewHolder.mImageViewVoice.setScaleType(ScaleType.MATRIX);
		viewHolder.mImageViewVoice.setImageResource(R.drawable.voice_playing_unplay);
		final int width = viewHolder.mImageViewVoice.getDrawable().getIntrinsicWidth();
		final int height = viewHolder.mImageViewVoice.getDrawable().getIntrinsicHeight();
		Matrix m = new Matrix();
		//final int fPx = SystemUtils.dipToPixel(convertView.getContext(), 10);
		//final int fPy = SystemUtils.dipToPixel(convertView.getContext(), 9);
		m.setRotate(180, (float)width / 2,(float)height / 2);
		viewHolder.mImageViewVoice.setImageMatrix(m);
	}

	@Override
	protected View onCreateVoiceView(Context context) {
		return LayoutInflater.from(context).inflate(R.layout.message_content_voice_right, null);
	}

}
