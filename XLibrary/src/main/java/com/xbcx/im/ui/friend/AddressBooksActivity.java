package com.xbcx.im.ui.friend;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.xbcx.adapter.FilterAdapterWrapper.ItemFilter;
import com.xbcx.adapter.LetterSortAdapterWrapper;
import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.adapter.LetterSortAdapterWrapper.OnLettersChangeListener;
import com.xbcx.core.Event;
import com.xbcx.core.IDObject;
import com.xbcx.core.NameObject;
import com.xbcx.im.IMKernel;
import com.xbcx.im.extention.roster.IMContact;
import com.xbcx.im.extention.roster.IMGroup;
import com.xbcx.im.extention.roster.RosterServicePlugin;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.im.ui.simpleimpl.BaseUserChooseActivity;
import com.xbcx.im.ui.simpleimpl.OnChildViewClickListener;
import com.xbcx.library.R;
import com.xbcx.utils.PinyinUtils;
import com.xbcx.utils.SystemUtils;
import com.xbcx.view.SectionIndexerView;

public class AddressBooksActivity extends BaseUserChooseActivity implements 
													View.OnClickListener,
													OnItemClickListener,
													OnItemLongClickListener,
													SectionIndexerView.OnSectionListener,
													TextWatcher,
													OnChildViewClickListener,
													OnCheckCallBack,
													OnLettersChangeListener{
	
	protected static final int MENUID_DELETE = 1;
	
	protected SectionIndexerView		mSectionIndexerView;
	protected TextView					mTextViewLetter;
	protected EditText					mEditText;
	
	protected LetterSortAdapterWrapper mLetterSortAdapter;
	protected IMContactAdapter			mContactAdapter;
	protected IMGroupAdapter			mGroupAdapter;
	
	protected boolean					mIsCheck;
	protected boolean					mIsCheckGroup = true;
	protected HashMap<String, Object> 	mMapCheckIdToItem = new HashMap<String, Object>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setIsHideViewFirstLoad(false);
		mIsCheck = getIntent().getBooleanExtra("ischeck", false);
		super.onCreate(savedInstanceState);
		
		disableRefresh();
		setNoResultTextId(R.string.no_result_addressbooks);
		
		mContactAdapter.setOnCheckCallBack(this);
		mContactAdapter.setOnChildViewClickListener(this);
		mContactAdapter.setIsCheck(mIsCheck);
		mGroupAdapter.setOnCheckCallBack(this);
		mGroupAdapter.setOnChildViewClickListener(this);
		mGroupAdapter.setIsCheck(mIsCheck);
		
		mPullToRefreshPlugin.setOnItemLongClickListener(this);
		registerForContextMenu(getListView());
		mSectionIndexerView = (SectionIndexerView)findViewById(R.id.si);
		mSectionIndexerView.setOnSectionListener(this);
		mTextViewLetter = (TextView)findViewById(R.id.tvLetter);
		mSectionIndexerView.setTextViewPrompt(mTextViewLetter);
		
		mLetterSortAdapter.setOnLettersChangeListener(this)
		.setTopName(getString(R.string.groups));
		
		findViewById(R.id.ivClear).setOnClickListener(this);
		mEditText = (EditText)findViewById(R.id.etSearch);
		mEditText.addTextChangedListener(this);
		SystemUtils.filterEnterKey(mEditText);
		
		if(mIsCheck){
			if(!TextUtils.isEmpty(mGroupId)){
				mLetterSortAdapter.addItemFilter(new GroupMemberFilter(mGroupId));
			}
		}
		
		showXProgressDialog();
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				PinyinUtils.getFirstSpell(getString(R.string.app_name));
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				dismissXProgressDialog();
				mContactAdapter.replaceAll(RosterServicePlugin.getInterface().getFriends());
				mGroupAdapter.replaceAll(RosterServicePlugin.getInterface().getGroups());
				
				addAndManageEventListener(RosterServicePlugin.IM_FriendListChanged);
				addAndManageEventListener(RosterServicePlugin.IM_GroupChatListChanged);
			}
		}.execute();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mActivityLayoutId = R.layout.xlibrary_activity_addressbooks;
	}
	
	@Override
	public ListAdapter onCreateAdapter() {
		mContactAdapter = onCreateContactAdapter();
		mGroupAdapter = onCreateGroupAdapter();
		LetterSortAdapterWrapper adapter = new LetterSortAdapterWrapper()
			.addAdapter(mContactAdapter)
			.addAdapter(mGroupAdapter)
			.setSectionAdapter(new LetterSectionAdapter());
		mLetterSortAdapter = adapter;
		return adapter;
	}
	
	protected IMGroupAdapter	onCreateGroupAdapter(){
		return new IMGroupAdapter(this);
	}
	
	protected IMContactAdapter 	onCreateContactAdapter(){
		return new IMContactAdapter(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		final Object item = parent.getItemAtPosition(position);
		if(item != null){
			if(item instanceof IMContact){
				onContactClicked((IMContact)item);
			}else if(item instanceof IMGroup){
				onGroupClicked((IMGroup)item);
			}
		}
	}
	
	protected void onContactClicked(IMContact contact){
		if(mIsCheck){
			checkItem(contact);
		}else{
			ActivityType.launchChatActivity(this, ActivityType.SingleChat, contact.getId(), contact.getName());
		}
	}
	
	protected void onGroupClicked(IMGroup group){
		if(mIsCheck){
			checkItem(group);
		}else{
			ActivityType.launchChatActivity(this, ActivityType.GroupChat, group.getId(), group.getName());
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		final Object item = parent.getItemAtPosition(position);
		if(item != null){
			if(item instanceof IMGroup ||
					item instanceof IMContact){
				setTag(item);
			}
		}
		return false;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		final Object tag = getTag();
		if(tag != null){
			if(tag instanceof IMGroup){
				final IMGroup group = (IMGroup)tag;
				menu.setHeaderTitle(group.getName());
				if(IMGroup.ROLE_ADMIN.equals(group.getMemberRole(IMKernel.getLocalUser()))){
					menu.add(0, MENUID_DELETE, 0, R.string.delete_group);
				}else{
					menu.add(0, MENUID_DELETE, 0, R.string.quit_group);
				}
			}else if(tag instanceof IMContact){
				final IMContact c = (IMContact)tag;
				menu.setHeaderTitle(c.getName())
				.add(0, MENUID_DELETE, 0, R.string.delete_friend);
			}
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int id = item.getItemId();
		if(id == MENUID_DELETE){
			requestDeleteItem(getTag());
		}
		return super.onContextItemSelected(item);
	}
	
	public void requestDeleteItem(Object item){
		if(item != null){
			if(item instanceof IMGroup){
				final IMGroup g = (IMGroup)item;
				if(IMGroup.ROLE_ADMIN.equals(g.getMemberRole(IMKernel.getLocalUser()))){
					pushEvent(RosterServicePlugin.IM_DeleteGroupChat, g.getId());
				}else{
					pushEvent(RosterServicePlugin.IM_QuitGroupChat, g.getId());
				}
			}else if(item instanceof IMContact){
				final IMContact c = (IMContact)item;
				pushEvent(RosterServicePlugin.IM_DeleteFriend, c.getId());
			}
		}
	}

	@Override
	public void onClick(View v) {
		//super.onClick(v);
		final int id = v.getId();
		if(id == R.id.ivClear){
			mEditText.setText(null);
		}
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		mLetterSortAdapter.setFilterKey(String.valueOf(s));
	}

	@Override
	public void afterTextChanged(Editable s) {
	}
	
	@Override
	public void onLettersChanged(List<String> letters) {
		mSectionIndexerView.setSections(letters);
	}
	
	@Override
	public void onSectionSelected(SectionIndexerView view, int section) {
		int pos = mLetterSortAdapter.getLetterSectionPos(section);
		setSelection(pos);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == RosterServicePlugin.IM_FriendListChanged){
			mContactAdapter.replaceAll(event.findParam(Collection.class));
		}else if(code == RosterServicePlugin.IM_GroupChatListChanged){
			mGroupAdapter.replaceAll(event.findParam(Collection.class));
		}else if(code == RosterServicePlugin.IM_DeleteGroupChat){
			if(!event.isSuccess()){
				final Exception e = event.getFailException();
				if(e != null && e instanceof XMPPException){
					final XMPPException xe = (XMPPException)e;
					XMPPError error = xe.getXMPPError();
					if(error != null){
						if(error.getCode() == 405){
							mToastManager.show(R.string.toast_delete_group_fail_by_permission);
						}
					}
				}
			}
		}
	}
	
	protected void onPreAddSection(){
	}

	@Override
	public boolean isCheck(Object item) {
		if(item instanceof IDObject){
			final String id = ((IDObject)item).getId();
			return mMapCheckIdToItem.containsKey(id);
		}
		return false;
	}

	@Override
	public void onChildViewClicked(BaseAdapter adapter, Object item,int viewId, View v) {
		if(viewId == R.id.cb){
			checkItem(item);
		}else if(viewId == R.id.ivAvatar){
			if(item != null){
				if(item instanceof IMContact){
					onContactAvatarClicked((IMContact)item);
				}else if(item instanceof IMGroup){
					onGroupAvatarClicked((IMGroup)item);
				}
			}
		}
	}
	
	protected void onContactAvatarClicked(IMContact contact){
		if(mIsCheck){
			checkItem(contact);
		}else{
			ActivityType.launchChatActivity(this, ActivityType.UserDetailActivity, 
					contact.getId(), contact.getName());
		}
	}
	
	protected void onGroupAvatarClicked(IMGroup group){
		if(mIsCheck){
			checkItem(group);
		}else{
			onGroupClicked(group);
		}
	}
	
	protected void checkItem(Object item){
		if(isCheck(item)){
			removeCheckItem(item);
		}else{
			addCheckItem(item);
		}
		invalidateViews();
	}
	
	@Override
	protected void onHandleDefaultUser(String id, String name) {
		super.onHandleDefaultUser(id, name);
		addCheckItem(new IMContact(id, name));
	}
	
	protected void addCheckItem(Object item){
		if(!isCheck(item)){
			if(item instanceof IDObject){
				final String id = ((IDObject)item).getId();
				mMapCheckIdToItem.put(id, item);
				if(item instanceof IMContact){
					addCheckUser((NameObject)item);
				}else if(item instanceof IMGroup){
					final IMGroup group = (IMGroup)item;
					for(IMContact c : group.getMembers()){
						addCheckUser(c);
					}
				}
				onAddChecked(item);
			}
		}
	}
	
	protected void removeCheckItem(Object item){
		if(item instanceof IDObject){
			final String id = ((IDObject)item).getId();
			if(TextUtils.equals(id, mDefaultUserId)){
				return;
			}
			mMapCheckIdToItem.remove(id);
			if(item instanceof IMContact){
				mMapCheckUserIds.remove(id);
			}else if(item instanceof IMGroup){
				final IMGroup group = (IMGroup)item;
				for(IMContact c : group.getMembers()){
					if(!mMapCheckIdToItem.containsKey(c.getId())){
						mMapCheckUserIds.remove(c.getId());
					}
				}
			}
			onRemoveChecked(item);
		}
	}
	
	protected void onAddChecked(Object item){
	}
	
	protected void onRemoveChecked(Object item){
	}
	
	public static class LetterSectionAdapter extends SetBaseAdapter<String>{
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = SystemUtils.inflate(parent.getContext(), R.layout.xlibrary_adapter_adb_section);
			}
			
			final TextView tv = (TextView)convertView.findViewById(R.id.tvName);
			tv.setText((String)getItem(position));
			
			return convertView;
		}
	}
	
	public static class GroupMemberFilter implements ItemFilter{
		
		public String	mGroupId;
		
		public GroupMemberFilter(String groupId){
			mGroupId = groupId;
		}
		
		@Override
		public boolean onFilter(Object item) {
			if(!TextUtils.isEmpty(mGroupId)){
				IMGroup group = RosterServicePlugin.getInterface().getGroup(mGroupId);
				if(group != null){
					if(item != null && item instanceof IMContact){
						final IMContact c = (IMContact)item;
						return group.hasMember(c.getId());
					}
				}
			}
			return false;
		}
	}
}
