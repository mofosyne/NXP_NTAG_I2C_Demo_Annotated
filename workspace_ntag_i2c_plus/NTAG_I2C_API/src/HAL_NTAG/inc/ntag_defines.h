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
#ifndef NTAG_DEFINES_H
#define NTAG_DEFINES_H
/** @file ntag_defines.h
 * \brief Constants used by the NTAG I2C tag driver.
 */

#define NTAG_MAX_WRITE_DELAY_MS 					   10

#define NTAG_I2C_BLOCK_SIZE                            0x10

#ifndef NULL
#define NULL 0
#endif

//----------------------------------------------------------------------
///
/// memory addresses as seen from i2c interface
#define NTAG_MEM_ADRR_I2C_ADDRESS        			   0x00
#define NTAG_MEM_BLOCK_START_USER_MEMORY               0x01
#define NTAG_MEM_ADDR_START_USER_MEMORY                NTAG_MEM_BLOCK_START_USER_MEMORY*NTAG_I2C_BLOCK_SIZE

#define NTAG_MEM_BLOCK_CONFIGURATION_2k                0x7A
#define NTAG_MEM_ADDR_CONFIGURATION_2k 				   NTAG_MEM_BLOCK_CONFIGURATION_2k * NTAG_I2C_BLOCK_SIZE
#define NTAG_MEM_BLOCK_CONFIGURATION_1k                0x3A
#define NTAG_MEM_ADDR_CONFIGURATION_1k 				   NTAG_MEM_BLOCK_CONFIGURATION_1k * NTAG_I2C_BLOCK_SIZE

#define NTAG_MEM_ADDR_DEFAULT_REGS					   NTAG_MEM_BLOCK_DEFAULT_REGS*NTAG_I2C_BLOCK_SIZE
#define NTAG_MEM_BLOCK_SESSION_REGS                    0xFE

#define NTAG_MEM_BLOCK_START_SRAM                      0xF8
#define NTAG_MEM_ADDR_START_SRAM                       NTAG_MEM_BLOCK_START_SRAM * NTAG_I2C_BLOCK_SIZE

#define NTAG_MEM_SRAM_BLOCKS                           4
#define NTAG_MEM_SRAM_SIZE                             NTAG_MEM_SRAM_BLOCKS * NTAG_I2C_BLOCK_SIZE

//----------------------------------------------------------------------
///
///  byte offset in session and configuration
#define NTAG_MEM_OFFSET_NC_REG                         0x00
#define NTAG_MEM_OFFSET_LAST_NDEF_BLOCK                0x01
#define NTAG_MEM_OFFSET_SRAM_MIRROR_BLOCK              0x02
#define NTAG_MEM_OFFSET_WDT_LS                         0x03
#define NTAG_MEM_OFFSET_WDT_MS                         0x04
#define NTAG_MEM_OFFSET_I2C_CLOCK_STR                  0x05
#define NTAG_MEM_OFFSET_REG_LOCK                       0x06
#define NTAG_MEM_OFFSET_NS_REG                         0x06

//----------------------------------------------------------------------
///
///  memory bit masks
#define NTAG_NC_REG_MASK_I2C_RST_ON_OFF                0x80
#define NTAG_NC_REG_MASK_PTHRU_ON_OFF                  0x40
#define NTAG_NC_REG_MASK_FD_OFF                        0x30
#define NTAG_NC_REG_MASK_FD_ON                         0x0C
#define NTAG_NC_REG_MASK_SRAM_MIRROR_ON_OFF            0x02
#define NTAG_NC_REG_MASK_TRANSFER_DIR                  0x01

#define NTAG_REG_LOCK_MASK_CONF_BYTES_ACCESS_I2C       0x02
#define NTAG_REG_LOCK_MASK_CONF_BYTES_ACCESS_RF        0x01

#define NTAG_NS_REG_MASK_NDEF_DATA_READ                0x80
#define NTAG_NS_REG_MASK_I2C_LOCKED                    0x40
#define NTAG_NS_REG_MASK_RF_LOCKED                     0x20
#define NTAG_NS_REG_MASK_SRAM_I2C_READY                0x10
#define NTAG_NS_REG_MASK_SRAM_RF_READY                 0x08
#define NTAG_NS_REG_MASK_EEPROM_WR_ERR                 0x04
#define NTAG_NS_REG_MASK_EEPROM_WR_BUSY                0x02
#define NTAG_NS_REG_MASK_RF_FIELD_PRESENT              0x01

//----------------------------------------------------------------------
///
/// error codes

#define NTAG_ERR_OK                                    0x00
#define NTAG_ERR_COMMUNICATION                        -0x01
#define NTAG_ERR_BUFF_OVERFLOW                        -0x02
#define NTAG_ERR_INIT_FAILED                          -0x03
#define NTAG_ERR_INVALID_PARAM                        -0x09

#endif // NTAG_DEFINES_H
