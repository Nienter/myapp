package com.xbcx.im.ui.simpleimpl;

import java.util.ArrayList;
import java.util.Collection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.xbcx.core.Event;
import com.xbcx.core.BaseActivity;
import com.xbcx.im.IMKernel;
import com.xbcx.im.extention.roster.IMContact;
import com.xbcx.im.extention.roster.IMGroup;
import com.xbcx.im.extention.roster.RosterServicePlugin;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.im.ui.simpleimpl.AbsBaseAdapter.ViewHolder;
import com.xbcx.im.vcard.VCardProvider;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

public class GroupMemberActivity extends BaseActivity implements 
													AdapterView.OnItemClickListener,
													OnChildViewClickListener{

	protected String				mId;
	
	protected TextView				mTextViewTitleRight;
	
	protected ListView				mListView;
	protected GroupMemberAdapter	mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mId = getIntent().getStringExtra("id");
		super.onCreate(savedInstanceState);
		mIsShowChatRoomBar = false;
		
		mListView = (ListView)findViewById(R.id.lv);
		mListView.setDivider(null);
		mListView.setOnItemClickListener(this);
		mAdapter = onCreateMemberAdapter();
		mAdapter.setOnChildViewClickListener(this);
		final IMGroup group = RosterServicePlugin.getInterface().getGroup(mId);
		if(group != null){
			mAdapter.addAll(group.getMembers());
			if(IMGroup.ROLE_ADMIN.equals(group.getMemberRole(IMKernel.getLocalUser()))){
				mTextViewTitleRight = (TextView)addTextButtonInTitleRight(R.string.delete);
			}
			updateTitle(group);
		}
		mListView.setAdapter(mAdapter);
		
		addAndManageEventListener(RosterServicePlugin.IM_GroupChatListChanged);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
		ba.mActivityLayoutId = R.layout.xlibrary_activity_groupmember;
		ba.mTitleText = "";
	}

	public static void launch(Activity activity,String groupId){
		Intent intent = new Intent(activity, GroupMemberActivity.class);
		intent.putExtra("id", groupId);
		activity.startActivity(intent);
	}
	
	protected GroupMemberAdapter onCreateMemberAdapter(){
		return new GroupMemberAdapter(this);
	}
	
	protected void updateTitle(IMGroup group){
		getTextViewTitle().setText(getString(R.string.group_member) + 
				"(" + group.getMemberCount() + getString(R.string.people) + ")");
	}

	@Override
	protected void onTitleRightButtonClicked(View v) {
		super.onTitleRightButtonClicked(v);
		if(getTextViewTitle() != null){
			mAdapter.setIsEdit(!mAdapter.isEdit());
			if(mAdapter.isEdit()){
				mTextViewTitleRight.setText(R.string.complete);
			}else{
				mTextViewTitleRight.setText(R.string.delete);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		final Object item = parent.getItemAtPosition(position);
		if(item != null && item instanceof IMContact){
			final IMContact contact = (IMContact)item;
			if(IMKernel.isLocalUser(contact.getId())){
				ActivityType.launchChatActivity(this, 
						ActivityType.SelfDetailActivity,
						contact.getId(),contact.getName());
			}else{
				ActivityType.launchChatActivity(this, 
						ActivityType.UserDetailActivity,
						contact.getId(),contact.getName());
			}
		}
	}

	@Override
	public void onChildViewClicked(BaseAdapter adapter, Object item, int viewId, View v) {
		if(viewId == R.id.btnDelete){
			final IMContact c = (IMContact)item;
			Collection<String> ids = new ArrayList<String>();
			ids.add(c.getId());
			pushEvent(RosterServicePlugin.IM_DeleteGroupChatMember,mId,ids);
		}
	}

	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == RosterServicePlugin.IM_GroupChatListChanged){
			final IMGroup group = RosterServicePlugin.getInterface().getGroup(mId);
			if(group == null){
				finish();
			}else{
				mAdapter.replaceAll(group.getMembers());
				updateTitle(group);
			}
		}
	}
	
	protected static class MemberViewHolder extends ViewHolder{
		public ImageView 	mImageViewAvatar;
		public TextView		mTextViewName;
		public TextView		mTextViewDetail;
		public View			mViewDelete;
	}
	
	protected static class GroupMemberAdapter extends AbsBaseAdapter<IMContact,MemberViewHolder>{
		
		protected boolean	mIsEdit;
		
		public GroupMemberAdapter(Context context) {
			super(context);
		}

		public void setIsEdit(boolean bEdit){
			mIsEdit = bEdit;
			notifyDataSetChanged();
		}
		
		public boolean isEdit(){
			return mIsEdit;
		}

		@Override
		protected View onCreateConvertView() {
			return SystemUtils.inflate(mContext, R.layout.xlibrary_adapter_imgroup_member);
		}

		@Override
		protected MemberViewHolder onCreateViewHolder() {
			return new MemberViewHolder();
		}

		@Override
		protected void onSetViewHolder(MemberViewHolder viewHolder, View convertView) {
			viewHolder.mImageViewAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
			viewHolder.mTextViewName = (TextView)convertView.findViewById(R.id.tvName);
			viewHolder.mTextViewDetail = (TextView)convertView.findViewById(R.id.tvDetail);
			viewHolder.mViewDelete = convertView.findViewById(R.id.btnDelete);
			viewHolder.mViewDelete.setOnClickListener(this);
		}

		@Override
		protected void onSetChildViewTag(MemberViewHolder viewHolder, Object item) {
			viewHolder.mViewDelete.setTag(item);
		}

		@Override
		protected void onUpdateView(MemberViewHolder viewHolder, IMContact item, int position) {
			if(mIsEdit){
				if(IMKernel.isLocalUser(item.getId())){
					viewHolder.mViewDelete.setVisibility(View.GONE);
				}else{
					viewHolder.mViewDelete.setVisibility(View.VISIBLE);
				}
			}else{
				viewHolder.mViewDelete.setVisibility(View.GONE);
			}
			
			VCardProvider.getInstance().setAvatar(viewHolder.mImageViewAvatar, item.getId());
			VCardProvider.getInstance().setName(viewHolder.mTextViewName, item.getId(),item.getName());
		}
	}
}
