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
#include "HAL_BSP_System.h"

/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/
/***********************************************************************/
/* GLOBAL VARIABLES                                                    */
/***********************************************************************/
/***********************************************************************/
/* LOCAL FUNCTION PROTOTYPES                                           */
/***********************************************************************/
static void debounce_ucb3_i2c();

/***********************************************************************/
/* GLOBAL FUNCTIONS                                                    */
/***********************************************************************/
void HAL_BSP_BoardInit()
{
	/* Stop watchdog timer */
	WDTCTL = WDTPW | WDTHOLD;

	/* Tie unused ports */
	PAOUT  = 0;
	PADIR  = 0xFFFF;
	PASEL  = 0;
	PBOUT  = 0;
	PBDIR  = 0xFFFF;
	PBSEL  = 0;
	PCOUT  = 0;
	PCDIR  = 0xFFFF;
	PCSEL  = 0;
	PDOUT  = 0;
	PDDIR  = 0xFFFF;
	PDSEL  = 0x0003;
	PEOUT  = 0;
	PEDIR  = 0xF8FF;                        /* P10.0 to USB RST pin,
	                                           ...if enabled with J5 */
	PESEL  = 0;
	P11OUT = 0;
	P11DIR = 0xFF;
	PJOUT  = 0;
	PJDIR  = 0xFF;
	P11SEL = 0;

	/* audio init */
	P6OUT = BIT6;

	/* USB init */
	P5DIR &= ~BIT7;           /* USB RX Pin, Input with
	                             ...pulled down Resistor */
	P5OUT &= ~BIT7;
	P5REN |= BIT7;

	/* output system clocks to test points */
	P11DIR |= 0x07;
	P11SEL |= 0x07;

	/* during system power up one I2C device sometimes asserts SDA */
	/* send clock toggle to signal it to release bus */
// 	debounce_ucb3_i2c();

	/* enable interrupts */
	_EINT();
}
/***********************************************************************/
/* LOCAL FUNCTIONS                                                     */
/***********************************************************************/
static void debounce_ucb3_i2c()
{
	P10SEL &= ~0x06;
	P10OUT &= ~0x06;
	P10DIR |=  0x06;
	P10OUT |=  0x06;
	P10OUT &= ~0x06;
	P10DIR &= ~0x06;
}
