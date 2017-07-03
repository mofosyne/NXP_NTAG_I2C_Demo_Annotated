/*
 * @brief Common SystemInit function for LPC11u6x chips
 *
 * @note
 * Copyright(C) NXP Semiconductors, 2013
 * All rights reserved.
 *
 * @par
 * Software that is described herein is for illustrative purposes only
 * which provides customers with programming information regarding the
 * LPC products.  This software is supplied "AS IS" without any warranties of
 * any kind, and NXP Semiconductors and its licensor disclaim any and
 * all warranties, express or implied, including all implied warranties of
 * merchantability, fitness for a particular purpose and non-infringement of
 * intellectual property rights.  NXP Semiconductors assumes no responsibility
 * or liability for the use of the software, conveys no license or rights under any
 * patent, copyright, mask work right, or any other intellectual property rights in
 * or to any products. NXP Semiconductors reserves the right to make changes
 * in the software without notification. NXP Semiconductors also makes no
 * representation or warranty that such application will be suitable for the
 * specified use without further testing or modification.
 *
 * @par
 * Permission to use, copy, modify, and distribute this software and its
 * documentation is hereby granted, under NXP Semiconductors' and its
 * licensor's relevant copyrights in the software, without fee, provided that it
 * is used in conjunction with NXP Semiconductors microcontrollers.  This
 * copyright, permission, and disclaimer notice must appear in all copies of
 * this code.
 */

 #include "board.h"

/*****************************************************************************
 * Private types/enumerations/variables
 ****************************************************************************/
#define VCC_PIN 	29
#define VCC_PORT	1
#define FD_PIN  	8
#define FD_PORT 	0

/*****************************************************************************
 * Public types/enumerations/variables
 ****************************************************************************/

/*****************************************************************************
 * Private functions
 ****************************************************************************/

/*****************************************************************************
 * Public functions
 ****************************************************************************/
void HAL_BSP_BoardInit()
{
	SystemCoreClockUpdate();
	Board_Init();

	Chip_IOCON_PinMuxSet(LPC_IOCON, VCC_PORT, VCC_PIN, IOCON_FUNC0 | IOCON_DIGMODE_EN);
	Chip_IOCON_PinMuxSet(LPC_IOCON, FD_PORT , FD_PIN,  IOCON_FUNC0 | IOCON_MODE_PULLUP);
	Chip_GPIO_SetPinDIROutput(LPC_GPIO, VCC_PORT, VCC_PIN);
	Chip_GPIO_SetPinDIRInput(LPC_GPIO, FD_PORT, FD_PIN);

	Chip_GPIO_SetPinState(LPC_GPIO, VCC_PORT, VCC_PIN, true);
}

/* Set up and initialize hardware prior to call to main */
void SystemInit(void)
{
#if defined(NO_BOARD_LIB)
	/* Chip specific SystemInit */
	Chip_SystemInit();
#else
	/* Setup system clocking and muxing */
	Board_SystemInit();
#endif
}
