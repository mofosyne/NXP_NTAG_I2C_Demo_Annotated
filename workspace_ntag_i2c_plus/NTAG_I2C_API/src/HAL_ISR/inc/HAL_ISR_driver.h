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

#ifndef _HAL_ISR_DRIVER_H_
#define _HAL_ISR_DRIVER_H_
/** @file HAL_ISR_driver.h
 * \brief Public interface for registering callbacks and waiting for interrupts.
 */

/***********************************************************************/
/* INCLUDES                                                            */
/***********************************************************************/
#include "global_types.h"

/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/
#ifndef API_DESCRIPTION
#define ISR_CALLBACK_LIST_BEGIN                      typedef enum \
                                                     {
#define ISR_CALLBACK_ENTRY(label, channel, pin_func)  	label
#define ISR_CALLBACK_LIST_END                         	, ISR_MAX_CALLBACKS \
                                                     } ISR_SOURCE_T;
#endif

#define ISR_DEFAULT_ARGUMENT	NULL
#define ISR_INVALID_HANDLER 	NULL
#define ISR_EMPTY_HANDLER   	ISR_INVALID_HANDLER
/***********************************************************************/
/* TYPES                                                               */
/***********************************************************************/
#ifndef API_DESCRIPTION
ISR_CALLBACK_LIST_BEGIN
#  include "HAL_ISR_callback_list.h"    /* allowed here - generator header */
ISR_CALLBACK_LIST_END
#endif

typedef void* ISR_ARGUMENT_T;

typedef void (*ISR_HANDLER_T) (ISR_ARGUMENT_T);

typedef enum
{
	ISR_EDGE_RISE,
	ISR_EDGE_FALL,
	ISR_LEVEL_HI,
	ISR_LEVEL_LO
} ISR_MODE_T;

/***********************************************************************/
/* GLOBAL VARIABLES                                                    */
/***********************************************************************/
/***********************************************************************/
/* GLOBAL FUNCTION PROTOTYPES                                          */
/***********************************************************************/

/**
 * \brief initialize the interrupt hardware
 *
 * This function initializes the interrupt hardware and activates the
 * specified device pins as interrupt source.
 *
 * Interrupts will NOT be enabled.
 *
 * \return		none
 */
void HAL_ISR_Init();

/**
 * \brief register a callback with the specified interrupt source
 *
 * This function registers a callback for the specified interrupt source and
 * switches the source to the selected mode.
 *
 * This function enables the corresponding interrupt.
 *
 * \param	source 	callback will be called when this source activates
 * \param	mode   	function or mode of the source e.g level sensitive, active high
 * \param	handler	function to be called when the source triggers, may be NULL
 * \param	arg    	argument passed to callback
 *
 * \return	       	none
 */
void HAL_ISR_RegisterCallback(ISR_SOURCE_T source, ISR_MODE_T mode, ISR_HANDLER_T handler, ISR_ARGUMENT_T arg);

/**
 * \brief wait for the specified event source to trigger
 *
 * This function waits until the specified event source triggers or the timeout value
 * is reached.
 *
 * This function enables the corresponding interrupt on entry and disables it
 * before exit.
 *
 * \param	source 	  	callback will be called when this source activates
 * \param	timeout_ms	timeout value in ms
 *
 * \return	          	TRUE on timeout, FALSE if event triggered
 */
BOOL HAL_ISR_SleepWithTimeout(ISR_SOURCE_T source, uint32_t timeout_ms);

#endif /* _HAL_TIMER_DRIVER_H_ */
