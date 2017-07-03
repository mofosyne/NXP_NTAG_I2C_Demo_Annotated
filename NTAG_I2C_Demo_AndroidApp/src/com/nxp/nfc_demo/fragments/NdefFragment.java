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

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.nxp.nfc_demo.activities.MainActivity;
import com.nxp.ntagi2cdemo.R;

public class NdefFragment extends Fragment implements OnClickListener,
		OnCheckedChangeListener {

	private static RadioGroup ndefWriteOptions;
	private static LinearLayout ndefReadType;
	private static TextView ndefText;
	private static EditText ndefEditText;
	private LinearLayout linearBt;
	private static EditText ndefEditMac;
	private static EditText ndefEditName;
	private static EditText ndefEditClass;
	private LinearLayout linearSp;
	private static EditText ndefEditTitle;
	private static EditText ndefEditLink;
	private static TextView ndefTypeText;
	private static TextView ndefCallback;
	private static TextView ndefDataRateCallback;
	private TextView ndefPerformance;
	private Button readNdefButton;
	private Button writeNdefButton;
	private Button writeDefaultNdefButton;
	private static CheckBox ndefReadLoop;
	private static CheckBox addAar;
	private static boolean writeChosen = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = inflater
				.inflate(R.layout.fragment_ndef, container, false);

		ndefText = (TextView) layout.findViewById(R.id.ndefText);
		ndefEditText = (EditText) layout.findViewById(R.id.ndefEditText);
		linearBt = (LinearLayout) layout.findViewById(R.id.layoutBt);
		ndefEditMac = (EditText) layout.findViewById(R.id.ndefEditMac);
		ndefEditName = (EditText) layout.findViewById(R.id.ndefEditName);
		ndefEditClass = (EditText) layout.findViewById(R.id.ndefEditClass);
		linearSp = (LinearLayout) layout.findViewById(R.id.layoutSp);
		ndefEditTitle = (EditText) layout.findViewById(R.id.ndefEditTitle);
		ndefEditLink = (EditText) layout.findViewById(R.id.ndefEditLink);
		ndefTypeText = (TextView) layout.findViewById(R.id.ndefTypeText);
		ndefCallback = (TextView) layout.findViewById(R.id.ndef_textCallback);
		ndefDataRateCallback = (TextView) layout.findViewById(R.id.ndef_datarateCallback);
		ndefPerformance = (TextView) layout.findViewById(R.id.ndef_performance);
		ndefWriteOptions = (RadioGroup) layout.findViewById(R.id.ndefOptions);
		ndefReadType = (LinearLayout) layout.findViewById(R.id.ndefTypeLayout);
		readNdefButton = (Button) layout.findViewById(R.id.readNdefButton);
		writeNdefButton = (Button) layout.findViewById(R.id.writeNdefButton);
		writeDefaultNdefButton = (Button) layout.findViewById(R.id.writeDefaultButton);
		addAar = (CheckBox) layout.findViewById(R.id.Add_aar_checkbox);
		ndefReadLoop = (CheckBox) layout.findViewById(R.id.ndef_readLoop);
		ndefReadLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// Read content
				if (isChecked == true && MainActivity.demo.isReady()) {
					MainActivity.demo.finishAllTasks();
					MainActivity.launchNdefDemo(MainActivity.getAuthStatus(), MainActivity.getPassword());
				}
			}
		});
		readNdefButton.setOnClickListener(this);
		writeNdefButton.setOnClickListener(this);
		writeDefaultNdefButton.setOnClickListener(this);
		ndefWriteOptions.setOnCheckedChangeListener(this);

		// Set the variable to false to avoid sending the last selected value
		writeChosen = false;

		return layout;
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.readNdefButton:
			// Reset the values of the view
			MainActivity.demo.NDEFReadFinish();
			ndefPerformance.setText(getResources().getString(R.string.layout_input_ndef_read));
			ndefCallback.setText(getResources().getString(R.string.readNdefMsg));
			readNdefButton.setBackgroundResource(R.drawable.btn_blue);
			writeNdefButton.setBackgroundColor(Color.BLACK);
			ndefWriteOptions.setVisibility(View.GONE);
			ndefReadType.setVisibility(View.VISIBLE);
			linearBt.setVisibility(View.GONE);
			linearSp.setVisibility(View.GONE);
			ndefEditText.setVisibility(View.GONE);
			ndefText.setVisibility(View.VISIBLE);
			ndefReadLoop.setVisibility(View.VISIBLE);
			writeChosen = false;
			
			// Read content
			if (MainActivity.demo.isReady()) {
				MainActivity.demo.finishAllTasks();
				MainActivity.launchNdefDemo(MainActivity.getAuthStatus(), MainActivity.getPassword());
			}
			
			// Make the writeDefaultButtons and AAR checkbox invisible
			writeDefaultNdefButton.setVisibility(View.GONE);
			addAar.setVisibility(View.GONE);

			break;

		case R.id.writeNdefButton:
			// Make the writeDefaultButtons and AAR checkbox visible
			writeDefaultNdefButton.setVisibility(View.VISIBLE);
			addAar.setVisibility(View.VISIBLE);
			ndefPerformance.setText(getResources().getString(R.string.layout_input_ndef_write));
			ndefCallback.setText(getResources().getString(R.string.writeNdefMsg));
			
			// Close the ReadNdef Taks
			MainActivity.demo.NDEFReadFinish();
			
			if (writeChosen == true)
			{
				if (MainActivity.demo.isReady()) {
					MainActivity.demo.finishAllTasks();
					MainActivity.launchNdefDemo(MainActivity.getAuthStatus(), MainActivity.getPassword());
				}
			}
			else {
				ndefCallback.setText(getResources().getString(R.string.writeNdefMsg));
				writeNdefButton.setBackgroundResource(R.drawable.btn_blue);
				readNdefButton.setBackgroundColor(Color.BLACK);
				ndefWriteOptions.setVisibility(View.VISIBLE);
				ndefReadType.setVisibility(View.GONE);
				ndefReadLoop.setVisibility(View.GONE);
				if (getNdefType().equals(
						getResources().getString(R.string.radio_btpair))) {
					linearBt.setVisibility(View.VISIBLE);
					linearSp.setVisibility(View.GONE);
					ndefEditText.setVisibility(View.GONE);
				} else if (getNdefType().equals(
						getResources().getString(R.string.radio_sp))) {
					linearBt.setVisibility(View.GONE);
					linearSp.setVisibility(View.VISIBLE);
					ndefEditText.setVisibility(View.GONE);
				} else {
					linearBt.setVisibility(View.GONE);
					linearSp.setVisibility(View.GONE);
					ndefEditText.setVisibility(View.VISIBLE);
				}
				ndefText.setVisibility(View.GONE);
				writeChosen = true;
			}

			break;
			
			case R.id.writeDefaultButton:
				ndefCallback.setText(getResources()
						.getString(R.string.writeNdefMsg));
				writeNdefButton.setBackgroundResource(R.drawable.btn_blue);
				readNdefButton.setBackgroundColor(Color.BLACK);
				ndefWriteOptions.setVisibility(View.VISIBLE);
				ndefReadType.setVisibility(View.GONE);
				RadioButton uri = (RadioButton) ndefWriteOptions.getChildAt(6);
				uri.setChecked(true);
				linearSp.setVisibility(View.VISIBLE);
				linearBt.setVisibility(View.GONE);
				ndefEditText.setVisibility(View.GONE);
				ndefEditTitle.setText(getResources().getString(R.string.ndef_default_text));
				ndefEditLink.setText(getResources().getString(R.string.ndef_default_uri));
				ndefText.setVisibility(View.GONE);
				addAar.setChecked(true);
				writeChosen = true;
	
				// Write content
				if (MainActivity.demo.isReady()) {
					MainActivity.demo.finishAllTasks();
					MainActivity.launchNdefDemo(MainActivity.getAuthStatus(),
                            MainActivity.getPassword());
				}
				break;
		default:
			break;
		}
	} // END onClick (View v)

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (checkedId == R.id.radioNdefText) {
			ndefEditText.setVisibility(View.VISIBLE);
			ndefEditText.setText("");
			linearBt.setVisibility(View.GONE);
			linearSp.setVisibility(View.GONE);
		} else if (checkedId == R.id.radioNdefUrl) {
			ndefEditText.setVisibility(View.VISIBLE);
			ndefEditText.setText("http://www.");
			linearBt.setVisibility(View.GONE);
			linearSp.setVisibility(View.GONE);
		} else if (checkedId == R.id.radioNdefBt) {
			ndefEditText.setVisibility(View.GONE);
			linearBt.setVisibility(View.VISIBLE);
			linearSp.setVisibility(View.GONE);
		} else if (checkedId == R.id.radioNdefSp) {
			ndefEditText.setVisibility(View.GONE);
			linearBt.setVisibility(View.GONE);
			linearSp.setVisibility(View.VISIBLE);
			ndefEditLink.setText("http://www.");
		}
	}
	
	public static void resetNdefDemo() {
		if (writeChosen == true) {
            setAnswer("Tap tag to write NDEF content");
        } else {
            setAnswer("Tap tag to read NDEF content");
        }
		setNdefMessage("");
		setNdefType("");
		setDatarate("");
		setNdefType("");
	}

	public static String getText() {
		return ndefEditText.getText().toString();
	}

	public static String getBtMac() {
		return ndefEditMac.getText().toString();
	}

	public static String getBtName() {
		return ndefEditName.getText().toString();
	}

	public static String getBtClass() {
		return ndefEditClass.getText().toString();
	}
	
	public static String getSpTitle() {
		return ndefEditTitle.getText().toString();
	}
	
	public static String getSpLink() {
		return ndefEditLink.getText().toString();
	}

	public static boolean isAarRecordSelected() {
		return addAar.isChecked();
	}
	
	public static boolean isNdefReadLoopSelected() {
		return ndefReadLoop.isChecked();
	}
	
	public static void setAnswer(String answer) {
		ndefCallback.setText(answer);
	}
	
	public static void setDatarate(String datarate) {
		ndefDataRateCallback.setText(datarate);
	}

	public static void setNdefType(String type) {
		ndefTypeText.setText(type);
	}

	public static void setNdefMessage(String answer) {
		ndefText.setText(answer);
	}

	public static String getNdefType() {
		int id = ndefWriteOptions.getCheckedRadioButtonId();
		View radioButton = ndefWriteOptions.findViewById(id);
		int radioId = ndefWriteOptions.indexOfChild(radioButton);
		RadioButton btn = (RadioButton) ndefWriteOptions.getChildAt(radioId);
		return (String) btn.getText();
	}

	public static boolean isWriteChosen() {
		return writeChosen;
	}
}
