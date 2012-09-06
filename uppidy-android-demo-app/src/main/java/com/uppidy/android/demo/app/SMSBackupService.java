package com.uppidy.android.demo.app;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.util.FileCopyUtils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextUtils;

import com.uppidy.android.sdk.api.ApiBodyPart;
import com.uppidy.android.sdk.api.ApiContact;
import com.uppidy.android.sdk.api.ApiContactInfo;
import com.uppidy.android.sdk.api.ApiMessage;
import com.uppidy.android.sdk.api.ApiSync;
import com.uppidy.android.sdk.api.Uppidy;
import com.uppidy.android.sdk.backup.BackupService;
import com.uppidy.android.sdk.backup.MessageProvider;

import de.akquinet.android.androlog.Log;

public class SMSBackupService extends BackupService
{	
	private static final String   ACTION_SMS_BACKUP = "com.uppidy.android.demo.SMS_BACKUP";
	private static final String   ACTION_MMS_BACKUP = "com.uppidy.android.demo.MMS_BACKUP";
	
	public  static final String[] CONTACTS_QUERY    = new String[] { 
		ContactsContract.PhoneLookup.NUMBER,
		ContactsContract.PhoneLookup.DISPLAY_NAME 
	};
	// ContentResolver SMS lookup constants
	public  static final String[] SMS_QUERY         = new String[] { 
		"_id", "address", "body", "date", "type" 
		};
	public  static final Uri      SMS_URI           = Uri.parse("content://sms/");
	public  static final String   SMS_WHERE_CLAUSE  = "date > ? and date < ? and type IN(1,2)";
	public  static final String   SMS_ORDER_ASC     = "date asc";
	public  static final long     SMS_TIME_GAP      = 2000L; // 2 minutes
	public  static final int      SMS_TYPE_IN       = 1;
	public  static final int      SMS_TYPE_SENT     = 2;
	private static final int      MAX_SMS           = 10;

	// ContentResolver MMS lookup constants
	public  static final Uri      MMS_URI           = Uri.parse("content://mms/");
	public  static final String[] MMS_QUERY         = new String[] { "_id", "date", "m_type"	};
	public  static final String   MMS_WHERE_CLAUSE  = "date > ? and date < ? and m_type IN(1,2)";
	public  static final String   MMS_ORDER_ASC     = "date asc";
	private static final int      MAX_MMS           = 1;
	
	private static boolean  enabled = false;
	private MainApplication appContext;
	
	public SMSBackupService()
	{
		super( "SMS Backup" );
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		this.appContext = (MainApplication)getApplicationContext();
		super.addMessageProvider( ACTION_SMS_BACKUP, new SMSMessageProvider() );		
		super.addMessageProvider( ACTION_MMS_BACKUP, new MMSMessageProvider() );		
	}
	
	@Override
	protected Uppidy getUppidy()
	{
		return appContext.getConnectionRepository().findPrimaryConnection(Uppidy.class).getApi();
	}
	
	@Override
	protected boolean isEnabled()
	{
		return enabled;
	}
	
	public static void start( Context context )
	{
		enabled = true;
		context.startService( new Intent(BackupService.ACTION_BACKUP_ALL) );
	}
	
	public static void stop()
	{
		enabled = false;
	}
	
	// ***************************************
	// Private classes
	// ***************************************
	class SMSMessageProvider implements MessageProvider
	{
		Date lastSyncDate = null;
		
		public SMSMessageProvider()
		{
		}
		
		@Override
		public void backupDone( ApiSync sync )
		{
			List<ApiMessage> messages = sync.getMessages();
			lastSyncDate = messages.get(messages.size()-1).getSentTime();
		}

		@Override
		public String getContainerId()
		{
			return appContext.getContainer().getId();
		}

		@Override
		public ApiSync getNextSyncBundle()
		{
			List<ApiMessage> messages = getSmsMessages( lastSyncDate, MAX_SMS );
			ApiSync sync = null;
			if( messages.size() > 0 ) 
			{
				sync = new ApiSync();
				sync.setRef("sync:" + System.currentTimeMillis());
				sync.setMessages(messages);
				sync.setContacts(getContactsFromMessages(messages));
			}
			return sync;
		}
	}

	class MMSMessageProvider implements MessageProvider
	{
		Date lastSyncDate = null;
		
		public MMSMessageProvider()
		{
		}
		
		@Override
		public void backupDone( ApiSync sync )
		{
			List<ApiMessage> messages = sync.getMessages();
			lastSyncDate = messages.get(messages.size()-1).getSentTime();
		}

		@Override
		public String getContainerId()
		{
			return appContext.getContainer().getId();
		}

		@Override
		public ApiSync getNextSyncBundle() 
		{
			ApiSync sync = null;
			try 
			{
				List<ApiMessage> messages = getMmsMessages( lastSyncDate, MAX_MMS );
			
				if( messages.size() > 0 ) 
				{
					sync = new ApiSync();
					sync.setMessages(messages);
					sync.setContacts(getContactsFromMessages(messages));
				}
			} 
			catch( Exception ex)
			{
				Log.e(TAG, "Error while fetching MMS messages: ", ex );
			}
			return sync;
		}
	}
	
	// **************************************
	// Private methods
	// **************************************
	private List<ApiMessage> getMmsMessages( Date from, int num ) throws Exception
	{
		List<ApiMessage> messages = new ArrayList<ApiMessage>();
		
		long startFrom = (from==null) ? 0l : from.getTime();
		Cursor cursor = null;
		cursor = getContentResolver().query(MMS_URI, MMS_QUERY, 
					MMS_WHERE_CLAUSE, 
					new String[] { 
						String.valueOf(startFrom),
						String.valueOf(Calendar.getInstance().getTimeInMillis() - SMS_TIME_GAP) 
					}, 
					MMS_ORDER_ASC );
		cursor.moveToFirst();
		int size = cursor.getCount();
		ApiContactInfo me = appContext.getContainer().getOwner();
		for( int i = 0; i < num && i < size; i++, cursor.moveToNext() )
		{
			//"_id", "date", "m_type"
			String id = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
			long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
			int type = cursor.getInt(cursor.getColumnIndexOrThrow("m_type"));
			
			ApiMessage m = new ApiMessage();
			m.setRef( "message:" + id );
			m.setSentTime( new Date(date) );
			
			// get recipients
	        final Uri msgRef  = Uri.withAppendedPath(MMS_URI, id);
	        Cursor curAddr = getContentResolver().query(Uri.withAppendedPath(msgRef, "addr"), null, null, null, null);
	        final List<ApiContactInfo> other = new ArrayList<ApiContactInfo>();
	        while (curAddr != null && curAddr.moveToNext()) {
	           ApiContactInfo address = new ApiContactInfo();
	           address.setAddress(curAddr.getString(curAddr.getColumnIndex("address")));
	           other.add(address);
	        }
	        if (curAddr != null) curAddr.close();
	        
			switch( type )
			{
				case SMS_TYPE_IN: 
					m.setFrom(other.size()==0 ? null : other.get(0));
					m.setTo(Collections.singletonList(me));
					m.setSent(false);
					break;
				//case SMS_TYPE_SENT:
				default:
					m.setFrom(me);
					m.setTo(other); 
					m.setSent(true);
					break;
			}
			
			m.setParts( getBodyParts(Uri.withAppendedPath(msgRef, "part")));			
			messages.add( m );
		}
		
		return messages;
	}
	
	private List<ApiBodyPart> getBodyParts(final Uri uriPart) throws Exception 
	{
        final List<ApiBodyPart> parts = new ArrayList<ApiBodyPart>();
        Cursor curPart = appContext.getContentResolver().query(uriPart, null, null, null, null);

        // _id, mid, seq, ct, name, chset, cd, fn, cid, cl, ctt_s, ctt_t, _data, text
		while (curPart != null && curPart.moveToNext())	{
			final String id = curPart.getString(curPart.getColumnIndex("_id"));
			final String contentType = curPart.getString(curPart.getColumnIndex("ct"));
			final String fileName = curPart.getString(curPart.getColumnIndex("cl"));
			final String text = curPart.getString(curPart.getColumnIndex("text"));

			ApiBodyPart part = new ApiBodyPart();
			part.setRef("part:" + id);
			part.setContentType(contentType);
			part.setFileName(fileName);

			// extract part body
			if (contentType.startsWith("text/") && !TextUtils.isEmpty(text)) {
				part.setData(new ByteArrayResource(text.getBytes("UTF-8")));
			} else {
				Uri partUri = Uri.withAppendedPath(MMS_URI, "part/" + id);
				InputStream in = getContentResolver().openInputStream(partUri);
				byte[] data = FileCopyUtils.copyToByteArray(in);
				part.setData(new ByteArrayResource(data));
			}			
		}

        if (curPart != null) curPart.close();
        return parts;
    }
	
	private List<ApiMessage> getSmsMessages( Date from, int num )
	{
		List<ApiMessage> messages = new ArrayList<ApiMessage>();
		
		long startFrom = (from==null) ? 0l : from.getTime();
		Cursor cursor = null;
		cursor = getContentResolver().query(SMS_URI, SMS_QUERY, 
					SMS_WHERE_CLAUSE, 
					new String[] { 
						String.valueOf(startFrom),
						String.valueOf(Calendar.getInstance().getTimeInMillis() - SMS_TIME_GAP) 
					}, 
					SMS_ORDER_ASC );
		
		cursor.moveToFirst();
		int size = cursor.getCount();
		ApiContactInfo me = appContext.getContainer().getOwner();
		for( int i = 0; i < num && i < size; i++, cursor.moveToNext() )
		{
			String mID = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
			String smsAddress = cursor.getString(cursor.getColumnIndexOrThrow("address"));
			String smsBody = cursor.getString(cursor.getColumnIndexOrThrow("body"));
			Long smsDate = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
			int type = cursor.getInt(cursor.getColumnIndexOrThrow("type"));
			ApiMessage m = new ApiMessage();
			m.setRef( "message:" + mID );
			ApiContactInfo other = new ApiContactInfo();
			other.setAddress(smsAddress);
			switch( type )
			{
				case SMS_TYPE_IN: 
					m.setFrom( other );
					m.setTo(Collections.singletonList( me ) );
					m.setSent( false );
					break;
				//case SMS_TYPE_SENT:
				default:
					m.setFrom(me);
					m.setTo( Collections.singletonList( other ) ); 
					m.setSent( true );
					break;
			}
			m.setSentTime( new Date(smsDate) );
			m.setText(smsBody);
			messages.add( m );
		}
		
		return messages;
	}
		
	private List<ApiContact> getContactsFromMessages( List<ApiMessage> messages )
	{
		ArrayList<ApiContact> contacts = new ArrayList<ApiContact>();
		HashSet<String> addresses = new HashSet<String>();
		
		for( ApiMessage m : messages )
		{
			addresses.add(m.getFrom().getAddress());
			for( ApiContactInfo ref : m.getTo() ) addresses.add( ref.getAddress() );
		}

		for( String id : addresses ) contacts.add(getContactFromNumber(id));
		
		return contacts;
	}
	
	private ApiContact getContactFromNumber(String phoneNumber) 
	{
		if (phoneNumber == null ) return null; 
		ApiContact contact = new ApiContact();
		contact.setRef("contact:" + phoneNumber);
		// temp variables to hold the contact information
		String name = phoneNumber;
		String number = phoneNumber;
		if( phoneNumber.length() != 0 ) 
		{
			Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

			Cursor contactsCursor = getContentResolver().query(lookupUri, CONTACTS_QUERY,
																			null, null, null );
			if (contactsCursor.moveToFirst()) 
			{
				// we create a contact with the number and name as they are stored by the user
				name = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
				number = contactsCursor.getString(contactsCursor.getColumnIndexOrThrow(PhoneLookup.NUMBER));
			} 
			
			contactsCursor.close();
		} 
		else // no phone number provided 
		{
			name = "Unknown";
			number = "Unknown";
		}
		contact.setName( name );
		Map<String, List<String>> addressByType = new HashMap<String, List<String>>();
		addressByType.put("phone", Collections.singletonList(number));
		contact.setAddressByType(addressByType);
		return contact;
	}
}
