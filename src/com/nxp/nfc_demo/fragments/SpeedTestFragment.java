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
package com.nxp.nfc_demo.fragments;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.nxp.nfc_demo.activities.MainActivity;
import com.nxp.ntagi2cdemo.R;

public class SpeedTestFragment extends Fragment implements
		OnCheckedChangeListener, OnClickListener {

	private static RadioGroup rfReadOptions;
	private static RadioGroup rfMemOptions;
	private static TextView rfTextCallback;
	private static TextView rfDatarateCallback;
	private static boolean rfChosen = false;
	private static boolean rfMemChosen = false;
	private static EditText rfEditCharMulti;
	private static TextView rfTextCharMulti;
	private static Button rfButtonSpeedtest;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Values per default
		this.rfMemChosen = true;
		this.rfChosen = true;
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.fragment_speedtest, container,
				false);
		rfButtonSpeedtest = (Button) layout.findViewById(R.id.startSpeedtest);
		rfButtonSpeedtest.setOnClickListener(this);
		rfReadOptions = (RadioGroup) layout
				.findViewById(R.id.radioReadOptions);
		rfMemOptions = (RadioGroup) layout
				.findViewById(R.id.radioMemoryOptions);
		rfMemOptions.setOnCheckedChangeListener(this);
		rfTextCallback = (TextView) layout.findViewById(R.id.rf_textCallback);
		rfDatarateCallback = (TextView) layout.findViewById(R.id.rf_datarateCallback);
		rfDatarateCallback.setMovementMethod(new ScrollingMovementMethod());
		rfEditCharMulti = (EditText) layout.findViewById(R.id.editCharMultipl);
		rfTextCharMulti = (TextView) layout.findViewById(R.id.textCharMultipl);
		rfEditCharMulti.setText("10");
		rfEditCharMulti.addTextChangedListener(charMultiListener);
		return layout;
	}
	
	private final TextWatcher charMultiListener = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        public void afterTextChanged(Editable s) {
            if (rfMemChosen == false) {
            	try
                {	
            		int bytes = Integer.parseInt(rfEditCharMulti.getText().toString());
            		int overhead = eepromCalculateOverhead(bytes);
                	if(overhead > 0) {
                        rfTextCharMulti.setText("+ " + overhead + " " + getActivity().getResources().getString(R.string.Block_multipl_eeprom_overhead));
                    } else {
                        rfTextCharMulti.setText(getActivity().getResources().getString(R.string.Block_multipl_eeprom));
                    }
                } catch (NumberFormatException ex) {
                	rfTextCharMulti.setText(getActivity().getResources().getString(R.string.Block_multipl_eeprom));
                	ex.printStackTrace();
                }
            }
        }
    };
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (checkedId == R.id.radioMemoryEeprom) {
			rfMemChosen = false;
			rfReadOptions.setVisibility(View.GONE);
			rfEditCharMulti.setHint(R.string.Ndef_char_multipl);
			String textCharMulti = getrf_ndef_value_charmulti();
			
			// getting text multiplier
			int chMultiplier = 1;
			int chMultiLength = textCharMulti.length();
			if (chMultiLength == 0) {
				chMultiplier = 1;
			} else {
				chMultiplier = Integer.parseInt(textCharMulti);
			}
			rfEditCharMulti.setText(Integer.toString(chMultiplier * 64));
			// rf_ndef_CharMulti.setText("");
		} else if (checkedId == R.id.radioMemorySram) {
			rfMemChosen = true;
//			rfReadOptions.setVisibility(View.VISIBLE);
			rfReadOptions.setVisibility(View.GONE);
			rfEditCharMulti.setHint(R.string.Block_multipl);
			rfTextCharMulti.setText(getActivity().getResources().getString(R.string.Block_multipl_sram));
			String textCharMulti = getrf_ndef_value_charmulti();

			// getting text multiplier
			int chMultiplier = 1;
			int chMultiLength = textCharMulti.length();
			if (chMultiLength == 0) {
				chMultiplier = 1;
			} else {
				chMultiplier = Integer.parseInt(textCharMulti);
			}
			rfEditCharMulti.setText(Integer.toString(chMultiplier / 64));
		}
	}
	
	private int eepromCalculateOverhead(int bytes) {
		int overhead = 0;
				
		String messageText = "";
		for (int i = 0; i < bytes; i++) {
			messageText = messageText.concat(" ");
		}
		
		// Calculate the overhead
		NdefMessage msg;
		try {
			msg = createNdefMessage(messageText);
			int ndef_message_size = (msg.toByteArray().length + 5);
			ndef_message_size = (int) Math.round(ndef_message_size / 4)	* 4;
			overhead = ndef_message_size - (bytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			e.printStackTrace();
		}
		return overhead;
	}
	
	private NdefMessage createNdefMessage(String text)
			throws UnsupportedEncodingException {
		String lang = "en";
		byte[] textBytes = text.getBytes();
		byte[] langBytes = lang.getBytes("US-ASCII");
		int langLength = langBytes.length;
		int textLength = textBytes.length;
		byte[] payload = new byte[1 + langLength + textLength];
		payload[0] = (byte) langLength;
		System.arraycopy(langBytes, 0, payload, 1, langLength);
		System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

		NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_TEXT, new byte[0], payload);
		NdefRecord[] records = { record };
		NdefMessage message = new NdefMessage(records);
		return message;
	}

	private void StartEEPROMSpeedTest() {
		if (MainActivity.demo.isReady() && MainActivity.demo.isConnected()) {
			MainActivity.demo.finishAllTasks();
			try {
				MainActivity.demo.EEPROMSpeedtest();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (FormatException e) {
				e.printStackTrace();
			}
		}
	}

	private void StartSRAMSpeedTest() {
		if (MainActivity.demo.isReady() && MainActivity.demo.isConnected()) {
			MainActivity.demo.finishAllTasks();
			try {
				MainActivity.demo.SRAMSpeedtest();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (FormatException e) {
				e.printStackTrace();
			}
		}
	}

	public static String getrf_ndef_value_charmulti() {
        return rfEditCharMulti.getText().toString();
	}

	public static Boolean isSRamEnabled() {
		return rfMemChosen;
	}

	public static void setAnswer(String answer) {
		rfTextCallback.setText(answer);

		// Reset datarate textview
		rfDatarateCallback.setText("");
	}

	public static boolean getChosen() {
		return rfChosen;
	}

	public static String getReadOptions() {
		int id = rfReadOptions.getCheckedRadioButtonId();
		View radioButton = rfReadOptions.findViewById(id);
		int radioId = rfReadOptions.indexOfChild(radioButton);
		RadioButton btn = (RadioButton) rfReadOptions.getChildAt(radioId);
		return (String) btn.getText();
	}

	public static void setReadOptions(int i) {
		rfReadOptions.check(i);
	}

	public static void setDatarateCallback(String datarate) {
        rfDatarateCallback.setText(datarate);
	}

	public static String getDatarateCallback() {
		return rfDatarateCallback.getText().toString();
	}

	@Override
	public void onClick(View v) {
		if (rfMemChosen) {
            StartSRAMSpeedTest();
        } else {
            StartEEPROMSpeedTest();
        }
	}
}
