package com.uppidy.android.demo.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.encrypt.AndroidEncryptors;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.sqlite.SQLiteConnectionRepository;
import org.springframework.social.connect.sqlite.support.SQLiteConnectionRepositoryHelper;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.oauth2.AccessGrant;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.uppidy.android.sdk.R;
import com.uppidy.android.sdk.api.ApiContactInfo;
import com.uppidy.android.sdk.api.ApiContainer;
import com.uppidy.android.sdk.api.Uppidy;
import com.uppidy.android.sdk.connect.UppidyConnectionFactory;
import com.uppidy.android.sdk.connect.UppidyOAuth2Template;

/**
 * @author arudnev@uppidy.com
 */
public class MainApplication extends Application {
	private ConnectionFactoryRegistry connectionFactoryRegistry;
	private SQLiteOpenHelper repositoryHelper;
	private ConnectionRepository connectionRepository;
	private ApiContainer container;

	// ***************************************
	// Application Methods
	// ***************************************
	@Override
	public void onCreate() {
		// create a new ConnectionFactoryLocator and populate it with Uppidy ConnectionFactory
		this.connectionFactoryRegistry = new ConnectionFactoryRegistry();
		this.connectionFactoryRegistry.addConnectionFactory(new UppidyConnectionFactory(getUppidyAppId(), getBaseUrl()));

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
	
	public ApiContainer getContainer() {
		return this.container;
	}
	
	public void setContainer(ApiContainer container) {
		this.container = container;
	}
	
	public String getContainerId() {
		return getSettings().getString("ContainerId", null);
	}
	
	public boolean setContainerId(String containerId) {
		return getSettings().edit().putString("ContainerId", containerId).commit();
	}
	
	public AccessGrant login(String username, String password) {
		return ((UppidyOAuth2Template) getUppidyConnectionFactory().getOAuthOperations()).login(username, password, null);
	}
	
	public Map<String, String> getContainerSearchParams() {
		Map<String, String> queryParams = new HashMap<String, String>();
		String containerId = getContainerId();
		if(containerId != null) {
			queryParams.put("id", containerId);
		} else {
			// Returns the phone number string for line 1, for example, the MSISDN for a GSM phone. 
			String phoneNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
			if (phoneNumber != null && phoneNumber.length() > 0) queryParams.put("owner.address", phoneNumber);
			// Returns the unique device ID, for example, the IMEI for GSM and the MEID or ESN for CDMA phones. 
			String deviceId = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
			if (deviceId != null && deviceId.length() > 0) queryParams.put("deviceId", deviceId);
		}
		return queryParams;		
	}
	public ApiContainer containerData() {
		ApiContainer result = new ApiContainer();
		result.setOwner(new ApiContactInfo());
		// Returns the phone number string for line 1, for example, the MSISDN for a GSM phone. 
		String phoneNumber = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
		if (phoneNumber != null && phoneNumber.length() > 0) result.getOwner().setAddress(phoneNumber);
		// Returns the unique device ID, for example, the IMEI for GSM and the MEID or ESN for CDMA phones. 
		String deviceId = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		if (deviceId != null && deviceId.length() > 0) result.setDeviceId(deviceId);
		result.setDescription(Build.MODEL);
		result.setModel(Build.MODEL);
		result.setVersion("12.08");
		result.setType("ANDROID");
		// this is required to automatically update container data with modifications from the server
		result.setRef(phoneNumber + ":" + deviceId);
		return result;		
	}
	
	public void cleanup() {
		setContainer(null);
		setContainerId(null);
	}

	public void init() {
		if(getContainer() == null) {
			Uppidy uppidy = connectionRepository.findPrimaryConnection(Uppidy.class).getApi();
			List<ApiContainer> containers = uppidy.backupOperations().listContainers(getContainerSearchParams());
			if(containers.isEmpty()) {
				ApiContainer data = containerData();
				uppidy.backupOperations().saveContainer(data);
				setContainer(data);
			} else {
				setContainer(containers.get(0));
			}		
			setContainerId(getContainer().getId());
		}
	}
}
