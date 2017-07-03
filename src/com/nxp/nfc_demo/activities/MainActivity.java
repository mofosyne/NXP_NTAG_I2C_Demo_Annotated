/*
****************************************************************************
* Copyright(c) 2014 NXP Semiconductors                                     *
* All rights are reserved.                                                 *
*                                                                          *
* Software that is described herein is for illustrative purposes only.     *
* This software is supplied "AS IS" without any warranties of any kind,    *
* and NXP Semiconductors disclaims any and all warranties, express or      *
* implied, including all implied warranties of merchantability,            *
* fitness for a particular purpose and non-infringement of intellectual    *
* property rights.  NXP Semiconductors assumes no responsibility           *
* or liability for the use of the software, conveys no license or          *
* rights under any patent, copyright, mask work right, or any other        *
* intellectual property rights in or to any products. NXP Semiconductors   *
* reserves the right to make changes in the software without notification. *
* NXP Semiconductors also makes no representation or warranty that such    *
* application will be suitable for the specified use without further       *
* testing or modification.                                                 *
*                                                                          *
* Permission to use, copy, modify, and distribute this software and its    *
* documentation is hereby granted, under NXP Semiconductors' relevant      *
* copyrights in the software, without fee, provided that it is used in     *
* conjunction with NXP Semiconductor products(UCODE I2C, NTAG I2C).        *
* This  copyright, permission, and disclaimer notice must appear in all    *
* copies of this code.                                                     *
****************************************************************************
*/
package com.nxp.nfc_demo.activities;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;

import com.nxp.nfc_demo.activities.AuthActivity.AuthStatus;
import com.nxp.nfc_demo.adapters.TabsAdapter;
import com.nxp.nfc_demo.fragments.ConfigFragment;
import com.nxp.nfc_demo.fragments.LedFragment;
import com.nxp.nfc_demo.fragments.NdefFragment;
import com.nxp.nfc_demo.fragments.SpeedTestFragment;
import com.nxp.nfc_demo.reader.Ntag_I2C_Demo;
import com.nxp.ntagi2cdemo.R;

public class MainActivity extends FragmentActivity {
	public final static String EXTRA_MESSAGE = "com.nxp.nfc_demo.MESSAGE";
	public final static int AUTH_REQUEST = 0;
	public static Ntag_I2C_Demo demo;
	private TabHost mTabHost;
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	private PendingIntent mPendingIntent;
	private NfcAdapter mAdapter;
	public static String PACKAGE_NAME;

	// Android app Version
	private static String appVersion = "";

	// Board firmware Version
	private static String boardFirmwareVersion = "";

	private static Intent mIntent;
	
	// Reference to menu item for icon changing
	private MenuItem mAuthMenuItem;
	
	// Current authentication state
	private static int mAuthStatus;
	
	// Current used password
	private static byte[] mPassword;

	public static Intent getmIntent() {
		return mIntent;
	}

	public static void setBoardFirmwareVersion(String boardFirmwareVersion) {
		MainActivity.boardFirmwareVersion = boardFirmwareVersion;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Application package name to be used by the AAR record
		PACKAGE_NAME = getApplicationContext().getPackageName();
		String languageToLoad = "en";
		Locale locale = new Locale(languageToLoad);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());
		setContentView(R.layout.activity_main);
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("leds").setIndicator(
						getString(R.string.leds)), LedFragment.class, null);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("ndef").setIndicator(
						getString(R.string.ndefs)), NdefFragment.class, null);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("ntag_rf").setIndicator(
						getString(R.string.ntag_rf_text)),
				SpeedTestFragment.class, null);
		mTabsAdapter.addTab(
				mTabHost.newTabSpec("config").setIndicator(
						getString(R.string.settings)), ConfigFragment.class,
				null);

		// set current Tag to the Speedtest, so it loads the values
		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}
		// Get App version
		appVersion = "";
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			appVersion = pInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		// Board firmware version
		boardFirmwareVersion = "Unknown";

		// Notifier to be used for the demo changing
		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				if (demo.isReady()) {
					demo.finishAllTasks();
					if (tabId.equalsIgnoreCase("leds") && demo.isConnected()) {
						launchDemo(tabId);
					}
				}
				mTabsAdapter.onTabChanged(tabId);
			}
		});
		// When we open the application by default we set the status to disabled (we don't know the product yet)
		mAuthStatus = AuthStatus.Disabled.getValue();

		// Initialize the demo in order to handle tab change events
		demo = new Ntag_I2C_Demo(null, this, null, 0);
		mAdapter = NfcAdapter.getDefaultAdapter(this);
		setNfcForeground();
		checkNFC();
	}

	@SuppressLint("InlinedApi")
	private void checkNFC() {
		if (mAdapter != null) {
			if (!mAdapter.isEnabled()) {
				new AlertDialog.Builder(this)
						.setTitle("NFC not enabled")
						.setMessage("Go to Settings?")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										 if (android.os.Build.VERSION.SDK_INT >= 16) {
											 startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
										 } else {
											 startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
										 }
									}
								})
						.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										System.exit(0);
									}
								}).show();
			}
		} else {
			new AlertDialog.Builder(this)
					.setTitle("No NFC available. App is going to be closed.")
					.setNeutralButton("Ok",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									System.exit(0);
								}
							}).show();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mAdapter != null) {
			mAdapter.disableForegroundDispatch(this);
		}
				
		if (demo.isReady()) {
			demo.finishAllTasks();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// Update the Auth Status Icon
		updateAuthIcon(mAuthStatus);
		
		if (mAdapter != null) {
			mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mAuthStatus = 0;
		mPassword = null;
		mIntent = null;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // Check which request we're responding to
		// Make sure the request was successful
		if (requestCode == AUTH_REQUEST
			&& resultCode == RESULT_OK
			&& demo != null
			&& demo.isReady()) {
			String currTab = mTabHost.getCurrentTabTag();
			launchDemo(currTab);
	    }
	}

	@Override
	protected void onNewIntent(Intent nfc_intent) {
		super.onNewIntent(nfc_intent);
		// Set the pattern for vibration
		long pattern[] = { 0, 100 };

		// Set the initial auth parameters
		mAuthStatus = AuthStatus.Disabled.getValue();
		mPassword = null;

		// Vibrate on new Intent
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(pattern, -1);
		doProcess(nfc_intent);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		
		// Get the reference to the menu item
		mAuthMenuItem = menu.findItem(R.id.action_auth);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_auth:
			showAuthDialog();
			return true;
		case R.id.action_flash:
			showFlashDialog();
			return true;
		case R.id.action_about:
			showAboutDialog();
			return true;
		case R.id.action_feedback:
			sendFeedback();
			return true;
		case R.id.action_help:
			showHlepDialog();
			return true;
		case R.id.action_debug:
			showDebugDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void doProcess(Intent nfc_intent) {
		mIntent = nfc_intent;
		Tag tag = nfc_intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		demo = new Ntag_I2C_Demo(tag, this, mPassword, mAuthStatus);
		if (demo.isReady()) {
			// Retrieve Auth Status before doing any operation
			mAuthStatus = obtainAuthStatus();
			String currTab = mTabHost.getCurrentTabTag();
			launchDemo(currTab);
		}
	}

	private void launchDemo(String currTab) {
		if(mAuthStatus == AuthStatus.Authenticated.getValue()) {
			demo.Auth(mPassword, AuthStatus.Protected_RW.getValue());
		}
		
		// ===========================================================================
		// LED Test
		// ===========================================================================
		if (currTab.equalsIgnoreCase("leds")) {
			// This demo is available even if the product is protected 
			// as long as the SRAM is unprotected  			
			if(mAuthStatus == AuthStatus.Disabled.getValue()
					|| mAuthStatus == AuthStatus.Unprotected.getValue()
					|| mAuthStatus == AuthStatus.Authenticated.getValue()
					|| mAuthStatus == AuthStatus.Protected_W.getValue()
					|| mAuthStatus == AuthStatus.Protected_RW.getValue() ) {
				try {
					// if (LedFragment.getChosen()) {
					demo.LED();
				} catch (Exception e) {
					e.printStackTrace();
					LedFragment.setAnswer(getString(R.string.Tag_lost));
				}
			} else {
				Toast.makeText(getApplicationContext(), "NTAG I2C Plus memory is protected", 
						Toast.LENGTH_LONG).show();
				showAuthDialog();
			}
		}
		// ===========================================================================
		// NDEF Demo
		// ===========================================================================
		if (currTab.equalsIgnoreCase("ndef")) {
			// This demo is only available when the tag is not protected  
			if(mAuthStatus == AuthStatus.Disabled.getValue()
					|| mAuthStatus == AuthStatus.Unprotected.getValue()
					|| mAuthStatus == AuthStatus.Authenticated.getValue()) {
				NdefFragment.setAnswer("Tag detected");
				try {
					demo.NDEF();
				} catch (Exception e) {
					// NdefFragment.setAnswer(getString(R.string.Tag_lost));
				}
			} else {
				Toast.makeText(getApplicationContext(), "NTAG I2C Plus memory is protected", 
						Toast.LENGTH_LONG).show();
				showAuthDialog();
			}
		}
		// ===========================================================================
		// Config
		// ===========================================================================
		if (currTab.equalsIgnoreCase("config")) {
		}
		// ===========================================================================
		// Speedtest
		// ===========================================================================
		if (currTab.equalsIgnoreCase("ntag_rf")) {
			try {
				// SRAM Test
				if ((SpeedTestFragment.isSRamEnabled() == true)) {
					// This demo is available even if the product is protected 
					// as long as the SRAM is unprotected  	
					if(mAuthStatus == AuthStatus.Disabled.getValue()
							|| mAuthStatus == AuthStatus.Unprotected.getValue()
							|| mAuthStatus == AuthStatus.Authenticated.getValue()
							|| mAuthStatus == AuthStatus.Protected_W.getValue()
							|| mAuthStatus == AuthStatus.Protected_RW.getValue()) {
						demo.SRAMSpeedtest();
					} else {
						Toast.makeText(getApplicationContext(), "NTAG I2C Plus memory is protected", 
								Toast.LENGTH_LONG).show();
						showAuthDialog();
					}
				}
				// EEPROM Test
				if ((SpeedTestFragment.isSRamEnabled() == false)) {
					// This demo is only available when the tag is not protected  
					if(mAuthStatus == AuthStatus.Disabled.getValue()
							|| mAuthStatus == AuthStatus.Unprotected.getValue()
							|| mAuthStatus == AuthStatus.Authenticated.getValue()) {
						demo.EEPROMSpeedtest();
					} else {
						Toast.makeText(getApplicationContext(), "NTAG I2C Plus memory is protected", 
								Toast.LENGTH_LONG).show();
						showAuthDialog();
					}
				} // end if eeprom test
			} catch (Exception e) {
				SpeedTestFragment.setAnswer(getString(R.string.Tag_lost));
				e.printStackTrace();
			}
		}		
	}
	
	private int obtainAuthStatus() {
		mAuthStatus = demo.ObtainAuthStatus();

		// Update the Auth Status Icon
		updateAuthIcon(mAuthStatus);
		return mAuthStatus;
	}

	/**
	 * NDEF Demo execution is launched from its fragmend.
	 */
	public static void launchNdefDemo(int auth, byte[] pwd) {
		if (demo.isReady()) {
			if (demo.isConnected()) {
				if(auth == AuthStatus.Authenticated.getValue()) {
					demo.Auth(pwd, AuthStatus.Protected_RW.getValue());
				}
				NdefFragment.setAnswer("Tag detected");
				try {
					demo.NDEF();
				} catch (Exception e) {
					NdefFragment.setAnswer("Error: Tag lost, try again");
					e.printStackTrace();
				}
			} else {
				if(NdefFragment.isWriteChosen()) {
					NdefFragment.setAnswer("Tap tag to write NDEF content");
				} else {
					NdefFragment.setAnswer("Tap tag to read NDEF content");
				}
			}
		}
	}

	public void sendFeedback() {
		Intent intent = new Intent(Intent.ACTION_SENDTO);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT,
				this.getString(R.string.email_titel_feedback));
		intent.putExtra(Intent.EXTRA_TEXT, "Android Version: "
				+ android.os.Build.VERSION.RELEASE + "\nManufacurer: "
				+ android.os.Build.MANUFACTURER + "\nModel: "
				+ android.os.Build.MODEL + "\nBrand: " + android.os.Build.BRAND
				+ "\nDisplay: " + android.os.Build.DISPLAY + "\nProduct: "
				+ android.os.Build.PRODUCT + "\nIncremental: "
				+ android.os.Build.VERSION.INCREMENTAL);
		intent.setData(Uri.parse(this.getString(R.string.support_email)));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivity(intent);
	}

	public void showHlepDialog() {
		Intent intent = null;
		intent = new Intent(this, HelpActivity.class);
		startActivity(intent);
	}

	public void showDebugDialog() {
		Intent intent = null;
		intent = new Intent(this, DebugActivity.class);
		startActivity(intent);
	}

	public void showAboutDialog() {
		Intent intent = null;
		intent = new Intent(this, VersionInfoActivity.class);
		if(MainActivity.mIntent != null)
			intent.putExtras(MainActivity.mIntent);

		startActivity(intent);
	}
	
	public void showFlashDialog() {
		Intent intent = null;
		intent = new Intent(this, FlashMemoryActivity.class);
		startActivity(intent);
	}
	
	public void showAuthDialog() {
		if(mAuthStatus != AuthStatus.Disabled.getValue()) {
			Intent intent = null;
			intent = new Intent(this, AuthActivity.class);
			intent.putExtras(MainActivity.mIntent);
			startActivityForResult(intent, AUTH_REQUEST);
		} else {
			Toast.makeText(getApplicationContext(), "You need to tap a NTAG I2C "
							+ "Plus product to access authentication features",
					Toast.LENGTH_LONG).show();
		}
	}

	public void setNfcForeground() {
		// Create a generic PendingIntent that will be delivered to this
		// activity. The NFC stack will fill
		// in the intent with the details of the discovered tag before
		// delivering it to this activity.
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(
				getApplicationContext(), getClass())
				.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
	}
	
	/**
	 * Set the Icon that informs the user about the protection status.
	 */
	public void updateAuthIcon(int status) {
		if(mAuthMenuItem != null) {
			if(status == AuthStatus.Disabled.getValue()) {
				mAuthMenuItem.setIcon(getResources().getDrawable(R.drawable.disabled));
			} else if(status == AuthStatus.Unprotected.getValue()) {
				mAuthMenuItem.setIcon(getResources().getDrawable(R.drawable.unlock));
			} else if(status == AuthStatus.Authenticated.getValue()) {
				mAuthMenuItem.setIcon(getResources().getDrawable(R.drawable.authenticated));
			} else if(status == AuthStatus.Protected_RW.getValue()
					|| status == AuthStatus.Protected_W.getValue()
					|| status == AuthStatus.Protected_RW_SRAM.getValue()
					|| status == AuthStatus.Protected_W_SRAM.getValue()) {
				mAuthMenuItem.setIcon(getResources().getDrawable(R.drawable.lock));
			}
		}
	}
	
	// ===========================================================================
	// NTAG I2C Plus getters and setters
	// ===========================================================================
	public static int getAuthStatus() {
		return mAuthStatus;
	}
	
	public static byte[] getPassword() {
		return mPassword;
	}
	
	public static Intent getNfcIntent() {
		return mIntent;
	}
	
	public static void setAuthStatus(int status) {
		mAuthStatus = status;
	}
	
	public static void setPassword(byte[] pwd) {
		mPassword = pwd;
	}
	
	public static void setNfcIntent(Intent intent) {
		mIntent = intent;
	}
}
