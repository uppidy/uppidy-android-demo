package com.uppidy.android.demo.app;

import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.uppidy.android.sdk.social.api.Uppidy;
import com.uppidy.android.sdk.social.api.Message;

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
	private void showResult(List<Message> entries) {
		UppidyFeedListAdapter adapter = new UppidyFeedListAdapter(this, entries);
		setListAdapter(adapter);
	}

	// ***************************************
	// Private classes
	// ***************************************
	private class FetchFeedTask extends AsyncTask<Void, Void, List<Message>> {

		@Override
		protected void onPreExecute() {
			showProgressDialog("Fetching feed...");
		}

		@Override
		protected List<Message> doInBackground(Void... params) {
			try {
				return uppidy.backupOperations().listMessages(getApplicationContext().getContainerId(), null);
			} catch (Exception e) {
				Log.e(TAG, e.getLocalizedMessage(), e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Message> entries) {
			dismissProgressDialog();
			showResult(entries);
		}
	}

}
