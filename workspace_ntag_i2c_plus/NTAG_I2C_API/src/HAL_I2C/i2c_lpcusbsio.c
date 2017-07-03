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

#include <stdint.h>
#include <stdio.h>
#include "HAL_I2C_driver.h"
#include "HAL_Timer_driver.h"

/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/
/***********************************************************************/
/* LOCAL TYPES                                                         */
/***********************************************************************/
/***********************************************************************/
/* GLOBAL VARIABLES                                                    */
/***********************************************************************/
extern LPC_HANDLE handle;
/***********************************************************************/
/* LOCAL FUNCTION PROTOTYPES                                           */
/***********************************************************************/
/***********************************************************************/
/* GLOBAL FUNCTIONS                                                    */
/***********************************************************************/
HAL_I2C_HANDLE_T HAL_I2C_InitDevice(HAL_I2C_INIT_PARAM_T bitrate)
{
    I2C_PORTCONFIG_T config;

    I2C_Reset(handle);

    config.ClockRate = bitrate;
    config.Options = 0;  

    I2C_Init(handle, &config);

    return handle;
}

HAL_I2C_STATUS_T HAL_I2C_RecvBytes(HAL_I2C_HANDLE_T i2cbus, uint8_t address, uint8_t *bytes, uint8_t len)
{
    if( len == I2C_DeviceRead(i2cbus, address, bytes, len, HAL_I2C_TRANSFER_OPTION) )
        return FALSE;
    printf("Read failed: %ls\n", I2C_Error(handle));
    return TRUE;
}

HAL_I2C_STATUS_T HAL_I2C_SendBytes(HAL_I2C_HANDLE_T i2cbus, uint8_t address, const uint8_t *bytes, uint8_t len)
{
    int32_t retval = I2C_DeviceWrite(i2cbus, address, (uint8_t *) bytes, len, HAL_I2C_TRANSFER_OPTION);
    if( len == retval)
        return FALSE;
    printf("Write failed: %ls\n", I2C_Error(handle));
    return TRUE;
}

void HAL_I2C_Reset()
{
}
/***********************************************************************/
/* LOCAL FUNCTIONS                                                     */
/***********************************************************************/
/***********************************************************************/
/* INTERUPT SERVICE ROUTINES                                           */
/***********************************************************************/
