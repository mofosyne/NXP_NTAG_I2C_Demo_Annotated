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
#include "ucode_driver_intern.h"

/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/
#undef UCODE_DEVICE_LIST_BEGIN
#undef UCODE_DEVICE_ENTRY
#undef UCODE_DEVICE_LIST_END

#define UCODE_DEVICE_LIST_BEGIN                struct UCODE_DEVICE ucode_device_list[UCODE_ID_MAX_DEVICES] = \
                                               {
#define UCODE_DEVICE_ENTRY(label, i2c_address)     { UCODE_CLOSED, HAL_I2C_INVALID_HANDLE, i2c_address, {0}, {0} }
#define UCODE_DEVICE_LIST_END                  };

/***********************************************************************/
/* GLOBAL VARIABLES                                                    */
/***********************************************************************/
/* second include of device list for generation of ucode_device_list array */
UCODE_DEVICE_LIST_BEGIN
#   include "ucode_device_list.h"
UCODE_DEVICE_LIST_END

/***********************************************************************/
/* LOCAL FUNCTION PROTOTYPES                                           */
/***********************************************************************/
BOOL UCODE_AlignedWrite(UCODE_HANDLE_T ucode, uint16_t address, const uint8_t *bytes, uint8_t len, uint8_t align);

/***********************************************************************/
/* GLOBAL PUBLIC FUNCTIONS                                             */
/***********************************************************************/
UCODE_HANDLE_T UCODE_InitDevice(UCODE_ID_T ucode_id, HAL_I2C_HANDLE_T i2cbus)
{
	if( ucode_id < UCODE_ID_MAX_DEVICES )
	{
		if( ucode_device_list[ucode_id].status == UCODE_CLOSED )
		{
			ucode_device_list[ucode_id].i2cbus = i2cbus;
			ucode_device_list[ucode_id].status = UCODE_OK;
			return &ucode_device_list[ucode_id];
		}
	}
	return UCODE_INVALID_HANDLE;
}

void UCODE_CloseDevice(UCODE_HANDLE_T ucode)
{
	if( ucode )
	{
		ucode->i2cbus = HAL_I2C_INVALID_HANDLE;
		ucode->status = UCODE_CLOSED;
	}
}

BOOL UCODE_ReadBytes(UCODE_HANDLE_T ucode, uint16_t address, uint8_t *bytes, uint16_t len)
{
	uint16_t bytes_read = 0;

	if( ucode->status == UCODE_CLOSED )
		return TRUE;

	ucode->status = UCODE_OK;

	while( bytes_read < len )
	{
		uint8_t current_len   = MIN(len-bytes_read, UCODE_MAX_READ_SIZE);

		if( UCODE_ReadBlock(ucode, address+bytes_read, bytes+bytes_read, current_len) )
			break;

		bytes_read += current_len;
	}
	return ucode->status;
}

BOOL UCODE_WriteBytes(UCODE_HANDLE_T ucode, uint16_t address, const uint8_t *bytes, uint16_t len)
{
	uint16_t bytes_written = 0;

	if( ucode->status == UCODE_CLOSED )
		return TRUE;

	ucode->status = UCODE_OK;

	while( bytes_written < len )
	{
		uint16_t current       = (address+bytes_written);
		uint16_t begin         = (address+bytes_written) % UCODE_PAGE_SIZE;
		uint16_t current_len   = MIN(len-bytes_written, UCODE_PAGE_SIZE - begin);

		if( current_len < UCODE_PAGE_SIZE )
		{
			size_t i = 0;
			size_t align = UCODE_PAGE_SIZE;

			/* a word write is only efficient if the number of bytes to be written
			 * is less than or equal to the word size AND does not cross a word
			 * boundary */
			if( current_len <= UCODE_WORD_SIZE && ( begin % UCODE_WORD_SIZE ) + current_len <= UCODE_WORD_SIZE)
			{
				begin = begin % UCODE_WORD_SIZE;
				align = UCODE_WORD_SIZE;
			}

			/* align to page/word boundary */
			current = current - begin;

			/* read block into ucode->rx_buffer only */
			if( UCODE_ReadBlock(ucode, current, NULL, align) )
				break;

			/* modify rx_buffer */
			for( i = 0; i < current_len; i++ )
				ucode->rx_buffer[RX_START+begin+i] = bytes[bytes_written+i];

			/* writeback modified buffer */
			if( align == UCODE_WORD_SIZE)
			{
				if( UCODE_WriteWord(ucode, current, ucode->rx_buffer+RX_START, UCODE_WORD_SIZE) )
					break;
			}
			else
			{
				if( UCODE_WritePage(ucode, current, ucode->rx_buffer+RX_START, UCODE_PAGE_SIZE) )
					break;
			}
		}
		else
		{
			/* full block write */
			if( UCODE_WritePage(ucode, current, bytes + bytes_written, UCODE_PAGE_SIZE) )
				break;
		}

		bytes_written += current_len;
	}

	return ucode->status;
}

UCODE_STATUS_T UCODE_GetLastError(UCODE_HANDLE_T ucode)
{
	return ucode->status;
}

/***********************************************************************/
/* GLOBAL PRIVATE FUNCTIONS                                            */
/***********************************************************************/
BOOL UCODE_ReadBlock(UCODE_HANDLE_T ucode, uint16_t address, uint8_t *bytes, uint8_t len)
{
	size_t i = 0;

	ucode->tx_buffer[TX_START+0] = UINT16_BYTE_HI(address);
	ucode->tx_buffer[TX_START+1] = UINT16_BYTE_LO(address);

	/* send address number */
	if( HAL_I2C_OK != HAL_I2C_SendBytes(ucode->i2cbus, ucode->address, ucode->tx_buffer, 2) )
	{
		ucode->status = UCODE_ERROR_TX_FAILED;
		return TRUE;
	}

	len = MIN(len, UCODE_MAX_READ_SIZE);

	/* receive bytes */
	if( HAL_I2C_OK != HAL_I2C_RecvBytes(ucode->i2cbus, ucode->address, ucode->rx_buffer, len) )
	{
		ucode->status = UCODE_ERROR_RX_FAILED;
		return TRUE;
	}

	if(NULL != bytes)
	{
		/* write to bytes buffer */
		for( i = 0; i < len; i++ )
			bytes[i] = ucode->rx_buffer[RX_START+i];
	}

	return FALSE;
}

BOOL UCODE_WritePage(UCODE_HANDLE_T ucode, uint16_t address, const uint8_t *bytes, uint8_t len)
{
	return UCODE_AlignedWrite(ucode, address, bytes, len, UCODE_PAGE_SIZE);
}

BOOL UCODE_WriteWord(UCODE_HANDLE_T ucode, uint16_t address, const uint8_t *bytes, uint8_t len)
{
	return UCODE_AlignedWrite(ucode, address, bytes, len, UCODE_WORD_SIZE);
}

/***********************************************************************/
/* LOCAL FUNCTIONS                                                     */
/***********************************************************************/
BOOL UCODE_AlignedWrite(UCODE_HANDLE_T ucode, uint16_t address, const uint8_t *bytes, uint8_t len, uint8_t align)
{
	uint32_t timeout = UCODE_MAX_WRITE_DELAY_MS + 1;
	size_t i = 0;

	ucode->tx_buffer[TX_START+0] = UINT16_BYTE_HI(address);
	ucode->tx_buffer[TX_START+1] = UINT16_BYTE_LO(address);

	len = MIN(len, align);

	/* copy len bytes */
	for( i = 0; i < len; i++ )
		ucode->tx_buffer[TX_START+UCODE_ADDRESS_SIZE+i] = bytes[i];

	/* zero rest of the buffer */
	for( i = len; i < align; i++ )
		ucode->tx_buffer[TX_START+UCODE_ADDRESS_SIZE+i] = 0;

	/* write bytes */
	if( HAL_I2C_OK != HAL_I2C_SendBytes(ucode->i2cbus, ucode->address, ucode->tx_buffer, UCODE_ADDRESS_SIZE+align) )
	{
		ucode->status = UCODE_ERROR_TX_FAILED;
		return TRUE;
	}

	/* wait for completion */
	do
	{
		timeout--;
		HAL_Timer_delay_ms(1);
		if( HAL_I2C_OK == HAL_I2C_SendBytes(ucode->i2cbus, ucode->address, ucode->tx_buffer,2) )
			break;
	}while(timeout);

	if(0 == timeout)
		ucode->status = UCODE_ERROR_WRITE_TIMEOUT;

	return ucode->status;

}
