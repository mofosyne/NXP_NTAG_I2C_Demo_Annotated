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

#include <stdint.h>
#include <msp430.h>
#include <stdio.h>
#include "HAL_I2C_driver.h"

/***********************************************************************/
/* DEFINES                                                             */
/***********************************************************************/
/***********************************************************************/
/* LOCAL TYPES                                                         */
/***********************************************************************/
typedef enum
{
	I2C_OK,
	I2C_INPROGRESS,
	I2C_ERROR_NACK,
	I2C_ERROR_ARB_LOST,
	I2C_CLOSED,
	I2C_STATUS_MAX_NUMBER
}I2C_STATUS_T;

typedef struct I2C_DEVICE
{
	I2C_STATUS_T status;
	uint8_t *bytes;
	uint8_t len;
	uint8_t active;
}I2C_HANDLE_T;

/***********************************************************************/
/* GLOBAL VARIABLES                                                    */
/***********************************************************************/
static I2C_HANDLE_T i2c_handle = {I2C_CLOSED, NULL, 0, 0};

/***********************************************************************/
/* LOCAL FUNCTION PROTOTYPES                                           */
/***********************************************************************/
/***********************************************************************/
/* GLOBAL FUNCTIONS                                                    */
/***********************************************************************/
I2C_HANDLE_T* HAL_I2C_InitDevice()
{
	if( i2c_handle.status != I2C_CLOSED )
		return NULL;

    P10SEL |= 0x06;                           // Assign I2C pins to USCI_B3

    UCB3CTL1 |= UCSWRST;                      // Enable SW reset
    UCB3CTL0 = UCMST + UCMODE_3 + UCSYNC;     // I2C Master, synchronous mode
    UCB3CTL1 = UCSSEL_2 + UCSWRST;            // Use SMCLK, keep SW reset
    UCB3BR0 = 12;                             // fSCL = SMCLK/12 = ~100kHz
    UCB3BR1 = 0;
    UCB3CTL1 &= ~UCSWRST;                     // Clear SW reset, resume operation

	i2c_handle.status = I2C_OK;
	return &i2c_handle;
}

void HAL_I2C_CloseDevice(HAL_I2C_HANDLE_T i2cbus)
{
	UCB3CTL1 |= UCSWRST;
	i2c_handle.status = I2C_CLOSED;
	i2c_handle.bytes = NULL;
	i2c_handle.len = 0;
	i2c_handle.active = 0;
}

HAL_I2C_STATUS_T HAL_I2C_RecvBytes(HAL_I2C_HANDLE_T i2cbus, uint8_t address, uint8_t *bytes, uint8_t len)
{
	if( I2C_INPROGRESS == i2cbus->status || I2C_CLOSED == i2cbus->status )
		return i2cbus->status;

	while (UCB3CTL1 & UCTXSTP);      // Ensure stop condition got sent

	i2cbus->bytes = (uint8_t*) bytes;
	i2cbus->len = len;

    UCB3CTL1 |= UCSWRST;                      // Enable SW reset
    UCB3CTL0 = UCMST + UCMODE_3 + UCSYNC;     // I2C Master, synchronous mode
    UCB3CTL1 = UCSSEL_2 + UCSWRST;            // Use SMCLK, keep SW reset
    UCB3BR0 = 12;                             // fSCL = SMCLK/12 = ~100kHz
    UCB3BR1 = 0;
    UCB3CTL1 &= ~UCSWRST;                     // Clear SW reset, resume operation

    UCB3I2CSA = address;
	UCB3IE = UCRXIE + UCNACKIE;

	i2cbus->active = 1;
	i2cbus->status = I2C_INPROGRESS;

    UCB3CTL1 |= UCTXSTT;             // I2C TX, start condition

    while(i2cbus->active);

	UCB3CTL1 |= UCTXSTP;
    while(UCB3CTL1 & UCTXSTP);

	UCB3IE = 0; //~(UCRXIE + UCNACKIE);

    if( i2cbus->status == I2C_INPROGRESS )
    	i2cbus->status = I2C_OK;

	i2cbus->bytes = NULL;
	i2cbus->len = 0;
	return i2cbus->status;
}

HAL_I2C_STATUS_T HAL_I2C_SendBytes(HAL_I2C_HANDLE_T i2cbus, uint8_t address, const uint8_t *bytes, uint8_t len)
{
	if( I2C_INPROGRESS == i2cbus->status || I2C_CLOSED == i2cbus->status )
		return i2cbus->status;

 	while (UCB3CTL1 & UCTXSTP);             // Ensure stop condition got sent

	i2cbus->bytes = (uint8_t*) bytes;
	i2cbus->len = len;

    UCB3CTL1 |= UCSWRST;                      // Enable SW reset
    UCB3CTL0 = UCMST + UCMODE_3 + UCSYNC;     // I2C Master, synchronous mode
    UCB3CTL1 = UCTR + UCSSEL_2 + UCSWRST;     // Use SMCLK, keep SW reset
    UCB3BR0 = 12;                             // fSCL = SMCLK/12 = ~100kHz
    UCB3BR1 = 0;
    UCB3CTL1 &= ~UCSWRST;                     // Clear SW reset, resume operation

    UCB3I2CSA = address;
	UCB3IE = UCTXIE + UCNACKIE;

	i2cbus->active = 1;
	i2cbus->status = I2C_INPROGRESS;

    UCB3CTL1 |= UCTXSTT;                    // I2C TX, start condition

    while(i2cbus->active);

    UCB3CTL1 |= UCTXSTP;
    while(UCB3CTL1 & UCTXSTP);

    UCB3IE = 0; //~(UCTXIE + UCNACKIE);

    if( i2cbus->status == I2C_INPROGRESS )
    	i2cbus->status = I2C_OK;

	// printf("ucb3ctl1 %d\n", UCB3CTL1);
	i2cbus->bytes = NULL;
	i2cbus->len = 0;
	return i2cbus->status;
}

/***********************************************************************/
/* LOCAL FUNCTIONS                                                     */
/***********************************************************************/
/***********************************************************************/
/* INTERUPT SERVICE ROUTINES                                           */
/***********************************************************************/
#pragma vector = USCI_B3_VECTOR
__interrupt void USCI_B3_ISR(void)
{
  switch(__even_in_range(UCB3IV,12))
  {
  case  0: break;               // Vector  0: No interrupts
  case  2: break;               // Vector  2: ALIFG
  case  4:					    // Vector  4: NACKIFG
	  i2c_handle.status = I2C_ERROR_NACK;
	  UCB3IFG &= ~( UCNACKIFG + UCRXIFG + UCTXIFG );
	  i2c_handle.active = 0;
  	  break;
  case  6: break;                // Vector  6: STTIFG
  case  8: break;                // Vector  8: STPIFG
  case 10:                       // Vector 10: RXIFG
  	  if (i2c_handle.len)
  	  {
  		  *i2c_handle.bytes = UCB3RXBUF;
  		  i2c_handle.bytes++;
  		  i2c_handle.len--;
  	  }
  	  else
  	  {
  		  i2c_handle.active = 0;
  	  }
      UCB3IFG &= ~UCRXIFG;                  // clear USCI_B3 TX interrupt flag
  	  break;

  case 12:                                  // Vector 12: TXIFG
    if (i2c_handle.len)
    {
      UCB3TXBUF = *i2c_handle.bytes;  // load byte from buffer
      i2c_handle.bytes++;
      i2c_handle.len--;               // decrement tx counter
    }
    else
    {
      i2c_handle.active = 0;
    }
    UCB3IFG &= ~UCTXIFG;                  // clear USCI_B3 TX interrupt flag
    break;
  default:
	  break;
  }
}
