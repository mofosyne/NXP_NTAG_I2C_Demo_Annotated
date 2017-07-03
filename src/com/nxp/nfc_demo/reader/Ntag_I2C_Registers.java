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
package com.nxp.nfc_demo.reader;

/**
 * Class for the Config/Session Register settings
 * @author NXP67729
 *
 */
public class Ntag_I2C_Registers {
	public String Manufacture;
	public int Mem_size;
	public Boolean I2C_RST_ON_OFF;
	public String FD_OFF;
	public String FD_ON;
	public int LAST_NDEF_PAGE;
	public Boolean NDEF_DATA_READ;
	public Boolean RF_FIELD_PRESENT;
	public Boolean PTHRU_ON_OFF;
	public Boolean I2C_LOCKED;
	public Boolean RF_LOCKED;
	public Boolean SRAM_I2C_READY;
	public Boolean SRAM_RF_READY;
	public Boolean PTHRU_DIR;
	public int SM_Reg;
	public int WD_LS_Reg;
	public int WD_MS_Reg;
	public String NDEF_Message;
	public Boolean SRAM_MIRROR_ON_OFF;
	public Boolean I2C_CLOCK_STR;

}
