package com.xbcx.adapter.anim;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.xbcx.adapter.AnimatableAdapter;
import com.xbcx.core.XApplication;

public class XSwingBottomInAnimationAdapter extends SwingBottomInAnimationAdapter implements AnimatableAdapter{

	protected int		mInsertPos = -1;
	protected boolean	mHandleInsertPos = false;
	protected boolean	mAddListener;
	
	public XSwingBottomInAnimationAdapter(final BaseAdapter baseAdapter) {
        super(baseAdapter, 0, DEFAULTANIMATIONDURATIONMILLIS);
        super.setShouldAlpha(false);
        super.setShouldAnimate(false);
    }

    public XSwingBottomInAnimationAdapter(final BaseAdapter baseAdapter, final long animationDelayMillis) {
    	super(baseAdapter, animationDelayMillis, DEFAULTANIMATIONDURATIONMILLIS);
    	super.setShouldAlpha(false);
    	super.setShouldAnimate(false);
    }

    public XSwingBottomInAnimationAdapter(final BaseAdapter baseAdapter, final long animationDelayMillis, final long animationDurationMillis) {
    	super(baseAdapter,animationDelayMillis,animationDurationMillis);
    	super.setShouldAlpha(false);
    	super.setShouldAnimate(false);
    }

    @Override
	public void setShouldAlpha(boolean shouldAlpha) {
	}
    
	@Override
    protected Animator getAnimator(final ViewGroup parent, final View view) {
		Animator a = null;
		if(mInsertPos == -1){
			 a = ObjectAnimator.ofFloat(view, "translationY", view.getHeight() == 0 ? 500 : view.getHeight(), 0);
		}else{
			if(mHandleInsertPos){
				mHandleInsertPos = false;
				a = ObjectAnimator.ofFloat(view, "translationX", view.getWidth() <= 0 ? XApplication.getScreenWidth() : view.getWidth(),0);
			}else{
				a = ObjectAnimator.ofFloat(view, "translationY", view.getHeight() == 0 ? -100 : -view.getHeight(),0);
			}
		}
		if(mAddListener){
			mAddListener = false;
			a.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator arg0) {
				}
				@Override
				public void onAnimationRepeat(Animator arg0) {
				}
				@Override
				public void onAnimationEnd(Animator arg0) {
					setShouldAnimate(false);
					mInsertPos = -1;
				}
				@Override
				public void onAnimationCancel(Animator arg0) {
					setShouldAnimate(false);
					mInsertPos = -1;
				}
			});
		}
        return a;
    }

	@Override
	public void playAddAnimation(int pos, BaseAdapter adapter) {
		if(getDecoratedBaseAdapter().getCount() - 1 == pos){
			setShouldAnimateFromPosition(pos);
		}else{
			mInsertPos = pos;
			mHandleInsertPos = true;
			setShouldAnimateFromPosition(pos);
		}
		mAddListener = true;
	}

	@Override
	public void playRemoveAnimation(int pos, BaseAdapter adapter) {
		setShouldAnimateFromPosition(pos);
		mAddListener = true;
	}

	@Override
	public void setAnimatableAdapter(AnimatableAdapter adapter) {
	}
}
