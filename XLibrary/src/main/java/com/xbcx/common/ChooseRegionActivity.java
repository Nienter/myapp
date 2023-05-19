package com.xbcx.common;

import java.util.ArrayList;
import java.util.List;

import com.xbcx.core.XApplication;
import com.xbcx.core.BaseActivity;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class ChooseRegionActivity extends BaseActivity implements
											ExpandableListView.OnChildClickListener{
	
	public static final String EXTRA_RETURN_REGION = "region";
	
	private ExpandableListView mListView;
	private ChooseRegionAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initListView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mTitleTextStringId = R.string.region;
		ba.mActivityLayoutId = R.layout.xlibrary_choose_region_activity;
		ba.mAddBackButton = true;
	}

	public static void launchForResult(Activity activity,int nRequestCode){
		Intent intent = new Intent(activity, ChooseRegionActivity.class);
		activity.startActivityForResult(intent, nRequestCode);
	}
	
	private void initListView(){
		mAdapter = new ChooseRegionAdapter(this);
		mListView = (ExpandableListView)findViewById(R.id.lv);
		mListView.setFadingEdgeLength(0);
		mListView.setFooterDividersEnabled(false);
		mListView.setHeaderDividersEnabled(false);
		mListView.setIndicatorBounds(SystemUtils.dipToPixel(this,5),
				SystemUtils.dipToPixel(this,30));
		
		final LayoutInflater layoutInflater = LayoutInflater.from(this);
		View viewHeader = layoutInflater.inflate(R.layout.xlibrary_choose_region_section_divider, null);
		viewHeader.setMinimumHeight(SystemUtils.dipToPixel(this,10));
		mListView.addHeaderView(viewHeader);
		
		View viewFooter = layoutInflater.inflate(R.layout.xlibrary_choose_region_section_divider, null);
		viewFooter.setMinimumHeight(SystemUtils.dipToPixel(this,5));
		mListView.addFooterView(viewFooter);
		
		String strRegion = getString(R.string.region_info);
		String strParent[] = strRegion.split(",");
		for(String str : strParent){
			final Region region = new Region();
			final int nStart = str.indexOf(":");
			region.mParent = str.substring(0, nStart).trim();
			region.mChild = str.substring(nStart + 1).split("\\|");
			mAdapter.mListRegion.add(region);
		}
		
		mListView.setAdapter(mAdapter);
		mListView.setOnChildClickListener(this);
	}

	public boolean onChildClick(ExpandableListView parent, 
			View v, int groupPosition, int childPosition, long id) {
		final String strParent = (String)mAdapter.getGroup(groupPosition);
		final String strChild = (String)mAdapter.getChild(groupPosition, childPosition);
		if(strParent != null && strChild != null){
			Intent intent = new Intent();
			intent.putExtra(EXTRA_RETURN_REGION, strParent + "-" + strChild);
			setResult(RESULT_OK, intent);
			finish();
			return true;
		}
		return false;
	}
	
	private static class Region{
		String mParent;
		String mChild[];
	}
	
	private static class ChooseRegionAdapter extends BaseExpandableListAdapter{
		
		private List<Region> mListRegion = new ArrayList<ChooseRegionActivity.Region>();
		
		private Context mContext;
		
		public ChooseRegionAdapter(Context context){
			mContext = context;
		}

		public int getGroupCount() {
			return mListRegion.size();
		}

		public int getChildrenCount(int groupPosition) {
			final Region region = mListRegion.get(groupPosition);
			if(region != null){
				return region.mChild.length;
			}
			return 0;
		}

		public Object getGroup(int groupPosition) {
			return mListRegion.get(groupPosition).mParent;
		}

		public Object getChild(int groupPosition, int childPosition) {
			return mListRegion.get(groupPosition).mChild[childPosition];
		}

		public long getGroupId(int groupPosition) {
			return 0;
		}

		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		public boolean hasStableIds() {
			return false;
		}

		public View getGroupView(int groupPosition, 
				boolean isExpanded, View convertView, ViewGroup parent) {
			View v = convertView;
			if(v == null){
				v = LayoutInflater.from(mContext).inflate(R.layout.xlibrary_choose_region_textview_region, null);
			}
			
			TextView textView = (TextView)v;
			textView.setPadding(SystemUtils.dipToPixel(
					XApplication.getApplication(),35), 0, 0, 0);
			textView.setText((String)getGroup(groupPosition));
			
			return v;
		}

		public View getChildView(int groupPosition, 
				int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			View v = convertView;
			if(v == null){
				v = LayoutInflater.from(mContext).inflate(R.layout.xlibrary_choose_region_textview_region, null);
			}
			TextView textView = (TextView)v;
			v.setPadding(SystemUtils.dipToPixel(XApplication.getApplication(),55), 0, 0, 0);
			textView.setText((String)getChild(groupPosition, childPosition));
			return v;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}

}
