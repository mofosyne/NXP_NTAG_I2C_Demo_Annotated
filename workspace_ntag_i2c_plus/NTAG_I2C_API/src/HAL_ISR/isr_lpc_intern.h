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
#ifndef _ISR_LPC_INTERN_H_
#define _ISR_LPC_INTERN_H_

/***********************************************************************/
/* INCLUDES                                                            */
/***********************************************************************/
#include <board.h>
/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/
#define ISR_PIN_FUNC_INVALID    	0xFFFFFFFF

#define ISR_SEMAPHORE_GIVEN     	true
#define ISR_SEMAPHORE_TAKEN     	false

#define ISR_SEMAPHORE_INIT(x)         	do { x = ISR_SEMAPHORE_TAKEN; } while(0)
#define ISR_SEMAPHORE_GIVE(x)         	do { x = ISR_SEMAPHORE_GIVEN; } while(0)
#define ISR_SEMAPHORE_GIVE_FROM_ISR(x)	do { x = ISR_SEMAPHORE_GIVEN; } while(0)

#define ISR_SEMAPHORE_TAKE(x)   	do { x = false; } while(0)

#define ISR_SEMAPHORE_IS_GIVEN(x)	(x == ISR_SEMAPHORE_GIVEN)
#define ISR_SEMAPHORE_IS_TAKEN(x)	(x == ISR_SEMAPHORE_TAKEN)

#define ISR_WAIT_FOR_INTERRUPT() 	Chip_PMU_SleepState(LPC_PMU)

#if defined(__LPC8XX__)
#  define GPIO_PININT_NVIC(channel) ( PININT0_IRQn + channel )
#  define GPIO_PIN_MIN    	0
#  define GPIO_NUMBER_PINS	18
#endif /* LPC8xx */
#if defined(__LPC11UXX__) || defined(__LPC11U37H__)
#  define GPIO_PININT_NVIC(channel) ( PIN_INT0_IRQn + channel )
#  define GPIO_PIN_MIN    	0
#  define GPIO_NUMBER_PINS	72
#endif /* LPC11U6x */

/***********************************************************************/
/* TYPES                                                               */
/***********************************************************************/
typedef BOOL	ISR_SEMAPHORE_T;

/***********************************************************************/
/* GLOBAL VARIABLES                                                    */
/***********************************************************************/
/***********************************************************************/
/* GLOBAL FUNCTION PROTOTYPES                                          */
/***********************************************************************/
#endif /* _ISR_DRIVER_INTERN_H_ */
