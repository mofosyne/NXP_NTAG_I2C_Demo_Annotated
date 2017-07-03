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
#ifndef _GLOBAL_TYPES_H_
#define _GLOBAL_TYPES_H_

/***********************************************************************/
/* INCLUDES                                                            */
/***********************************************************************/
#ifdef HAVE_STDINT_H
#  include <stdint.h>
#endif

#ifdef HAVE_STDBOOL_H
#include <stdbool.h>
#endif /* HAVE_STDBOOL_H */

#ifdef __WINDOWS_MSVC__
#include "windows.h"
#endif
/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/
#ifdef HAVE_STDBOOL_H
#ifndef BOOL
#  define BOOL bool
#endif
#endif /* HAVE_STDBOOL_H */

#if !defined(MIN)
#define MIN(a, b) (((a) < (b)) ? (a) : (b))
#endif

#if !defined(NULL)
#define NULL 0
#endif

#define UINT16_BYTE_HI(word) ((word & 0xFF00) >>  8)
#define UINT16_BYTE_LO(word) ((word & 0x00FF)      )

/***********************************************************************/
/* TYPES                                                               */
/***********************************************************************/
#ifdef __MSP430F5438A__
typedef enum {FALSE = 0, TRUE = !FALSE} Bool;
typedef uint16_t size_t;
#endif

/***********************************************************************/
/* GLOBAL VARIABLES                                                    */
/***********************************************************************/

/***********************************************************************/
/* GLOBAL FUNCTION PROTOTYPES                                          */
/***********************************************************************/

#endif /* _GLOBAL_TYPES_H_ */
