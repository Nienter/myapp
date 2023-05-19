package com.xbcx.im.ui.messageviewprovider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.URLSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xbcx.core.ToastManager;
import com.xbcx.core.XApplication;
import com.xbcx.im.ExpressionCoding;
import com.xbcx.im.XMessage;
import com.xbcx.im.ui.IMGlobalSetting;
import com.xbcx.im.ui.TextMessageImageCoder;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

public class TextViewLeftProvider extends CommonViewProvider<TextViewLeftProvider.TextViewHolder> {
	
	private Activity mActivity;
	
	public TextViewLeftProvider(Activity activity) {
		mActivity = activity;
	}

	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.getType() == XMessage.TYPE_TEXT){
			XMessage hm = (XMessage)message;
			return !hm.isFromSelf();
		}
		return false;
	}
	
	@Override
	protected void onSetViewHolder(View convertView, TextViewHolder viewHolder,XMessage xm) {
		super.onSetViewHolder(convertView, viewHolder,xm);
		final Context context = convertView.getContext();
		viewHolder.mTextView = (TextView)LayoutInflater.from(context)
				.inflate(R.layout.message_content_text, null);
		viewHolder.mTextView.setClickable(false);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT, 
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER);
		viewHolder.mContentView.addView(viewHolder.mTextView,lp);
		final int avatarSize = context.getResources().getDimensionPixelSize(R.dimen.chat_avatar_size);
		viewHolder.mTextView.setMaxWidth(XApplication.getScreenWidth() - 
				avatarSize * 2 - 
				SystemUtils.dipToPixel(context, 52));
	}

	@Override
	protected TextViewHolder onCreateViewHolder() {
		return new TextViewHolder();
	}

	@Override
	protected void onUpdateView(TextViewHolder viewHolder, XMessage m) {
		final String strContent = m.getContent();
		Drawable d = null;
		for(TextMessageImageCoder codec : IMGlobalSetting.textMsgImageCodeces){
			if(codec.isSingleDrawable()){
				d = codec.decode(strContent);
				if(d != null){
					viewHolder.mTextView.setCompoundDrawablesWithIntrinsicBounds(
							d, null, null, null);
					viewHolder.mTextView.setText(null);
					viewHolder.mContentView.setBackgroundResource(0);
					break;
				}
			}
		}
		if(d == null){
			viewHolder.mTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			SpannableStringBuilder ssb = ExpressionCoding.spanAllExpression(m.getContent());
			
			viewHolder.mTextView.setText(ssb);
			viewHolder.mContentView.setBackgroundResource(m.isFromSelf() ? 
					R.drawable.chat_bubble_right : R.drawable.chat_bubble_left);
			
			URLSpan spans[] = viewHolder.mTextView.getUrls();
			if(spans != null){
				CharSequence text = viewHolder.mTextView.getText();
				if(text instanceof Spannable){
					Spannable s = (Spannable)text;
					for(URLSpan span : spans){
						s.setSpan(new MyUrlSpan(span.getURL()), 
								s.getSpanStart(span),
								s.getSpanEnd(span), 
								SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
						s.removeSpan(span);
					}
				}
			}
		}
		
		viewHolder.mTextView.requestLayout();
	}

	protected static class TextViewHolder extends CommonViewProvider.CommonViewHolder{
		public TextView mTextView;
	}
	
	private class MyUrlSpan extends URLSpan{
		public MyUrlSpan(String url) {
			super(url);
		}
		
		@Override
		public void onClick(View widget) {
			if(getURL().startsWith("http")){
				if(IMGlobalSetting.textMsgUrlJumpActivity == null){
					try{
						super.onClick(widget);
					}catch(Exception e){
						ToastManager.getInstance(widget.getContext()).show(R.string.toast_no_browser);
					}
				}else{
					Intent i = new Intent(mActivity, IMGlobalSetting.textMsgUrlJumpActivity);
					i.putExtra("url", getURL());
					i.putExtra("hastab", true);
					mActivity.startActivity(i);
				}
			}else{
				try{
					super.onClick(widget);
				}catch(Exception e){
				}
			}
		}
	}
}
