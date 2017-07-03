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


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nxp.nfc_demo.activities.MainActivity;
import com.nxp.nfc_demo.activities.ReadMemoryActivity;
import com.nxp.nfc_demo.activities.RegisterConfigActivity;
import com.nxp.nfc_demo.activities.RegisterSessionActivity;
import com.nxp.nfc_demo.activities.ResetMemoryActivity;
import com.nxp.ntagi2cdemo.R;

public class ConfigFragment extends Fragment implements OnClickListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_config, container, false);
		
		((ImageView) v.findViewById(R.id.configSessionRegister)).setOnClickListener(this);
		((ImageView) v.findViewById(R.id.configConfigRegister)).setOnClickListener(this);
		((ImageView) v.findViewById(R.id.readMemory)).setOnClickListener(this);
		((ImageView) v.findViewById(R.id.resetMemory)).setOnClickListener(this);
		
		return v;
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		
		switch(v.getId()) {
			case R.id.readMemory:
				intent= new Intent(getActivity(), ReadMemoryActivity.class);
				break;
				
			case R.id.resetMemory:
				intent  = new Intent(getActivity(), ResetMemoryActivity.class);
				break;
		
			case R.id.configSessionRegister:
				intent  = new Intent(getActivity(), RegisterSessionActivity.class);
				break;
				
			case R.id.configConfigRegister:
				intent = new Intent(getActivity(), RegisterConfigActivity.class);
				break;
		}
		
		if(MainActivity.getmIntent() != null)
			intent.putExtras(MainActivity.getmIntent());
		
		startActivity(intent);
	}
}
