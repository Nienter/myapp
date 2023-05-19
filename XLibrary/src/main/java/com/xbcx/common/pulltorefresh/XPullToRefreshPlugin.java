package com.xbcx.common.pulltorefresh;

import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import androidx.core.view.ViewCompat;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshAdapterViewBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.xbcx.adapter.StickyHeader;
import com.xbcx.common.ListViewScrollPlugin;
import com.xbcx.common.ListViewScrollPluginListener;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.Event;
import com.xbcx.core.XApplication;
import com.xbcx.library.R;

import java.util.LinkedList;
import java.util.List;

public class XPullToRefreshPlugin<T extends BaseActivity> extends PullToRefreshPlugin<T> implements 
													OnRefreshListener2<ListView>,
													ListViewScrollPlugin{
	
	protected 	PullToRefreshAdapterViewBase<ListView>	mRefreshView;
	
	protected	ListView				mListView;
	
	protected	boolean					mAddFooter = true;
	
	private		boolean					mIsStickyHeader;
	private		StickyHeader			mStickyHeader;
	private 	FrameLayout				mStickyViewWrap;
	private 	View					mCurrentStickyView;
	private		int						mCurrentStickyViewType;
	private		StickyRecycle			mStickyRecycle;
	
	private		PositionScroller		mPositionScroller;
	
	public XPullToRefreshPlugin<T> setRefreshView(PullToRefreshAdapterViewBase<ListView> rv){
		mRefreshView = rv;
		return this;
	}
	
	@Override
	protected void onAttachActivity(T activity) {
		mListView = onCreateListView();
		super.onAttachActivity(activity);
		mListView.setOnItemClickListener(mOnItemClickListener);
		mListView.setOnScrollListener(new ListViewScrollPluginListener(activity));
		mListView.setSelector(new ColorDrawable(0x00000000));
		mListView.setCacheColorHint(0x00000000);
		mListView.setDivider(null);
		if(mAddFooter){
			checkOrCreateViewFooter();
			mListView.addFooterView(mViewFooter);
		}
		
		onSetAdapter();
		
		mRefreshView.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(mIsCreateRefresh && !isRefreshDisabled()){
					if(mRefreshView.getHeaderSize() != 0){
						mRefreshView.setRefreshing();
					}else{
						mRefreshView.post(this);
					}
				}
			}
		},mCreateRefreshDelayTime);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mListView.setAdapter(null);
	}
	
	@Override
	public PullToRefreshPlugin<T> setOnItemClickListener(OnItemClickListener listener) {
		if(mListView != null){
			mListView.setOnItemClickListener(listener);
		}
		return super.setOnItemClickListener(listener);
	}
	
	@Override
	public void setOnItemLongClickListener(OnItemLongClickListener listener) {
		super.setOnItemLongClickListener(listener);
		mListView.setOnItemLongClickListener(listener);
	}
	
	@Override
	public void smoothSetSelection(int pos) {
		if(mPositionScroller == null){
			mPositionScroller = new PositionScroller();
		}
		mPositionScroller.start(pos);
	}
	
	@Override
	public void resetAdapter() {
		onSetAdapter();
	}
	
	@Override
	public ListView getListView(){
		return mListView;
	}
	
	@Override
	public View getRefreshView() {
		return mRefreshView;
	}
	
	@SuppressWarnings("unchecked")
	protected ListView onCreateListView(){
		if(mRefreshView == null){
			mRefreshView = (PullToRefreshAdapterViewBase<ListView>)mActivity.findViewById(R.id.prlv);
		}
		mRefreshView.setOnRefreshListener(this);
		mRefreshView.setScrollingWhileRefreshingEnabled(true);
		return mRefreshView.getRefreshableView();
	}
	
	@Override
	protected void onScrollToFirstItem(){
		mListView.setSelection(0);
		mListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				startRefresh();
			}
		},200);
	}

	protected void onSetAdapter(){
		if(mEndlessAdapter != null){
			if(mAnimationAdapter == null){
				mListView.setAdapter(mEndlessAdapter);
			}else{
				mAnimationAdapter.setAbsListView(mListView);
				mListView.setAdapter(mAnimationAdapter);
			}
			if(!mIsStickyHeader){
				final ListAdapter adapter = mEndlessAdapter.getWrappedAdapter();
				if(adapter instanceof StickyHeader){
					mIsStickyHeader = true;
					mStickyRecycle = new StickyRecycle();
					mStickyHeader = (StickyHeader)adapter;
				}
			}
		}
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState != OnScrollListener.SCROLL_STATE_IDLE) {
			XApplication.pauseImageLoader();
		} else {
			XApplication.resumeImageLoader();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if(mIsStickyHeader){
			int viewType = mListView.getAdapter().getItemViewType(firstVisibleItem);
			if(isItemViewTypeSticky(viewType)){
				if(mCurrentStickyView != null){
					goneStickView(mCurrentStickyView);
				}
				final int headerCount = mListView.getHeaderViewsCount();
				if(firstVisibleItem > headerCount ||
						mListView.getChildAt(0).getTop() < 0){
					final View v = mStickyHeader.getStickyHeaderView(
							mStickyRecycle.obtainView(viewType),
							viewType,firstVisibleItem - headerCount,mListView);
					setCurrentStickyView(v,viewType);
					if(visibleItemCount > 1){
						offsetByNextStickView(firstVisibleItem + 1, firstVisibleItem);
					}
				}
			}else{
				boolean bFind = false;
				for(int index = firstVisibleItem;index >= 0;--index){
					viewType = mListView.getAdapter().getItemViewType(index);
					if(isItemViewTypeSticky(viewType)){
						if(mCurrentStickyView != null){
							mStickyRecycle.recycleView(mCurrentStickyViewType, mCurrentStickyView);
						}
						final View v = mStickyHeader.getStickyHeaderView(
								mStickyRecycle.obtainView(viewType),
								viewType,index - mListView.getHeaderViewsCount(),
								mListView);
						setCurrentStickyView(v,viewType);
						bFind = true;
						break;
					}
				}
				if(bFind){
					if(mCurrentStickyView != null){
						final int end = firstVisibleItem + visibleItemCount;
						for(int index = firstVisibleItem;index < end;++index){
							if(offsetByNextStickView(index, firstVisibleItem)){
								break;
							}
						}
					}
				}else{
					if(mCurrentStickyView != null){
						goneStickView(mCurrentStickyView);
						mCurrentStickyView = null;
					}
				}
			}
		}
	}
	
	protected boolean offsetByNextStickView(int index,int firstVisibleItem){
		int viewType = mListView.getAdapter().getItemViewType(index);
		if(isItemViewTypeSticky(viewType)){
			final View nextView = mListView.getChildAt(index - firstVisibleItem);
			if(nextView != null){
				final View nextHeader = mStickyHeader.getStickyHeaderView(
						mStickyRecycle.obtainView(viewType),
						viewType,index - mListView.getHeaderViewsCount(),
						mListView);
				goneStickView(nextHeader);
				final int height = mCurrentStickyView.getHeight();
				final int disY = nextView.getTop() - height;
				if(disY < 0){
					ViewCompat.setTranslationY(mCurrentStickyView, disY);
				}else{
					ViewCompat.setTranslationY(mCurrentStickyView, 0);
				}
			}
			return true;
		}
		return false;
	}
	
	protected boolean isItemViewTypeSticky(int viewType){
		if(viewType < 0){
			return false;
		}
		return mStickyHeader.isItemViewTypeSticky(viewType);
	}
	
	protected void	setCurrentStickyView(final View v,int viewType){
		if(mCurrentStickyView != null && mCurrentStickyView != v){
			goneStickView(mCurrentStickyView);
		}
		if(v.getParent() == null){
			final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.MATCH_PARENT, 
					FrameLayout.LayoutParams.WRAP_CONTENT);
			lp.gravity = Gravity.TOP;
			if(mStickyViewWrap == null){
				mStickyViewWrap = new FrameLayout(mActivity);
				FrameLayout.LayoutParams lpWrap = new FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.MATCH_PARENT, 
						FrameLayout.LayoutParams.WRAP_CONTENT);
				lpWrap.gravity = Gravity.TOP;
				mContentStatusViewProvider.addContentView(mStickyViewWrap, lpWrap);
				if(mStickyViewWrap.getParent() != getRefreshView().getParent()){
					MarginLayoutParams mlp = (MarginLayoutParams)mStickyViewWrap.getLayoutParams();
					Rect r = new Rect();
					getRefreshView().getGlobalVisibleRect(r);
					Rect rDector = new Rect();
					((ViewGroup)mStickyViewWrap.getParent()).getGlobalVisibleRect(rDector);
					mlp.topMargin = r.top - rDector.top;
					mStickyViewWrap.setLayoutParams(mlp);
				}else{
					LayoutParams rvlp = getRefreshView().getLayoutParams();
					if(rvlp != null && rvlp instanceof MarginLayoutParams){
						MarginLayoutParams mlp = (MarginLayoutParams)mStickyViewWrap.getLayoutParams();
						final int topMargin = ((MarginLayoutParams)rvlp).topMargin;
						if(mlp.topMargin != topMargin){
							mlp.topMargin = topMargin;
							mStickyViewWrap.setLayoutParams(mlp);
						}
					}
				}
			}
			mStickyViewWrap.addView(v, lp);
		}else{
			v.setVisibility(View.VISIBLE);
		}
		mCurrentStickyView = v;
		mCurrentStickyViewType = viewType;
	}
	
	private void	goneStickView(View v){
		v.setVisibility(View.GONE);
		if(v == mCurrentStickyView){
			mStickyRecycle.recycleView(mCurrentStickyViewType, v);
		}
	}
	
	@Override
	public void onStartRefreshImpl() {
		mRefreshView.setRefreshing();
	}
	
	@Override
	public boolean isRefreshDisabled() {
		return mRefreshView.getMode() == Mode.DISABLED;
	}
	
	@Override
	public boolean isRefreshing() {
		return mRefreshView.isRefreshing();
	}

	@Override
	public void completeRefresh() {
		mRefreshView.onRefreshComplete();
	}

	@Override
	public void disableRefresh() {
		mRefreshView.setMode(Mode.DISABLED);
	}
	
	@Override
	public void enableRefresh() {
		mRefreshView.setMode(Mode.PULL_FROM_START);
	}
	
	@Override
	public final void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		callPullDownToRefresh();
	}

	@Override
	public final void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		callPullUpToRefresh();
	}

	@Override
	protected void onPullUpLoadEventEnd(Event event) {
		mRefreshView.onRefreshComplete();
		super.onPullUpLoadEventEnd(event);
	}
	
	private static class StickyRecycle{
		SparseArray<List<View>> mMapViewTypeToConvertViews = new SparseArray<List<View>>();
		
		public View obtainView(int viewType){
			List<View> views = mMapViewTypeToConvertViews.get(viewType);
			if(views == null){
				return null;
			}else{
				if(views.size() > 0){
					View v = views.remove(0);
					ViewCompat.setTranslationY(v, 0);
					return v;
				}
				return null;
			}
		}
		
		public void recycleView(int viewType,View v){
			List<View> views = mMapViewTypeToConvertViews.get(viewType);
			if(views == null){
				views = new LinkedList<View>();
				mMapViewTypeToConvertViews.put(viewType, views);
			}
			views.add(v);
		}
	}
	
	class PositionScroller implements Runnable {
        private static final int SCROLL_DURATION = 200;

        private static final int MOVE_DOWN_POS = 1;
        private static final int MOVE_UP_POS = 2;

        private int mMode;
        private int mTargetPos;
        private int mLastSeenPos;
        private int mScrollDuration;
        private final int mExtraScroll;

        PositionScroller() {
            mExtraScroll = ViewConfiguration.get(mActivity).getScaledFadingEdgeLength();
        }

        void start(final int position) {
            stop();

            final int childCount = mListView.getChildCount();
            if (childCount == 0) {
                return;
            }

            final int firstPos = mListView.getFirstVisiblePosition();
            //final int lastPos = firstPos + childCount - 1;

            int viewTravelCount; 
            int clampedPosition = Math.max(0, Math.min(mListView.getCount() - 1, position));
            if (clampedPosition < firstPos) {
                viewTravelCount = firstPos - clampedPosition + 1;
                mMode = MOVE_UP_POS;
            } else if (clampedPosition > firstPos) {
                viewTravelCount = clampedPosition - firstPos + 1;
                mMode = MOVE_DOWN_POS;
            } else {
                scrollToVisible(clampedPosition, ListView.INVALID_POSITION, SCROLL_DURATION);
                return;
            }

            if (viewTravelCount > 0) {
                mScrollDuration = SCROLL_DURATION / viewTravelCount;
                if(mScrollDuration <= 0){
                	mScrollDuration = 1;
                }
            } else {
                mScrollDuration = SCROLL_DURATION;
            }
            mTargetPos = clampedPosition;
//            if(mMode == MOVE_DOWN_POS){
//            	mTargetPos -= 1;
//            }
            mLastSeenPos = ListView.INVALID_POSITION;

            ViewCompat.postOnAnimation(mListView,this);
        }

        /**
         * Scroll such that targetPos is in the visible padded region without scrolling
         * boundPos out of view. Assumes targetPos is onscreen.
         */
        void scrollToVisible(int targetPos, int boundPos, int duration) {
            final int firstPos = mListView.getFirstVisiblePosition();
            final int childCount = mListView.getChildCount();
            final int lastPos = firstPos + childCount - 1;
            final int paddedTop = mListView.getListPaddingTop();
            final int paddedBottom = mListView.getHeight() - mListView.getListPaddingBottom();

            if (boundPos < firstPos || boundPos > lastPos) {
                // boundPos doesn't matter, it's already offscreen.
                boundPos = ListView.INVALID_POSITION;
            }

            final View targetChild = mListView.getChildAt(targetPos - firstPos);
            final int targetTop = targetChild.getTop();
            final int targetBottom = targetChild.getBottom();
            int scrollBy = 0;

            if (targetBottom > paddedBottom) {
                scrollBy = targetBottom - paddedBottom;
            }
            if (targetTop < paddedTop) {
                scrollBy = targetTop - paddedTop;
            }

            if (scrollBy == 0) {
                return;
            }

            if (boundPos >= 0) {
                final View boundChild = mListView.getChildAt(boundPos - firstPos);
                final int boundTop = boundChild.getTop();
                final int boundBottom = boundChild.getBottom();
                final int absScroll = Math.abs(scrollBy);

                if (scrollBy < 0 && boundBottom + absScroll > paddedBottom) {
                    // Don't scroll the bound view off the bottom of the screen.
                    scrollBy = Math.max(0, boundBottom - paddedBottom);
                } else if (scrollBy > 0 && boundTop - absScroll < paddedTop) {
                    // Don't scroll the bound view off the top of the screen.
                    scrollBy = Math.min(0, boundTop - paddedTop);
                }
            }

            mListView.smoothScrollBy(scrollBy, duration);
        }

        void stop() {
            removeCallbacks(this);
        }

        public void run() {
            final int firstPos = mListView.getFirstVisiblePosition();

            switch (mMode) {
            case MOVE_DOWN_POS: {
            	final int endPos = mListView.getLastVisiblePosition();
            	if(mTargetPos <= endPos){
            		final View v = mListView.getChildAt(mTargetPos - firstPos);
            		mListView.smoothScrollBy(v.getTop(), mScrollDuration * (mTargetPos - firstPos));
            	}else{
            		if(mLastSeenPos > firstPos && endPos < mListView.getAdapter().getCount() - 1){
                		ViewCompat.postOnAnimation(mListView, this);
                		return;
                	}
            		final View v= mListView.getChildAt(endPos - firstPos);
            		final int extraScroll = endPos < mListView.getAdapter().getCount() - 1 ?
                         Math.max(mListView.getListPaddingBottom(), mExtraScroll) : mListView.getListPaddingBottom();
            		mListView.smoothScrollBy(v.getTop() + extraScroll, mScrollDuration * (endPos - firstPos));
            		mLastSeenPos = endPos;
            		ViewCompat.postOnAnimation(mListView, this);
            	}
                
//            	final int listHeight = mListView.getHeight();
//                final int lastViewIndex = mListView.getChildCount() - 1;
//                final int lastPos = firstPos + lastViewIndex;
//
//                if (lastViewIndex < 0) {
//                    return;
//                }
//
//                if (lastPos == mLastSeenPos) {
//                    ViewCompat.postOnAnimation(mListView,this);
//                    return;
//                }
//                final View lastView = mListView.getChildAt(lastViewIndex);
//                final int lastViewHeight = lastView.getHeight();
//                final int lastViewTop = lastView.getTop();
//                final int lastViewPixelsShowing = listHeight - lastViewTop;
//                final int extraScroll = lastPos < mListView.getAdapter().getCount() - 1 ?
//                        Math.max(mListView.getListPaddingBottom(), mExtraScroll) : mListView.getListPaddingBottom();
//
//                final int scrollBy = lastViewHeight - lastViewPixelsShowing + extraScroll;
//                mListView.smoothScrollBy(scrollBy, mScrollDuration);
//
//                mLastSeenPos = lastPos;
//                if (lastPos < mTargetPos) {
//                    ViewCompat.postOnAnimation(mListView,this);
//                }else if(mTargetPos > firstPos){
//                	if(lastPos < mListView.getAdapter().getCount() - 1){
//                		ViewCompat.postOnAnimation(mListView, this);
//                	}
//                }
                break;
            }

            case MOVE_UP_POS: {
                if (firstPos == mLastSeenPos) {
                    // No new views, let things keep going.
                    ViewCompat.postOnAnimation(mListView,this);
                    return;
                }

                final View firstView = mListView.getChildAt(0);
                if (firstView == null) {
                    return;
                }
                final int firstViewTop = firstView.getTop();
                final int extraScroll = firstPos > 0 ?
                        Math.max(mExtraScroll, mListView.getListPaddingTop()) : mListView.getListPaddingTop();

                mListView.smoothScrollBy(firstViewTop - extraScroll, mScrollDuration);

                mLastSeenPos = firstPos;

                if (firstPos > mTargetPos) {
                    ViewCompat.postOnAnimation(mListView,this);
                }
                break;
            }
            default:
                break;
            }
        }
    }
}
