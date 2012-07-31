package com.uppidy.android.demo.app;

import org.springframework.social.connect.ConnectionRepository;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.uppidy.android.sdk.R;
import com.uppidy.android.sdk.backup.BackupService;
import com.uppidy.android.sdk.api.Uppidy;
import com.uppidy.android.sdk.connect.UppidyConnectionFactory;

/**
 * @author arudnev@uppidy.com
 */
public class UppidyActivity extends AbstractAsyncActivity {

	protected static final String TAG = UppidyActivity.class.getSimpleName();

	private ConnectionRepository connectionRepository;

	private UppidyConnectionFactory connectionFactory;

	// ***************************************
	// Activity methods
	// ***************************************
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.connectionRepository = getApplicationContext().getConnectionRepository();
		this.connectionFactory = getApplicationContext().getUppidyConnectionFactory();
	}

	@Override
	public void onStart() {
		super.onStart();
		if (isConnected()) {
			showUppidyOptions();
		} else {
			showConnectOption();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	// ***************************************
	// Private methods
	// ***************************************
	private boolean isConnected() {
		return connectionRepository.findPrimaryConnection(Uppidy.class) != null;
	}

	private void disconnect() {
		this.connectionRepository.removeConnections(this.connectionFactory.getProviderId());
	}

	private void showConnectOption() {
		String[] options = { "Connect" };
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options);
		ListView listView = (ListView) this.findViewById(R.id.uppidy_activity_options_list);
		listView.setAdapter(arrayAdapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
				switch (position) {
				case 0:
					displayUppidyAuthorization();
					break;
				default:
					break;
				}
			}
		});
	}

	private void showUppidyOptions() {
		String[] options = { "Disconnect", "Browse", "Sync" };
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options);
		ListView listView = (ListView) this.findViewById(R.id.uppidy_activity_options_list);
		listView.setAdapter(arrayAdapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
				switch (position) {
				case 0:
					disconnect();
					showConnectOption();
					break;
				default:
					break;
				}
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
				Intent intent;
				switch (position) {
				case 0:
					disconnect();
					showConnectOption();
					break;
				case 1:
					intent = new Intent();
					intent.setClass(parentView.getContext(), UppidyBrowseActivity.class);
					startActivity(intent);
					break;
				case 2:
					startService(new Intent(BackupService.ACTION_START));
					break;
				default:
					break;
				}
			}
		});
	}

	private void displayUppidyAuthorization() {
		Intent intent = new Intent();
		intent.setClass(this, UppidyWebOAuthActivity.class);
		startActivity(intent);
		finish();
	}

}
