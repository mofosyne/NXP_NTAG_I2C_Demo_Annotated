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
/***********************************************************************/
/* INCLUDES                                                            */
/***********************************************************************/
#include "HAL_I2C_driver.h"
#include "ntag_bridge.h"
#include "tnpi_bridge.h"

/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/

/***********************************************************************/
/* GLOBAL VARIABLES                                                    */
/***********************************************************************/

/***********************************************************************/
/* LOCAL VARIABLES                                                     */
/***********************************************************************/

/***********************************************************************/
/* GLOBAL FUNCTIONS                                                    */
/***********************************************************************/
//---------------------------------------------------------------------
BOOL TNPI_WaitForEvent(TNPI_HANDLE_T tnpi, NTAG_EVENT_T event,
		uint32_t timeout_ms, BOOL set_fd_pin_function) {
	return NTAG_WaitForEvent(tnpi, event, timeout_ms, set_fd_pin_function);
}

//---------------------------------------------------------------------
BOOL TNPI_SetI2CRstOnOff(TNPI_HANDLE_T tnpi, BOOL on) {
	return NTAG_SetI2CRstOnOff(tnpi, on);
}

//---------------------------------------------------------------------
BOOL TNPI_GetI2CRstOnOff(TNPI_HANDLE_T tnpi, BOOL *on) {
	return NTAG_GetI2CRstOnOff(tnpi, on);
}

//---------------------------------------------------------------------
BOOL TNPI_SetRFConfigurationWrite(TNPI_HANDLE_T tnpi);

//---------------------------------------------------------------------
BOOL TNPI_GetRFConfigurationLock(TNPI_HANDLE_T tnpi, BOOL *locked);

//---------------------------------------------------------------------
BOOL TNPI_SetI2CConfigurationWrite(TNPI_HANDLE_T tnpi);

//---------------------------------------------------------------------
BOOL TNPI_GetI2CConfigurationLock(TNPI_HANDLE_T tnpi, BOOL *locked);

//---------------------------------------------------------------------
BOOL TNPI_GetI2CClockStr(TNPI_HANDLE_T tnpi, BOOL *clk) {
	return NTAG_GetI2CClockStr(tnpi, clk);
}

//---------------------------------------------------------------------
BOOL TNPI_ReleaseI2CLocked(TNPI_HANDLE_T tnpi) {
	return NTAG_ReleaseI2CLocked(tnpi);
}

//---------------------------------------------------------------------
BOOL TNPI_SetTransferDir(TNPI_HANDLE_T tnpi, NTAG_TRANSFER_DIR_T dir) {
	return NTAG_SetTransferDir(tnpi, dir);
}

//---------------------------------------------------------------------
BOOL TNPI_GetTransferDir(TNPI_HANDLE_T tnpi, NTAG_TRANSFER_DIR_T *dir) {
	return NTAG_GetTransferDir(tnpi, dir);
}

//---------------------------------------------------------------------
BOOL TNPI_SetFDOnFunction(TNPI_HANDLE_T tnpi, NTAG_FD_ON_FUNCTIONS_T func) {
	return NTAG_SetFDOnFunction(tnpi, func);
}

//---------------------------------------------------------------------
BOOL TNPI_GetFDOnFunction(TNPI_HANDLE_T tnpi, NTAG_FD_ON_FUNCTIONS_T *func) {
	return NTAG_GetFDOnFunction(tnpi, func);
}

//---------------------------------------------------------------------
BOOL TNPI_SetFDOffFunction(TNPI_HANDLE_T tnpi, NTAG_FD_OFF_FUNCTIONS_T func) {
	return NTAG_SetFDOffFunction(tnpi, func);
}

//---------------------------------------------------------------------
BOOL TNPI_GetFDOffFunction(TNPI_HANDLE_T tnpi, NTAG_FD_OFF_FUNCTIONS_T *func) {
	return NTAG_GetFDOffFunction(tnpi, func);
}

//---------------------------------------------------------------------
BOOL TNPI_SetPthruOnOff(TNPI_HANDLE_T tnpi, BOOL on) {
	return NTAG_SetPthruOnOff(tnpi, on);
}

//---------------------------------------------------------------------
BOOL TNPI_GetPthruOnOff(TNPI_HANDLE_T tnpi, BOOL *on) {
	return NTAG_GetPthruOnOff(tnpi, on);
}

//---------------------------------------------------------------------
BOOL TNPI_SetSRAMMirrorOnOff(TNPI_HANDLE_T tnpi, BOOL on) {
	return NTAG_SetSRAMMirrorOnOff(tnpi, on);
}

//---------------------------------------------------------------------
BOOL TNPI_GetSRAMMirrorOnOff(TNPI_HANDLE_T tnpi, BOOL *on) {
	return NTAG_GetSRAMMirrorOnOff(tnpi, on);
}

//---------------------------------------------------------------------
BOOL TNPI_SetLastNDEFBlock(TNPI_HANDLE_T tnpi, uint8_t block) {
	return NTAG_SetLastNDEFBlock(tnpi, block);
}

//---------------------------------------------------------------------
BOOL TNPI_GetLastNDEFBlock(TNPI_HANDLE_T tnpi, uint8_t *block) {
	return NTAG_GetLastNDEFBlock(tnpi, block);
}

//---------------------------------------------------------------------
BOOL TNPI_SetSRAMMirrorBlock(TNPI_HANDLE_T tnpi, uint8_t block) {
	return NTAG_SetSRAMMirrorBlock(tnpi, block);
}

//---------------------------------------------------------------------
BOOL TNPI_GetSRAMMirrorBlock(TNPI_HANDLE_T tnpi, uint8_t *block) {
	return NTAG_GetSRAMMirrorBlock(tnpi, block);
}

//---------------------------------------------------------------------
BOOL TNPI_SetWatchdogTime(TNPI_HANDLE_T tnpi, uint16_t time) {
	return NTAG_SetWatchdogTime(tnpi, time);
}

//---------------------------------------------------------------------
BOOL TNPI_GetWatchdogTime(TNPI_HANDLE_T tnpi, uint16_t *time) {
	return NTAG_GetWatchdogTime(tnpi, time);
}
