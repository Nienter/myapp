package com.xbcx.core;

import android.view.View;
import android.widget.FrameLayout;

public interface ContentStatusViewProvider {

	public void showFailView();
	
	public void hideFailView();
	
	public boolean isFailViewVisible();
	
	public void setFailText(CharSequence text);
	
	public CharSequence getFailText();
	
	public void showNoResultView();
	
	public void hideNoResultView();
	
	public boolean isNoResultViewVisible();
	
	public void setNoResultText(CharSequence text);
	
	public boolean hasSetNoResultText();
	
	public CharSequence getNoResultText();
	
	public void addContentView(View v,FrameLayout.LayoutParams lp);
	
	public void	setTopMarginProvider(TopMarginProvider provider);
	
	public static interface TopMarginProvider{
		public int getTopMargin(View statusView);
	}
}
