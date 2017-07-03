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
#include "isr_driver_intern.h"
#include "HAL_timer_driver.h"
/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/
#undef  ISR_CALLBACK_LIST_BEGIN
#undef  ISR_CALLBACK_ENTRY
#undef  ISR_CALLBACK_LIST_END

#define ISR_CALLBACK_LIST_BEGIN                      ISR_CALLBACK_T isr_callback_list[ISR_MAX_CALLBACKS] = \
											         {
#define ISR_CALLBACK_ENTRY(label, channel, pin_func)  	{ channel, pin_func, ISR_DEFAULT_MODE, ISR_INVALID_HANDLER, \
                                                          ISR_DEFAULT_ARGUMENT, ISR_SEMAPHORE_TAKEN }
#define ISR_CALLBACK_LIST_END                        };

/***********************************************************************/
/* TYPES                                                               */
/***********************************************************************/
ISR_CALLBACK_LIST_BEGIN
#   include "HAL_ISR_callback_list.h"    /* allowed here - generator header */
ISR_CALLBACK_LIST_END
/***********************************************************************/
/* GLOBAL VARIABLES                                                    */
/***********************************************************************/
/***********************************************************************/
/* LOCAL FUNCTION PROTOTYPES                                           */
/***********************************************************************/
/***********************************************************************/
/* GLOBAL FUNCTIONS                                                    */
/***********************************************************************/
void HAL_ISR_RegisterCallback(ISR_SOURCE_T source, ISR_MODE_T mode, ISR_HANDLER_T handler, ISR_ARGUMENT_T arg)
{
	ISR_CALLBACK_T *callback = &isr_callback_list[(size_t) source];

	/* enter critical section */
	disableInterrupt(callback->channel);

	callback->mode = mode;
	callback->handler = handler;
	callback->arg = arg;

	ISR_SEMAPHORE_INIT(callback->sema);

	/* setup interrupt mode */
	setupMode(callback->channel, callback->mode);

	/* leave critical section */
	enableInterrupt(callback->channel);
}

BOOL HAL_ISR_SleepWithTimeout(ISR_SOURCE_T source, uint32_t timeout_ms)
{
	SELECT_CALLBACK((size_t) source);
	uint32_t current = HAL_Timer_getTime_ms();
	uint32_t until = current + timeout_ms;

	do
	{
		/* start waiting for interrupt */
		enableInterrupt(callback->channel);

		ISR_WAIT_FOR_INTERRUPT();

		/* disable interrupt during check */
		disableInterrupt(callback->channel);
		current = HAL_Timer_getTime_ms();
	}while(ISR_SEMAPHORE_IS_TAKEN(callback->sema) && until > current);

	if( ISR_SEMAPHORE_IS_GIVEN(callback->sema) )
	{
		/* reinit mode - necessary for edge sensitive on LPCxxx */
		setupMode(callback->channel, callback->mode);

		ISR_SEMAPHORE_TAKE(callback->sema);
		return FALSE;
	}

	return TRUE;
}

/***********************************************************************/
/* LOCAL FUNCTIONS                                                     */
/***********************************************************************/
/***********************************************************************/
/* INTERUPT SERVICE ROUTINES                                           */
/***********************************************************************/
