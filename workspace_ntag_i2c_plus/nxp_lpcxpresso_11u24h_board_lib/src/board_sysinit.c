/*
 * @brief NXP LPCXpresso 11U14 Sysinit file
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
 #include "chip.h"
 #include "romapi_11xx.h"

//#define WDO

/* The System initialization code is called prior to the application and
   initializes the board for run-time operation. Board initialization
   includes clock setup and default pin muxing configuration. */

/*****************************************************************************
 * Private types/enumerations/variables
 ****************************************************************************/


/*****************************************************************************
 * Public types/enumerations/variables
 ****************************************************************************/



/*****************************************************************************
 * Private functions
 ****************************************************************************/

/* Setup system clocking */
STATIC void SystemSetupClocking(void)
{

#ifdef WDO
	Chip_SYSCTL_PowerUp(SYSCTL_POWERDOWN_WDTOSC_PD);

	LPC_SYSCTL->SYSAHBCLKCTRL |= (1<<15);  // enable clock to WDT block

	Chip_Clock_SetWDTOSC(WDTLFO_OSC_4_60, 2);

	LPC_WWDT->CLKSEL |= (1<<0);  // select watchdog oscillator

	Chip_Clock_SetMainClockSource(SYSCTL_MAINCLKSRC_WDTOSC);


//#else  /* the IRC is enabled by default */
//		Chip_Clock_SetSysClockDiv(1);
#endif

	Chip_FMC_SetFLASHAccess(FLASHTIM_20MHZ_CPU);
	SystemCoreClockUpdate();
}


/* Sets up system pin muxing */
STATIC void SystemSetupMuxing(void)
{
	LPC_SYSCTL->SYSAHBCLKCTRL |= (1<<16);
	LPC_IOCON->PIO0[0] = IOCON_FUNC0 | IOCON_MODE_INACT | IOCON_DIGMODE_EN; // RESET pin
	LPC_IOCON->PIO0[1] = IOCON_FUNC0 | IOCON_MODE_INACT | IOCON_DIGMODE_EN;	// ISP pin
	LPC_IOCON->PIO0[3] = IOCON_FUNC0 | IOCON_MODE_PULLDOWN | IOCON_DIGMODE_EN; // GPIO ... will be VBUS when USB enabled
	LPC_IOCON->PIO0[4] = IOCON_FUNC1 | IOCON_DIGMODE_EN;	// I2C standard mode
	LPC_IOCON->PIO0[5] = IOCON_FUNC1 | IOCON_DIGMODE_EN;    // I2C standard mode
	LPC_IOCON->PIO0[6] = IOCON_FUNC0 | IOCON_MODE_PULLUP | IOCON_DIGMODE_EN;  // GPIO ... will be USB_CONNECT when USB enabled
	LPC_IOCON->PIO0[7] = IOCON_FUNC0 | IOCON_OPENDRAIN_EN | IOCON_MODE_INACT | IOCON_DIGMODE_EN; // RED LED
	LPC_IOCON->PIO0[8] = IOCON_FUNC0 | IOCON_OPENDRAIN_EN | IOCON_MODE_INACT | IOCON_DIGMODE_EN; // GREEN LED
	LPC_IOCON->PIO0[9] = IOCON_FUNC0 | IOCON_OPENDRAIN_EN | IOCON_MODE_INACT | IOCON_DIGMODE_EN; // BLUE LED

	LPC_IOCON->PIO0[12] = IOCON_FUNC1 | IOCON_MODE_PULLUP | IOCON_DIGMODE_EN;  // SW1
	LPC_IOCON->PIO0[13] = IOCON_FUNC1 | IOCON_MODE_PULLUP | IOCON_DIGMODE_EN;  // SW2
	LPC_IOCON->PIO0[14] = IOCON_FUNC1 | IOCON_MODE_PULLUP | IOCON_DIGMODE_EN;  // SW3
	LPC_IOCON->PIO0[16] = IOCON_FUNC1 | IOCON_ADMODE_EN   | IOCON_MODE_INACT | IOCON_FILT_DIS;  // Reference voltage: AD5
	LPC_IOCON->PIO0[23] = IOCON_FUNC0 | IOCON_DIGMODE_EN  | IOCON_MODE_INACT;  // VCC_SW

	LPC_IOCON->PIO1[19] = IOCON_FUNC0 | IOCON_MODE_PULLUP | IOCON_DIGMODE_EN  | IOCON_MODE_INACT;  // FD input
}


void SetPowerProfile(void)
{
	static uint32_t command[4], result[3];

	rom = (ROM **)0x1FFF1FF8;
#ifdef WDO  /* the WDO is running at 4.6 MHz but is divided by two */
	/****	Call the set_power routine ****/
	command[0] = 3;									/* Current freq in MHz  */
	command[1] = PARAM_LOW_CURRENT;					/* Use the designated low current power mode  */
	command[2] = 3;									/* Change the set_power  */

#elif defined (IRC_4MHZ) /* the IRC is running at 4 MHz */
	/****	Call the set_power routine ****/
	command[0] = 4;								/* Current freq in MHz  */
	command[1] = PARAM_LOW_CURRENT;					/* Use the designated low current power mode  */
	command[2] = 4;								/* Change the set_power  */

#elif  defined(IRC_12MHZ) /* the IRC is running at 12 MHz */
	/****	Call the set_power routine ****/
	command[0] = 12;								/* Current freq in MHz  */
	command[1] = PARAM_LOW_CURRENT;					/* Use the designated low current power mode  */
	command[2] = 12;								/* Change the set_power  */
#endif

	(*rom)->pPWRD->set_power(command, result);		/*Apply new power profile  */
}


// Timer used for NDEF message scrolling and other timing (delays).
void Init_Scrolling_Timer(void) {
	LPC_SYSCTL->SYSAHBCLKCTRL |= (1<<9);	// Enable clock to CT32B0
	LPC_TIMER32_0->TCR = (1<<1); 			// reset CT32B0.  Timer not running.

#ifdef WDO
	LPC_TIMER32_0->PR  = 2300;				// tick every millisecond with 2.3 MHz WDO
#elif defined(IRC_4MHZ)
	LPC_TIMER32_0->PR  = 4000;				// tick every millisecond with 12 MHz IRC
#elif defined(IRC_12MHZ)
	LPC_TIMER32_0->PR  = 12000;				// tick every millisecond with 12 MHz IRC
#endif

	LPC_TIMER32_0->MR[0] = 400; 			// Time, in milliseconds
	LPC_TIMER32_0->MCR |= (1<<1) | (1<<0);	// Reset the timer on a match.
}


/*****************************************************************************
 * Public functions
 ****************************************************************************/

/* Set up and initialize hardware prior to call to main */
void Board_SystemInit(void)
{
	/* Setup system clocking and muxing */
	SystemSetupMuxing();
	SystemSetupClocking();
	SetPowerProfile();

}
