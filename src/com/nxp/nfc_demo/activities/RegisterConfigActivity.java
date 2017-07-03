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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.nxp.nfc_demo.activities.AuthActivity.AuthStatus;
import com.nxp.nfc_demo.exceptions.CommandNotSupportedException;
import com.nxp.nfc_demo.reader.Ntag_I2C_Demo;
import com.nxp.nfc_demo.reader.Ntag_I2C_Plus_Registers;
import com.nxp.nfc_demo.reader.Ntag_I2C_Registers;
import com.nxp.ntagi2cdemo.R;

public class RegisterConfigActivity extends Activity implements
		OnClickListener, OnCheckedChangeListener {
	private PendingIntent pendingIntent;
	private NfcAdapter mAdapter;

	private static TextView ChipInfo_1_text;
	private static TextView ChipInfo_2_text;
	private static Switch I2C_RST_switch;
	private static Spinner FD_OFF_spinner;
	private static Spinner FD_ON_spinner;
	private static TextView LAST_NDEF_PAGE_edit;
	private static Switch PTHRU_DIR_switch;
	private static Switch WRITE_ACCESS_switch;
	private static TextView SRAM_MIRROR_PAGE_edit_edit;
	private static TextView I2C_WD_LS_Timer_edit;
	private static TextView I2C_WD_MS_edit;
	private static Switch I2C_CLOCK_STR_switch;

	private static TextView PLUS_AUTH0_edit;
	private static Switch PLUS_NFC_Prot_switch;
	private static Switch PLUS_NFC_Disc_Sec1_switch;
	private static TextView PLUS_AUTHLim_edit;
	private static Switch PLUS_2K_Prot_switch;
	private static Switch PLUS_Sram_Prot_switch;
	private static TextView PLUS_I2C_Prot_edit;
	
	private static Button readConfigButton;
	private static Button writeConfigButton;
	
	private static LinearLayout layoutPlusAuthVisible;

	private LinearLayout layoutChipInfo;
	private RelativeLayout rlChipInfo;

	private LinearLayout layoutFD;
	private RelativeLayout rlFD;

	private LinearLayout layoutPT;
	private LinearLayout rlPT;

	private LinearLayout layoutMemory;
	private RelativeLayout rlMemory;

	private LinearLayout layoutI2C;
	private RelativeLayout rlI2C;
	
	private LinearLayout layoutPlusAuth;
	private RelativeLayout rlPlusAuth;

	private ImageView imageChipInfo;
	private ImageView imageFD;
	private ImageView imagePT;
	private ImageView imageSram;
	private ImageView imageI2C;
	private ImageView imagePlus;

	private LinearLayout layout_read;
	private LinearLayout layout_buttons;
	private ScrollView scroll_regs;

	private boolean layoutChipInfoExpanded = false;
	private boolean layoutFDExpanded = false;
	private boolean layoutPTExpanded = false;
	private boolean layoutMemoryExpanded = false;
	private boolean layoutI2CExpanded = false;
	private boolean layoutPlusAuthExpanded = false;

	private static String option;
	private static boolean writeChosen;

	private static int NC_Reg = 0;
	private static int LD_Reg = 0;
	private static int SM_Reg = 0;
	private static int NS_Reg = 0;
	private static int WD_LS_Reg = 0;
	private static int WD_MS_Reg = 0;
	private static int I2C_CLOCK_STR = 0;
	private static int PLUS_AUTH0_REG = 0;
	private static int PLUS_ACCESS_REG = 0;
	private static int PLUS_PT_I2C_REG = 0;

	private static int FD_OFF_Value = 0;
	private static int FD_ON_Value = 0;

	private Ntag_I2C_Demo demo;
	private boolean isWriteProtected;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registerconfig);
		
		layout_read = (LinearLayout) findViewById(R.id.lconf);
		layout_buttons = (LinearLayout) findViewById(R.id.lconfbuttons);
		scroll_regs = (ScrollView) findViewById(R.id.sconf);

		ChipInfo_1_text = (TextView) findViewById(R.id.ChipProd_1_text);
		ChipInfo_2_text = (TextView) findViewById(R.id.ChipInfo_2_text);

		I2C_RST_switch = (Switch) findViewById(R.id.I2C_RST_STR);
		FD_OFF_spinner = (Spinner) findViewById(R.id.FD_OFF_Spinner);
		FD_ON_spinner = (Spinner) findViewById(R.id.FD_ON_Spinner);
		
		// Set the Write protection check
		isWriteProtected = false;

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.FD_OFF_Options,
				android.R.layout.simple_spinner_item);

		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Apply the adapter to the spinner
		FD_OFF_spinner.setAdapter(adapter);

		adapter = ArrayAdapter.createFromResource(this, R.array.FD_ON_Options,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		FD_ON_spinner.setAdapter(adapter);

		LAST_NDEF_PAGE_edit = (TextView) findViewById(R.id.LAST_NDEF_PAGE_edit);
		// PTHRU_ON_OFF_switch = (Switch) findViewById(R.id.PTHRU_ON_OFF);
		PTHRU_DIR_switch = (Switch) findViewById(R.id.PTHRU_DIR);
		WRITE_ACCESS_switch = (Switch) findViewById(R.id.WRITE_ACCESS);

		PTHRU_DIR_switch.setOnCheckedChangeListener(this);
		WRITE_ACCESS_switch.setOnCheckedChangeListener(this);

		// SRAM_MIRROR_ON_OFF_switch = (Switch)
		// findViewById(R.id.SRAM_MIRROR_ON_OFF);
		SRAM_MIRROR_PAGE_edit_edit = (TextView) findViewById(R.id.SRAM_MIRROR_PAGE_edit);
		I2C_WD_LS_Timer_edit = (TextView) findViewById(R.id.I2C_WD_LS_edit);
		I2C_WD_MS_edit = (TextView) findViewById(R.id.I2C_WD_MS_edit);

		I2C_CLOCK_STR_switch = (Switch) findViewById(R.id.I2C_CLOCK_STR);
		
		PLUS_AUTH0_edit = (TextView) findViewById(R.id.Plus_Auth0_edit);
		PLUS_NFC_Prot_switch = (Switch) findViewById(R.id.Plus_NFC_Prot);
		PLUS_NFC_Disc_Sec1_switch = (Switch) findViewById(R.id.Plus_NFC_DIS_SEC1);
		PLUS_AUTHLim_edit = (TextView) findViewById(R.id.Plus_AUTHLIM_edit);
		PLUS_2K_Prot_switch = (Switch) findViewById(R.id.Plus_2K_Prot);
		PLUS_Sram_Prot_switch = (Switch) findViewById(R.id.Plus_SRAM_Prot);
		PLUS_I2C_Prot_edit = (TextView) findViewById(R.id.Plus_I2C_Prot_edit);	

		readConfigButton = (Button) findViewById(R.id.readConfigButton);
		writeConfigButton = (Button) findViewById(R.id.writeConfigButton);

		readConfigButton.setOnClickListener(this);
		writeConfigButton.setOnClickListener(this);

		layoutChipInfo = (LinearLayout) findViewById(R.id.General_Chip_Information);
		rlChipInfo = (RelativeLayout) findViewById(R.id.General_Chip_Information_info);

		layoutFD = (LinearLayout) findViewById(R.id.Section_FD);
		rlFD = (RelativeLayout) findViewById(R.id.Section_FD_Info);

		layoutPT = (LinearLayout) findViewById(R.id.Section_PT);
		rlPT = (LinearLayout) findViewById(R.id.Section_PT_Info);

		layoutMemory = (LinearLayout) findViewById(R.id.Section_Memory);
		rlMemory = (RelativeLayout) findViewById(R.id.Section_Memory_info);

		layoutI2C = (LinearLayout) findViewById(R.id.Section_I2C_C);
		rlI2C = (RelativeLayout) findViewById(R.id.Section_I2C_C_Info);
		
		layoutPlusAuthVisible = (LinearLayout) findViewById(R.id.layoutPlusAuthVisible);
		layoutPlusAuth = (LinearLayout) findViewById(R.id.Section_Plus_Auth);
		rlPlusAuth = (RelativeLayout) findViewById(R.id.Section_Plus_Auth_Info);

		// Default Selection: Read
		writeChosen = false;

		layoutChipInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (layoutChipInfoExpanded == true) {
					imageChipInfo.setImageResource(R.drawable.expand);
					rlChipInfo.setVisibility(View.GONE);
					layoutChipInfoExpanded = false;
				} else {
					imageChipInfo.setImageResource(R.drawable.hide);
					rlChipInfo.setVisibility(View.VISIBLE);
					layoutChipInfoExpanded = true;
				}
			}
		});

		layoutFD.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (layoutFDExpanded == true) {
					imageFD.setImageResource(R.drawable.expand);
					rlFD.setVisibility(View.GONE);
					layoutFDExpanded = false;
				} else {
					imageFD.setImageResource(R.drawable.hide);
					rlFD.setVisibility(View.VISIBLE);
					layoutFDExpanded = true;
				}
			}
		});

		layoutPT.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (layoutPTExpanded == true) {
					imagePT.setImageResource(R.drawable.expand);
					rlPT.setVisibility(View.GONE);
					layoutPTExpanded = false;
				} else {
					imagePT.setImageResource(R.drawable.hide);
					rlPT.setVisibility(View.VISIBLE);
					layoutPTExpanded = true;
				}
			}
		});

		layoutMemory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (layoutMemoryExpanded == true) {
					imageSram.setImageResource(R.drawable.expand);
					rlMemory.setVisibility(View.GONE);
					layoutMemoryExpanded = false;
				} else {
					imageSram.setImageResource(R.drawable.hide);
					rlMemory.setVisibility(View.VISIBLE);
					layoutMemoryExpanded = true;
				}
			}
		});

		layoutI2C.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (layoutI2CExpanded == true) {
					imageI2C.setImageResource(R.drawable.expand);
					rlI2C.setVisibility(View.GONE);
					layoutI2CExpanded = false;
				} else {
					imageI2C.setImageResource(R.drawable.hide);
					rlI2C.setVisibility(View.VISIBLE);
					layoutI2CExpanded = true;
				}
			}
		});
		
		layoutPlusAuth.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (layoutPlusAuthExpanded == true) {
					imagePlus.setImageResource(R.drawable.expand);
					rlPlusAuth.setVisibility(View.GONE);
					layoutPlusAuthExpanded = false;
				} else {
					imagePlus.setImageResource(R.drawable.hide);
					rlPlusAuth.setVisibility(View.VISIBLE);
					layoutPlusAuthExpanded = true;
				}
			}
		});

		imageChipInfo = (ImageView) findViewById(R.id.imageGeneralChip);
		imageFD = (ImageView) findViewById(R.id.imageFD);
		imagePT = (ImageView) findViewById(R.id.imagePT);
		imageSram = (ImageView) findViewById(R.id.imageSramMirror);
		imageI2C = (ImageView) findViewById(R.id.imageI2C);
		imagePlus = (ImageView) findViewById(R.id.imagePlus);
		
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
		if (mAdapter != null)
			mAdapter.disableForegroundDispatch(this);
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
		if(requestCode == MainActivity.AUTH_REQUEST
		&& resultCode == RESULT_OK
		&& demo != null
		&& demo.isReady()) {
			if(isWriteProtected)  {
				writeConfigButton.setBackgroundResource(R.drawable.btn_blue);
				readConfigButton.setBackgroundColor(Color.BLACK);
				writeChosen = true;
			} else {
				try {
					demo.readWriteConfigRegister();
				} catch (CommandNotSupportedException e) {
					new AlertDialog.Builder(this)
							.setMessage(
									"This NFC device does not support the NFC Forum "
											+ "commands needed to access the config register")
							.setTitle("Command not supported")
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,
															int which) {

										}
									}).show();
					return;
				}

				layout_read.setVisibility(View.GONE);
				layout_buttons.setVisibility(View.VISIBLE);
				scroll_regs.setVisibility(View.VISIBLE);
			}
		}
	}

	protected void onNewIntent(Intent nfc_intent) {
		// Set the initial auth parameters
		MainActivity.setAuthStatus(AuthStatus.Disabled.getValue());
		MainActivity.setPassword(null);
		
		// Set the Write protection check
		isWriteProtected = false;
		
		// Store the intent information
		MainActivity.setNfcIntent(nfc_intent);
		final Tag tag = nfc_intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		startDemo(tag, true);
	}

	private void startDemo(final Tag tag, boolean getAuthStatus) {
		demo = new Ntag_I2C_Demo(tag, this, MainActivity.getPassword(), MainActivity.getAuthStatus());
		if (!demo.isReady())
			return;

		// Calculate the Register Values according to what has been selected by
		// the user
		calcConfiguration();
		
		// Retrieve the Auth Status
		if(getAuthStatus == true) {
			MainActivity.setAuthStatus(demo.ObtainAuthStatus());
		}
		
		if(MainActivity.getAuthStatus() == AuthStatus.Disabled.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Unprotected.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Authenticated.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Protected_W.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Protected_W_SRAM.getValue()) {
			try {
				demo.readWriteConfigRegister();
			} catch (CommandNotSupportedException e) {
				new AlertDialog.Builder(this)
						.setMessage(
								"This NFC device does not support the NFC Forum commands needed to access the config register")
						.setTitle("Command not supported")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
	
									}
								}).show();
				return;
			}
			layout_read.setVisibility(View.GONE);
			layout_buttons.setVisibility(View.VISIBLE);
			scroll_regs.setVisibility(View.VISIBLE);
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
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.PTHRU_DIR:
			if (PTHRU_DIR_switch.isChecked()) {
				WRITE_ACCESS_switch.setChecked(true);
			} else {
				WRITE_ACCESS_switch.setChecked(false);
			}
			break;
		case R.id.WRITE_ACCESS:
			if (WRITE_ACCESS_switch.isChecked()) {
				PTHRU_DIR_switch.setChecked(true);
			} else {
				PTHRU_DIR_switch.setChecked(false);
			}
			break;
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.readConfigButton:
			readConfigButton.setBackgroundResource(R.drawable.btn_blue);
			writeConfigButton.setBackgroundColor(Color.BLACK);
			writeChosen = false;
			if (demo.isConnected())
				try {
					demo.readWriteConfigRegister();
				} catch (CommandNotSupportedException e) {
					// can never happen
					e.printStackTrace();
				}
			break;

		case R.id.writeConfigButton:
			if (writeChosen == true) {
				// Calculate the Register Values according to what has been
				// selected by the user
				calcConfiguration();
				if (demo.isConnected())
					try {
						demo.readWriteConfigRegister();
					} catch (CommandNotSupportedException e) {
						// can never happen
						e.printStackTrace();
					}
			} else {
				if(MainActivity.getAuthStatus() == AuthStatus.Disabled.getValue()
						|| MainActivity.getAuthStatus() == AuthStatus.Unprotected.getValue()
						|| MainActivity.getAuthStatus() == AuthStatus.Authenticated.getValue()) {
					writeConfigButton.setBackgroundResource(R.drawable.btn_blue);
					readConfigButton.setBackgroundColor(Color.BLACK);
					writeChosen = true;
				} else {
					// Enable the write protection check
					isWriteProtected = true;
					showAuthDialog();
				}
			}
			break;
		default:
			break;
		}
	} // END onClick (View v)

	public static String getOption() {
		return option;
	}

	public static void setAnswer(Ntag_I2C_Registers answer, Context cont) {
		ChipInfo_1_text.setText(answer.Manufacture);
		ChipInfo_2_text.setText(String.valueOf(answer.Mem_size) + " Bytes");

		// I2C_RST Switch
		I2C_RST_switch.setChecked(answer.I2C_RST_ON_OFF);

		if (answer.FD_OFF.equals(cont.getString(R.string.FD_OFF_ON_11))) {
			FD_OFF_spinner.setSelection(3);
		}
		if (answer.FD_OFF.equals(cont.getString(R.string.FD_OFF_ON_10))) {
			FD_OFF_spinner.setSelection(2);
		}
		if (answer.FD_OFF.equals(cont.getString(R.string.FD_OFF_ON_01))) {
			FD_OFF_spinner.setSelection(1);
		}
		if (answer.FD_OFF.equals(cont.getString(R.string.FD_OFF_ON_00))) {
			FD_OFF_spinner.setSelection(0);
		}
		if (answer.FD_ON.equals(cont.getString(R.string.FD_OFF_ON_11))) {
			FD_ON_spinner.setSelection(3);
		}
		if (answer.FD_ON.equals(cont.getString(R.string.FD_OFF_ON_10))) {
			FD_ON_spinner.setSelection(2);
		}
		if (answer.FD_ON.equals(cont.getString(R.string.FD_OFF_ON_01))) {
			FD_ON_spinner.setSelection(1);
		}
		if (answer.FD_ON.equals(cont.getString(R.string.FD_OFF_ON_00))) {
			FD_ON_spinner.setSelection(0);
		}

		// Get the Last NDEF Page
		LAST_NDEF_PAGE_edit.setText(String.valueOf(answer.LAST_NDEF_PAGE));

		// PassThrough Dir + Write Access
		if (answer.PTHRU_DIR) {
			PTHRU_DIR_switch.setChecked(true);
			WRITE_ACCESS_switch.setChecked(true);
		} else {
			PTHRU_DIR_switch.setChecked(false);
			WRITE_ACCESS_switch.setChecked(false);
		}
		SRAM_MIRROR_PAGE_edit_edit.setText(String.valueOf(answer.SM_Reg));
		I2C_WD_LS_Timer_edit.setText(String.valueOf(answer.WD_LS_Reg));
		I2C_WD_MS_edit.setText(String.valueOf(answer.WD_MS_Reg));

		// SRAM_MIRROR_ON_OFF_switch.setChecked(answer.SRAM_MIRROR_ON_OFF);
		I2C_CLOCK_STR_switch.setChecked(answer.I2C_CLOCK_STR);
		
		// Set the Auth Layout to Gone by default
		layoutPlusAuthVisible.setVisibility(View.GONE);
	} // END set Answer
	
	public static void setAnswerPlus(Ntag_I2C_Plus_Registers answer, Context cont) {
		// Set the Auth Layout to Visible
		layoutPlusAuthVisible.setVisibility(View.VISIBLE);
		
		// Auth0 Register
		PLUS_AUTH0_edit.setText(String.valueOf(answer.auth0));
		
		// Access Register
//		PLUS_NFC_Prot_edit.setText(String.valueOf(answer.nfcProt));
		if(answer.nfcProt) {
			PLUS_NFC_Prot_switch.setChecked(true);
		} else {
			PLUS_NFC_Prot_switch.setChecked(false);
		}
		
//		PLUS_NFC_Disc_Sec1_edit.setText(String.valueOf(answer.nfcDisSec1));
		if(answer.nfcDisSec1) {
			PLUS_NFC_Disc_Sec1_switch.setChecked(true);
		} else {
			PLUS_NFC_Disc_Sec1_switch.setChecked(false);
		}
		PLUS_AUTHLim_edit.setText(String.valueOf(answer.authlim));
		
		// PT_I2C Register
		if(answer.k2Prot) {
			PLUS_2K_Prot_switch.setChecked(true);
		} else {
			PLUS_2K_Prot_switch.setChecked(false);
		}
		
//		PLUS_NFC_Disc_Sec1_edit.setText(String.valueOf(answer.nfcDisSec1));
		if(answer.sram_prot) {
			PLUS_Sram_Prot_switch.setChecked(true);
		} else {
			PLUS_Sram_Prot_switch.setChecked(false);
		}
		PLUS_I2C_Prot_edit.setText(String.valueOf(answer.i2CProt));
	}

	public static void calcConfiguration() {
		FD_OFF_Value = FD_OFF_spinner.getSelectedItemPosition();
		FD_ON_Value = FD_ON_spinner.getSelectedItemPosition();

		if (FD_OFF_Value == 3) {
			NC_Reg = (NC_Reg | 0x30);
		}

		if (FD_OFF_Value == 2) {
			NC_Reg = (NC_Reg & 0xcf);
			NC_Reg = (NC_Reg | 0x20);
		}

		if (FD_OFF_Value == 1) {
			NC_Reg = (NC_Reg & 0xcf);
			NC_Reg = (NC_Reg | 0x10);
		}

		if (FD_OFF_Value == 0) {
			NC_Reg = (NC_Reg & 0xcf);
		}

		if (FD_ON_Value == 3) {
			NC_Reg = (NC_Reg | 0x0c);
		}

		if (FD_ON_Value == 2) {
			NC_Reg = (NC_Reg & 0xf3);
			NC_Reg = (NC_Reg | 0x08);
		}

		if (FD_ON_Value == 1) {
			NC_Reg = (NC_Reg & 0xf3);
			NC_Reg = (NC_Reg | 0x04);
		}

		if (FD_ON_Value == 0) {
			NC_Reg = (NC_Reg & 0xf3);
		}

		if (PTHRU_DIR_switch.isChecked()) {
			NC_Reg = (NC_Reg | (byte) 0x01);
		} else {
			NC_Reg = (NC_Reg & (byte) 0xfe);
		}
		LD_Reg = Integer.parseInt(LAST_NDEF_PAGE_edit.getText().toString());
		SM_Reg = Integer.parseInt(SRAM_MIRROR_PAGE_edit_edit.getText().toString());
		WD_LS_Reg = Integer.parseInt(I2C_WD_LS_Timer_edit.getText().toString());
		WD_MS_Reg = Integer.parseInt(I2C_WD_MS_edit.getText().toString());

		if (I2C_CLOCK_STR_switch.isChecked()) {
			I2C_CLOCK_STR = 1;
		} else {
			I2C_CLOCK_STR = 0;
		}

		if (I2C_RST_switch.isChecked()) {
			NC_Reg = (NC_Reg | (byte) 0x80);
		} else {
			NC_Reg = (NC_Reg & (byte) 0x7f);
		}
		PLUS_AUTH0_REG = Integer.parseInt(PLUS_AUTH0_edit.getText().toString());

		if (PLUS_NFC_Prot_switch.isChecked()) {
			PLUS_ACCESS_REG = (PLUS_ACCESS_REG | (byte) 0x80);
		} else {
			PLUS_ACCESS_REG = (PLUS_ACCESS_REG & (byte) 0x7f);
		}

		if (PLUS_NFC_Disc_Sec1_switch.isChecked()) {
			PLUS_ACCESS_REG = (PLUS_ACCESS_REG | (byte) 0x20);
		} else {
			PLUS_ACCESS_REG = (PLUS_ACCESS_REG & (byte) 0xdf);
		}
		PLUS_ACCESS_REG |= Integer.parseInt(PLUS_AUTHLim_edit.getText().toString());
		
		if (PLUS_2K_Prot_switch.isChecked()) {
			PLUS_PT_I2C_REG = (PLUS_PT_I2C_REG | (byte) 0x08);
		} else {
			PLUS_PT_I2C_REG = (PLUS_PT_I2C_REG & (byte) 0xf7);
		}
		
		if (PLUS_Sram_Prot_switch.isChecked()) {
			PLUS_PT_I2C_REG = (PLUS_PT_I2C_REG | (byte) 0x04);
		} else {
			PLUS_PT_I2C_REG = (PLUS_PT_I2C_REG & (byte) 0xfB);
		}
		PLUS_PT_I2C_REG |= Integer.parseInt(PLUS_I2C_Prot_edit.getText().toString());
		return;
	}

	public static boolean isWriteChosen() {
		return writeChosen;
	}

	// return registers
	public static int getNC_Reg() {
		return NC_Reg;
	}

	public static int getLD_Reg() {
		return LD_Reg;
	}

	public static int getSM_Reg() {
		return SM_Reg;
	}

	public static int getNS_Reg() {
		return NS_Reg;
	}

	public static int getWD_LS_Reg() {
		return WD_LS_Reg;
	}

	public static int getWD_MS_Reg() {
		return WD_MS_Reg;
	}

	public static int getI2C_CLOCK_STR() {
		return I2C_CLOCK_STR;
	}
	
	public static int getAuth0() {
		return PLUS_AUTH0_REG;
	}
	
	public static int getAccess() {
		return PLUS_ACCESS_REG;
	}
	
	public static int getPTI2C() {
		return PLUS_PT_I2C_REG;
	}

	// set registers
	public static void setNC_Reg(int nC_Reg2) {
		NC_Reg = nC_Reg2;
	}

	public static void setLD_Reg(int lD_Reg2) {
		LD_Reg = lD_Reg2;
	}

	public static void setSM_Reg(int sM_Reg2) {
		SM_Reg = sM_Reg2;
	}

	public static void setNS_Reg(int nS_Reg2) {
		NS_Reg = nS_Reg2;
	}

	public static void setWD_LS_Reg(int wD_LS_Reg2) {
		WD_LS_Reg = wD_LS_Reg2;
	}

	public static void setWD_MS_Reg(int wD_MS_Reg2) {
		WD_MS_Reg = wD_MS_Reg2;
	}

	public static void setI2C_CLOCK_STR(int i2C_CLOCK_STR) {
		I2C_CLOCK_STR = i2C_CLOCK_STR;
	}
	
	public static void setPlus_Auth0_Reg(int plus_Auth0_Reg) {
		PLUS_AUTH0_REG = plus_Auth0_Reg;
	}
	
	public static void setPlus_Access_Reg(int plus_Access_Reg) {
		PLUS_ACCESS_REG = plus_Access_Reg;
	}
	
	public static void setPlus_Pti2c_Reg(int plus_Pti2c_Reg) {
		PLUS_PT_I2C_REG = plus_Pti2c_Reg;
	}

	public void showAboutDialog() {
		Intent intent = null;
		intent = new Intent(this, VersionInfoActivity.class);
		startActivity(intent);
	}
}
