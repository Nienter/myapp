package com.xbcx.im.message.photo;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.xbcx.core.XApplication;
import com.xbcx.im.XMessage;
import com.xbcx.im.ui.messageviewprovider.CommonViewProvider;
import com.xbcx.library.R;

public class PhotoViewLeftProvider extends CommonViewProvider<PhotoViewLeftProvider.PhotoViewHolder> {
	
	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.getType() == XMessage.TYPE_PHOTO){
			XMessage m = (XMessage)message;
			return !m.isFromSelf();
		}
		return false;
	}

	@Override
	protected void onUpdateView(PhotoViewHolder pHolder, XMessage m) {
		ProgressBar progressBar = pHolder.mProgressBar;
		if(m.isThumbDownloading()){
			pHolder.mImageViewPhoto.setImageResource(R.drawable.chat_img);
			progressBar.setVisibility(View.VISIBLE);
			progressBar.setProgress(m.getThumbDownloadPercentage());
		}else{
			if(m.isThumbFileExists()){
				if(m.isFileExists()){
					if(!XApplication.setBitmapFromFile(
							pHolder.mImageViewPhoto, m.getFilePath())){
						pHolder.mImageViewPhoto.setImageResource(R.drawable.chat_img);
					}
				}else{
					if(!XApplication.setBitmapFromFile(
							pHolder.mImageViewPhoto, m.getThumbFilePath())){
						pHolder.mImageViewPhoto.setImageResource(R.drawable.chat_img);
					}
				}
			}else if(m.isDownloaded()){
				pHolder.mImageViewPhoto.setImageResource(R.drawable.chat_img_wrong);
				pHolder.mViewWarning.setVisibility(View.VISIBLE);
			}else{
				pHolder.mImageViewPhoto.setImageResource(R.drawable.chat_img);
				pHolder.mViewWarning.setVisibility(View.GONE);
			}
			progressBar.setVisibility(View.GONE);
		}
	}

	@Override
	protected PhotoViewHolder onCreateViewHolder() {
		return new PhotoViewHolder();
	}

	@Override
	protected void onSetViewHolder(View convertView, PhotoViewHolder pHolder,XMessage xm) {
		super.onSetViewHolder(convertView, pHolder,xm);
		final View contentView = LayoutInflater.from(convertView.getContext())
				.inflate(R.layout.message_content_photo, null);
		pHolder.mImageViewPhoto = (ImageView)contentView.findViewById(R.id.ivPhoto);
		pHolder.mProgressBar = (ProgressBar)contentView.findViewById(R.id.pb);
		pHolder.mProgressBar.setMax(100);
		pHolder.mContentView.addView(contentView);
	}

	protected static class PhotoViewHolder extends CommonViewProvider.CommonViewHolder{
		public ImageView 	mImageViewPhoto;
		
		public ProgressBar 	mProgressBar;
	}
}
