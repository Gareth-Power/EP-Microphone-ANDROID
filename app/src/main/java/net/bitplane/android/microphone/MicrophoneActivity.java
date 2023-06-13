package net.bitplane.android.microphone;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.view.MotionEvent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static android.Manifest.permission.RECORD_AUDIO;



public class MicrophoneActivity extends Activity implements OnSharedPreferenceChangeListener, OnTouchListener {

	private static final String APP_TAG = "Microphone";
	private static final int ABOUT_DIALOG_ID = 0;

	private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 1;

	SharedPreferences mSharedPreferences;
	int mActive = 0; // 0=Mic Off , 1=left, 2=right, 3=both
	//boolean           mLeftActive = false;
	//boolean           mRightActive = false;

	/**
	 * Called when the activity is first created.
	 */



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
				!= PackageManager.PERMISSION_GRANTED) {
			// Permission is not granted, request it
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.RECORD_AUDIO},
					RECORD_AUDIO_PERMISSION_REQUEST_CODE);
		} else {
			// Permission is already granted, continue with your logic
		}

		Log.d(APP_TAG, "Opening mic activity");

		// listen for preference changes
		mSharedPreferences = getSharedPreferences(APP_TAG, MODE_PRIVATE);
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

		// listen for preference changes
		mSharedPreferences = getSharedPreferences(APP_TAG, MODE_PRIVATE);
		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

		mActive = mSharedPreferences.getInt("active", 0);
		if (mActive == 1 || mActive == 2 || mActive == 3) {
			startService(new Intent(this, MicrophoneService.class));
		}


		setContentView(R.layout.main);

		ImageButton b = (ImageButton) findViewById(R.id.RecordButton);
		b.setOnTouchListener(this);
		b.setImageBitmap(BitmapFactory.decodeResource(getResources(), (mActive == 1 || mActive == 3) ? R.drawable.red : R.drawable.mic));

		ImageButton m = (ImageButton) findViewById(R.id.RecordButtonRight);
		m.setOnTouchListener(this);
		m.setImageBitmap(BitmapFactory.decodeResource(getResources(), (mActive == 2 || mActive == 3) ? R.drawable.red : R.drawable.mic));

		int lastVersion = mSharedPreferences.getInt("lastVersion", 0);
		int thisVersion = -1;
		try {
			thisVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		if (lastVersion != thisVersion) {
			SharedPreferences.Editor e = mSharedPreferences.edit();
			e.putInt("lastVersion", thisVersion);
			e.commit();
			showDialog(ABOUT_DIALOG_ID);
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d(APP_TAG, "Closing mic activity");

		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.about:
				showDialog(ABOUT_DIALOG_ID);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
			case ABOUT_DIALOG_ID:
				Builder b = new AlertDialog.Builder(this);
				b.setTitle(getString(R.string.about));

				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				View aboutView = inflater.inflate(R.layout.about, (ViewGroup) findViewById(R.id.AboutWebView));

				b.setView(aboutView);

				String data = "";

				InputStream in = getApplicationContext().getResources().openRawResource(R.raw.about);
				try {
					int ch;
					StringBuffer buf = new StringBuffer();
					while ((ch = in.read()) != -1) {
						buf.append((char) ch);
					}
					data = buf.toString();
				} catch (IOException e) {
					// this is fucking silly. do something nicer than this shit method
				}

				WebView wv = (WebView) aboutView.findViewById(R.id.AboutWebView);
				wv.loadDataWithBaseURL(null, data, "text/html", "UTF-8", null);

				dialog = b.create();

				break;
		}
		return dialog;
	}





	public boolean onTouch (View v, MotionEvent mEvent){
		switch (v.getId()) {
			case R.id.RecordButton  :

				if(mEvent.getAction() == MotionEvent.ACTION_DOWN){
					SharedPreferences.Editor mActive = mSharedPreferences.edit();
					mActive.putInt("active", 1);
					mActive.commit();
				}
				if(mEvent.getAction() == MotionEvent.ACTION_UP){
					SharedPreferences.Editor mActive = mSharedPreferences.edit();
					mActive.putInt("active", 0);
					mActive.commit();
				}


				break;

			case R.id.RecordButtonRight:
				// 0=Mic Off , 1=left, 2=right, 3=both

				if(mEvent.getAction() == MotionEvent.ACTION_DOWN){
					SharedPreferences.Editor f = mSharedPreferences.edit();
					f.putInt("active", 2);
					f.commit();
				}
				if(mEvent.getAction() == MotionEvent.ACTION_UP){
					SharedPreferences.Editor f = mSharedPreferences.edit();
					f.putInt("active", 0);
					f.commit();
				}

				break;
		}
		return true;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// intercept the preference change.
		if (key.equals("active")) {
			int bActive = sharedPreferences.getInt("active", 0);
			if (bActive == 1 || bActive == 2 || bActive == 3) {
				startService(new Intent(this, MicrophoneService.class));
			}
			mActive = bActive;
			runOnUiThread(new Runnable() {
				public void run() {
					ImageButton b = (ImageButton) findViewById(R.id.RecordButton);
					b.setImageBitmap(BitmapFactory.decodeResource(getResources(), (mActive == 1 || mActive == 3) ? R.drawable.red : R.drawable.mic));
					ImageButton m = (ImageButton)findViewById(R.id.RecordButtonRight);
					m.setImageBitmap(BitmapFactory.decodeResource(getResources(), (mActive == 2 || mActive == 3) ? R.drawable.red : R.drawable.mic));
				}
			});
		}
	}

	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// Microphone permission is granted, continue with your logic
			} else {
				// Microphone permission is denied, handle accordingly (e.g., show a message or disable the microphone functionality)
			}
		}
	}

}