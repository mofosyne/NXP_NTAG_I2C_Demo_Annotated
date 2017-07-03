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
#ifndef _UCODE_DRIVER_INTERN_H_
#define _UCODE_DRIVER_INTERN_H_

/***********************************************************************/
/* INCLUDES                                                            */
/***********************************************************************/
#include "HAL_I2C_driver.h"
#include "ucode_driver.h"

/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/
#define TX_START HAL_I2C_TX_RESERVED_BYTES
#define RX_START HAL_I2C_RX_RESERVED_BYTES

#define UCODE_MAX_WRITE_DELAY_MS	10

#define UCODE_MAX_READ_SIZE     	16 /* reserved size of the receive buffer  *
                                	    * this is an artificial restriction to *
                                	    * reduce memory consumption and may be *
                                	    * adapted for better performance       *
                                	    */

#define UCODE_MAX_WRITE_SIZE    	UCODE_PAGE_SIZE
#define UCODE_ADDRESS_SIZE      	2

/***********************************************************************/
/* TYPES                                                               */
/***********************************************************************/
struct UCODE_DEVICE
{
	UCODE_STATUS_T status;
	HAL_I2C_HANDLE_T i2cbus;
	uint8_t address;
	uint8_t tx_buffer[TX_START+UCODE_MAX_WRITE_SIZE+UCODE_ADDRESS_SIZE];
	uint8_t rx_buffer[RX_START+UCODE_MAX_READ_SIZE];
};

/***********************************************************************/
/* GLOBAL VARIABLES                                                    */
/***********************************************************************/
extern struct UCODE_DEVICE ucode_device_list[];

/***********************************************************************/
/* GLOBAL FUNCTION PROTOTYPES                                          */
/***********************************************************************/

/**
 * \brief read len number of bytes from the selected UCODE device
 *
 *	This functions reads up to UCODE_MAX_READ_SIZE number of bytes from the
 *	selected UCODE device. Reading from an invalid address will result in
 *	a failed read.
 *
 * \param	ucode	handle to identify the UCODE device instance
 * \param	bytes	array of bytes to store read data
 * \param	block	block number to read
 * \param	len  	number of bytes to be read
 *
 * \return	     	TRUE on failure
 */
BOOL UCODE_ReadBlock(UCODE_HANDLE_T ucode, uint16_t address,       uint8_t *bytes, uint8_t len);

/**
 * \brief write exactly one page to the selected NTAG device
 *
 *	This functions writes the specified 4-byte sized page to the selected UCODE device.
 *	Writing to an non page-aligned or invalid address in the device memory space is
 *	undefined. Writing less than a full page will write 0 to the rest of the page.
 *
 * \param	ucode	handle to identify the UCODE device instance
 * \param	bytes	array of bytes to be written
 * \param	address	address to be written to, needs to be page-aligned
 * \param	len 	number of bytes to be written
 *
 * \return			TRUE on failure
 */
BOOL UCODE_WritePage(UCODE_HANDLE_T ucode, uint16_t address, const uint8_t *bytes, uint8_t len);

/**
 * \brief write exactly one word to the selected NTAG device
 *
 *	This functions writes the specified 2-byte sized word to the selected UCODE device.
 *	Writing to an non word-aligned or invalid address in the device memory space is
 *	undefined. Writing less than a full word will write 0 to the rest of the word.
 *
 * \param	ucode	handle to identify the UCODE device instance
 * \param	bytes	array of bytes to be written
 * \param	address	address to be written to, needs to be word-aligned
 * \param	len 	number of bytes to be written
 *
 * \return			TRUE on failure
 */
BOOL UCODE_WriteWord(UCODE_HANDLE_T ucode, uint16_t address, const uint8_t *bytes, uint8_t len);

#endif /* _UCODE_DRIVER_INTERN_H_ */
