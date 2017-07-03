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

/** @file HAL_timer_driver.h
 * \brief Public interface to access the timing hardware.
 */
#ifndef _HAL_TIMER_DRIVER_H_
#define _HAL_TIMER_DRIVER_H_

/***********************************************************************/
/* INCLUDES                                                            */
/***********************************************************************/
#include "global_types.h"

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
 * \brief initialize the timing hardware
 *
 * This function prepares the timing hardware to points where calling
 * any of the following delay functions guarantees at least the desired delay
 * time while not blocking indefinitely.
 *
 * \return		none
 */
void HAL_Timer_Init();

/**
 * \brief wait for the specified number of milliseconds
 *
 * This function delays the current thread of execution for at least the
 * number of specified milliseconds. In tasked or threaded environments
 * this function may additionally yield the CPU.
 *
 * \param	ms	wait time in milliseconds
 * \return		none
 */
void HAL_Timer_delay_ms(uint32_t ms);

/**
 * \brief returns the current time in ms
 *
 * This function returns the current value of the ms tick timer.
 *
 * \return		current time in ms
 */
uint32_t HAL_Timer_getTime_ms();

#endif /* _HAL_TIMER_DRIVER_H_ */
