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
#include "HAL_timer_driver.h"
#include "ntag_driver_intern.h"
#include "tnpi_driver.h"

/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/
#undef TNPI_DEVICE_LIST_BEGIN
#undef TNPI_DEVICE_ENTRY
#undef TNPI_DEVICE_LIST_END

#define TNPI_DEVICE_LIST_BEGIN                    struct NTAG_DEVICE tnpi_device_list[TNPI_ID_MAX_DEVICES] = \
                                                  {
#ifdef HAVE_NTAG_INTERRUPT
#define TNPI_DEVICE_ENTRY(label, i2c_address, isr)     { NTAG_CLOSED, HAL_I2C_INVALID_HANDLE, i2c_address, isr, {0}, {0} }
#else
#define TNPI_DEVICE_ENTRY(label, i2c_address, isr)     { NTAG_CLOSED, HAL_I2C_INVALID_HANDLE, i2c_address, {0}, {0} }
#endif

#define TNPI_DEVICE_LIST_END                      };

/***********************************************************************/
/* GLOBAL VARIABLES                                                    */
/***********************************************************************/
/* second include of device list for generation of tnpi_device_list array */
TNPI_DEVICE_LIST_BEGIN
#include "tnpi_device_list.h"
TNPI_DEVICE_LIST_END

/***********************************************************************/
/* GLOBAL PUBLIC FUNCTIONS                                             */
/***********************************************************************/
TNPI_HANDLE_T TNPI_InitDevice(TNPI_ID_T tnpi_id, HAL_I2C_HANDLE_T i2cbus)
{
	if( tnpi_id < TNPI_ID_MAX_DEVICES )
	{
		if( tnpi_device_list[tnpi_id].status == NTAG_CLOSED )
		{
			tnpi_device_list[tnpi_id].i2cbus = i2cbus;
			tnpi_device_list[tnpi_id].status = NTAG_OK;
			return &tnpi_device_list[tnpi_id];
		}
	}
	return TNPI_INVALID_HANDLE;
}

void TNPI_CloseDevice(TNPI_HANDLE_T tnpi)
{
	NTAG_CloseDevice(tnpi);
}

BOOL TNPI_ReadBytes(TNPI_HANDLE_T tnpi, uint16_t address, uint8_t *bytes, uint16_t len)
{
	uint16_t bytes_read = 0;
	uint8_t skipped_blocks = 0;

	if( tnpi->status == NTAG_CLOSED )
		return TRUE;

	/* read cannot start on a key block */
	if( 3 == ((address / NTAG_I2C_BLOCK_SIZE) % 4 ) )
	{
		tnpi->status = NTAG_ERROR_INVALID_PARAM;
		return TRUE;
	}

	tnpi->status = NTAG_OK;

	while( bytes_read < len )
	{
		uint8_t current_block = (address+bytes_read) / NTAG_I2C_BLOCK_SIZE + skipped_blocks;
		uint8_t begin         = (address+bytes_read) % NTAG_I2C_BLOCK_SIZE;
		uint8_t current_len   = MIN(len-bytes_read, NTAG_I2C_BLOCK_SIZE - begin);

		/* skip key block */
		if( 3 == (current_block % 4))
		{
			skipped_blocks++;
			continue;
		}

		if( current_len < NTAG_I2C_BLOCK_SIZE )
		{
			size_t i = 0;

			/* read block into tnpi->rx_buffer only */
			if( NTAG_ReadBlock(tnpi, current_block, NULL, 0) )
				break;

			/* modify rx_buffer */
			for( i = 0; i < current_len; i++ )
				bytes[bytes_read+i] = tnpi->rx_buffer[RX_START+begin+i];
		}
		else
		{
			/* full block read */
			if( NTAG_ReadBlock(tnpi, current_block, bytes + bytes_read, NTAG_I2C_BLOCK_SIZE) )
				break;
		}

		bytes_read += current_len;
	}
	return tnpi->status;
}

BOOL TNPI_WriteBytes(TNPI_HANDLE_T tnpi, uint16_t address, const uint8_t *bytes, uint16_t len)
{
	uint16_t bytes_written = 0;
	uint8_t skipped_blocks = 0;

	if( tnpi->status == NTAG_CLOSED )
		return TRUE;

	/* write cannot start on a key block */
	if( 3 == ((address / NTAG_I2C_BLOCK_SIZE) % 4 ) )
	{
		tnpi->status = NTAG_ERROR_INVALID_PARAM;
		return TRUE;
	}

	tnpi->status = NTAG_OK;

	while( bytes_written < len )
	{
		uint8_t current_block = (address+bytes_written) / NTAG_I2C_BLOCK_SIZE + skipped_blocks;
		uint8_t begin         = (address+bytes_written) % NTAG_I2C_BLOCK_SIZE;
		uint8_t current_len   = MIN(len-bytes_written, NTAG_I2C_BLOCK_SIZE - begin);

		/* skip key block */
		if( 3 == (current_block % 4))
		{
			skipped_blocks++;
			continue;
		}

		if( current_len < NTAG_I2C_BLOCK_SIZE )
		{
			size_t i = 0;

			/* read block into tnpi->rx_buffer only */
			if( NTAG_ReadBlock(tnpi, current_block, NULL, 0) )
				break;

			/* check if it is the first Block(0x00) and not the I2C Addr */
			/* be careful with writing of first byte in management block */
			/* the byte contains part of the serial number on read but   */
			/* on write the I2C address of the device can be modified    */
			if( 0x00 == current_block && NTAG_MEM_ADRR_I2C_ADDRESS < begin )
				tnpi->rx_buffer[RX_START+0] = tnpi->address;

			/* modify rx_buffer */
			for( i = 0; i < current_len; i++ )
				tnpi->rx_buffer[RX_START+begin+i] = bytes[bytes_written+i];

			/* writeback modified buffer */
			if( NTAG_WriteBlock(tnpi, current_block, tnpi->rx_buffer+RX_START, NTAG_I2C_BLOCK_SIZE) )
				break;
		}
		else
		{
			/* full block write */
			if( NTAG_WriteBlock(tnpi, current_block, bytes + bytes_written, NTAG_I2C_BLOCK_SIZE) )
				break;
		}

		bytes_written += current_len;
	}

	return tnpi->status;
}

BOOL TNPI_ReadRegister (TNPI_HANDLE_T tnpi, uint8_t reg, uint8_t *val)
{
	return NTAG_ReadRegister(tnpi, reg, val);
}

BOOL TNPI_WriteRegister(NTAG_HANDLE_T tnpi, uint8_t reg, uint8_t mask, uint8_t val)
{
	return NTAG_WriteRegister(tnpi, reg, mask, val);
}

NTAG_STATUS_T TNPI_GetLastError(TNPI_HANDLE_T tnpi)
{
	return tnpi->status;
}

/***********************************************************************/
/* GLOBAL PRIVATE FUNCTIONS                                            */
/***********************************************************************/
