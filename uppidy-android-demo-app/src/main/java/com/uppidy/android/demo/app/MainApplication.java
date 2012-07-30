package com.uppidy.android.demo.app;

import org.springframework.security.crypto.encrypt.AndroidEncryptors;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.sqlite.SQLiteConnectionRepository;
import org.springframework.social.connect.sqlite.support.SQLiteConnectionRepositoryHelper;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;

import android.app.Application;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;

import com.uppidy.android.sdk.R;
import com.uppidy.android.sdk.social.api.Uppidy;
import com.uppidy.android.sdk.social.connect.UppidyConnectionFactory;
import com.uppidy.server.api.Container;

/**
 * @author arudnev@uppidy.com
 */
public class MainApplication extends Application {
	private ConnectionFactoryRegistry connectionFactoryRegistry;
	private SQLiteOpenHelper repositoryHelper;
	private ConnectionRepository connectionRepository;
	private Container container;

	// ***************************************
	// Application Methods
	// ***************************************
	@Override
	public void onCreate() {
		// create a new ConnectionFactoryLocator and populate it with Uppidy ConnectionFactory
		this.connectionFactoryRegistry = new ConnectionFactoryRegistry();
		this.connectionFactoryRegistry.addConnectionFactory(new UppidyConnectionFactory(getUppidyAppId(), getUppidyAppSecret(), getBaseUrl()));

		// set up the database and encryption
		this.repositoryHelper = new SQLiteConnectionRepositoryHelper(this);
		this.connectionRepository = new SQLiteConnectionRepository(this.repositoryHelper,
				this.connectionFactoryRegistry, AndroidEncryptors.text("password", "5c0744940b5c369b"));
	}

	// ***************************************
	// Private methods
	// ***************************************
	private String getUppidyAppId() {
		return getString(R.string.uppidy_app_id);
	}

	private String getUppidyAppSecret() {
		return getString(R.string.uppidy_app_secret);
	}
	
	private String getBaseUrl() {
		return getString(R.string.uppidy_base_url);
	}

	// ***************************************
	// Public methods
	// ***************************************
	public ConnectionRepository getConnectionRepository() {
		return this.connectionRepository;
	}

	public UppidyConnectionFactory getUppidyConnectionFactory() {
		return (UppidyConnectionFactory) this.connectionFactoryRegistry.getConnectionFactory(Uppidy.class);
	}
	
	public SharedPreferences getSettings() {
		return getSharedPreferences("UppidyDemoSettings", MODE_PRIVATE);		
	}
	
	public Container getContainer() {
		return this.container;
	}
	
	public void setContainer(Container container) {
		this.container = container;
	}
	
	public String getContainerId() {
		return getSettings().getString("ContainerId", null);
	}
	
	public boolean setContainerId(String containerId) {
		return getSettings().edit().putString("ContainerId", containerId).commit();
	}
}
