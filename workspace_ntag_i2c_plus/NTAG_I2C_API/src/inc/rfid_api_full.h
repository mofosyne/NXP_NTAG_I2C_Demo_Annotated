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
#ifndef _RFID_API_FULL_H_
#define _RFID_API_FULL_H_


/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/
//#define HAVE_NTAG_INTERRUPT

/***********************************************************************/
/* INCLUDES                                                            */
/***********************************************************************/
#include "global_types.h"
#ifdef __LPC11UXX__
#include "../HAL_BSP11U6x/inc/HAL_BSP_System.h"
#include "../HAL_ISR/inc/HAL_ISR_driver.h"
#endif
#ifdef __LPC11U37H__
#include "../HAL_BSPLPC11U37H/inc/HAL_BSP_System.h"
#include "../HAL_ISR/inc/HAL_ISR_driver.h"
#endif
#ifdef __LPC8XX__
#include "../HAL_BSP_LPC8xx/inc/HAL_BSP_System.h"
#include "../HAL_ISR/inc/HAL_ISR_driver.h"
#endif
#ifdef __MSP430F5438A__
#include "../HAL_BSPMSP430/inc/HAL_BSP_System.h"
#endif
#ifdef __WINDOWS_MSVC__
#include "../HAL_BSP_WINDOWS/inc/HAL_BSP_System.h"
#endif
#include "../HAL_TMR/inc/HAL_timer_driver.h"
#include "../HAL_I2C/inc/HAL_I2C_driver.h"
#include "../HAL_UCODE/inc/ucode_driver.h" // for UCODE I2C SL3S4011
#include "../HAL_NTAG/inc/ntag_driver.h"   // for NTAG I2C 2k NT3H1201 and NTAG I2C 1k NT3H1101
#include "../HAL_NTAG/inc/ntag_bridge.h"
#include "../HAL_NTAG/inc/tnpi_driver.h"   // for TNPI

#endif /* _RFID_API_FULL_H_ */
