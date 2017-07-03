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
#ifndef _HAL_BSP_SYSTEM_H_
#define _HAL_BSP_SYSTEM_H_
/***********************************************************************/
/* INCLUDES                                                            */
/***********************************************************************/
#include <board.h>
/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/
#define RED 0
#define HAL_BSP_RED_LED_ON()   Board_LED_Set(RED, true)
#define HAL_BSP_RED_LED_OFF()  Board_LED_Set(RED, false)
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
 * \brief initialize the board hardware
 *
 *	This function should prepare the hardware to the point that the main
 *	function can be run safely. This may include RAM initialization,
 *	switching to an external oscillator for execution and setting GPIO ports
 *	to a safe state.
 *
 *	\return	none
 */
void HAL_BSP_BoardInit();

#endif /* _HAL_BSP_SYSTEM_H_ */
