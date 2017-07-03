/*
 * @brief LPCXpresso 11U137H board file
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

#ifndef __BOARD_H_
#define __BOARD_H_

#include "chip.h"
/* board_api.h is included at the bottom of this file after DEBUG setup */

#ifdef __cplusplus
extern "C" {
#endif

/** @defgroup BOARD_NXP_XPRESSO_11U37H NXP LPC11U37H LPCXpresso board support software API functions
 * @ingroup LPCOPEN_11XX_BOARD_XPRESSO_11U37H
 * The board support software API functions provide some simple abstracted
 * functions used across multiple LPCOpen board examples. See @ref BOARD_COMMON_API
 * for the functions defined by this board support layer.<br>
 * @{
 */

/** @defgroup BOARD_NXP_XPRESSO_11U37H_OPTIONS BOARD: NXP LPC11U37H LPCXpresso board build options
 * This board has options that configure its operation at build-time.<br>
 * @{
 */

/** Define DEBUG_ENABLE to enable IO via the DEBUGSTR, DEBUGOUT, and
    DEBUGIN macros. If not defined, DEBUG* functions will be optimized
	out of the code at build time.
 */
//#define DEBUG_ENABLE

/** Define DEBUG_SEMIHOSTING along with DEBUG_ENABLE to enable IO support
    via semihosting. You may need to use a C library that supports
	semihosting with this option.
 */
//#define DEBUG_SEMIHOSTING

/** Board UART used for debug output and input using the DEBUG* macros. This
    is also the port used for Board_UARTPutChar, Board_UARTGetChar, and
	Board_UARTPutSTR functions.
 */
//#define DEBUG_UART LPC_USART

/**
 * @}
 */

/* Board name */
//#define BOARD_NXP_XPRESSO_11U37H
//#define Board_LPCxpresso
//#define Board_Demo_v1_0
//#define Board_Demo_v1_4
#define Board_NTAG_I2C_Explorer


#define RED   0
#define GREEN 1
#define BLUE  2
#define firmwareRev 0x21

#define WDO
//#define IRC_4MHZ
//#define IRC_12MHZ

char CHAR_BOARD_MAJ;
char CHAR_BOARD_MIN;


#define TEMP_I2C_ADDRESS        (0x90)

#ifdef Board_LPCxpresso
// no buttons, but we need the typedef
typedef enum Button__
{
  Button1 = 12, Button2 = 13, Button3 = 17
}Button;
#endif
#ifdef Board_Demo_v1_0
#define BUTTON1_PIO IOCON_PIO12
#define BUTTON2_PIO IOCON_PIO13
#define BUTTON3_PIO IOCON_PIO17
typedef enum Button__
{
	Button1 = 12, Button2 = 13, Button3 = 17
}Button;
#endif

#ifdef Board_Demo_v1_4
#define VCC_SW  15
#define FD_PIN  4
#define BUTTON1_PIO IOCON_PIO13
#define BUTTON2_PIO IOCON_PIO17
#define BUTTON3_PIO IOCON_PIO8
typedef enum Button__ {
	Button1 = 13, Button2 = 17, Button3 = 8
} Button;
#endif

#ifdef Board_NTAG_I2C_Explorer
#define VCC_SW  23
#define FD_PIN  19
#define BUTTON1_PIO 12
#define BUTTON2_PIO 13
#define BUTTON3_PIO 14
typedef enum Button__ {
	Button1 = 12, Button2 = 13, Button3 = 14
} Button;
#endif


/**
 * @}
 */

#include "board_api.h"

#ifdef __cplusplus
}
#endif

#endif /* __BOARD_H_ */
