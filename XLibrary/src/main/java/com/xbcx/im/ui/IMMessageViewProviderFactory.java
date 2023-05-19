package com.xbcx.im.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;

import com.xbcx.im.ui.messageviewprovider.PromptViewProvider;
import com.xbcx.im.ui.messageviewprovider.TextViewLeftProvider;
import com.xbcx.im.ui.messageviewprovider.TextViewRightProvider;
import com.xbcx.im.ui.messageviewprovider.TimeViewProvider;

public class IMMessageViewProviderFactory {
	
	public List<IMMessageViewProvider> createIMMessageViewProviders(
			Context context){
		List<IMMessageViewProvider> providers = new ArrayList<IMMessageViewProvider>();
		providers.add(new TextViewLeftProvider((Activity)context));
		providers.add(new TextViewRightProvider((Activity)context));
		providers.add(new TimeViewProvider());
		providers.add(new PromptViewProvider());
		return providers;
	}
}
