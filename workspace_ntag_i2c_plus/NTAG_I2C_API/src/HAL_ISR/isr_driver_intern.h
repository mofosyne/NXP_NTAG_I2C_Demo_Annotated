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
#ifndef _ISR_DRIVER_INTERN_H_
#define _ISR_DRIVER_INTERN_H_

/***********************************************************************/
/* INCLUDES                                                            */
/***********************************************************************/
#include "HAL_ISR_driver.h"

#if defined(__LPC8XX__) || defined(__LPC11UXX__) || defined(__LPC11U37H__)
#include "isr_lpc_intern.h"
#endif

/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/
#define ISR_DEFAULT_MODE    	ISR_EDGE_RISE

#define SELECT_CALLBACK(index) ISR_CALLBACK_T *callback = &isr_callback_list[index];

#define CHECK_VALID_AND_CALL_HANDLER() \
		if( callback->handler != ISR_INVALID_HANDLER) \
			callback->handler(callback->arg);

#define WAKEUP_TASKS() ISR_SEMAPHORE_GIVE(callback->sema)

#define DISABLE_INTERRUPT() disableInterrupt(callback->channel)

/***********************************************************************/
/* TYPES                                                               */
/***********************************************************************/
typedef uint32_t  ISR_CHANNEL_T;
typedef uint32_t  ISR_PIN_FUNC_T;

typedef struct
{
	ISR_CHANNEL_T   channel;
	ISR_PIN_FUNC_T  pin_func;
	ISR_MODE_T      mode;
	ISR_HANDLER_T   handler;
	ISR_ARGUMENT_T  arg;
	ISR_SEMAPHORE_T sema;
}ISR_CALLBACK_T;

/***********************************************************************/
/* GLOBAL VARIABLES                                                    */
/***********************************************************************/
extern ISR_CALLBACK_T isr_callback_list[];

/***********************************************************************/
/* GLOBAL FUNCTION PROTOTYPES                                          */
/***********************************************************************/
void disableInterrupt(uint32_t channel);
void enableInterrupt (uint32_t channel);
void setupMode(uint32_t channel, ISR_MODE_T mode);

#endif /* _ISR_DRIVER_INTERN_H_ */
