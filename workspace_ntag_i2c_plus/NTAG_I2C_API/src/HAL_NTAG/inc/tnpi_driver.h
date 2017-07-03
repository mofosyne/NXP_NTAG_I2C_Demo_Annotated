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
* conjunction with NXP Semiconductor products(UCODE I2C, NTAG I2C, TNPI).  *
* This  copyright, permission, and disclaimer notice must appear in all    *
* copies of this code.                                                     *
****************************************************************************
*/
#ifndef _TNPI_DRIVER_H_
#define _TNPI_DRIVER_H_
/** @file tnpi_driver.h
 * \brief Public interface to access a TNPI tag over I2C.
 */

/***********************************************************************/
/* INCLUDES                                                            */
/***********************************************************************/
#include "rfid_api_full.h"
#include "global_types.h"
#include "ntag_defines.h"
#include "tnpi_defines.h"

/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/
#define TNPI_INVALID_HANDLE NULL

#ifndef API_DESCRIPTION
#define TNPI_DEVICE_LIST_BEGIN                     typedef enum \
                                                   {
#define TNPI_DEVICE_ENTRY(label, i2c_address, isr)  	label
#define TNPI_DEVICE_LIST_END                        	, TNPI_ID_MAX_DEVICES \
                                                   } TNPI_ID_T;
#endif /* hide from doxygen */

/***********************************************************************/
/* TYPES                                                               */
/***********************************************************************/
#ifndef API_DESCRIPTION
TNPI_DEVICE_LIST_BEGIN
#  include "tnpi_device_list.h"    /* allowed here - generator header */
TNPI_DEVICE_LIST_END
#endif /* hide from doxygen */

typedef struct NTAG_DEVICE* TNPI_HANDLE_T;

/***********************************************************************/
/* GLOBAL VARIABLES                                                    */
/***********************************************************************/

/***********************************************************************/
/* GLOBAL FUNCTION PROTOTYPES                                          */
/***********************************************************************/

/**
 * \brief initialize the selected TNPI device for operation
 *
 *	This function registers the specified I2C bus and returns a
 *	handle to the selected device to be used for further operations.
 *
 * \param	tnpi_id	identifier of device to be selected
 * \param	i2cbus	handle to I2C bus instance to be used
 *
 * \return 		 	TNPI_INVALID_HANDLE on failure
 */
TNPI_HANDLE_T TNPI_InitDevice(TNPI_ID_T tnpi_id, HAL_I2C_HANDLE_T i2cbus);

/**
 * \brief close handle and unregister I2C bus instance
 *
 *	This function unregisters the associated I2C bus handle and closes
 *	the handle. The handle can no longer be used for further operation.
 *
 * \param	tnpi	handle to identify the TNPI device instance
 *
 * \return	none
 */
void TNPI_CloseDevice(TNPI_HANDLE_T tnpi);

/**
 * \brief read len number of bytes from the selected TNPI device
 *
 *	This functions reads the specified number of bytes from the selected
 *	TNPI device, starting at the given address. Reading from an invalid address
 *	will result in a failed read.
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	bytes	array of bytes to store read data
 * \param	address	byte address in device memory space to start reading from
 * \param	len 	number of bytes to be read
 *
 * \return			TRUE on failure
 */
BOOL TNPI_ReadBytes (TNPI_HANDLE_T tnpi, uint16_t address,       uint8_t *bytes, uint16_t len);

/**
 * \brief write len number of bytes to the selected TNPI device
 *
 *	This functions writes the specified number of bytes to the given address of
 *	the selected TNPI device. Writing to an invalid address
 *	is undefined.
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	bytes	array of bytes to be written
 * \param	address	byte address in device memory space to start writing to
 * \param	len 	number of bytes to be written
 *
 * \return			TRUE on failure
 */
BOOL TNPI_WriteBytes(TNPI_HANDLE_T tnpi, uint16_t address, const uint8_t *bytes, uint16_t len);

/**
 * \brief read a register from the selected TNPI device
 *
 *	This functions reads the specified 8-bit register from the selected
 *	TNPI device.
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	reg 	register offset from the start of the register block
 * \param	val 	byte to store read value
 *
 * \return			TRUE on failure
 */
BOOL TNPI_ReadRegister (TNPI_HANDLE_T tnpi, uint8_t reg, uint8_t *val);

/**
 * \brief write a register of the selected TNPI device
 *
 *	This functions writes the specified 8-bit register of the selected
 *	TNPI device. Only the mask selected bits will be written.
 *
 * \param	tnpi	handle to identify the TNPI device instance
 * \param	reg 	register offset from the start of the register block
 * \param	mask	only selected(1) bits will be written
 * \param	val 	8-bit value to be written
 *
 * \return			TRUE on failure
 */
BOOL TNPI_WriteRegister(TNPI_HANDLE_T tnpi, uint8_t reg, uint8_t mask, uint8_t val);

/**
 * \brief get the error code of the last failure
 *
 *	If the previous call to any of the read/write function failed this function
 *	will return the reason via the corresponding error code. The error code is not
 *	latched, therefore any successful read/write after a failure will reset the
 *	error code.
 *	This function will return TNPI_CLOSED on a closed handle.
 *
 * \param	tnpi	handle to identify the TNPI device instance
 *
 * \return			TNPI status code
 */
NTAG_STATUS_T TNPI_GetLastError(TNPI_HANDLE_T tnpi);

#endif /* _TNPI_DRIVER_H_ */
