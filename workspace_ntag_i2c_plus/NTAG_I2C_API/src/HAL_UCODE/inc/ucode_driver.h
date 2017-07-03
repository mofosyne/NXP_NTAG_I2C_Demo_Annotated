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
#ifndef _UCODE_DRIVER_H_
#define _UCODE_DRIVER_H_
/** @file ucode_driver.h
 * \brief Public interface to access a UCODE I2C tag over I2C.
 */

/***********************************************************************/
/* INCLUDES                                                            */
/***********************************************************************/
#include "global_types.h"

/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/
#define UCODE_INVALID_HANDLE NULL

#define UCODE_PAGE_SIZE				4
#define UCODE_WORD_SIZE				2

#define UCODE_BANK00_BASE_ADDRESS	/* not accessible via I2C */
#define UCODE_BANK01_BASE_ADDRESS	0x2000
#define UCODE_BANK10_BASE_ADDRESS	0x4000
#define UCODE_BANK11_BASE_ADDRESS	0x6000

#define UCODE_EPC_REG_SIZE			2

#define UCODE_EPC                   UCODE_BANK01_BASE_ADDRESS
#  define UCODE_EPC_CRC16_REG		UCODE_EPC + 0x00
#  define UCODE_EPC_PC_REG			UCODE_EPC + 0x02
#  define UCODE_EPC_NUMBER			UCODE_EPC + 0x04
#  define UCODE_EPC_DL_REG			UCODE_EPC + 0x3E
#  define UCODE_EPC_CONFIG_WORD		UCODE_EPC + 0x40
#    define UCODE_CONFIG_DL_FLAG	(1 <<  7) /* download indicator   RO */
#    define UCODE_CONFIG_EXT_VDD	(1 <<  6) /* external supply      RO */
#    define UCODE_CONFIG_RF_ACTIVE	(1 <<  5) /* RF active flag       RO */
#    define UCODE_CONFIG_UL_FLAG	(1 <<  4) /* upload indicator     RO */
#    define UCODE_CONFIG_I2C_ADDRESS_BIT3	(1 <<  3) /* I2C address bit3  RO */
#    define UCODE_CONFIG_I2C_ADDRESS_BIT2	(1 <<  2) /* I2C address bit2  RO */
#    define UCODE_CONFIG_I2C_ADDRESS_BIT1	(1 <<  1) /* I2C address bit1  RO */
#    define UCODE_CONFIG_I2C_PORT	(1 <<   0) /* I2C port on/off       1 */
#    define UCODE_CONFIG_UHF_PORT1	(1 <<  15) /* UHF port 1 on/off    RW */
#    define UCODE_CONFIG_UHF_PORT2	(1 <<  14) /* UHF port 2 on/off    RW ( SL3S4021 only ) */
#    define UCODE_CONFIG_RESERVED	(1 <<  13) /* reserved */
#    define UCODE_CONFIG_SCL_INT	(1 <<  12) /* SCL interrupt signal RO */
#    define UCODE_CONFIG_LOCK_USER	(1 <<  11) /* read protect user memory   RW */
#    define UCODE_CONFIG_LOCK_EPC	(1 <<  10) /* read protect EPC region    RW */
#    define UCODE_CONFIG_LOCK_TID	(1 <<   9) /* read protect TID SNR 48bit RW */
#    define UCODE_CONFIG_PSF_ALARM	(1 <<   8) /* PSF alarm flag       RO */

#define UCODE_TID_SIZE				12

#define UCODE_TID					UCODE_BANK10_BASE_ADDRESS
#  define UCODE_TID_HEADER      	UCODE_TID + 0x00
#  define UCODE_TID_XTID_HEADER 	UCODE_TID + 0x04
#  define UCODE_TID_SERIAL_NR   	UCODE_TID + 0x06

#define UCODE_USER_MEMORY           UCODE_BANK11_BASE_ADDRESS

#ifndef API_DESCRIPTION
#  define UCODE_DEVICE_LIST_BEGIN                 typedef enum \
                                                  {
#  define UCODE_DEVICE_ENTRY(label, i2c_address)  	label
#  define UCODE_DEVICE_LIST_END                   	, UCODE_ID_MAX_DEVICES \
                                                  } UCODE_ID_T;
#endif /* hide from doxygen */
/***********************************************************************/
/* TYPES                                                               */
/***********************************************************************/
#ifndef API_DESCRIPTION
UCODE_DEVICE_LIST_BEGIN
#  include "ucode_device_list.h"    /* allowed here - generator header */
UCODE_DEVICE_LIST_END
#endif

typedef struct UCODE_DEVICE* UCODE_HANDLE_T;

typedef enum
{
	UCODE_OK,
	UCODE_ERROR_TX_FAILED,
	UCODE_ERROR_RX_FAILED,
	UCODE_ERROR_WRITE_TIMEOUT,
	UCODE_ERROR_INVALID_PARAM,
	UCODE_CLOSED,
	UCODE_STATUS_MAX_NUMBER
} UCODE_STATUS_T;

/***********************************************************************/
/* GLOBAL VARIABLES                                                    */
/***********************************************************************/

/***********************************************************************/
/* GLOBAL FUNCTION PROTOTYPES                                          */
/***********************************************************************/

/**
 * \brief initialize the selected UCODE device for operation
 *
 *	This function registers the specified I2C bus and returns a
 *	handle to the selected device to be used for further operations.
 *
 * \param	ucode_id	identifier of device to be selected
 * \param	i2cbus  	handle to I2C bus instance to be used
 *
 * \return	        	UCODE_INVALID_HANDLE on failure
 */
UCODE_HANDLE_T UCODE_InitDevice(UCODE_ID_T ucode_id, HAL_I2C_HANDLE_T i2cbus);

/**
 * \brief close handle and unregister I2C bus instance
 *
 *	This function unregisters the associated I2C bus handle and closes
 *	the handle. The handle can no longer be used for further operation.
 *
 * \param	ucode	handle to identify the NTAG device instance
 *
 * \return	none
 */
void UCODE_CloseDevice(UCODE_HANDLE_T ucode);

/**
 * \brief read len number of bytes from the selected UCODE device
 *
 *	This functions reads the specified number of bytes from the selected
 *	UCODE device, starting at the given address. Reading from an invalid address
 *	will result in a failed read.
 *
 * \param	ucode	handle to identify the UCODE device instance
 * \param	bytes	array of bytes to store read data
 * \param	address	byte address in device memory space to start reading from
 * \param	len 	number of bytes to be read
 *
 * \return			TRUE on failure
 */
BOOL UCODE_ReadBytes (UCODE_HANDLE_T ucode, uint16_t address,       uint8_t *bytes, uint16_t len);

/**
 * \brief write len number of bytes to the selected UCODE device
 *
 *	This functions writes the specified number of bytes to the given address of
 *	the selected UCODE device. Writing to an invalid address
 *	is undefined.
 *
 * \param	ucode	handle to identify the UCODE device instance
 * \param	bytes	array of bytes to be written
 * \param	address	byte address in device memory space to start writing to
 * \param	len 	number of bytes to be written
 *
 * \return			TRUE on failure
 */
BOOL UCODE_WriteBytes(UCODE_HANDLE_T ucode, uint16_t address, const uint8_t *bytes, uint16_t len);

/**
 * \brief get the error code of the last failure
 *
 *	If the previous call to any of the read/write function failed this function
 *	will return the reason via the corresponding error code. The error code is not
 *	latched, therefore any successful read/write after a failure will reset the
 *	error code.
 *	This function will return UCODE_CLOSED on a closed handle.
 *
 * \param	ucode	handle to identify the UCODE device instance
 *
 * \return			UCODE status code
 */
UCODE_STATUS_T UCODE_GetLastError(UCODE_HANDLE_T ucode);

#endif /* _UCODE_DRIVER_H_ */
