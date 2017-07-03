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
#ifndef HAL_I2C_LPC8XX_H
#define HAL_I2C_LPC8XX_H

#include "board.h"

/** 7-bit slave address */
#define I2C_SLAVE_ADDR_7BIT     (0xAA)

/** 1Mbps I2C bit-rate */
#define I2C_BITRATE             (100)
#define I2C_BITRATE_370         (370)

/** Memory size for I2C ROM */
#define I2C_ROM_MEM_SIZE        (0x100UL)

/* Initializes pin muxing for I2C interface - note that SystemInit() may
 already setup your pin muxing at system startup */
void Init_I2C_PinMux(void);

/* If error occurs, turn LED 0 (red LED) on and wait forever */
void spin_on_error(void);

/* I2C Receive Callback function */
void App_I2C_Callback_Receive(uint32_t err_code, uint32_t n);

/* I2C Transmit Callback function */
void App_I2C_Callback_Transmit(uint32_t err_code, uint32_t n);

/* I2C Transmit and Receive Callback function */
void App_I2C_Callback_MasterTxRx(uint32_t err_code, uint32_t n);

/* I2C Interrupt Service Routine */
void I2C_IRQHandler(void);

/* Set up hardware for lpc8xx */
int16_t I2CMasterInit(uint32_t bitrate);

int16_t TransmitReceive(uint8_t *tx, uint8_t tx_len, uint8_t *rx,
    uint8_t rx_len);
int16_t TransmitInvalid();
int16_t ReceivePoll(uint8_t *rx, uint8_t rx_len);
int16_t TransmitPoll(uint8_t *tx, uint8_t tx_len);

#endif /* HAL_I2C_LPC8XX_H */
