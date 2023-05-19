package com.xbcx.im.message.file;

import android.view.View;

import com.xbcx.im.XMessage;
import com.xbcx.library.R;

public class FileViewRightProvider extends FileViewLeftProvider {

	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.isFromSelf()){
			return message.getType() == XMessage.TYPE_FILE;
		}
		return false;
	}

	@Override
	protected void onUpdateView(FileViewHolder viewHolder, XMessage m) {
		FileViewHolder fViewHolder = (FileViewHolder)viewHolder;
		/*final String ext = FileHelper.getFileExt(m.getDisplayName(), "xls");
		if("pdf".equalsIgnoreCase(ext) ||
				"txt".equalsIgnoreCase(ext)){
			fViewHolder.mImageViewIcon.setImageResource(R.drawable.msg_icon_pdf);
		}else{
			fViewHolder.mImageViewIcon.setImageResource(R.drawable.msg_icon_office);
		}*/
		fViewHolder.mTextViewName.setText(m.getDisplayName());
		fViewHolder.mTextViewStatus.setTextColor(0xff535353);
		if(m.isUploading()){
			fViewHolder.mProgressBar.setVisibility(View.VISIBLE);
			fViewHolder.mTextViewStatus.setText(R.string.sending);
			fViewHolder.mProgressBar.setProgress(m.getUploadPercentage());
			fViewHolder.mButton.setVisibility(View.VISIBLE);
			fViewHolder.mButton.setBackgroundResource(R.drawable.msg_btn_gray);
			fViewHolder.mButton.setText(R.string.cancel);
			fViewHolder.mButton.setTextColor(0xffa40000);
		}else if(m.isDownloading()){
			fViewHolder.mProgressBar.setVisibility(View.VISIBLE);
			fViewHolder.mTextViewStatus.setText(R.string.receiving);
			fViewHolder.mProgressBar.setProgress(m.getDownloadPercentage());
			fViewHolder.mButton.setVisibility(View.VISIBLE);
			fViewHolder.mButton.setBackgroundResource(R.drawable.msg_btn_gray);
			fViewHolder.mButton.setText(R.string.cancel);
			fViewHolder.mButton.setTextColor(0xffa40000);
		}else{
			fViewHolder.mProgressBar.setVisibility(View.GONE);
			boolean bShowFileSize = false;
			if(m.isUploadSuccess()){
				bShowFileSize = true;
				fViewHolder.mButton.setVisibility(View.GONE);
			}else{
				fViewHolder.mTextViewStatus.setText(R.string.msg_receive_fail);
				fViewHolder.mTextViewStatus.setTextColor(0xffa40000);
				fViewHolder.mButton.setVisibility(View.VISIBLE);
				fViewHolder.mButton.setBackgroundResource(R.drawable.msg_btn_gray);
				fViewHolder.mButton.setText(R.string.retry);
				fViewHolder.mButton.setTextColor(0xff000000);
			}
			
			if(bShowFileSize){
				fViewHolder.mTextViewStatus.setText(
						fViewHolder.mTextViewStatus.getContext().getString(R.string.file_size) + 
						"  " + getFileSizeShow(m.getFileSize()));
			}
		}
	}

}
