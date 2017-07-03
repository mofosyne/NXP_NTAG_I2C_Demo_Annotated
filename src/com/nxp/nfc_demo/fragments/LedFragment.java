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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.nxp.ntagi2cdemo.R;

public class LedFragment extends Fragment implements OnClickListener {

	private ImageView ntagLogo;
	private Animation anim;

	static ImageView nxpPressedButtons;
	private static CheckBox lcdCheck;
	private static CheckBox tempCheck;
	private static CheckBox Scroll_check;
	private static TextView textCallback;
	private static TextView texttransferDir;
	private static TextView textSelectColor;
	private static String option;
	private static String lastOption;
	private static double voltage;
	private static double temperatureC;
	private static double temperatureF;
	private boolean isSwitchedOn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		anim = AnimationUtils.loadAnimation(getActivity(), R.anim.ntag);
		voltage = 0;
		temperatureC = 0;
		temperatureF = 0;
		// We start with L2 so that Blue LED is switched on
		option = "L2";
		lastOption = "L2";
		isSwitchedOn = true;
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.fragment_leddemo, container,
				false);

		initVariables(layout);
		setButtonCallbacks(layout);
		refreschOption();
		return layout;
	}

	private void setButtonCallbacks(View layout) {
		((Button) layout.findViewById(R.id.redButton)).setOnClickListener(this);
		((Button) layout.findViewById(R.id.blueButton)).setOnClickListener(this);
		((Button) layout.findViewById(R.id.greenButton)).setOnClickListener(this);
		((Button) layout.findViewById(R.id.offButton)).setOnClickListener(this);
	}

	private void initVariables(View layout) {
		nxpPressedButtons = (ImageView) layout.findViewById(R.id.nxp_pressed_buttons);
		lcdCheck = (CheckBox) layout.findViewById(R.id.LCD_checkbox);
		tempCheck = (CheckBox) layout.findViewById(R.id.Temp_Sensor_checkbox);
		Scroll_check = (CheckBox) layout.findViewById(R.id.Ndef_Scroll_checkbox);
		
		// Scroll is only available when LCD is enabled
		lcdCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					Scroll_check.setVisibility(View.VISIBLE);
				} else {
					Scroll_check.setChecked(false);
					Scroll_check.setVisibility(View.GONE);
				}
			}
		});
		ntagLogo = (ImageView) layout.findViewById(R.id.trafficlight);
		textCallback = (TextView) layout.findViewById(R.id.textCallback);
		texttransferDir = (TextView) layout.findViewById(R.id.TransferDirection);
		textSelectColor = (TextView) layout.findViewById(R.id.textSelectColor);
		
		// Let the user switch off the LED by clicking on the NTAG logo
		ntagLogo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isSwitchedOn) {
					option = "L0";
					refreschOption();
					textSelectColor.setText(getResources().getString(R.string.tap_switch_on));
					isSwitchedOn = false;
				} else {
					option = lastOption;
					refreschOption();
					textSelectColor.setText(getResources().getString(R.string.tap_switch_off));
					isSwitchedOn = true;
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.offButton:
			option = "L0";
			lastOption = "L0";
			break;
		case R.id.redButton:
			option = "L1";
			lastOption = "L1";
			break;
		case R.id.blueButton:
			option = "L2";
			lastOption = "L2";
			break;
		case R.id.greenButton:
			option = "L3";
			lastOption = "L3";
			break;
		default:
			break;
		}
		refreschOption();
	}

	private void refreschOption() {
		if (option.equals("L0")) {
			ntagLogo.setImageResource(R.drawable.ntaggrey);
		} else if (option.equals("L1")) {
			ntagLogo.setImageResource(R.drawable.ntagorange);
		} else if (option.equals("L2")) {
			ntagLogo.setImageResource(R.drawable.ntagblue);
		} else if (option.equals("L3")) {
			ntagLogo.setImageResource(R.drawable.ntaggreen);
		}
		ntagLogo.startAnimation(anim);
	}

	public static void setButton(byte Bit_field) {
		int pressed = 0;
		
		if ((Bit_field & 0x01) == 0x01) {
			pressed = pressed + 1;
		}
		if ((Bit_field & 0x02) == 0x02) {
			pressed = pressed + 2;
		}
		if ((Bit_field & 0x04) == 0x04) {
			pressed = pressed + 4;
		} 
		
		switch (pressed) {
		case 0 : 
			nxpPressedButtons.setImageResource(R.drawable.no_pressed);
			break;
		case 1 : 
			nxpPressedButtons.setImageResource(R.drawable.left_pressed);
			break;
		case 2 : 
			nxpPressedButtons.setImageResource(R.drawable.middle_pressed);
			break;
		case 3 : 
			nxpPressedButtons.setImageResource(R.drawable.left_middle_pressed);
			break;
		case 4 : 
			nxpPressedButtons.setImageResource(R.drawable.right_pressed);
			break;
		case 5 : 
			nxpPressedButtons.setImageResource(R.drawable.right_left_pressed);
			break;
		case 6 : 
			nxpPressedButtons.setImageResource(R.drawable.middle_right_pressed);
			break;
		case 7 : 
			nxpPressedButtons.setImageResource(R.drawable.all_pressed);
			break;
		default:
			nxpPressedButtons.setImageResource(R.drawable.no_pressed);
			break;
		}
	}

	public static String getOption() {
		return option;
	}

	public static double getVoltage() {
		return voltage;
	}

	public static double getTemperatureC() {
		return temperatureC;
	}

	public static double getTemperatureF() {
		return temperatureF;
	}

	public static boolean isScrollEnabled() {
		return Scroll_check.isChecked();
	}
	
	public static boolean isLCDEnabled() {
		return lcdCheck.isChecked();
	}
	
	public static boolean isTempEnabled() {
		return tempCheck.isChecked();
	}
	
	public static void setTransferDir(String answer) {
		texttransferDir.setText(answer);
	}

	public static void setAnswer(String answer) {
		textCallback.setText(answer);
	}

	public static void setVoltage(double v) {
		voltage = v;
	}

	public static void setTemperatureC(double t) {
		temperatureC = t;
	}

	public static void setTemperatureF(double t) {
		temperatureF = t;
	}
}
