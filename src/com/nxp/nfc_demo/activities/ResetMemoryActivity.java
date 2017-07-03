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

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nxp.nfc_demo.activities.AuthActivity.AuthStatus;
import com.nxp.nfc_demo.reader.Ntag_I2C_Demo;
import com.nxp.ntagi2cdemo.R;

public class ResetMemoryActivity extends Activity {
	private PendingIntent pendingIntent;
	private NfcAdapter mAdapter;
	private Ntag_I2C_Demo demo;
    private ProgressDialog dialog;
	public static Context mContext;
	public static TextView dataRate_callback;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_resetmemory);
		
		// Get the context reference
		mContext = getApplicationContext();
		dataRate_callback = (TextView) findViewById(R.id.resetmemorydata_datarateCallback);
	
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
            new resetTask().execute();
	    }
	}
	
	protected void onNewIntent(Intent nfcIntent) {
		// Set the initial auth parameters
		MainActivity.setAuthStatus(AuthStatus.Disabled.getValue());
		MainActivity.setPassword(null);
		
		// Store the intent information
		MainActivity.setNfcIntent(nfcIntent);
		
		// Complete the task in a new thread in order to be able to show the dialog
		Tag tag = nfcIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		startDemo(tag, true);
	}

	private void startDemo(Tag tag, boolean getAuthStatus) {
		demo = new Ntag_I2C_Demo(tag, this, MainActivity.getPassword(),
                MainActivity.getAuthStatus());
		if(!demo.isReady()) {
            return;
        }
		
		// Retrieve the Auth Status
		if(getAuthStatus) {
            MainActivity.setAuthStatus(demo.ObtainAuthStatus());
        }
		
		// Demo is only available when the tag is not protected
		if(MainActivity.getAuthStatus() == AuthStatus.Disabled.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Unprotected.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Authenticated.getValue()) {
			// Launch the thread
			new resetTask().execute();
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
	
	public void setDataRate(int bytes, long time) {
		if(bytes > 0) {
			String readTimeMessage = "";
			
			// Transmission Results
			readTimeMessage = readTimeMessage.concat("NTAG Memory reset\n");
			readTimeMessage = readTimeMessage.concat("Speed (" + bytes + " Byte / "
					+ time + " ms): "
					+ String.format("%.0f", bytes / (time / 1000.0))
					+ " Bytes/s");
			
			// Make the board input layout visible
			((LinearLayout) findViewById(R.id.layoutResetMemoryStatistics))
                    .setVisibility(View.VISIBLE);
			dataRate_callback.setText(readTimeMessage);
		} else {
			// Make the board input layout visible
			((LinearLayout) findViewById(R.id.layoutResetMemoryStatistics))
                    .setVisibility(View.GONE);
			dataRate_callback.setText("");
		}
			
	}
	
	private class resetTask extends AsyncTask<Intent, Integer, Integer> {
		private long timeToResetMemory = 0;
		
        @Override
		protected void onPostExecute(Integer bytes) {  
        	// Inform the user about the task completion
        	contentReseted(bytes);
        	setDataRate(bytes, timeToResetMemory);
        	
    		// Action completed
    		dialog.dismiss();
        }

		@Override
		protected Integer doInBackground(Intent... nfc_intent) {    
			long regTimeOutStart = System.currentTimeMillis();
			
			// Reset the tag content
			int bytes = 0;
			
			// Reset Tag demo will return the number of bytes written
			bytes = demo.resetTagMemory(); 

			// Memory erase time statistics
			timeToResetMemory = System.currentTimeMillis() - regTimeOutStart;
        	return bytes; 
		}
		
		@Override
		protected void onPreExecute() {
			// Show the progress dialog on the screen to inform about the action
			dialog = ProgressDialog.show(ResetMemoryActivity.this, "Formatting",
                    "Reseting memory content ...", true, true);
		}
    }
	
	public void contentReseted(int bytes) {
		if(bytes > 0) {
            Toast.makeText(mContext, "Reset Completed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, "Error during memory content reset",
                    Toast.LENGTH_SHORT).show();
        }
	}
	
	public void showAboutDialog() {
		Intent intent = null;
		intent= new Intent(this, VersionInfoActivity.class);		
		startActivity(intent);
	}
}
