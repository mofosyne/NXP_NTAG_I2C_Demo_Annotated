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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nxp.nfc_demo.activities.AuthActivity.AuthStatus;
import com.nxp.nfc_demo.exceptions.CommandNotSupportedException;
import com.nxp.nfc_demo.reader.Ntag_I2C_Demo;
import com.nxp.nfc_demo.reader.Ntag_I2C_Registers;
import com.nxp.ntagi2cdemo.R;

public class RegisterSessionActivity extends Activity {

	private PendingIntent pendingIntent;
	private NfcAdapter mAdapter;
	public Ntag_I2C_Demo demo;
	
	private static TextView IC_Manufacturer_text;
	private static TextView Mem_size_text;
	private static TextView FD_OFF_text;
	private static TextView FD_ON_text;
	private static TextView LAST_NDEF_Page_text;
	private static TextView SRAM_Mirror_Reg_text;
	private static TextView WD_LS_Reg_text;
	private static TextView WD_MS_Reg_text;

	private static CheckBox I2C_RST_ON_OFF_checkbox;
	private static CheckBox NDEF_DATA_READ_checkbox;
	private static CheckBox RF_FIELD_PRESENT_checkbox;
	private static CheckBox PT_ON_OFF_checkbox;
	private static CheckBox I2C_LOCKED_checkbox;
	private static CheckBox RF_LOCKED_checkbox;
	private static CheckBox SRAM_I2C_ready_checkbox;
	private static CheckBox SRAM_RF_ready_checkbox;
	private static CheckBox PT_DIR_checkbox;
	private static CheckBox SRAM_Miror_checkbox;
	private static CheckBox CI2C_CLOCK_STR_checkbox;
	
	private LinearLayout layoutChipInfo;
	private RelativeLayout rlChipInfo;
	
	private LinearLayout layoutNtagConfig;
	private RelativeLayout rlNtagConfig;
	
	private LinearLayout layoutFD;
	private RelativeLayout rlFD;
	
	private LinearLayout layoutPT;
	private RelativeLayout rlPT;
	
	private LinearLayout layoutSram;
	private RelativeLayout rlSram;
	
	private LinearLayout layoutI2C;
	private RelativeLayout rlI2C;
	
	private ImageView imageChipInfo;
	private ImageView imageNtagConfig;
	private ImageView imageFD;
	private ImageView imagePT;
	private ImageView imageSram;
	private ImageView imageI2C;
	
	private boolean layoutChipInfoExpanded = false;
	private boolean layoutNtagConfigExpanded = false;
	private boolean layoutFDExpanded = false;
	private boolean layoutPTExpanded = false;
	private boolean layoutSramExpanded = false;
	private boolean layoutI2CExpanded = false;
	
	private LinearLayout layout_read;
	private ScrollView scroll_regs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registersession);

		layout_read = (LinearLayout) findViewById(R.id.lsession);
		scroll_regs = (ScrollView) findViewById(R.id.ssession);
		
		layoutChipInfo = (LinearLayout) findViewById(R.id.General_Chip_Information);
		rlChipInfo = (RelativeLayout) findViewById(R.id.General_Chip_Information_info);
		
		layoutNtagConfig = (LinearLayout) findViewById(R.id.Section_NTAG_Config);
		rlNtagConfig = (RelativeLayout) findViewById(R.id.Section_NTAG_Config_Info);
		
		layoutFD = (LinearLayout) findViewById(R.id.Section_Field_Detection);
		rlFD = (RelativeLayout) findViewById(R.id.Section_Field_Detection_Info);
		
		layoutPT = (LinearLayout) findViewById(R.id.Section_Passthrough);
		rlPT = (RelativeLayout) findViewById(R.id.Section_Passthrough_Info);
		
		layoutSram = (LinearLayout) findViewById(R.id.Section_SRam_Mirrot);
		rlSram = (RelativeLayout) findViewById(R.id.Section_SRam_Mirrot_Info);
		
		layoutI2C = (LinearLayout) findViewById(R.id.Section_I2C);
		rlI2C = (RelativeLayout) findViewById(R.id.Section_I2C_Info);
		
		IC_Manufacturer_text = (TextView) findViewById(R.id.IC_Product_text);
		Mem_size_text = (TextView) findViewById(R.id.Mem_size_text);
		FD_OFF_text = (TextView) findViewById(R.id.FD_OFF_text);
		FD_ON_text = (TextView) findViewById(R.id.FD_ON_text);
		LAST_NDEF_Page_text = (TextView) findViewById(R.id.LAST_NDEF_Page_text);
		SRAM_Mirror_Reg_text = (TextView) findViewById(R.id.SRAM_Mirror_Reg_text);
		WD_LS_Reg_text = (TextView) findViewById(R.id.WD_LS_Reg_text);
		WD_MS_Reg_text = (TextView) findViewById(R.id.WD_MS_Reg_text);

		I2C_RST_ON_OFF_checkbox = (CheckBox) findViewById(R.id.I2C_RST_ON_OFF_checkbox);
		NDEF_DATA_READ_checkbox = (CheckBox) findViewById(R.id.NDEF_DATA_READ_checkbox);
		RF_FIELD_PRESENT_checkbox = (CheckBox) findViewById(R.id.RF_FIELD_PRESENT_checkbox);
		PT_ON_OFF_checkbox = (CheckBox) findViewById(R.id.PT_ON_OFF_checkbox);
		I2C_LOCKED_checkbox = (CheckBox) findViewById(R.id.I2C_LOCKED_checkbox);
		RF_LOCKED_checkbox = (CheckBox) findViewById(R.id.RF_LOCKED_checkbox);
		SRAM_I2C_ready_checkbox = (CheckBox) findViewById(R.id.SRAM_I2C_ready_checkbox);
		SRAM_RF_ready_checkbox = (CheckBox) findViewById(R.id.SRAM_RF_ready_checkbox);
		PT_DIR_checkbox = (CheckBox) findViewById(R.id.PT_DIR_checkbox);
		SRAM_Miror_checkbox = (CheckBox) findViewById(R.id.SRAM_Miror_checkbox);
		CI2C_CLOCK_STR_checkbox = (CheckBox) findViewById(R.id.I2C_CLOCK_STR_checkbox);
		
		layoutChipInfo.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				if (layoutChipInfoExpanded) {
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
		layoutNtagConfig.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				if (layoutNtagConfigExpanded) {
					imageNtagConfig.setImageResource(R.drawable.expand);
					rlNtagConfig.setVisibility(View.GONE);
					layoutNtagConfigExpanded = false;
				} else {
					imageNtagConfig.setImageResource(R.drawable.hide);
					rlNtagConfig.setVisibility(View.VISIBLE);
					layoutNtagConfigExpanded = true;
				}
			}
		});
		layoutFD.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				if (layoutFDExpanded) {
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
				if (layoutPTExpanded) {
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
		layoutSram.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				if (layoutSramExpanded) {
					imageSram.setImageResource(R.drawable.expand);
					rlSram.setVisibility(View.GONE);
					layoutSramExpanded = false;
				} else {
					imageSram.setImageResource(R.drawable.hide);
					rlSram.setVisibility(View.VISIBLE);
					layoutSramExpanded = true;
				}
			}
		});
		layoutI2C.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				if (layoutI2CExpanded) {
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
		imageChipInfo = (ImageView) findViewById(R.id.imageGeneralChip);
		imageNtagConfig = (ImageView) findViewById(R.id.imageNTAGConfig);
		imageFD = (ImageView) findViewById(R.id.imageFD);
		imagePT = (ImageView) findViewById(R.id.imagePT);
		imageSram = (ImageView) findViewById(R.id.imageSramMirror);
		imageI2C = (ImageView) findViewById(R.id.imageI2C);
		
		// Capture intent to check whether the operation should be automatically launch or not
		Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if(tag != null && Ntag_I2C_Demo.isTagPresent(tag)) {
			startDemo(tag, false);
		}
		// Add Foreground dispatcher
		mAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
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
	    if (requestCode == MainActivity.AUTH_REQUEST
         && resultCode == RESULT_OK
         && demo != null
         && demo.isReady()) {
            // Launch the thread
            try {
                demo.readSessionRegisters();
            } catch (CommandNotSupportedException e) {
                new AlertDialog.Builder(this)
                .setMessage(
                        "This NFC device does not support the NFC Forum "
                      + "commands needed to access the session register")
                .setTitle("Command not supported")
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
            layout_read.setVisibility(View.GONE);
            scroll_regs.setVisibility(View.VISIBLE);
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

	private void startDemo(final Tag tag, boolean getAuthStatus) {
		demo = new Ntag_I2C_Demo(tag, this, MainActivity.getPassword(), MainActivity.getAuthStatus());
		if(!demo.isReady()) {
            return;
        }

		// Retrieve the Auth Status
		if(getAuthStatus) {
            MainActivity.setAuthStatus(demo.ObtainAuthStatus());
        }
		
		// Demo is available when the tag is not protected or the memory is only write-protected
		if(MainActivity.getAuthStatus() == AuthStatus.Disabled.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Unprotected.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Authenticated.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Protected_W.getValue()
				|| MainActivity.getAuthStatus() == AuthStatus.Protected_W_SRAM.getValue()) {
			try {
				demo.readSessionRegisters();
			} catch (CommandNotSupportedException e) {
				new AlertDialog.Builder(this)
				.setMessage(
						"This NFC device does not support the NFC Forum commands needed to access the session register")
				.setTitle("Command not supported")
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
			layout_read.setVisibility(View.GONE);
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
	
	public static void SetAnswer(Ntag_I2C_Registers answer, Context cont) {
        IC_Manufacturer_text.setText(answer.Manufacture);
        Mem_size_text.setText(String.valueOf(answer.Mem_size) + " Bytes");

        if (answer.I2C_RST_ON_OFF) {
            I2C_RST_ON_OFF_checkbox.setChecked(true);
        } else {
            I2C_RST_ON_OFF_checkbox.setChecked(false);
        }

        if (answer.FD_OFF.equals("00b")) {
            FD_OFF_text.setText(R.string.FD_OFF00);
        } else if (answer.FD_OFF.equals("01b")) {
            FD_OFF_text.setText(R.string.FD_OFF01);
        } else if (answer.FD_OFF.equals("10b")) {
            FD_OFF_text.setText(R.string.FD_OFF10);
        } else
            FD_OFF_text.setText(R.string.FD_OFF11);

        if (answer.FD_ON.equals("00b")) {
            FD_ON_text.setText(R.string.FD_ON00);
        } else if (answer.FD_ON.equals("01b")) {
            FD_ON_text.setText(R.string.FD_ON01);
        } else if (answer.FD_ON.equals("10b")) {
            FD_ON_text.setText(R.string.FD_ON10);
        } else
            FD_ON_text.setText(R.string.FD_ON11);

        LAST_NDEF_Page_text.setText(String.valueOf(answer.LAST_NDEF_PAGE));

        if (answer.NDEF_DATA_READ) {
            NDEF_DATA_READ_checkbox.setChecked(true);
        } else {
            NDEF_DATA_READ_checkbox.setChecked(false);
        }

        if (answer.RF_FIELD_PRESENT) {
            RF_FIELD_PRESENT_checkbox.setChecked(true);
        } else {
            RF_FIELD_PRESENT_checkbox.setChecked(false);
        }

        if (answer.PTHRU_ON_OFF) {
            PT_ON_OFF_checkbox.setChecked(true);
        } else {
            PT_ON_OFF_checkbox.setChecked(false);
        }

		if (answer.I2C_LOCKED) {
            I2C_LOCKED_checkbox.setChecked(true);
        } else {
            I2C_LOCKED_checkbox.setChecked(false);
        }

		if (answer.RF_LOCKED) {
            RF_LOCKED_checkbox.setChecked(true);
        } else {
            RF_LOCKED_checkbox.setChecked(false);
        }

		if (answer.SRAM_I2C_READY) {
            SRAM_I2C_ready_checkbox.setChecked(true);
        } else {
            SRAM_I2C_ready_checkbox.setChecked(false);
        }

		if (answer.SRAM_RF_READY) {
            SRAM_RF_ready_checkbox.setChecked(true);
        } else {
            SRAM_RF_ready_checkbox.setChecked(false);
        }

		if (answer.PTHRU_DIR) {
            PT_DIR_checkbox.setChecked(true);
        } else {
            PT_DIR_checkbox.setChecked(false);
        }

		if (answer.SRAM_MIRROR_ON_OFF) {
            SRAM_Miror_checkbox.setChecked(true);
        } else {
            SRAM_Miror_checkbox.setChecked(false);
        }
		SRAM_Mirror_Reg_text.setText(String.valueOf(answer.SM_Reg));
		WD_LS_Reg_text.setText(String.valueOf(answer.WD_LS_Reg));
		WD_MS_Reg_text.setText(String.valueOf(answer.WD_MS_Reg));

		if (answer.I2C_CLOCK_STR) {
            CI2C_CLOCK_STR_checkbox.setChecked(true);
        } else {
            CI2C_CLOCK_STR_checkbox.setChecked(false);
        }
	}
	
	public void showAboutDialog() {
		Intent intent = null;
		intent= new Intent(this, VersionInfoActivity.class);		
		startActivity(intent);
	}
}
