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

import java.io.IOException;
import java.util.Locale;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nxp.nfc_demo.activities.AuthActivity.AuthStatus;
import com.nxp.nfc_demo.reader.Ntag_Get_Version.Prod;
import com.nxp.nfc_demo.reader.Ntag_I2C_Demo;
import com.nxp.ntagi2cdemo.R;

public class ReadMemoryActivity extends Activity {
	private PendingIntent pendingIntent;
	private NfcAdapter mAdapter;
	private static Context mContext;
	private Ntag_I2C_Demo demo;
	public static LinearLayout ll;
	public static TextView datarateCallback;
	
	ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_readmemory);

		// Get the context reference
		mContext = getApplicationContext();
		ll = (LinearLayout) findViewById(R.id.layoutPages);
		datarateCallback = (TextView) findViewById(R.id.readmemorydata_datarateCallback);
			
		// Capture intent to check whether the operation should be automatically launch or not
		Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if(tag != null && Ntag_I2C_Demo.isTagPresent(tag)) {
			startDemo(tag, false);
		}

		// Add Foreground dispatcher
		mAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		return; // end onCreate
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mAdapter != null) {
			mAdapter.disableForegroundDispatch(this);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mAdapter != null) {
			mAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // Check which request we're responding to
	        // Make sure the request was successful
	        if (requestCode == MainActivity.AUTH_REQUEST
			 && resultCode == RESULT_OK
			 && demo != null
			 && demo.isReady()) {
				// Launch the thread
				new readTask().execute();
	    }
	}

	protected void onNewIntent(Intent nfcIntent) {
		// Set the initial auth parameters
		MainActivity.setAuthStatus(AuthStatus.Disabled.getValue());
		MainActivity.setPassword(null);
		
		// Store the intent information
		MainActivity.setNfcIntent(nfcIntent);
		
		Tag tag = nfcIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		startDemo(tag, true);
	}

	private void startDemo(Tag tag, boolean getAuthStatus) {
		// Complete the task in a new thread in order to be able to show the dialog
		demo = new Ntag_I2C_Demo(tag, this, MainActivity.getPassword(), MainActivity.getAuthStatus());
		if(!demo.isReady()) {
			return;
		}
		
		// Retrieve the Auth Status
		if(getAuthStatus) {
			MainActivity.setAuthStatus(demo.ObtainAuthStatus());
		}
		
		if(MainActivity.getAuthStatus() == AuthStatus.Disabled.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Unprotected.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Authenticated.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Protected_W.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Protected_W_SRAM.getValue()) {
			// Launch the thread
			new readTask().execute();
		} else {
			showAuthDialog();
		}
	}
	
	public void showAuthDialog() {
		Intent intent = null;
		intent = new Intent(this, AuthActivity.class);
		intent.putExtras(MainActivity.getNfcIntent());
		startActivityForResult(intent, MainActivity.AUTH_REQUEST);
	}

	public void setContent(byte[] b) {
		// Check if the data has successfully been read
		if (b == null) {
			Toast.makeText(mContext, "Error reading the memory content",
					Toast.LENGTH_LONG).show();
			
			// Clean the GUI
			((ImageView) findViewById(R.id.imageTap)).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.textTap)).setVisibility(View.VISIBLE);

			((LinearLayout) findViewById(R.id.layoutPages)).setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.layoutReadMemoryStatistics)).setVisibility(View.GONE);
		} else {
			// Clean the GUI
			((ImageView) findViewById(R.id.imageTap)).setVisibility(View.GONE);
			((TextView) findViewById(R.id.textTap)).setVisibility(View.GONE);

			((LinearLayout) findViewById(R.id.layoutPages)).setVisibility(View.VISIBLE);
			((LinearLayout) findViewById(R.id.layoutReadMemoryStatistics)).setVisibility(View.VISIBLE);

			ll.removeAllViews();
			int div = 1;
			for (int i = 0, j = 0; i < b.length; i = i + 4, j++) {
				String sPage = "";
				try {
					if (demo.getProduct() == Prod.NTAG_I2C_2k_Plus) {
						if(j / div > 0xE1 && j / div < 0x100) {
							// Make sure we don't lose data
							i = i - 4;
							continue;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				// Page Number
				sPage = sPage
						.concat("["
								+ "000".substring(Integer.toHexString(j / div)
										.length())
								+ Integer.toHexString(j / div).toUpperCase(
										Locale.getDefault()) + "]  ");

				// Hexadecimal values
				sPage = sPage.concat("00".substring(Integer.toHexString(
						b[i] & 0xFF).length())
						+ Integer.toHexString(b[i] & 0xFF).toUpperCase(
								Locale.getDefault())
						+ ":"
						+ "00".substring(Integer.toHexString(b[i + 1] & 0xFF)
								.length())
						+ Integer.toHexString(b[i + 1] & 0xFF).toUpperCase(
								Locale.getDefault())
						+ ":"
						+ "00".substring(Integer.toHexString(b[i + 2] & 0xFF)
								.length())
						+ Integer.toHexString(b[i + 2] & 0xFF).toUpperCase(
								Locale.getDefault())
						+ ":"
						+ "00".substring(Integer.toHexString(b[i + 3] & 0xFF)
								.length())
						+ Integer.toHexString(b[i + 3] & 0xFF).toUpperCase(
								Locale.getDefault()) + " ");

				// ASCII values
				if (j > 3) {
					byte[] tempAsc = new byte[4];

					// Only printable characters are displayed
					for (int k = 0; k < 4; k++) {
						if (b[i + k] < 0x20 || b[i + k] > 0x7D)
							tempAsc[k] = '.';
						else
							tempAsc[k] = b[i + k];
					}
					sPage = sPage.concat("|" + new String(tempAsc) + "|");
				}
				TextView tPage = new TextView(mContext);
				tPage.setLayoutParams(new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				tPage.setText(sPage);
				tPage.setTextColor(Color.BLACK);
				tPage.setTextSize(14);
				tPage.setTypeface(Typeface.MONOSPACE);
				ll.addView(tPage);
			}
		}
	}
	
	public void setDataRate(byte[] b, long time) {
		if (b != null) {
			String readTimeMessage = "";
			
			// Transmission Results
			readTimeMessage = readTimeMessage.concat("NTAG Memory read\n");
			readTimeMessage = readTimeMessage.concat("Speed (" + b.length + " Byte / "
					+ time + " ms): "
					+ String.format("%.0f", b.length / (time / 1000.0))
					+ " Bytes/s");
			datarateCallback.setText(readTimeMessage);
		}
	}

	private class readTask extends AsyncTask<Intent, Integer, byte[]> {
		private long timeToReadMemory = 0;
		
		@Override
		protected void onPostExecute(byte[] bytes) {
			// Action completed
			dialog.dismiss();
			setContent(bytes);
			setDataRate(bytes, timeToReadMemory);
		}

		@Override
		protected byte[] doInBackground(Intent... nfcIntent) {
			long regTimeOutStart = System.currentTimeMillis();
			
			// Read content and print it on the screen
			byte[] response = demo.readTagContent();

			// NDEF Reading time statistics
			timeToReadMemory = System.currentTimeMillis() - regTimeOutStart;

			// Get the tag
			return response;
		}

		@Override
		protected void onPreExecute() {
			// Show the progress dialog on the screen to inform about the action
			dialog = ProgressDialog.show(ReadMemoryActivity.this, "Reading",
					"Reading memory content ...", true, true);
		}
	}

	protected void showAboutDialog() {
		Intent intent = null;
		intent = new Intent(this, VersionInfoActivity.class);
		startActivity(intent);
	}
}
