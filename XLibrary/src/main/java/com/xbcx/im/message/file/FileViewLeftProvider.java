package com.xbcx.im.message.file;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xbcx.im.XMessage;
import com.xbcx.im.ui.messageviewprovider.CommonViewProvider;
import com.xbcx.library.R;

public class FileViewLeftProvider extends CommonViewProvider<FileViewLeftProvider.FileViewHolder> {
	
	@Override
	public boolean acceptHandle(XMessage message) {
		if(!message.isFromSelf()){
			return message.getType() == XMessage.TYPE_FILE;
		}
		return false;
	}

	@Override
	protected FileViewHolder onCreateViewHolder() {
		return new FileViewHolder();
	}

	@Override
	protected void onSetViewHolder(View convertView, FileViewHolder viewHolder,XMessage xm) {
		super.onSetViewHolder(convertView, viewHolder,xm);
		final View v = LayoutInflater.from(convertView.getContext()).inflate(
				R.layout.message_content_file, null);
		FileViewHolder fViewHolder = (FileViewHolder)viewHolder;
		fViewHolder.mImageViewIcon = (ImageView)v.findViewById(R.id.ivIcon);
		fViewHolder.mTextViewName = (TextView)v.findViewById(R.id.tvName);
		fViewHolder.mTextViewStatus = (TextView)v.findViewById(R.id.tvStatus);
		fViewHolder.mProgressBar = (ProgressBar)v.findViewById(R.id.pb);
		fViewHolder.mContentView.addView(v);
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
		if(m.isDownloading()){
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
			if(m.isFileExists()){
				bShowFileSize = true;
				fViewHolder.mButton.setVisibility(View.GONE);
			}else if(m.isDownloaded()){
				fViewHolder.mTextViewStatus.setText(R.string.msg_receive_fail);
				fViewHolder.mTextViewStatus.setTextColor(0xffa40000);
				fViewHolder.mButton.setVisibility(View.VISIBLE);
				fViewHolder.mButton.setBackgroundResource(R.drawable.msg_btn_gray);
				fViewHolder.mButton.setText(R.string.retry);
				fViewHolder.mButton.setTextColor(0xff000000);
			}else{
				bShowFileSize = true;
				fViewHolder.mButton.setVisibility(View.VISIBLE);
				fViewHolder.mButton.setText(R.string.receive);
				fViewHolder.mButton.setBackgroundResource(R.drawable.msg_btn_green);
				fViewHolder.mButton.setTextColor(0xffffffff);
			}
			
			if(bShowFileSize){
				fViewHolder.mTextViewStatus.setText(
						fViewHolder.mTextViewStatus.getContext().getString(R.string.file_size) + 
						"  " + getFileSizeShow(m.getFileSize()));
			}
		}
	}
	
	protected String getFileSizeShow(long size){
		if(size < 1024){
			return size + "B";
		}
		long kb = size / 1024;
		return kb + "KB";
	}

	protected static class FileViewHolder extends CommonViewProvider.CommonViewHolder{
		public ImageView	mImageViewIcon;
		public TextView		mTextViewName;
		public TextView		mTextViewStatus;
		public ProgressBar	mProgressBar;
	}
}
