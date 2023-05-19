package com.xbcx.im.ui.simpleimpl;

import java.util.List;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Scroller;

import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.im.XMessage;
import com.xbcx.im.ui.ChatActivity;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;
import com.xbcx.view.PulldownableListView;

public class ServerChatRecordActivity extends ChatActivity{
	
	protected String 				mId;
	protected int					mFromType;
	
	protected long					mFirstTime;
	protected long					mLastTime;
	
	private View		mFooterView;
	
	private boolean 	mFooterIsRunning;
	
	private int 		mFooterViewPaddingTop;
	private int 		mFooterViewPaddingTopMax;
	private int 		mFooterViewPaddingTopMin;
	
	private boolean 	mFooterIsTouching;
	private boolean 	mFooterCanHandleFooter;
	private boolean 	mFooterIsStartFooter;
	private float		mFooterTouchLastY;
	private float   	mFooterTouchDisYTotal;
	private float		mFooterDisYStartFooter;
	
	private Scroller 	mScrollerFooter;
	
	protected Runnable mRunnableFooter = new Runnable() {
		@Override
		public void run() {
			if(mScrollerFooter.computeScrollOffset()){
				setFooterViewPaddingTop(mScrollerFooter.getCurrX());
				mListView.post(this);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initFooter();
		
		onGetId();
		
		mListView.startRun();
	}
	
	protected void onGetId(){
		mId = getIntent().getStringExtra("id");
		mFromType = getIntent().getIntExtra("fromtype", XMessage.FROMTYPE_SINGLE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
		ba.mActivityLayoutId = R.layout.xlibrary_activity_serverchatrecord;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		return true;
	}

	@Override
	public boolean onViewLongClicked(XMessage message, int nViewId) {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == EventCode.HTTP_GetChatRecord){
			final int type = (Integer)event.getParamAtIndex(2);
			if(type == 0){
				mListView.endRun();
			}else{
				endFooterRun();
			}
			if(event.isSuccess()){
				final List<XMessage> msgs = (List<XMessage>)event.getReturnParamAtIndex(0);
				if(msgs.size() > 0){
					List<XMessage> xms = addGroupTimeMessage(msgs);
					
					if(type == 0){
						mMessageAdapter.addAllItem(0, xms);
						mFirstTime = xms.get(0).getSendTime();
						if(mLastTime == 0){
							mLastTime = xms.get(xms.size() - 1).getSendTime();
						}
						mListView.setSelection(xms.size() - 1 + mListView.getHeaderViewsCount());
					}else{
						mMessageAdapter.addAllItem(xms);
						mLastTime = xms.get(xms.size() - 1).getSendTime();
						if(mFirstTime == 0){
							mFirstTime = xms.get(0).getSendTime();
						}
					}
				}else{
					if(type == 0){
						mListView.setCanRun(false);
					}
				}
			}
		}
	}

	@Override
	public void onStartRun(PulldownableListView view) {
		//super.onStartRun(view);
		pushEventEx(EventCode.HTTP_GetChatRecord, false,false,null,
				mId,
				mFromType,
				0,
				mFirstTime);
	}

	public void onFooterRun() {
		pushEventEx(EventCode.HTTP_GetChatRecord, false, false, null, 
				mId,
				mFromType,
				1,
				mLastTime);
		
	}
	
	private void initFooter(){
		mFooterView = findViewById(R.id.viewFooter);
		
		mScrollerFooter = new Scroller(this);
		
		PulldownableListView.measurePullDownView(mFooterView);
		
		mFooterViewPaddingTop = mFooterView.getMeasuredHeight();
		
		final int viewHeight = mFooterView.getMeasuredHeight();
		final int viewHeightMax = viewHeight + SystemUtils.dipToPixel(this, 30);
		mFooterViewPaddingTopMax = mFooterViewPaddingTop + viewHeightMax - viewHeight;
		mFooterViewPaddingTopMin = mFooterViewPaddingTop - viewHeight;
		
		setFooterViewPaddingTop(mFooterViewPaddingTopMin);
		
		mFooterDisYStartFooter = 0;
	}
	
	protected void setFooterViewPaddingTop(int nPadding){
		if(nPadding > mFooterViewPaddingTopMax){
			nPadding = mFooterViewPaddingTopMax;
		}else if(nPadding < mFooterViewPaddingTopMin){
			nPadding = mFooterViewPaddingTopMin;
		}
		int nPaddingTopCur = getPadding();
		if(nPaddingTopCur != nPadding){
			ViewGroup.LayoutParams lp = mFooterView.getLayoutParams();
			lp.height = nPadding;
			mFooterView.setLayoutParams(lp);
//			mFooterView.setPadding(mFooterView.getPaddingLeft(),
//					mFooterView.getPaddingTop(), 
//					mFooterView.getPaddingRight(), 
//					nPadding);
			
			if(!mFooterIsTouching){
				if(getPadding() == mFooterViewPaddingTop){
					mFooterIsRunning = true;
					onFooterRun();
				}
			}
		}
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		final float fTouchCurY = ev.getY();
		if(action == MotionEvent.ACTION_MOVE){
			mFooterIsTouching = true;
			if(mFooterCanHandleFooter && !mFooterIsStartFooter){
				float fDisY = (fTouchCurY - mFooterTouchLastY) / 2.0f;
				mFooterTouchLastY = fTouchCurY;
				if(fDisY < 0){
					mFooterTouchDisYTotal += Math.abs(fDisY);
					if(mFooterTouchDisYTotal > mFooterDisYStartFooter){
						ev.setAction(MotionEvent.ACTION_CANCEL);
						super.onTouchEvent(ev);
						mFooterIsStartFooter = true;
						return true;
					}else{
						return true;
					}
				}else if(fDisY > 0){
					mFooterTouchDisYTotal = 0;
					mFooterCanHandleFooter = false;
				}
			}
			if(mFooterIsStartFooter){
				float fDisY = (fTouchCurY - mFooterTouchLastY) / 2.0f;
				mFooterTouchLastY = fTouchCurY;
				
				int nPaddingTopLast = getPadding();
				int nPaddingTop = (int)(nPaddingTopLast - fDisY);
				if(fDisY > 0){
					if(nPaddingTopLast > mFooterViewPaddingTopMin){
						setFooterViewPaddingTop(nPaddingTop);
					}
				}else if(fDisY < 0){
					if(nPaddingTopLast < mFooterViewPaddingTopMax){
						setFooterViewPaddingTop(nPaddingTop);
					}
				}
				
				return true;
			}
		}else if(!mFooterIsRunning && action == MotionEvent.ACTION_DOWN){
			if(mListView.getLastVisiblePosition() == mListView.getCount() - 1 && 
					!mFooterCanHandleFooter){
				mFooterCanHandleFooter = true;
				mFooterTouchLastY = ev.getY();
			}
		}else if(action == MotionEvent.ACTION_CANCEL ||
				action == MotionEvent.ACTION_UP ||
				action == MotionEvent.ACTION_OUTSIDE){
			mFooterIsTouching = false;
			
			mFooterCanHandleFooter = false;
			mFooterTouchDisYTotal = 0;
			if(mFooterIsStartFooter){
				mFooterIsStartFooter = false;
				if(ev.getAction() == MotionEvent.ACTION_UP){
					if(getPadding() <= mFooterViewPaddingTop){
						hideFooterView();
					}else{
						startFooterRun();
					}
				}else{
					setFooterViewPaddingTop(mFooterViewPaddingTopMin);
				}
				
				return true;
			}
		}
		return super.dispatchTouchEvent(ev);
	}
	
	protected void startFooterRun(){
		if(!mFooterIsRunning){
			startChangeFooterViewPaddingTop(mFooterViewPaddingTop);
		}
	}
	
	public void endFooterRun(){
		if(mFooterIsRunning){
			mFooterIsRunning = false;
			hideFooterView();
		}
	}
	
	private void hideFooterView(){
		startChangeFooterViewPaddingTop(mFooterViewPaddingTopMin);
	}
	
	protected void startChangeFooterViewPaddingTop(int nPaddingTopDst){
		int nPaddingTopCur = getPadding();
		mScrollerFooter.startScroll(nPaddingTopCur, 0, nPaddingTopDst - nPaddingTopCur, 0);
		mListView.post(mRunnableFooter);
	}
	
	protected int getPadding(){
		return mFooterView.getLayoutParams().height;
	}
}
