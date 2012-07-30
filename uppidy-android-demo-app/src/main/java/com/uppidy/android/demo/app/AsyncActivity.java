package com.uppidy.android.demo.app;


/**
 * @author arudnev@uppidy.com
 */
public interface AsyncActivity {
	
	public MainApplication getApplicationContext();

    public void showLoadingProgressDialog();

    public void showProgressDialog(CharSequence message);

    public void dismissProgressDialog();

}
