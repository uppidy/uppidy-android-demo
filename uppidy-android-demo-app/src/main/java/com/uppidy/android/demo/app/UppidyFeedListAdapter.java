package com.uppidy.android.demo.app;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uppidy.android.sdk.social.api.ContactInfo;
import com.uppidy.android.sdk.social.api.Message;

/**
 * @author arudnev@uppidy.com
 */
public class UppidyFeedListAdapter extends BaseAdapter {
    private List<Message> entries;
    private final LayoutInflater layoutInflater;

    public UppidyFeedListAdapter(Context context, List<Message> entries) {
        this.entries = entries;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return entries == null ? 0 : entries.size();
    }

    public Message getItem(int position) {
        return entries.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    private String toString(ContactInfo ref) {
    	if(ref == null) return null;
    	return ref.toString();
    }
    
    private String toString(List<ContactInfo> refs) {
    	String sep = "";
    	StringBuffer result = new StringBuffer();
    	result.append("[");
    	for(ContactInfo ref : refs) {
    		result.append(sep);
    		result.append(ref);
    		sep = ", ";
    	}
    	result.append("]");
    	return result.toString();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Message entry = getItem(position);
        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(R.layout.browse_item, parent, false);
        }

        boolean sent = entry.isSent(); 
        
        TextView t = (TextView) view.findViewById(R.id.direction);
        t.setText(sent ? "Sent" : "Received");        
        
        t = (TextView) view.findViewById(R.id.address);
       	t.setText(sent ? toString(entry.getTo()) : toString(entry.getFrom()));

        t = (TextView) view.findViewById(R.id.sent_time);
        t.setText(entry.getSentTime().toString());

        t = (TextView) view.findViewById(R.id.message);
        t.setText(entry.getText());

        return view;
    }

}
