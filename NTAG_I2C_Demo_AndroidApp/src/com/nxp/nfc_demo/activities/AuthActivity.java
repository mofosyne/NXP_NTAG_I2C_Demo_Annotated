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
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nxp.nfc_demo.reader.Ntag_I2C_Demo;
import com.nxp.ntagi2cdemo.R;

public class AuthActivity extends Activity implements OnClickListener{

	public static final int REQUEST_FILE_CHOOSER = 0;
	private PendingIntent pendingIntent;
	private NfcAdapter mAdapter;
	public static Context mContext;
	private TextView statusText;
	private Button passwd1Button;
	private Button passwd2Button;
	private Button passwd3Button;
	
	private static authTask task; 
	private static Tag mTag;
	
	public enum AuthStatus {
		Disabled(0),
        Unprotected(1),
        Authenticated(2),
        Protected_W(3),
        Protected_RW(4),
        Protected_W_SRAM(5),
        Protected_RW_SRAM(6);
		private int status;
		private AuthStatus(int status) {
			this.status = status;
		}
		public int getValue() {
			return status;
		}
	}
	
	public enum Pwds {
		PWD1(new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}),
		PWD2(new byte[] {(byte) 0x55, (byte) 0x55, (byte) 0x55, (byte) 0x55}),
		PWD3(new byte[] {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA});
		private byte[] pwd;
		private Pwds(byte[] value) {
			this.pwd = value;
		}
		public byte[] getValue() {
			return pwd;
		}
	}
	
	private Ntag_I2C_Demo demo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth);

		// Capture intent to check whether the operation should be automatically launch or not
		mTag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if(mTag != null && Ntag_I2C_Demo.isTagPresent(mTag)) {
			demo = new Ntag_I2C_Demo(mTag, this, MainActivity.getPassword(), MainActivity.getAuthStatus());
		}

		// Get the context reference
		mContext = getApplicationContext();

		// Add Foreground dispatcher
		mAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,	getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		statusText = (TextView) findViewById(R.id.auth_status);

		passwd1Button = (Button) findViewById(R.id.authPWD1);
		passwd2Button = (Button) findViewById(R.id.authPWD2);
		passwd3Button = (Button) findViewById(R.id.authPWD3);
		passwd1Button.setOnClickListener(this);
		passwd2Button.setOnClickListener(this);
		passwd3Button.setOnClickListener(this);
		
		// Set the Auth Status on the screen
		updateAuthStatus(MainActivity.getAuthStatus());
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.authPWD1:
			passwd1Button.setBackgroundResource(R.drawable.btn_blue);
			passwd2Button.setBackgroundColor(Color.BLACK);
			passwd3Button.setBackgroundColor(Color.BLACK);
			
			// Store the password for the auth
			MainActivity.setPassword(Pwds.PWD1.getValue());

			break;
		case R.id.authPWD2:
			passwd2Button.setBackgroundResource(R.drawable.btn_blue);
			passwd1Button.setBackgroundColor(Color.BLACK);
			passwd3Button.setBackgroundColor(Color.BLACK);
			
			// Store the password for the auth
			MainActivity.setPassword(Pwds.PWD2.getValue());
			
			break;
		case R.id.authPWD3:
			passwd3Button.setBackgroundResource(R.drawable.btn_blue);
			passwd1Button.setBackgroundColor(Color.BLACK);
			passwd2Button.setBackgroundColor(Color.BLACK);
			
			// Store the password for the auth
			MainActivity.setPassword(Pwds.PWD3.getValue());
			break;
		default:
			break;
		}
		startDemo(mTag);
	}	
	
	protected void onNewIntent(Intent nfc_intent) {
		super.onNewIntent(nfc_intent);
		// Set the pattern for vibration
		long pattern[] = { 0, 100 };

		// Vibrate on new Intent
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(pattern, -1);

		// Get the tag and start the demo
		Tag tag = nfc_intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		demo = new Ntag_I2C_Demo(tag, this, MainActivity.getPassword(), MainActivity.getAuthStatus());
		MainActivity.setAuthStatus(demo.ObtainAuthStatus());
		
		// This authentication is added in order to avoid authentication problems with old NFC Controllers
		if(MainActivity.getAuthStatus() == AuthStatus.Authenticated.getValue()) {
            demo.Auth(MainActivity.getPassword(), AuthStatus.Protected_RW.getValue());
        }
		
		// Set the Auth Status on the screen
		updateAuthStatus(MainActivity.getAuthStatus());
	}
	
	private void startDemo(Tag tag) {
		// This authentication is added in order to avoid authentication problems with old NFC Controllers
		if(MainActivity.getAuthStatus() == AuthStatus.Authenticated.getValue()) {
            demo.Auth(MainActivity.getPassword(), AuthStatus.Protected_RW.getValue());
        }
		if(demo != null && demo.isReady()) {
            // Launch the thread
            task = new authTask();
            task.execute();
		}
	}
	
	private class authTask extends AsyncTask<Intent, Integer, Boolean> {
		public ProgressDialog dialog;
		
		@Override
		protected void onPostExecute(Boolean success) {
			// Inform the user about the task completion
			authCompleted(success);
			
			// Action completed
			dialog.dismiss();
		}

		@Override
		protected Boolean doInBackground(Intent... nfc_intent) {
			// Perform auth operation based on the actual status
			boolean success = demo.Auth(MainActivity.getPassword(), MainActivity.getAuthStatus());
			return success;
		}

		@Override
		protected void onPreExecute() {
			// Show the progress dialog on the screen to inform about the action
			dialog = ProgressDialog.show(AuthActivity.this, "Authenticating",
					"Authenticating against NTAG I2C Plus ...", true, true);
		}
	}

	public void authCompleted(boolean success) {
		if (success) {
			// Update the status
			if(MainActivity.getAuthStatus() == AuthStatus.Unprotected.getValue()) {
				MainActivity.setAuthStatus(AuthStatus.Authenticated.getValue());
				
				Toast.makeText(mContext, "Tag Successfully protected", Toast.LENGTH_SHORT)
					.show();
				
				// Authenticate in order to let the user use the demos
				demo.Auth(MainActivity.getPassword(), AuthStatus.Protected_RW.getValue());
			} else if(MainActivity.getAuthStatus() == AuthStatus.Authenticated.getValue()) {
				MainActivity.setAuthStatus(AuthStatus.Unprotected.getValue());
				Toast.makeText(mContext, "Tag Successfully unprotected", Toast.LENGTH_SHORT)
					.show();
			} else if(MainActivity.getAuthStatus() == AuthStatus.Protected_RW.getValue()
					|| MainActivity.getAuthStatus() == AuthStatus.Protected_W.getValue()
					|| MainActivity.getAuthStatus() == AuthStatus.Protected_RW_SRAM.getValue()
					|| MainActivity.getAuthStatus() == AuthStatus.Protected_W_SRAM.getValue()) {
				MainActivity.setAuthStatus(AuthStatus.Authenticated.getValue());
				Toast.makeText(mContext, "Successful authentication", Toast.LENGTH_SHORT)
					.show();
			}
			updateAuthStatus(MainActivity.getAuthStatus());
			
			// Prepare the result intent for the MainActivity
			Intent resultIntent = new Intent();
			setResult(Activity.RESULT_OK, resultIntent);
			finish();
		} else {
			if(MainActivity.getAuthStatus() == AuthStatus.Unprotected.getValue()) {
				Toast.makeText(mContext, "Error protecting tag",
						Toast.LENGTH_SHORT).show();
			} else if(MainActivity.getAuthStatus() == AuthStatus.Authenticated.getValue()) {
				Toast.makeText(mContext, "Error unprotecting tag",
						Toast.LENGTH_SHORT).show();
			} else if(MainActivity.getAuthStatus() == AuthStatus.Protected_RW.getValue()
					|| MainActivity.getAuthStatus() == AuthStatus.Protected_W.getValue()
					|| MainActivity.getAuthStatus() == AuthStatus.Protected_RW_SRAM.getValue()
					|| MainActivity.getAuthStatus() == AuthStatus.Protected_W_SRAM.getValue()) {
				Toast.makeText(mContext, "Password was not correct, please try again",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	public void updateAuthStatus(int status) {
		if(MainActivity.getAuthStatus() == AuthStatus.Disabled.getValue()) {
			statusText.setText(getResources().getString(R.string.Auth_disabled));
		} else if(MainActivity.getAuthStatus() == AuthStatus.Unprotected.getValue()) {
			statusText.setText(getResources().getString(R.string.Auth_unprotected));
		} else if(MainActivity.getAuthStatus() == AuthStatus.Authenticated.getValue()) {
			statusText.setText(getResources().getString(R.string.Auth_authenticated));
		} else if(MainActivity.getAuthStatus() == AuthStatus.Protected_RW.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Protected_W.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Protected_RW_SRAM.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Protected_W_SRAM.getValue()) {
			statusText.setText(getResources().getString(R.string.Auth_protected));
		}	
	}
	
	public void showDisableAuthenticationDialog() {
		// Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(AuthActivity.this);
        builder.setTitle(getResources().getString(R.string.Dialog_disable_auth_title));
        builder.setMessage(getResources().getString(R.string.Dialog_disable_auth_msg));
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int index) {
        	   // Unprotect tag
        	   startDemo(mTag);
        	   
               dialog.dismiss();
           }
       });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	// We are done with this view
            	finish();
            	
                dialog.dismiss();
            }
        });
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();
	}
}
