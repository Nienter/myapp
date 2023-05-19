package com.xbcx.im.message.photo;

import android.view.View;
import android.widget.ProgressBar;

import com.xbcx.core.XApplication;
import com.xbcx.im.XMessage;
import com.xbcx.library.R;

public class PhotoViewRightProvider extends PhotoViewLeftProvider {

	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.getType() == XMessage.TYPE_PHOTO){
			XMessage m = (XMessage)message;
			return m.isFromSelf();
		}
		return false;
	}

	@Override
	protected void onUpdateView(PhotoViewHolder pHolder, XMessage m) {
		if(!XApplication.setBitmapFromFile(pHolder.mImageViewPhoto, 
				m.getFilePath())){
			if(!XApplication.setBitmapFromFile(pHolder.mImageViewPhoto, 
					m.getThumbFilePath())){
				pHolder.mImageViewPhoto.setImageResource(R.drawable.chat_img);
			}
		}
		if(m.isUploading()){
			final ProgressBar pb = pHolder.mProgressBar;
			pb.setVisibility(View.VISIBLE);
			pb.setProgress(m.getUploadPercentage());
		}else if(m.isThumbDownloading()){
			pHolder.mProgressBar.setVisibility(View.VISIBLE);
			pHolder.mProgressBar.setProgress(m.getThumbDownloadPercentage());
		}else if(m.isUploadSuccess()){
			pHolder.mProgressBar.setVisibility(View.GONE);
		}else{
			pHolder.mProgressBar.setVisibility(View.GONE);
			setShowWarningView(pHolder.mViewWarning, true);
		}
	}
}
