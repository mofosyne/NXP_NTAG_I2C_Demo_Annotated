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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.nxp.nfc_demo.activities.AuthActivity.AuthStatus;
import com.nxp.nfc_demo.reader.Ntag_Get_Version;
import com.nxp.nfc_demo.reader.Ntag_Get_Version.Prod;
import com.nxp.nfc_demo.reader.Ntag_I2C_Demo;
import com.nxp.ntagi2cdemo.R;

public class DebugActivity extends Activity {
	private PendingIntent pendingIntent;
	private NfcAdapter mAdapter;
	private Tag tag;
	private WebView view;

	@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug);

		// Add Foreground dispatcher
		mAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		view = (WebView) findViewById(R.id.webview);
		view.getSettings().setJavaScriptEnabled(true);
		view.addJavascriptInterface(new JSInterface(this), "JSInterface");
		view.setWebViewClient(new WebViewClient());
		view.loadUrl("file:///android_asset/debug.html");
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

//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.menu, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle presses on the action bar items
//		switch (item.getItemId()) {
//		case R.id.action_about:
//			showAboutDialog();
//
//			return true;
//
//		default:
//			return super.onOptionsItemSelected(item);
//		}
//	}

	@SuppressLint({ "JavascriptInterface", "SetJavaScriptEnabled" })
	protected void onNewIntent(Intent nfc_intent) {
		// Set the initial auth parameters
		MainActivity.setAuthStatus(AuthStatus.Disabled.getValue());
		MainActivity.setPassword(null);
		
		// Store the intent information
		MainActivity.setNfcIntent(nfc_intent);
		
		// Complete the task in a new thread in order to be able to show the process
		tag = nfc_intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		MainActivity.setAuthStatus(obtainAuthStatus());
		
		// Launch the thread
		if(MainActivity.getAuthStatus() == AuthStatus.Disabled.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Unprotected.getValue()) {
			// Clean the GUI
			findViewById(R.id.ldebug).setVisibility(View.GONE);
			findViewById(R.id.sdebug).setVisibility(View.VISIBLE);
			// resets the view
			view.loadUrl("about:blank");
			view.getSettings().setJavaScriptEnabled(true);
			view.addJavascriptInterface(new JSInterface(this), "JSInterface");

			view.setWebViewClient(new WebViewClient());
			view.loadUrl("file:///android_asset/debug.html");
			
			// Complete the task in a new thread in order to be able to show the process
			tag = nfc_intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			TestTask task = new TestTask();
			task.execute();
		} else {
			Toast.makeText(getApplicationContext(), "Debugging is not available on protected tags", Toast.LENGTH_LONG).show();
		}		
	}
	
	public int obtainAuthStatus() {
		try {
			MifareUltralight mfu = MifareUltralight.get(tag);
			mfu.connect();
			byte[] command = new byte[1];
			command[0] = (byte) 0x60;
			byte[] answer = mfu.transceive(command);
			Prod prod = (new Ntag_Get_Version(answer)).Get_Product();
			if (prod != Prod.NTAG_I2C_1k_Plus && prod != Prod.NTAG_I2C_2k_Plus) {
				mfu.close();
				return AuthStatus.Disabled.getValue();
			} else {
				byte[] auth0 = mfu.readPages(0xE3);
				mfu.close();

				if((auth0[3] & 0xFF) <= 0xEB) 
					return AuthStatus.Protected_RW.getValue();
				else
					return AuthStatus.Unprotected.getValue();
			}
		} catch (Exception e) {
			return AuthStatus.Protected_RW.getValue();
		}
	}

	class JSInterface {
		JSInterface(Context ctx) {
		}

		@JavascriptInterface
		public void sendLog(String html) {
			Intent sharingIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/html");
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					"Debug Error Report from NTAG I2C LED Demo");
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, html);
			startActivity(Intent.createChooser(sharingIntent, "Share via"));
		}
	}

	protected enum RESULT {
		OK, ERROR, WARNING
	}

	private class TestTask extends AsyncTask<Void, String, Void> {

		abstract protected class Test {
			String name;

			public Test(String Name) {
				name = Name;
				publishProgress("javascript:initTest('" + name + "')");
			}
			public abstract void execute();
			protected void updateResults(RESULT result, String detail) {
				if (result == RESULT.OK)
					publishProgress("javascript:okTest('" + name + "', '"
							+ detail + "')");
				else if (result == RESULT.WARNING)
					publishProgress("javascript:warningTest('" + name + "', '"
							+ detail + "')");
				else if (result == RESULT.ERROR)
					publishProgress("javascript:errorTest('" + name + "', '"
							+ detail + "')");
			}
		}

		@SuppressWarnings("unused")
		protected class SampleTest extends Test {
			public SampleTest() {
				super("SampleTest");
			}

			@Override
			public void execute() {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					updateResults(RESULT.ERROR, e.getMessage());
					return;
				}
				updateResults(RESULT.OK, "---");
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}
			NfcA nfca = NfcA.get(tag);
			publishProgress("javascript:setUID_SAK_ATQA('"
					+ byteArrayToHex(tag.getId()) + "', '"
					+ String.format("0x%02x", nfca.getSak()) + "', '"
					+ byteArrayToHex(nfca.getAtqa()) + "')");

			publishProgress("javascript:setModel_Version('" + Build.MODEL
					+ "', '" + Build.VERSION.RELEASE + "')");
			try {
				new Mfu_transceiveTest(new byte[] { (byte) 0xA2, (byte) 0x0F,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 },
						"Write Test").execute();
				new Nfca_transceiveTest(new byte[] { (byte) 0xA2, (byte) 0x0F,
						(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 },
						"Write Test").execute();
				new MfuSectorSelectWriteTest().execute();
				new NfcaSectorSelectWriteTest().execute();
				new NfcaSectorSelectReadTest().execute();
				new NfcaSectorSelectReadWriteTest().execute();
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... progress) {
			view.loadUrl(progress[0]);
		}

		protected class Mfu_transceiveTest extends Test {
			private byte[] data;
			public Mfu_transceiveTest(byte[] data, String commandName) {
				super("Mfu transceive " + data.length + " Byte(s): "
						+ commandName + " (" + byteArrayToHex(data) + ")");
				this.data = data;
			}

			@Override
			public void execute() {
				long current_time = 0;
				try {
					MifareUltralight mfu = MifareUltralight.get(tag);
					if (mfu == null) {
						updateResults(RESULT.ERROR, "Mfu not supported");
						return;
					}
					try {
						mfu.connect();
						current_time = System.currentTimeMillis();
						mfu.transceive(data);
						mfu.close();
					} catch (IOException e) {
						updateResults(RESULT.ERROR,
								"Failed to transive : " + e.getMessage());
						mfu.close();
						return;
					}
				} catch (Exception e1) {
					updateResults(RESULT.ERROR, e1.getMessage());
					return;
				}
				updateResults(RESULT.OK, "Took "
						+ (System.currentTimeMillis() - current_time)
						+ "ms to complete");
			}
		}

		protected class Nfca_transceiveTest extends Test {
			private byte[] data;

			public Nfca_transceiveTest(byte[] data, String commandName) {
				super("NfcA transceive " + data.length + " Byte(s): "
						+ commandName + " (" + byteArrayToHex(data) + ")");
				this.data = data;
			}

			@Override
			public void execute() {
				long current_time = 0;
				try {
					NfcA nfca = NfcA.get(tag);
					if (nfca == null) {
						updateResults(RESULT.ERROR, "NfcA not supported");
						return;
					}
					try {
						nfca.connect();
						current_time = System.currentTimeMillis();
						nfca.transceive(data);
						nfca.close();
					} catch (IOException e) {
						updateResults(RESULT.ERROR,
								"Failed to transive : " + e.getMessage());
						nfca.close();
						return;
					}
				} catch (Exception e1) {
					updateResults(RESULT.ERROR, e1.getMessage());
					return;
				}
				updateResults(RESULT.OK, "Took "
						+ (System.currentTimeMillis() - current_time)
						+ "ms to complete");
			}
		}

		protected class NfcaSectorSelectWriteTest extends Test {

			public NfcaSectorSelectWriteTest() {
				super("Nfca Sector Select Test (SS to 0 then write)");
			}

			@Override
			public void execute() {
				long current_time = 0;
				NfcA nfca = NfcA.get(tag);
				if (nfca == null) {
					updateResults(RESULT.ERROR, "NfcA not supported");
					return;
				}
				try {
					nfca.connect();
					byte[] command = null;
					try {
						command = new byte[2];
						command[0] = (byte) 0xc2;
						command[1] = (byte) 0xff;
						nfca.transceive(command);
					} catch (IOException e) {
						updateResults(RESULT.ERROR, "Failed first part of SS: "
								+ e.getMessage());
						nfca.close();
						return;
					}
					try {
						command = new byte[4];
						command[0] = (byte) 0x00;
						command[1] = (byte) 0x00;
						command[2] = (byte) 0x00;
						command[3] = (byte) 0x00;
						current_time = System.currentTimeMillis();
						nfca.setTimeout(20);
						nfca.transceive(command);
					} catch (TagLostException e) {
						// normal because SS
						current_time = System.currentTimeMillis()
								- current_time;
						// nfca.close();
						// nfca.connect();
					} catch (IOException e) {
						updateResults(RESULT.ERROR,
								"Failed second part of SS: " + e.getMessage());
						nfca.close();
						return;
					}
					try {
						nfca.transceive(new byte[] { (byte) 0xA2, (byte) 0x0F,
								(byte) 0x00, (byte) 0x00, (byte) 0x00,
								(byte) 0x00 });

					} catch (IOException e) {
						updateResults(RESULT.ERROR, "Write after SS Failed: "
								+ e.getMessage());
						nfca.close();
						return;
					}
				} catch (Exception e) {
					updateResults(RESULT.ERROR,"Unexpected Error: " + e.getMessage());
					return;
				}
				try {
					nfca.close();
				} catch (IOException e) {
				}
				updateResults(RESULT.OK, " Measured timeout: " + current_time);
			}

		}

		protected class NfcaSectorSelectReadTest extends Test {

			public NfcaSectorSelectReadTest() {
				super("Nfca Sector Select Test (SS to 0 then read)");
			}

			@Override
			public void execute() {
				long current_time = 0;
				NfcA nfca = NfcA.get(tag);
				if (nfca == null) {
					updateResults(RESULT.ERROR, "NfcA not supported");
					return;
				}
				try {
					nfca.connect();
					byte[] command = null;
					try {
						command = new byte[2];
						command[0] = (byte) 0xc2;
						command[1] = (byte) 0xff;
						nfca.transceive(command);
					} catch (IOException e) {
						updateResults(RESULT.ERROR, "Failed first part of SS: "
								+ e.getMessage());
						nfca.close();
						return;
					}
					try {
						command = new byte[4];
						command[0] = (byte) 0x00;
						command[1] = (byte) 0x00;
						command[2] = (byte) 0x00;
						command[3] = (byte) 0x00;
						current_time = System.currentTimeMillis();
						nfca.setTimeout(20);
						nfca.transceive(command);
					} catch (TagLostException e) {
						// normal because SS
						current_time = System.currentTimeMillis()
								- current_time;
						// nfca.close();
						// nfca.connect();
					} catch (IOException e) {
						updateResults(RESULT.ERROR,
								"Failed second part of SS: " + e.getMessage());
						nfca.close();
						return;
					}
					try {
						nfca.transceive(new byte[] { (byte) 0x30, (byte) 0x0F });
					} catch (IOException e) {
						updateResults(RESULT.ERROR,
								"Read after SS Failed:" + e.getMessage());
						nfca.close();
						return;
					}
				} catch (Exception e) {
					updateResults(RESULT.ERROR,"Unexpected Error: " + e.getMessage());
					return;
				}
				try {
					nfca.close();
				} catch (IOException e) {
				}
				updateResults(RESULT.OK, " Measured timeout: " + current_time);
			}

		}

		protected class NfcaSectorSelectReadWriteTest extends Test {

			public NfcaSectorSelectReadWriteTest() {
				super("Nfca Sector Select Test (SS to 0 then read then write)");
			}

			@Override
			public void execute() {
				long current_time = 0;
				NfcA nfca = NfcA.get(tag);
				if (nfca == null) {
					updateResults(RESULT.ERROR, "NfcA not supported");
					return;
				}
				try {
					nfca.connect();
					byte[] command = null;
					try {
						command = new byte[2];
						command[0] = (byte) 0xc2;
						command[1] = (byte) 0xff;
						nfca.transceive(command);
					} catch (IOException e) {
						updateResults(RESULT.ERROR, "Failed first part of SS: "
								+ e.getMessage());
						nfca.close();
						return;
					}
					try {
						command = new byte[4];
						command[0] = (byte) 0x00;
						command[1] = (byte) 0x00;
						command[2] = (byte) 0x00;
						command[3] = (byte) 0x00;
						current_time = System.currentTimeMillis();
						nfca.setTimeout(20);
						nfca.transceive(command);
					} catch (TagLostException e) {
						// normal because SS
						current_time = System.currentTimeMillis()
								- current_time;
						// nfca.close();
						// nfca.connect();
					} catch (IOException e) {
						updateResults(RESULT.ERROR,
								"Failed second part of SS: " + e.getMessage());
						nfca.close();
						return;
					}
					try {
						nfca.transceive(new byte[] { (byte) 0x30, (byte) 0x0F });
					} catch (IOException e) {
						updateResults(RESULT.ERROR,
								"Read after SS Failed:" + e.getMessage());
						nfca.close();
						return;
					}
					try {
						nfca.transceive(new byte[] { (byte) 0xA2, (byte) 0x0F,
								(byte) 0x00, (byte) 0x00, (byte) 0x00,
								(byte) 0x00 });
					} catch (IOException e) {
						updateResults(RESULT.ERROR, "Write after Read Failed:"
								+ e.getMessage());
						nfca.close();
						return;
					}
				} catch (Exception e) {
					updateResults(RESULT.ERROR,"Unexpected Error: " + e.getMessage());
					return;
				}
				try {
					nfca.close();
				} catch (IOException e) {
				}
				updateResults(RESULT.OK, " Measured timeout: " + current_time);
			}
		}

		protected class MfuSectorSelectWriteTest extends Test {

			public MfuSectorSelectWriteTest() {
				super("Mfu Sector Select Test  (SS to 0 then write)");
			}

			@Override
			public void execute() {
				long current_time = 0;
				MifareUltralight mfu = MifareUltralight.get(tag);
				if (mfu == null) {
					updateResults(RESULT.ERROR, "Mfu not supported");
					return;
				}
				try {
					mfu.connect();
					byte[] command = null;
					try {
						command = new byte[2];
						command[0] = (byte) 0xc2;
						command[1] = (byte) 0xff;
						mfu.transceive(command);
					} catch (IOException e) {
						updateResults(RESULT.ERROR, "Failed first part of SS: "
								+ e.getMessage());
						mfu.close();
						return;
					}
					try {
						command = new byte[4];
						command[0] = (byte) 0x00;
						command[1] = (byte) 0x00;
						command[2] = (byte) 0x00;
						command[3] = (byte) 0x00;
						current_time = System.currentTimeMillis();
						mfu.setTimeout(20);
						mfu.transceive(command);
					} catch (TagLostException e) {
						// normal because SS
						current_time = System.currentTimeMillis()
								- current_time;
						// nfca.close();
						// nfca.connect();
					} catch (IOException e) {
						updateResults(RESULT.ERROR,
								"Failed second part of SS: " + e.getMessage());
						mfu.close();
						return;
					}
					try {
						mfu.writePage(0x0F, new byte[] { (byte) 0x00,
								(byte) 0x00, (byte) 0x00, (byte) 0x00 });
					} catch (IOException e) {
						updateResults(RESULT.ERROR, "Write after SS Failed:"
								+ e.getMessage());
						mfu.close();
						return;
					}
				} catch (Exception e) {
					updateResults(RESULT.ERROR,"Unexpected Error: " + e.getMessage());
					return;
				}
				try {
					mfu.close();
				} catch (IOException e) {
				}
				updateResults(RESULT.OK, " Measured timeout: " + current_time);
			}

		}
	}

	private static String byteArrayToHex(byte[] array) {
		final StringBuilder builder = new StringBuilder();
		builder.append("0x");
		for (byte value : array) {
			builder.append(String.format("%02x ", value));
		}
		builder.deleteCharAt(builder.length() - 1);
		return builder.toString();
	}
}
