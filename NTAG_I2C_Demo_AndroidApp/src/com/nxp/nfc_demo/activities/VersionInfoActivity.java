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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nxp.nfc_demo.activities.AuthActivity.AuthStatus;
import com.nxp.nfc_demo.exceptions.CommandNotSupportedException;
import com.nxp.nfc_demo.reader.Ntag_I2C_Demo;
import com.nxp.ntagi2cdemo.R;

public class VersionInfoActivity extends Activity {
	private PendingIntent pendingIntent;
	private NfcAdapter mAdapter;
	private static TextView Board_Version_text;
	private static TextView boardFwVersionText;
	private LinearLayout versionInformation;
	private RelativeLayout versionInformationInfo;
	private ImageView imageVersion;
	private LinearLayout layoutRead;
	private ScrollView sconfVer;
	private boolean VersionInfoExpanded = false;
	private Ntag_I2C_Demo demo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_versioninfo);
		
		layoutRead = (LinearLayout) findViewById(R.id.lconf_ver);
		sconfVer = (ScrollView) findViewById(R.id.sconf_ver);
		Board_Version_text = (TextView) findViewById(R.id.Board_Version_text);
		boardFwVersionText = (TextView) findViewById(R.id.Board_FW_Version_text);
		versionInformation = (LinearLayout) findViewById(R.id.Version_Information);
		versionInformationInfo = (RelativeLayout) findViewById(R.id.Version_Information_info);
		versionInformation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (VersionInfoExpanded) {
					imageVersion.setImageResource(R.drawable.expand);
					versionInformationInfo.setVisibility(View.GONE);
					VersionInfoExpanded = false;
				} else {
					imageVersion.setImageResource(R.drawable.hide);
					versionInformationInfo.setVisibility(View.VISIBLE);
					VersionInfoExpanded = true;
				}
			}
		});
		imageVersion = (ImageView) findViewById(R.id.imageVersion);
		
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
	public void onBackPressed() {
		Intent output = new Intent();
		setResult(RESULT_OK, output);
		finish();
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
            try {
                demo.setBoardVersion();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            } catch (CommandNotSupportedException e) {
                new AlertDialog.Builder(this)
                .setMessage(
                        "VersionInfo not supported")
                .setTitle("Tag not supported")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {
                            }
                        }).show();
                return;
            }
            // Make visible the registers scrollview
            layoutRead.setVisibility(View.GONE);
            sconfVer.setVisibility(View.VISIBLE);
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
		if(!demo.isReady())
			return;
		
		// Retrieve the Auth Status
		if(getAuthStatus == true) {
            MainActivity.setAuthStatus(demo.ObtainAuthStatus());
        }
		
		if(MainActivity.getAuthStatus() == AuthStatus.Disabled.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Unprotected.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Authenticated.getValue()) {
			// Launch the thread
			try {
				demo.setBoardVersion();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (FormatException e) {
				e.printStackTrace();
			} catch (CommandNotSupportedException e) {
				new AlertDialog.Builder(this)
				.setMessage(
						"VersionInfo not supported")
				.setTitle("Tag not supported")
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface dialog,
									int which) {
							}
						}).show();
				return;
			}
			// Make visible the registers scrollview
			layoutRead.setVisibility(View.GONE);
			sconfVer.setVisibility(View.VISIBLE);
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
	
	public static void setBoardVersion(String version)
	{
		Board_Version_text.setText(version);
	}
	
	public static void setBoardFWVersion(String version)
	{
		boardFwVersionText.setText(version);
	}
}
