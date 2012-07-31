package com.uppidy.android.demo.app;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uppidy.android.sdk.api.ApiContactInfo;
import com.uppidy.android.sdk.api.ApiMessage;

/**
 * @author arudnev@uppidy.com
 */
public class UppidyFeedListAdapter extends BaseAdapter {
    private List<ApiMessage> entries;
    private final LayoutInflater layoutInflater;

    public UppidyFeedListAdapter(Context context, List<ApiMessage> entries) {
        this.entries = entries;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return entries == null ? 0 : entries.size();
    }

    public ApiMessage getItem(int position) {
        return entries.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    private String toString(ApiContactInfo ref) {
    	if(ref == null) return null;
    	return ref.toString();
    }
    
    private String toString(List<ApiContactInfo> refs) {
    	String sep = "";
    	StringBuffer result = new StringBuffer();
    	result.append("[");
    	for(ApiContactInfo ref : refs) {
    		result.append(sep);
    		result.append(ref);
    		sep = ", ";
    	}
    	result.append("]");
    	return result.toString();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ApiMessage entry = getItem(position);
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
