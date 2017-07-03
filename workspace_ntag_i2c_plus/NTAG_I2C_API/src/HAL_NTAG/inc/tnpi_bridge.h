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
#ifndef _TNPI_BRIDGE_H_
#define _TNPI_BRIDGE_H_
/** @file tnpi_bridge.h
 * \brief Public interface for using an TNPI I2C tag as I2C<->NFC bridge.
 */
/***********************************************************************/
/* INCLUDES                                                            */
/***********************************************************************/
#include "global_types.h"
#include "ntag_driver.h"
#include "tnpi_driver.h"
#include "ntag_bridge.h"

/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/

/***********************************************************************/
/* TYPES                                                               */
/***********************************************************************/

/***********************************************************************/
/* GLOBAL VARIABLES                                                    */
/***********************************************************************/

/***********************************************************************/
/* GLOBAL FUNCTION PROTOTYPES                                          */
/***********************************************************************/

/**
 * \brief wait for selected event
 *
 *	This functions waits until the selected event occurs or the timeout value is
 *	reached. See NTAG_EVENT_T for possible events to be waited on.
 *	If you want to use the Interrupted Events set the FD function accordingly
 *	before calling this function. Notice that some Interrupted Events are
 *	indistinguishable, so it will trigger at a false event.
 *
 * \param	tnpi      	handle to identify the TNPI device instance
 * \param	event     	event to be waited for
 * \param	timeout_ms	timeout value in ms
 * \param   set_fd_pin_function   when using an INTERRUPT EVENT
 * 								  if true FD_ON/FD_OFF is set to the according funtion priore to waiting for the event.
 * 								  if false nothing is done, so you should set the FD Pin function prior to calling this.
 * 								  when using an POLLING EVENT this param is ignored
 *
 * \return			TRUE on timeout
 */
BOOL TNPI_WaitForEvent(TNPI_HANDLE_T tnpi, NTAG_EVENT_T event, uint32_t timeout_ms, BOOL set_fd_pin_function);

/**
 * \brief enable/disable the I2C_RST
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	on		true enables the I2C_RST , false disable the I2C_RST
 *
 * \return	error code as defined in ntag_defines.h
 */
BOOL TNPI_SetI2CRstOnOff(TNPI_HANDLE_T tnpi, BOOL on);

/**
 * \brief get the status of the I2C_RST
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	on		status of the I2C_RST, true is on, false is off
 *
 * \return	error code as defined in ntag_defines.h
 */
BOOL TNPI_GetI2CRstOnOff(TNPI_HANDLE_T tnpi, BOOL *on);

// TODO: check if Config write is the same as NTAG
/**
 * \brief Locks the write of the Configuration from RF, note that this is
 *        one time programmable
 *
 * \param	tnpi	handle to identify the TNPI device instance
 *
 * \return			TRUE on failure*
 */
//BOOL TNPI_SetRFConfigurationWrite(TNPI_HANDLE_T tnpi);

/**
 * \brief Gets the Lock status of the RF Configuration Lock
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	locked	true RF writes to the Configuration is locked
 * 					false RF writes to the Configuration is possible
 *
 * \return			TRUE on failure*
 */
//BOOL TNPI_GetRFConfigurationLock(TNPI_HANDLE_T tnpi, BOOL *locked);

/**
 * \brief Locks the write of the Configuration from I2C, note that this is
 *        one time programmable
 *
 * \param	tnpi	handle to identify the TNPI device instance
 *
 * \return			TRUE on failure*
 */
//BOOL TNPI_SetI2CConfigurationWrite(TNPI_HANDLE_T tnpi);

/**
 * \brief Gets the Lock status of the I2C Configuration Lock
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	locked	true I2C writes to the Configuration is locked
 * 					false I2C writes to the Configuration is possible
 *
 * \return			TRUE on failure*
 */
//BOOL TNPI_GetI2CConfigurationLock(TNPI_HANDLE_T tnpi, BOOL *locked);

/**
 * \brief get the I2C_CLOCK_STR
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	clk		the I2C_CLOCK_STR setting
 *
 * \return			TRUE on failure*
 */
BOOL TNPI_GetI2CClockStr(TNPI_HANDLE_T tnpi, BOOL *clk);

/**
 * \brief Sets the I2C_LOCKED bit to 0 to free the LOCK immediately
 *
 * \param	tnpi	handle to identify the TNPI device instance
 *
 * \return			TRUE on failure*
 */
BOOL TNPI_ReleaseI2CLocked(TNPI_HANDLE_T tnpi);

/**
 * \brief set the transfer direction
 *
 * If the PT direction is already the desired direction nothing is done.
 * If the Pthru is switched on, it will be switched off and back on after the direction change.
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	dir		Direction of the transfer
 *
 * \return			TRUE on failure*
 */
BOOL TNPI_SetTransferDir(TNPI_HANDLE_T tnpi, NTAG_TRANSFER_DIR_T dir);

/**
 * \brief get the transfer direction
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	dir		Direction of the transfer
 *
 * \return			TRUE on failure*
 */
BOOL TNPI_GetTransferDir(TNPI_HANDLE_T tnpi, NTAG_TRANSFER_DIR_T *dir);

/**
 * \brief set the FD ON function
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	func	Function which should be set
 *
 * \return			TRUE on failure*
 */
BOOL TNPI_SetFDOnFunction(TNPI_HANDLE_T tnpi, NTAG_FD_ON_FUNCTIONS_T func);

/**
 * \brief get the FD ON function
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	func	Function which is set
 *
 * \return			TRUE on failure*
 */
BOOL TNPI_GetFDOnFunction(TNPI_HANDLE_T tnpi, NTAG_FD_ON_FUNCTIONS_T *func);


/**
 * \brief set the FD OFF functionFF
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	func	Function which should be set
 *
 * \return			TRUE on failure*
 */
BOOL TNPI_SetFDOffFunction(TNPI_HANDLE_T tnpi, NTAG_FD_OFF_FUNCTIONS_T func);

/**
 * \brief get the FD OFF function
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	func	Function which is set
 *
 * \return			TRUE on failure*
 */
BOOL TNPI_GetFDOffFunction(TNPI_HANDLE_T tnpi, NTAG_FD_OFF_FUNCTIONS_T *func);

/**
 * \brief enable/disable the I2C<->NFC bridge
 *
 * Activates the pass-through mode, that uses the TNPI's 84 Byte SRAM
 * for communication between a NFC device and the I2C bus
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	on		true enables the Pthru , false disable the Pthru
 *
 * \return	error code as defined in ntag_defines.h
 */
BOOL TNPI_SetPthruOnOff(TNPI_HANDLE_T tnpi, BOOL on);

/**
 * \brief get the status of the Pthru
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	on		status of Pthru, true is on, false is off
 *
 * \return	error code as defined in ntag_defines.h
 */
BOOL TNPI_GetPthruOnOff(TNPI_HANDLE_T tnpi, BOOL *on);

/**
 * \brief enable/disable SRAM projection to user memory addresses
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	on		true enables the SRAM Mirror , false disable the SRAM Mirror
 *
 * \return			TRUE on failure*
 */
BOOL TNPI_SetSRAMMirrorOnOff(TNPI_HANDLE_T tnpi, BOOL on);

/**
 * \brief get the status of the SRAM Mirror to user memory
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	on		status of the SRAM Mirror, true is on, false is off
 *
 * \return			TRUE on failure*
 */
BOOL TNPI_GetSRAMMirrorOnOff(TNPI_HANDLE_T tnpi, BOOL *on);

/**
 * \brief change the LAST_NDEF_BLOCK
 *
 *  Change the first block of user memory when using the LAST_NDEF_BLOCK.
 *  Granularity is 4 pages(4bytes each), so block * 4 is the real page address.
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	block	block address, 0x74 is the highest allowed value
 * \return			TRUE on failure*
 */
BOOL TNPI_SetLastNDEFBlock(TNPI_HANDLE_T tnpi, uint8_t block);

/**
 * \brief get the LAST_NDEF_BLOCK
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	block	LAST_NDEF_BLOCK
 *
 * \return			TRUE on failure*
 */
BOOL TNPI_GetLastNDEFBlock(TNPI_HANDLE_T tnpi, uint8_t *block);

/**
 * \brief change the first block of user memory for the SRAM Mirror
 *
 *  Change the first block of user memory when mirroring the SRAM.
 *  Granularity is 4 pages(4bytes each), so block * 4 is the real page address.
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	block	block to which the SRAM should be mirrored
 * \return			TRUE on failure*
 */
BOOL TNPI_SetSRAMMirrorBlock(TNPI_HANDLE_T tnpi, uint8_t block);

/**
 * \brief get the first block of user memory for the SRAM Mirror
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	block	block to which the SRAM is mirrored
 * \return			TRUE on failure*
 */
BOOL TNPI_GetSRAMMirrorBlock(TNPI_HANDLE_T tnpi, uint8_t *block);

/**
 * \brief change the value of the watchdog timer
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	time	new time value of the watchdog timer
 * \return			TRUE on failure*
 */
BOOL TNPI_SetWatchdogTime(TNPI_HANDLE_T tnpi, uint16_t time);

/**
 * \brief get the value of the watchdog timer
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	time	Watchdog time
 * \return			TRUE on failure*
 */
BOOL TNPI_GetWatchdogTime(TNPI_HANDLE_T tnpi, uint16_t *time);


#endif /* _TNPI_BRIDGE_H_ */
