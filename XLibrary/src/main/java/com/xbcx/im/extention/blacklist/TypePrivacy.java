package com.xbcx.im.extention.blacklist;

import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.packet.Privacy;
import org.jivesoftware.smack.packet.PrivacyItem;

import android.text.TextUtils;

public class TypePrivacy extends Privacy {

	private String mPrivacyType;
	
	private String mListType;
	
	public void setPrivacyType(String strType){
		mPrivacyType = strType;
	}
	
	public void setListType(String strListType){
		mListType = strListType;
	}
	
	public String getChildElementXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<query xmlns=\"jabber:iq:privacy\"");
        if(!TextUtils.isEmpty(mPrivacyType)){
        	buf.append(" type=\"").append(mPrivacyType).append("\"");
        }
        buf.append(">");
        
        // Add the active tag
        if (this.isDeclineActiveList()) {
        	buf.append("<active/>");
        } else {
        	if (this.getActiveName() != null) {
            	buf.append("<active name=\"").append(this.getActiveName()).append("\"/>");
            }
        }
        // Add the default tag
        if (this.isDeclineDefaultList()) {
        	buf.append("<default/>");
        } else {
	        if (this.getDefaultName() != null) {
	        	buf.append("<default name=\"").append(this.getDefaultName()).append("\"/>");
	        }
        }
        
        // Add the list with their privacy items
        for (Map.Entry<String, List<PrivacyItem>> entry : this.getItemLists().entrySet()) {
          String listName = entry.getKey();
          List<PrivacyItem> items = entry.getValue();
			// Begin the list tag
			if (items.isEmpty()) {
				buf.append("<list name=\"").append(listName).append("\"");
				if(!TextUtils.isEmpty(mListType)){
					buf.append(" type=\"")
					.append(mListType).append("\"");
				}
				buf.append("/>");
			} else {
				buf.append("<list name=\"").append(listName).append("\"");
				if(!TextUtils.isEmpty(mListType)){
					buf.append(" type=\"")
					.append(mListType).append("\"");
				}
				buf.append(">");
			}
	        for (PrivacyItem item : items) {
	        	// Append the item xml representation
	        	buf.append(item.toXML());
	        }
	        // Close the list tag
	        if (!items.isEmpty()) {
				buf.append("</list>");
			}
		}

        // Add packet extensions, if any are defined.
        buf.append(getExtensionsXML());
        buf.append("</query>");
        return buf.toString();
    }
}
