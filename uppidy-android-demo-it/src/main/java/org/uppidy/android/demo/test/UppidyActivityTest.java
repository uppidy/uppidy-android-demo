package org.uppidy.android.demo.test;

import android.test.ActivityInstrumentationTestCase2;

import com.uppidy.android.demo.app.UppidyActivity;

public class UppidyActivityTest extends ActivityInstrumentationTestCase2<UppidyActivity> {

    public UppidyActivityTest() {
        super("org.uppidy.android.demo.app", UppidyActivity.class);
    }

    public void testActivity() {
    	UppidyActivity activity = getActivity();
        assertNotNull(activity);
    }
}

