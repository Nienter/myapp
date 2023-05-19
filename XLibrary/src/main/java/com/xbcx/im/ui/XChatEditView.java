package com.xbcx.im.ui;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.xbcx.adapter.CommonPagerAdapter;
import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.core.ToastManager;
import com.xbcx.im.IMKernel;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;
import com.xbcx.view.PageIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class XChatEditView extends BaseEditView implements View.OnClickListener,
												View.OnFocusChangeListener,
												RecordViewHelper.OnRecordListener,
												OnItemClickListener,
		ViewPager.OnPageChangeListener {

	protected static final int EXPRESSION_ONEPAGE_COUNT = 23;
	
	protected boolean			mIsHideExpressionBtnWhenSwitchVoice;
	
	protected View 				mBtnPressTalk;
	protected View				mViewInput;
	
	protected View				mBtnExpression;
	protected View				mViewExpressionSet;
	protected ViewGroup			mViewExpressionTab;
	protected ViewGroup			mViewExpressionContent;
	protected ImageView			mBtnSwitch;
	
	protected boolean			mIsMoreInited;
	protected View				mViewMoreSet;
	protected ViewPager			mViewPagerMore;
	protected PageIndicator		mPageIndicatorMore;
	protected MorePagerAdapter	mMorePagerAdapter;
	protected List<SendPlugin> 	mSendPlugins = new ArrayList<SendPlugin>();
	
	protected boolean			mIsInitRecordView;
	protected RecordViewHelper 	mRecordViewHelper;
	
	protected OnEditListener 	mOnEditListner;
	
	protected HashMap<View, ExpressionTab> 	mMapTabToExpressionTab = new HashMap<View,ExpressionTab>();
	protected ExpressionTab					mLastExpressionTab;
	
	protected OnMoreViewShowListener mOnMoreViewShowListener;
	
	protected View				mBtnSend;
	protected View				mBtnMore;
	
	public XChatEditView(Context context) {
		super(context);
		init();
	}
	
	public XChatEditView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init(){
		View v = onCreateEditView();
		
		mBtnPressTalk = v.findViewById(R.id.btnPressTalk);
		mEditText = (EditText)v.findViewById(R.id.etTalk);
		mViewInput = v.findViewById(R.id.viewInput);
		int expressionSetId = getResources().getIdentifier(
				"viewExpressionSet", "id", getContext().getPackageName());
		if(expressionSetId != 0){
			mViewExpressionSet = v.findViewById(expressionSetId);
			if(mViewExpressionSet != null){
				mBtnExpression = v.findViewById(R.id.btnExpression);
				mBtnExpression.setOnClickListener(this);
			}
		}
		mViewMoreSet = v.findViewById(R.id.viewMoreSet);
		mViewPagerMore = (ViewPager)v.findViewById(R.id.vpMore);
		mPageIndicatorMore = (PageIndicator)v.findViewById(R.id.piMore);
		mViewPagerMore.setOnPageChangeListener(this);
		
		mBtnSwitch = (ImageView)v.findViewById(R.id.btnSwitch);
		mBtnSwitch.setOnClickListener(this);
		mBtnMore = v.findViewById(R.id.btnAdd);
		mBtnMore.setOnClickListener(this);
		mBtnSend = v.findViewById(R.id.btnSend);
		mBtnSend.setOnClickListener(this);
		mBtnPressTalk.setOnClickListener(this);
		
		mEditText.setOnFocusChangeListener(this);
		mEditText.setOnClickListener(this);
		mEditText.setFocusableInTouchMode(false);
		SystemUtils.addEditTextLengthFilter(mEditText, 500);
		
		mInputMethodManager = (InputMethodManager)getContext().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		
		addView(v);
		
		initExpressionView();
		
		addPullUpView(mViewMoreSet);
		hidePullUpView(mViewMoreSet, false);
		
		switchToTextInput(false);
	}
	
	protected View onCreateEditView(){
		return LayoutInflater.from(getContext()).inflate(R.layout.xlibrary_chatedit, null);
	}
	
	private void initRecordPrompt(){
		if(!mIsInitRecordView){
			mRecordViewHelper = onCreateRecordViewHelper();
			mRecordViewHelper.onCreate(mBtnPressTalk);
			mRecordViewHelper.setOnRecordListener(this);
			mIsInitRecordView = true;
		}
	}
	
	private void initExpressionView(){
		if(mViewExpressionSet != null){
			addPullUpView(mViewExpressionSet);
			
			int viewId = getResources().getIdentifier(
					"viewExpressionTab", "id", getContext().getPackageName());
			if(viewId != 0){
				mViewExpressionTab = (ViewGroup)mViewExpressionSet.findViewById(viewId);
			}
			viewId = getResources().getIdentifier("viewExpressionContent", "id", getContext().getPackageName());
			if(viewId != 0){
				mViewExpressionContent = (ViewGroup)mViewExpressionSet.findViewById(viewId);
			}
			
			List<EditViewExpressionProvider> providers = onCreateEditViewExpressionProvider();
			if(mViewExpressionTab == null){
				if(providers.size() > 0){
					EditViewExpressionProvider provider = providers.get(0);
					final View content = provider.createTabContent(getContext());
					if(content != null){
						if(mViewExpressionContent != null){
							mViewExpressionContent.addView(content, 
									new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
											ViewGroup.LayoutParams.MATCH_PARENT));
							provider.onAttachToEditView(this);
						}
					}
				}
			}else{
				boolean bAddTab = true;
				if(IMGlobalSetting.editViewExpProviders.size() == 1){
					mViewExpressionTab.setVisibility(View.GONE);
					bAddTab = false;
				}

				for(EditViewExpressionProvider provider : providers){
					ExpressionTab tab = new ExpressionTab();
					tab.mIsTabSelectable = provider.isTabSeletable();
					tab.mTabButton = provider.createTabButton(getContext());
					tab.mProvider = provider;
					if(tab.mTabButton != null && bAddTab){
						tab.mTabButton.setOnClickListener(this);
						mViewExpressionTab.addView(tab.mTabButton);
						mMapTabToExpressionTab.put(tab.mTabButton, tab);
					}else{
						if(!bAddTab){
							if(mViewExpressionContent != null){
								tab.mTabContent = provider.createTabContent(getContext());
								mViewExpressionContent.addView(tab.mTabContent, 
										new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
												ViewGroup.LayoutParams.MATCH_PARENT));
							}
						}
					}
					provider.onAttachToEditView(this);
				}
				if(bAddTab){
					setExpressionCurrentTab(0);
				}
			}
			
			hidePullUpView(mViewExpressionSet, false);
		}
	}
	
	public void setIsHideExpressionBtnWhenSwitchVoice(boolean bHide){
		mIsHideExpressionBtnWhenSwitchVoice = bHide;
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		initRecordPrompt();
	}
	
	protected RecordViewHelper onCreateRecordViewHelper(){
		return new RecordViewHelper();
	}
	
	protected List<EditViewExpressionProvider>	onCreateEditViewExpressionProvider(){
		final List<Class<? extends EditViewExpressionProvider>> classes = IMGlobalSetting.editViewExpProviders;
		List<EditViewExpressionProvider> providers = new ArrayList<EditViewExpressionProvider>();
		for(Class<? extends EditViewExpressionProvider> c : classes){
			try{
				providers.add(c.newInstance());
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return providers;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mIsInitRecordView = false;
		mRecordViewHelper.onDestroy();
	}
	
	public void registerSendBtnShowHandler(SendBtnShowHandler handler){
		if(handler != null){
			mEditText.addTextChangedListener(handler);
			handler.showSendBtn(!TextUtils.isEmpty(SystemUtils.getTrimText(mEditText)));
		}
	}
	
	public void	setExpressionCurrentTab(int index){
		if(mViewExpressionTab != null){
			View v = mViewExpressionTab.getChildAt(index);
			setExpressionCurrentTab(v);
		}
	}
	
	public void setExpressionCurrentTab(View v){
		if(v != null){
			setExpressionCurrentTabInternal(mMapTabToExpressionTab.get(v));
		}
	}
	
	public void setExpressionCurrentTabInternal(ExpressionTab tab){
		if(tab != null){
			if(mLastExpressionTab != null){
				mLastExpressionTab.mTabButton.setSelected(false);
				mLastExpressionTab.mTabContent.setVisibility(View.GONE);
			}
			tab.mTabButton.setSelected(true);
			if(tab.mTabContent == null){
				if(tab.mProvider != null){
					tab.mTabContent = tab.mProvider.createTabContent(getContext());
				}else{
					return;
				}
			}
			if(tab.mTabContent.getParent() == null){
				mViewExpressionContent.addView(tab.mTabContent, 
						new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
								ViewGroup.LayoutParams.MATCH_PARENT));
			}
			tab.mTabContent.setVisibility(View.VISIBLE);
			mLastExpressionTab = tab;
		}
	}
	
	public EditText			getEditText(){
		return mEditText;
	}
	
	public OnEditListener	getOnEditListener(){
		return mOnEditListner;
	}
	
	public void addAllSendPlugin(List<SendPlugin> plugs){
		mSendPlugins.addAll(plugs);
		mIsMoreInited = false;
	}
	
	public void addSendPlugin(SendPlugin plug){
		if(plug != null){
			mSendPlugins.add(plug);
			mIsMoreInited = false;
		}
	}

	@Override
	public void onClick(View v) {
		final int nId = v.getId();
		if(v == mBtnExpression){
			if(isPullUpViewVisible(mViewExpressionSet)){
				hidePullUpView(mViewExpressionSet, true);
			}else{
				showExpressionView();
			}
		}else if(nId == R.id.btnSwitch){
			onClickSwitchBtn(v);
		}else if(nId == R.id.etTalk){
			showInputMethod();
		}else if(nId == R.id.btnSend){
			String strMessage = mEditText.getText().toString();
			sendText(strMessage);
		}else if(nId == R.id.btnAdd){
			if(isPullUpViewVisible(mViewMoreSet)){
				hidePullUpView(mViewMoreSet, true);
				if (mOnMoreViewShowListener != null) mOnMoreViewShowListener.moreViewShow(false);
			}else{
				showPullUpview(mViewMoreSet);
				if (mOnMoreViewShowListener != null) mOnMoreViewShowListener.moreViewShow(true);
			}
		}else{
			setExpressionCurrentTabInternal(mMapTabToExpressionTab.get(v));
		}
	}

	@Override
	public void hideAllPullUpView(boolean bAnim) {
		super.hideAllPullUpView(bAnim);
		if (mOnMoreViewShowListener != null) mOnMoreViewShowListener.moreViewShow(false);
	}

	public void showExpressionView(){
		int selectionStart = -1;
		int selectionEnd = -1;
		if(mInputMethodVisible){
			selectionStart = mEditText.getSelectionStart();
			selectionEnd = mEditText.getSelectionEnd();
		}
		showPullUpview(mViewExpressionSet);
		mBtnSwitch.setImageResource(R.drawable.msg_bar_voice);
		mViewInput.setVisibility(View.VISIBLE);
		mBtnPressTalk.setVisibility(View.GONE);
		mEditText.setFocusableInTouchMode(true);
		mEditText.requestFocus();
		if(selectionStart != -1){
			mEditText.setSelection(selectionStart, selectionEnd);
		}
	}
	
	@Override
	public void showPullUpview(View view) {
		super.showPullUpview(view);
		if(view == mViewMoreSet){
			if(!mIsMoreInited){
				mIsMoreInited = true;
				onInitMoreView();
			}
		}
	}
	
	protected void onInitMoreView(){
		mMorePagerAdapter = onCreateMorePagerAdapter(mSendPlugins);
		mViewPagerMore.setAdapter(mMorePagerAdapter);
		final int pageCount = mMorePagerAdapter.getCount();
		if(pageCount > 1){
			mPageIndicatorMore.setVisibility(View.VISIBLE);
			mPageIndicatorMore.setPageCount(pageCount);
		}else{
			mPageIndicatorMore.setVisibility(View.GONE);
		}
	}
	
	protected MorePagerAdapter onCreateMorePagerAdapter(List<SendPlugin> plugins){
		return new MorePagerAdapter(plugins, this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final Object item = parent.getItemAtPosition(position);
		if(item != null && item instanceof SendPlugin){
			final SendPlugin sp = (SendPlugin)item;
			onSendPluginClicked(sp);
		}
	}
	
	protected void onSendPluginClicked(SendPlugin sp){
		if(mOnEditListner != null){
			mOnEditListner.onSendPlugin(sp);
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		mPageIndicatorMore.setPageCurrent(position);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}
	
	public void sendText(String text){
		text = text.trim();
		if (!TextUtils.isEmpty(text)) {
			if(SystemUtils.hasEmoji(text)){
				ToastManager.getInstance(getContext()).show(R.string.toast_cannot_send_emoji);
			}else{
				if(mOnEditListner != null){
					if(mOnEditListner.onSendCheck()){
						mOnEditListner.onSendText(text);
					
						mEditText.getEditableText().clear();
					}
				}
			}
		}
	}
	
	protected void onClickSwitchBtn(View v){
		if(mViewInput.getVisibility() == View.VISIBLE){
			switchToVoice();
		}else{
			switchToTextInput();
		}
	}
	
	public void switchToTextInput(){
		switchToTextInput(true);
	}
	
	public void switchToTextInput(boolean bShowInputMethod){
		mBtnSwitch.setImageResource(R.drawable.msg_bar_voice);
		mViewInput.setVisibility(View.VISIBLE);
		if(bShowInputMethod){
			showInputMethod();
		}
		mBtnPressTalk.setVisibility(View.GONE);
		if(mIsHideExpressionBtnWhenSwitchVoice){
			if(mBtnExpression != null){
				mBtnExpression.setVisibility(View.VISIBLE);
			}
		}
	}
	
	public void switchToVoice(){
		mBtnSwitch.setImageResource(R.drawable.msg_bar_text);
		mViewInput.setVisibility(View.GONE);
		mBtnPressTalk.setVisibility(View.VISIBLE);
		hidePullUpView(mViewExpressionSet, true);
		hidePullUpView(mViewMoreSet, true);
		hideInputMethod();
		if(mIsHideExpressionBtnWhenSwitchVoice){
			if(mBtnExpression != null){
				mBtnExpression.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(hasFocus){
//			hidePullUpView(mViewExpressionSet, false);
//			hidePullUpView(mViewMoreSet, false);
		}else{
			hideInputMethod();
		}
	}
	
	public void setOnMoreViewShowListener(OnMoreViewShowListener listener){
		mOnMoreViewShowListener = listener;
	}
	
	public void setOnEditListener(OnEditListener listener){
		mOnEditListner = listener;
	}
	
	public void onPause(){
		mRecordViewHelper.onPause();
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		if(visibility == View.VISIBLE){
			mRecordViewHelper.onResume();
		}else{
			mRecordViewHelper.onPause();
		}
	}
	

	@Override
	public boolean onRecordCheck() {
		if(!IMKernel.isIMConnectionAvailable()){
			if(mOnEditListner != null){
				mOnEditListner.onRecordFail(true);
			}
			return false;
		}
		if(mOnEditListner != null){
			return mOnEditListner.onSendCheck();
		}
		return false;
	}
	
	public void onRecordStarted() {
	}

	public void onRecordEnded(String strRecordPath) {
		if(mOnEditListner != null){
			mOnEditListner.onSendVoice(strRecordPath);
		}
	}

	public void onRecordFailed() {
		if(mOnEditListner != null){
			mOnEditListner.onRecordFail(false);
		}
	}
	
	protected static class	ExpressionTab{
		public View 	mTabButton;
		public boolean	mIsTabSelectable;
		public View		mTabContent;
		
		public EditViewExpressionProvider mProvider;
	}
	
	protected static class SendPluginAdapter extends SetBaseAdapter<SendPlugin>{
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = SystemUtils.inflate(parent.getContext(), R.layout.xlibrary_adapter_sendplugin);
			}
			
			final SendPlugin sp = (SendPlugin)getItem(position);
			final ImageView iv = (ImageView)convertView.findViewById(R.id.ivIcon);
			final TextView tv = (TextView)convertView.findViewById(R.id.tvName);
			iv.setImageResource(sp.getIcon());
			tv.setText(sp.getName());
			
			return convertView;
		}
	}
	
	protected static class MorePagerAdapter extends CommonPagerAdapter{
		
		private OnItemClickListener mListener;
		private List<SendPlugin> 	mSendPlugins = new ArrayList<SendPlugin>();
		private int					mOnePageItemCount = 8;
		
		public MorePagerAdapter(List<SendPlugin> plugins,OnItemClickListener listener){
			mListener = listener;
			mSendPlugins.addAll(plugins);
			setPageCount(plugins.size() / getOnePageItemCount() + 
					(plugins.size() % getOnePageItemCount() > 0 ? 1 : 0));
		}
		
		public int	getOnePageItemCount(){
			return mOnePageItemCount;
		}
		
		@Override
		protected View getView(View v, int nPos, ViewGroup parent) {
			SendPluginAdapter adapter;
			if(v == null){
				final Context context = parent.getContext();
				final GridView gridView = onCreateGridView(context);
				adapter = new SendPluginAdapter();
				gridView.setAdapter(adapter);
				gridView.setOnItemClickListener(mListener);
				gridView.setTag(adapter);
				
				v = gridView;
			}else{
				adapter = (SendPluginAdapter)v.getTag();
			}
			
			final int nStart = nPos * getOnePageItemCount();
			int nEnd = nStart + getOnePageItemCount();
			if(nEnd > mSendPlugins.size())nEnd = mSendPlugins.size();
			
			adapter.clear();
			for(int index = nStart;index < nEnd;++index){
				adapter.addItem(mSendPlugins.get(index));
			}
			
			return v;
		}
		
		public GridView onCreateGridView(Context context){
			final GridView gridView = new GridView(context);
			final int columnNum = 4;
			final int verticalSpace = SystemUtils.dipToPixel(context, 8);
			gridView.setColumnWidth(SystemUtils.dipToPixel(context, 63));
			gridView.setNumColumns(columnNum);
			gridView.setVerticalSpacing(verticalSpace);
			gridView.setCacheColorHint(0x00000000);
			gridView.setSelector(new ColorDrawable(0x00000000));
			gridView.setStretchMode(GridView.STRETCH_SPACING);
			final int padding = SystemUtils.dipToPixel(context, 12);
			gridView.setPadding(padding, verticalSpace, padding, 0);
			return gridView;
		}
	}
	
	public static interface OnEditListener{
		public boolean 	onSendCheck();
		
		public void		onRecordFail(boolean bFailByNet);
		
		public void 	onSendText(CharSequence s);
		
		public void	 	onSendVoice(String strPathName);
		
		public void		onSendPlugin(SendPlugin sp);
	}
	
	public interface OnMoreViewShowListener{
		void moreViewShow(boolean isShow);
	}
	
	public static abstract class SendBtnShowHandler implements TextWatcher{
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			showSendBtn(!TextUtils.isEmpty(s));
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
		
		public abstract void showSendBtn(boolean bShow);
	}
	
	public static class SimpleSendBtnShowHandler extends SendBtnShowHandler{
		
		private View mBtnSend;
		private View mBtnAdd;
		
		public SimpleSendBtnShowHandler(View btnSend,View btnAdd) {
			mBtnSend = btnSend;
			mBtnAdd = btnAdd;
		}
		
		@Override
		public void showSendBtn(boolean bShow) {
			if(bShow){
				mBtnSend.setVisibility(View.VISIBLE);
				mBtnAdd.setVisibility(View.GONE);
			}else{
				mBtnSend.setVisibility(View.GONE);
				mBtnAdd.setVisibility(View.VISIBLE);
			}
		}
	}
}
