package com.xbcx.im.ui;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.viewpager.widget.ViewPager;

import com.xbcx.adapter.CommonPagerAdapter;
import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.core.XApplication;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;
import com.xbcx.view.PageIndicator;

public abstract class BaseEditViewExpressionProvider extends EditViewExpressionProvider  implements
											ViewPager.OnPageChangeListener,
											AdapterView.OnItemClickListener{

	protected ViewPager			mViewPagerExpression;
	protected PageIndicator 	mPageIndicatorExpression;
	
	protected XChatEditView		mEditView;
	
	@Override
	public void onAttachToEditView(XChatEditView ev) {
		super.onAttachToEditView(ev);
		mEditView = ev;
	}

	@Override
	public View createTabButton(Context context) {
		return null;
	}

	@Override
	public boolean isTabSeletable() {
		return true;
	}

	@Override
	public View createTabContent(Context context) {
		View v = LayoutInflater.from(context).inflate(R.layout.xlibrary_editview_qqexpression, null);
		mViewPagerExpression = (ViewPager)v.findViewById(R.id.vpExpression);
		mPageIndicatorExpression = (PageIndicator)v.findViewById(R.id.pageIndicator);
		mViewPagerExpression.setOnPageChangeListener(this);
		CommonPagerAdapter pagerAdapter = onCreatePagerAdapter(context);
		final int nResIds[] = getExpressionResIds();
		int nPageCount = nResIds.length / getOnePageMaxCount();
		if(nResIds.length % getOnePageMaxCount() > 0){
			++nPageCount;
		}
		pagerAdapter.setPageCount(nPageCount);
		mPageIndicatorExpression.setSelectColor(0xff6f8536);
		mPageIndicatorExpression.setNormalColor(0xffafafaf);
		mPageIndicatorExpression.setPageCount(nPageCount);
		mPageIndicatorExpression.setPageCurrent(0);
		
		mViewPagerExpression.setAdapter(pagerAdapter);
		
		return v;
	}
	
	protected abstract TextMessageImageCoder	getImageCoder();
	
	protected int[] 				getExpressionResIds(){
		return getImageCoder().getResIds();
	}

	protected CommonPagerAdapter 	onCreatePagerAdapter(Context context){
		return new ExpressionPagerAdapter(context);
	}
	
	protected int		getOnePageMaxCount(){
		if(getImageCoder().isSingleDrawable()){
			return 8;
		}else{
			return 23;
		}
	}
	
	protected int		getColumnNum(){
		if(getImageCoder().isSingleDrawable()){
			return 4;
		}else{
			return 8;
		}
	}
	
	protected int		getImageWidth(){
		if(getImageCoder().isSingleDrawable()){
			return SystemUtils.dipToPixel(XApplication.getApplication(), 80);
		}else{
			return SystemUtils.dipToPixel(XApplication.getApplication(), 32);
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		mPageIndicatorExpression.setPageCurrent(position);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Integer resid = (Integer)parent.getItemAtPosition(position);
		if(resid == null){
			return;
		}
		if(resid.intValue() == 0){
			int nIndex = mEditText.getSelectionStart();
			if(nIndex > 0){
				final Editable editable = mEditText.getEditableText();
				ImageSpan[] spans = editable.getSpans(0, mEditText.length(), ImageSpan.class);
				final int length = spans.length;
				boolean bDelete = false;
				for (int i = 0; i < length; i++) {
					final int s = editable.getSpanStart(spans[i]);
					final int e = editable.getSpanEnd(spans[i]);
					if (e == nIndex) {
						editable.delete(s, e);
						bDelete = true;
						break;
					}
				}
				if(!bDelete){
					editable.delete(nIndex - 1, nIndex);
				}
			}
		}else{
			try{
				if(getImageCoder().isSingleDrawable()){
					mOnEditListener = mEditView.getOnEditListener();
					if(mOnEditListener != null){
						if(mOnEditListener.onSendCheck()){
							mOnEditListener.onSendText(getImageCoder().getCode(resid));
						}
					}
				}else{
					SpannableStringBuilder ssb = new SpannableStringBuilder(
							getImageCoder().getCode(resid));
					Drawable d = parent.getContext().getResources().getDrawable(resid);
					d.setBounds(0, 0, (int)(d.getIntrinsicWidth() * 0.6), 
							(int)(d.getIntrinsicHeight() * 0.6));
					ssb.setSpan(new ImageSpan(d),
							0, ssb.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
					mEditText.append(ssb);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	protected GridView	onCreateGridView(Context context){
		final GridView gridView = new GridView(context);
		final int columnNum = getColumnNum();
		final int imageWidth = getImageWidth();
		final int verticalSpace = SystemUtils.dipToPixel(context, 10);
		gridView.setColumnWidth(imageWidth);
		gridView.setNumColumns(columnNum);
		gridView.setVerticalSpacing(verticalSpace);
		gridView.setCacheColorHint(0x00000000);
		gridView.setSelector(new ColorDrawable(0x00000000));
		gridView.setStretchMode(GridView.STRETCH_SPACING);
		gridView.setPadding(verticalSpace, verticalSpace, verticalSpace, 0);
		
		return gridView;
	}
	
	protected class ExpressionPagerAdapter extends CommonPagerAdapter{
		
		public Context	mContext;
		
		public ExpressionPagerAdapter(Context context){
			mContext = context;
		}
		
		@Override
		protected View getView(View v, int nPos,ViewGroup parent) {
			ExpressionImageAdapter adapter;
			if(v == null){
				final Context context = mContext;
				final GridView gridView = onCreateGridView(context);
				adapter = new ExpressionImageAdapter(context);
				gridView.setAdapter(adapter);
				gridView.setOnItemClickListener(BaseEditViewExpressionProvider.this);
				gridView.setTag(adapter);
				
				v = gridView;
			}else{
				adapter = (ExpressionImageAdapter)v.getTag();
			}
			
			final int nResIds[] = getExpressionResIds();
			final int nStart = nPos * getOnePageMaxCount();
			int nEnd = nStart + getOnePageMaxCount();
			if(nEnd > nResIds.length)nEnd = nResIds.length;
			
			adapter.clear();
			for(int nIndex = nStart;nIndex < nEnd;++nIndex){
				adapter.addItem(Integer.valueOf(nResIds[nIndex]));
			}
			if(!getImageCoder().isSingleDrawable()){
				adapter.addItem(0);
			}
			
			return v;
		}
	}
	
	protected static class ExpressionImageAdapter extends SetBaseAdapter<Integer>{

		protected Context 	mContext;
		
		protected int		mSize;
		
		public ExpressionImageAdapter(Context context){
			mContext = context;
			mSize = SystemUtils.dipToPixel(context, 32);
		}
		
		public void setImageSize(int size){
			mSize = size;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = new ImageView(mContext);
				convertView.setLayoutParams(new GridView.LayoutParams(mSize,mSize));
			}
			
			Integer id = (Integer)getItem(position);
			final ImageView imageView = (ImageView)convertView;
			if(id.intValue() == 0){
				imageView.setImageResource(R.drawable.emotion_del);
			}else{
				imageView.setImageResource(id.intValue());
			}
			
			return imageView;
		}
	}
}
