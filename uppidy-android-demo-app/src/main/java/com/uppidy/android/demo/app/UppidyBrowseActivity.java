package com.uppidy.android.demo.app;

import java.util.Date;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.uppidy.android.sdk.api.ApiMessage;
import com.uppidy.android.sdk.api.Uppidy;

/**
 * @author arudnev@uppidy.com
 */
public class UppidyBrowseActivity extends AbstractAsyncListActivity {

	protected static final String TAG = UppidyBrowseActivity.class.getSimpleName();

	private Uppidy uppidy;

	// ***************************************
	// Activity methods
	// ***************************************
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.uppidy = getApplicationContext().getConnectionRepository().findPrimaryConnection(Uppidy.class).getApi();
	}

	@Override
	public void onStart() {
		super.onStart();
		new FetchFeedTask().execute();
	}

	// ***************************************
	// Private methods
	// ***************************************
	private void showResult(List<ApiMessage> entries) {
		UppidyFeedListAdapter adapter = new UppidyFeedListAdapter(this, entries);
		setListAdapter(adapter);
	}

	// ***************************************
	// Private classes
	// ***************************************
	private class FetchFeedTask extends AsyncTask<Void, Void, List<ApiMessage>> {

		@Override
		protected void onPreExecute() {
			showProgressDialog("Searching for 'fake' messages...");
		}

		@Override
		protected List<ApiMessage> doInBackground(Void... params) {
			try {				
				return uppidy.feedOperations().searchFeed("account", "me", "fake", 0, 100, new Date(0), new Date());
			} catch (Exception e) {
				Log.e(TAG, e.getLocalizedMessage(), e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<ApiMessage> entries) {
			dismissProgressDialog();
			showResult(entries);
		}
	}

}
