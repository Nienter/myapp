package com.xbcx.adapter.anim;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.xbcx.adapter.AnimatableAdapter;

public class HorizontalAnimationAdapter extends SingleAnimationAdapter implements AnimatableAdapter{

	private static final String TRANSLATION_X = "translationX";
    private final long mAnimationDelayMillis;
    private final long mAnimationDurationMillis;
    
    private boolean	mIsAdd;

    public HorizontalAnimationAdapter(final BaseAdapter baseAdapter) {
        this(baseAdapter, DEFAULTANIMATIONDELAYMILLIS, DEFAULTANIMATIONDURATIONMILLIS);
    }

    public HorizontalAnimationAdapter(final BaseAdapter baseAdapter, final long animationDelayMillis) {
        this(baseAdapter, animationDelayMillis, DEFAULTANIMATIONDURATIONMILLIS);
    }

    public HorizontalAnimationAdapter(final BaseAdapter baseAdapter, final long animationDelayMillis, final long animationDurationMillis) {
        super(baseAdapter);
        mAnimationDelayMillis = animationDelayMillis;
        mAnimationDurationMillis = animationDurationMillis;
    }

    @Override
    protected long getAnimationDelayMillis() {
        return mAnimationDelayMillis;
    }

    @Override
    protected long getAnimationDurationMillis() {
        return mAnimationDurationMillis;
    }

    @Override
    protected Animator getAnimator(final ViewGroup parent, final View view) {
    	view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		if(mIsAdd){
			return ObjectAnimator.ofFloat(view, TRANSLATION_X, -view.getMeasuredWidth(), 0);
		}else{
			return ObjectAnimator.ofFloat(view, TRANSLATION_X, view.getMeasuredWidth(), 0);
		}
    }

	@Override
	public void playAddAnimation(int pos, BaseAdapter adapter) {
		mIsAdd = true;
		setShouldAnimateFromPosition(pos);
	}

	@Override
	public void playRemoveAnimation(int pos, BaseAdapter adapter) {
		mIsAdd = false;
		setShouldAnimateFromPosition(pos);
	}

	@Override
	public void setAnimatableAdapter(AnimatableAdapter adapter) {
	}
}
