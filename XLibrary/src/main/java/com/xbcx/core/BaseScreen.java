package com.xbcx.core;

import java.util.Locale;

import com.xbcx.core.BaseActivity.BaseAttribute;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public abstract class BaseScreen implements View.OnClickListener{
	
	protected Context					mContext;
	
	protected ViewGroup					mContentParent;
	
	protected BaseAttribute				mBaseAttribute;
	protected XTitleView				mViewTitle;
	protected View 						mButtonBack;
	protected TextView 					mTextViewTitle;
	protected TextView					mTextViewSubTitle;
	protected View						mViewTitleRight;
	
	protected ProgressDialog 			mProgressDialog;
		
	protected boolean					mIsXProgressFocusable;
	private   boolean					mIsXProgressAdded;
	protected View						mViewXProgressDialog;
	protected TextView					mTextViewXProgress;
	protected boolean					mIsXProgressDialogShowing;
	protected int						mXProgressDialogShowCount;
	
	private	  ContentStatusViewProvider	mContentStatusViewProvider;
	
	public BaseScreen(Context context,BaseAttribute ba){
		mContext = context;
		mBaseAttribute = ba;
	}
	
	public void onCreate(){
		onInitScreen();
	}
	
	public void onDestory(){
	}
	
	public void onPostCreate(){
		
	}
	
	protected void onInitScreen(){
		if(mBaseAttribute.mActivityLayoutId == 0){
			String strClassName = mContext.getClass().getName();
			int nIndex = strClassName.lastIndexOf(".");//AboutActivity.class
			if(nIndex != -1){
				final String cName = strClassName.substring(nIndex + 1);
				String strResourceName = "activity_" + cName.replaceFirst("Activity", "");
				strResourceName = strResourceName.toLowerCase(Locale.getDefault());
				final int nLayoutId = mContext.getResources().getIdentifier(strResourceName, 
						"layout", mContext.getPackageName());
				if(nLayoutId != 0){
					setContentView(nLayoutId);
				}
			}
		}else{
			setContentView(mBaseAttribute.mActivityLayoutId);
		}
	}

	public void setContentView(int layoutResId){
		mContentParent.removeAllViews();
		LayoutInflater.from(mContext).inflate(layoutResId, mContentParent);
		
		initTitle();
	}
	
	public void setContentView(View view, ViewGroup.LayoutParams params){
		mContentParent.removeAllViews();
        mContentParent.addView(view, params);
        
        initTitle();
	}
	
	public void addContentView(View view, ViewGroup.LayoutParams params){
        mContentParent.addView(view, params);
	}
	
	public void initTitle(){
		if(mViewTitle != null){
			mViewTitle.setBaseScreen(this);
			if(mBaseAttribute.mHasTitle){
				if(mBaseAttribute.mAddBackButton){
					mButtonBack = onCreateTitleBackButton();
					mButtonBack.setOnClickListener(this);
					mViewTitle.addView(mButtonBack, onCreateBackButtonLayoutParams());
				}
				if(mBaseAttribute.mTitleText == null){
					if(mBaseAttribute.mTitleTextStringId == 0){
						addTextInTitle("");
					}else{
						addTextInTitle(mBaseAttribute.mTitleTextStringId);
					}
				}else{
					addTextInTitle(mBaseAttribute.mTitleText);
				}
			}else{
				mViewTitle.setVisibility(View.GONE);
			}
		}
	}
	
	public boolean hasTitle(){
		return mViewTitle != null && mViewTitle.getVisibility() == View.VISIBLE;
	}
	
	public void setIsXProgressFocusable(boolean bFocus){
		mIsXProgressFocusable = bFocus;
		if(mViewXProgressDialog != null){
			mViewXProgressDialog.setClickable(bFocus);
		}
	}
	
	public RelativeLayout getViewTitle(){
		return mViewTitle;
	}
	
	public TextView	getTextViewTitle(){
		return mTextViewTitle;
	}
	
	public TextView getTextViewSubTitle(){
		return mTextViewSubTitle;
	}
	
	public View	getButtonBack(){
		return mButtonBack;
	}
	
	public View getViewTitleRight(){
		return mViewTitleRight;
	}
	
	public BaseAttribute getBaseAttribute(){
		return mBaseAttribute;
	}
	
	public View onCreateTitleBackButton() {
		ImageView iv = new ImageView(mContext);
		iv.setImageResource(R.drawable.nav_image_back);
		return iv;
	}
	
	public RelativeLayout.LayoutParams onCreateBackButtonLayoutParams(){
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		lp.addRule(RelativeLayout.CENTER_VERTICAL);
		return lp;
	}
	
	public void 	addTextInTitle(int nResId){
		mTextViewTitle = onCreateTitleTextView(nResId);
		mViewTitle.addView(mTextViewTitle,onCreateTitleTextViewLayoutParams());
	}
	
	public void 	addTextInTitle(String strText){
		mTextViewTitle = onCreateTitleTextView(strText);
		mViewTitle.addView(mTextViewTitle,onCreateTitleTextViewLayoutParams());
	}
	
	public TextView	onCreateTitleTextView(int resId){
		return onCreateTitleTextView(mContext.getString(resId));
	}
	
	public TextView	onCreateTitleTextView(String strText){
		final TextView textView = (TextView)LayoutInflater.from(mContext)
				.inflate(R.layout.xlibrary_textview_title, null);
		textView.setText(strText);
		return textView;
	}
	
	public RelativeLayout.LayoutParams onCreateTitleTextViewLayoutParams(){
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.MATCH_PARENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		return lp;
	}
	
	public TextView addSubTitle(){
		mTextViewTitle.setPadding(mTextViewTitle.getPaddingLeft(),
				mTextViewTitle.getPaddingTop(),
				mTextViewTitle.getPaddingRight(),
				SystemUtils.dipToPixel(mContext, 15));
		
		final TextView textViewSubTitle = (TextView) LayoutInflater.from(mContext).inflate(
				R.layout.xlibrary_textview_subtitle, null);
		RelativeLayout.LayoutParams lp = onCreateTitleTextViewLayoutParams();
		RelativeLayout.LayoutParams lpTitle = (RelativeLayout.LayoutParams)mTextViewTitle.getLayoutParams();
		lp.leftMargin = lpTitle.leftMargin;
		lp.rightMargin = lpTitle.rightMargin;
		mViewTitle.addView(textViewSubTitle,lp);
		textViewSubTitle.setPadding(textViewSubTitle.getPaddingLeft(),
				SystemUtils.dipToPixel(mContext, 24),
				textViewSubTitle.getPaddingRight(),
				textViewSubTitle.getPaddingBottom());
		mTextViewSubTitle = textViewSubTitle;
		return mTextViewSubTitle;
	}
	
	public View addImageButtonInTitleRight(int resId){
		return addViewInTitleRight(
				createTitleRightImageButton(resId),
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				getTitleRightImageButtonTopMargin(),
				getTitleRightImageButtonRightMargin());
	}
	
	public View addTextButtonInTitleRight(int textId){
		return addViewInTitleRight(
				createTitleRightTextButton(textId),
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				getTitleRightTextButtonTopMargin(),
				getTitleRightTextButtonRightMargin());
	}
	
	public View addViewInTitleRight(View v,int width,int height,
			int nTopMargin,int nRightMargin){
		final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				width,
				height);
		lp.topMargin = nTopMargin;
		lp.rightMargin = nRightMargin;
		lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mViewTitle.addView(v, lp);
		mViewTitleRight = v;
		
		return v;
	}
	
	protected void updateTitleMargin(){
		if(mTextViewTitle != null){
			final View v = mViewTitleRight;
			int margin = mContext.getResources().getDimensionPixelSize(R.dimen.title_text_min_margin);
			if(v != null){
				v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
				int padding = v.getMeasuredWidth() + ((MarginLayoutParams)v.getLayoutParams()).rightMargin;
				if(padding > margin){
					margin = padding;
				}
			}
			
			if(mButtonBack != null){
				mButtonBack.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
						MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
				int backPadding = mButtonBack.getMeasuredWidth();
				MarginLayoutParams backLp = (MarginLayoutParams)mButtonBack.getLayoutParams();
				backPadding += backLp.leftMargin;
				if(backPadding > margin){
					margin = backPadding;
				}
			}
			MarginLayoutParams lpTitle = (MarginLayoutParams)mTextViewTitle.getLayoutParams();
			lpTitle.leftMargin = margin;
			lpTitle.rightMargin = margin;
			mTextViewTitle.setLayoutParams(lpTitle);
			
			if(mTextViewSubTitle != null){
				lpTitle = (MarginLayoutParams)mTextViewSubTitle.getLayoutParams();
				lpTitle.leftMargin = margin;
				lpTitle.rightMargin = margin;
				mTextViewSubTitle.setLayoutParams(lpTitle);
			}
		}
	}
	
	public View createTitleRightImageButton(int resId) {
		ImageView iv = new ImageView(mContext);
		iv.setImageResource(resId);
		return iv;
	}

	public int getTitleRightImageButtonRightMargin() {
		return SystemUtils.dipToPixel(mContext, 2);
	}

	public int getTitleRightImageButtonTopMargin() {
		return SystemUtils.dipToPixel(mContext, 0);
	}

	public View createTitleRightTextButton(int textId) {
		TextView v = (TextView)LayoutInflater.from(mContext).inflate(R.layout.xlibrary_textview_titleright, null);
		v.setText(textId);
		return v;
	}

	public int getTitleRightTextButtonRightMargin() {
		return SystemUtils.dipToPixel(mContext, 2);
	}

	public int getTitleRightTextButtonTopMargin() {
		return SystemUtils.dipToPixel(mContext, 10);
	}
	
	public void showProgressDialog(){
		showProgressDialog(null, null);
	}
	
	public void showProgressDialog(String strTitle,int nStringId){
		showProgressDialog(strTitle, mContext.getString(nStringId));
	}
	
	public void showProgressDialog(String strTitle,String strMessage){
		if(mProgressDialog == null){
			mProgressDialog = ProgressDialog.show(mContext, strTitle, strMessage, true, false);
		}
	}
	
	public void dismissProgressDialog(){
		try{
			if(mProgressDialog != null){
				mProgressDialog.dismiss();
			}
		}catch(Exception e){
		}
		mProgressDialog = null;
	}
	
	public void showXProgressDialog(){
		showXProgressDialog(null);
	}
	
	public void showXProgressDialog(String text){
		++mXProgressDialogShowCount;
		if(mIsXProgressDialogShowing){
			setXProgressText(text);
			return;
		}
		if(mIsXProgressAdded){
			mViewXProgressDialog.setVisibility(View.VISIBLE);
			mViewXProgressDialog.bringToFront();
			setXProgressText(text);
			mIsXProgressDialogShowing = true;
		}else{
		    final View layout = createXProgressDialog();
		    mTextViewXProgress = (TextView)layout.findViewById(R.id.tv);
		    setXProgressText(text);
		    FrameLayout wrap = addCoverView();
		    if(mIsXProgressFocusable){
		    	wrap.setClickable(true);
		    }
		    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
		    		FrameLayout.LayoutParams.WRAP_CONTENT, 
		    		FrameLayout.LayoutParams.WRAP_CONTENT);
		    lp.gravity = Gravity.CENTER;
		    wrap.addView(layout, lp);
			mViewXProgressDialog = wrap;
			mIsXProgressDialogShowing = true;
			
			mIsXProgressAdded = true;
		}
	}
	
	public boolean isXProgressDialogShowing(){
		return mIsXProgressDialogShowing;
	}
	
	public View createXProgressDialog() {
		final Context context = mContext;
		return LayoutInflater.from(context).inflate(R.layout.xlibrary_xprogress, null);
	}
	
	public FrameLayout addCoverView(){
		FrameLayout.LayoutParams lpWrap = new FrameLayout.LayoutParams(
	    		FrameLayout.LayoutParams.MATCH_PARENT, 
	    		FrameLayout.LayoutParams.MATCH_PARENT);
	    lpWrap.gravity = Gravity.TOP;
	    if(hasTitle()){
	    	lpWrap.topMargin = SystemUtils.dipToPixel(mContext, 50);
	    }
	    FrameLayout wrap = new FrameLayout(mContext);
	    addContentView(wrap, lpWrap);
	    return wrap;
	}
	
	public void dismissXProgressDialog(){
		if(mIsXProgressDialogShowing){
			if(--mXProgressDialogShowCount == 0){
				mViewXProgressDialog.setVisibility(View.GONE);

				mIsXProgressDialogShowing = false;
			}
		}
	}
	
	public void dismissAllXProgressDialog(){
		if(mIsXProgressDialogShowing){
			mXProgressDialogShowCount = 0;
			mViewXProgressDialog.setVisibility(View.GONE);
			mIsXProgressDialogShowing = false;
		}
	}
	
	public void setXProgressText(String text){
		if(mTextViewXProgress != null){
			if(TextUtils.isEmpty(text)){
				mTextViewXProgress.setVisibility(View.GONE);
			}else{
				mTextViewXProgress.setVisibility(View.VISIBLE);
				mTextViewXProgress.setText(text);
			}
		}
	}
	
	public Dialog showYesNoDialog(int msgTextId,DialogInterface.OnClickListener listener){
		return showYesNoDialog(R.string.ok, R.string.cancel, msgTextId, listener);
	}
	
	public Dialog showYesNoDialog(int yesTextId,int msgTextId,DialogInterface.OnClickListener listener){
		return showYesNoDialog(yesTextId, R.string.cancel, msgTextId, listener);
	}
	
	public Dialog showYesNoDialog(int yesTextId,int noTextId,int msgTextId,DialogInterface.OnClickListener listener){
		Dialog d = createYesNoDialog(getDialogContext(),
						yesTextId == 0 ? null : getString(yesTextId), 
						noTextId == 0 ? null : getString(noTextId),
						msgTextId == 0 ? null : getString(msgTextId),
						0, null, listener);
		d.show();
		return d;
	}
	
	public Dialog showYesNoDialog(int yesTextId,int noTextId,String message,DialogInterface.OnClickListener listener){
		Dialog d = createYesNoDialog(getDialogContext(),
						yesTextId == 0 ? null : getString(yesTextId), 
						noTextId == 0 ? null : getString(noTextId),
						message,
						0, null, listener);
		d.show();
		return d;
	}
	
	public Dialog showYesNoDialog(int yesTextId,int noTextId,int msgTextId,int titleTextId,DialogInterface.OnClickListener listener){
		Dialog d = createYesNoDialog(getDialogContext(),
						yesTextId == 0 ? null : getString(yesTextId), 
						noTextId == 0 ? null : getString(noTextId),
						msgTextId == 0 ? null : getString(msgTextId),
						0, 
						titleTextId == 0 ? null : getString(titleTextId), 
								listener);
		d.show();
		return d;
	}
	
	public Dialog showYesNoDialog(String yesText, String noText, String message,
			int titleIcon, String title,DialogInterface.OnClickListener listener){
		Dialog d = createYesNoDialog(getDialogContext(),
				yesText, 
				noText,
				message,
				titleIcon, 
				title, 
				listener);
		d.show();
		return d;
	}
	
	public Context getDialogContext(){
		return mContext;
	}
	
	public Dialog createYesNoDialog(Context context,String yesText, String noText, String message,
			int titleIcon, String title,DialogInterface.OnClickListener listener) {
		AlertDialog.Builder b = new AlertDialog.Builder(context);
		b.setMessage(message)
		.setPositiveButton(yesText, listener);
		if(noText != null){
			b.setNegativeButton(noText, listener);
		}
		if(titleIcon != 0){
			b.setIcon(titleIcon);
		}
		if(title != null){
			b.setTitle(title);
		}
		return b.create();
	}
	
	public String getString(int resid){
		return mContext.getString(resid);
	}
	
	public void showNetworkErrorTip(){
		ToastManager.getInstance(mContext).showNetworkErrorTip();
	}
	
	public void setContentStatusViewProvider(ContentStatusViewProvider provider){
		mContentStatusViewProvider = provider;
	}
	
	public ContentStatusViewProvider getContentStatusViewProvider(){
		initContentStatusViewProvider();
		return mContentStatusViewProvider;
	}
	
	public void setFailText(String text){
		initContentStatusViewProvider();
		mContentStatusViewProvider.setFailText(text);
	}
	
	public void showFailView(){
		initContentStatusViewProvider();
		mContentStatusViewProvider.showFailView();
	}
	
	public void hideFailView(){
		initContentStatusViewProvider();
		mContentStatusViewProvider.hideFailView();
	}
	
	public boolean isFailViewVisible(){
		initContentStatusViewProvider();
		return mContentStatusViewProvider.isFailViewVisible();
	}

	public void setNoResultTextId(int textId){
		setNoResultText(mContext.getString(textId));
	}
	
	public void setNoResultText(String text){
		initContentStatusViewProvider();
		mContentStatusViewProvider.setNoResultText(text);
	}
	
	public boolean hasSetNoResultText(){
		initContentStatusViewProvider();
		return mContentStatusViewProvider.hasSetNoResultText();
	}
	
	public void showNoResultView(){
		initContentStatusViewProvider();
		mContentStatusViewProvider.showNoResultView();
	}
	
	public void hideNoResultView(){
		initContentStatusViewProvider();
		mContentStatusViewProvider.hideNoResultView();
	}
	
	public boolean isNoResultViewVisible(){
		initContentStatusViewProvider();
		return mContentStatusViewProvider.isNoResultViewVisible();
	}

	protected void initContentStatusViewProvider(){
		if(mContentStatusViewProvider == null){
			mContentStatusViewProvider = new SimpleActivityContentStatusViewProvider((Activity)mContext);
		}
	}
}
