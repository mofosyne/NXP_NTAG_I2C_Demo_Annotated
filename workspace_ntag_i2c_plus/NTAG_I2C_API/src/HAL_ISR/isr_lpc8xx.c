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
#include <board.h>
#include "HAL_timer_driver.h"
#include "isr_driver_intern.h"
/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/

/***********************************************************************/
/* GLOBAL VARIABLES                                                    */
/***********************************************************************/
/***********************************************************************/
/* LOCAL FUNCTION PROTOTYPES                                           */
/***********************************************************************/
/***********************************************************************/
/* GLOBAL FUNCTIONS                                                    */
/***********************************************************************/
void HAL_ISR_Init()
{
	size_t i = 0;

	/* Enable PININT clock */
	Chip_Clock_EnablePeriphClock(SYSCTL_CLOCK_GPIO);

	for(i = 0; i < ISR_MAX_CALLBACKS; i++)
	{
		SELECT_CALLBACK(i);

		/* disable interrupt */
		DISABLE_INTERRUPT();

		/* connect pin to interrupt channel */
		if( callback->pin_func != ISR_PIN_FUNC_INVALID )
			Chip_SYSCTL_SetPinInterrupt(callback->channel, callback->pin_func);

		/* setup interrupt mode */
		setupMode(callback->channel, callback->mode);
	}
}

void setupMode(uint32_t channel, ISR_MODE_T mode)
{
	switch(mode)
	{
		case ISR_LEVEL_HI:
			LPC_PININT->ISEL |=  PININTCH(channel); /* level sensitive */
			LPC_PININT->IENR |=  PININTCH(channel);
			LPC_PININT->IENF |=  PININTCH(channel); /* high level */
			break;
		case ISR_LEVEL_LO:
			LPC_PININT->ISEL |=  PININTCH(channel); /* level sensitive */
			LPC_PININT->IENR |=  PININTCH(channel);
			LPC_PININT->IENF &= ~PININTCH(channel); /* low level */
			break;
		case ISR_EDGE_RISE:
			LPC_PININT->IST = PININTCH(channel); /* clear edge detected bit */
			LPC_PININT->ISEL &= ~PININTCH(channel); /* edge sensitive */
			LPC_PININT->IENR |=  PININTCH(channel); /* rising edge */
			LPC_PININT->IENF &= ~PININTCH(channel);
			break;
		case ISR_EDGE_FALL:
			LPC_PININT->IST = PININTCH(channel); /* clear edge detected bit */
			LPC_PININT->ISEL &= ~PININTCH(channel); /* edge sensitive */
			LPC_PININT->IENR &= ~PININTCH(channel);
			LPC_PININT->IENF |=  PININTCH(channel); /* falling edge */
			break;
		default:
			break;
	}
}

void disableInterrupt(uint32_t channel)
{
	NVIC_DisableIRQ(GPIO_PININT_NVIC(channel));
}

void enableInterrupt(uint32_t channel)
{
	/* Enable interrupt in the NVIC */
	NVIC_ClearPendingIRQ(GPIO_PININT_NVIC(channel));
	NVIC_EnableIRQ(GPIO_PININT_NVIC(channel));
}

/***********************************************************************/
/* LOCAL FUNCTIONS                                                     */
/***********************************************************************/
/***********************************************************************/
/* INTERUPT SERVICE ROUTINES                                           */
/***********************************************************************/
void PININT0_IRQHandler(void)
{
	SELECT_CALLBACK(0);
	CHECK_VALID_AND_CALL_HANDLER();
	WAKEUP_TASKS();
	DISABLE_INTERRUPT();
}

void PININT1_IRQHandler(void)
{
	SELECT_CALLBACK(1);
	CHECK_VALID_AND_CALL_HANDLER();
	WAKEUP_TASKS();
	DISABLE_INTERRUPT();
}

void PININT2_IRQHandler(void)
{
	SELECT_CALLBACK(2);
	CHECK_VALID_AND_CALL_HANDLER();
	WAKEUP_TASKS();
	DISABLE_INTERRUPT();
}
