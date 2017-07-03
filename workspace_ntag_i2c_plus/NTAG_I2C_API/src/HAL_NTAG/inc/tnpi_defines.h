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
#ifndef TNPI_DEFINES_H
#define TNPI_DEFINES_H
/** @file tnpi_defines.h
 * \brief Constants used by the NTAG I2C tag driver.
 */

#define TNPI_MAX_WRITE_DELAY_MS 					   10

#define TNPI_BLOCK_SIZE                                0x10
#define TNPI_PAGE_SIZE                                 0x04

#define TNPI_I2C_TX_BUFFER_SIZE                        0x12
#define TNPI_I2C_RX_BUFFER_SIZE                        0x11
//#define I2C_WRITE_MASK                                 0xFE

#define TNPI_ACTIVATE                                  0x01
#define TNPI_DEACTIVATE                                0x00

#ifndef NULL
#define NULL 0
#endif

//----------------------------------------------------------------------
///
/// memory addresses as seen from i2c interface
#define TNPI_MEM_BLOCK_MANAGEMENT                      0x00
#define TNPI_I2C_ADDRESS_SELECT				       	   0x00
#define TNPI_MEM_BLOCK_START_ADDR_USER_MEMORY          0x04
#define TNPI_MEM_START_ADDR_USER_MEMORY                TNPI_MEM_BLOCK_START_ADDR_USER_MEMORY*NTAG_BLOCK_SIZE
#define TNPI_MEM_PAGE_LAST_PAGE_USER_MEMORY            0x1DF
#define TNPI_MEM_SIZE_USER_MEMORY                      1904

#define TNPI_MEM_BLOCK_START_ADDR_DYN_LOCK_BYTES       0x78
#define TNPI_MEM_BLOCK_DEFAULT_REGS                    0x7A
#define TNPI_MEM_DEFAULT_REGS						   NTAG_MEM_BLOCK_DEFAULT_REGS*NTAG_BLOCK_SIZE
#define TNPI_MEM_BLOCK_SESSION_REGS                    0xFE

#define TNPI_MEM_BLOCK_START_ADDR_SRAM                 0xF8
#define TNPI_MEM_START_ADDR_SRAM                       NTAG_MEM_BLOCK_START_SRAM*NTAG_BLOCK_SIZE

#define TNPI_MEM_BLOCK_SIZE_SRAM                       3
#define TNPI_MEM_SIZE_SRAM                             TNPI_MEM_BLOCK_SIZE_SRAM * TNPI_BLOCK_SIZE

//----------------------------------------------------------------------
///
///  byte offset in session and default register
#define TNPI_MEM_OFFSET_NC_REG                         0x00
#define TNPI_MEM_OFFSET_LD_REG                         0x01
#define TNPI_MEM_OFFSET_SM_REG                         0x02
#define TNPI_MEM_OFFSET_WDT_LS                         0x03
#define TNPI_MEM_OFFSET_WDT_MS                         0x04
#define TNPI_MEM_OFFSET_REG_LOCK                       0x06
#define TNPI_MEM_OFFSET_NS_REG                         0x06

//----------------------------------------------------------------------
///
///  memory bit masks

#define TNPI_NC_REG_MASK_I2C_RST_ON_OFF                0x80
#define TNPI_NC_REG_MASK_TR_SRAM_ON_OFF                0x40
#define TNPI_NC_REG_MASK_FD_OFF                        0x30
#define TNPI_NC_REG_MASK_FD_ON                         0x0C
#define TNPI_NC_REG_MASK_SRAM_MIRROR_ON_OFF            0x02
#define TNPI_NC_REG_MASK_RF_WRITE_ON_OFF               0x01

#define TNPI_REG_LOCK_MASK_CONF_BYTES_ACCESS_I2C       0x02
#define TNPI_REG_LOCK_MASK_CONF_BYTES_ACCESS_RF        0x01

#define TNPI_NS_REG_MASK_RF_FIELD_PRESENT              0x01
#define TNPI_NS_REG_MASK_EEPROM_WR_BUSY                0x02
#define TNPI_NS_REG_MASK_EEPROM_WR_ERR                 0x04
#define TNPI_NS_REG_MASK_SRAM_RF_READY                 0x08
#define TNPI_NS_REG_MASK_SRAM_I2C_READY                0x10
#define TNPI_NS_REG_MASK_RF_IF_ON_OFF                  0x20
#define TNPI_NS_REG_MASK_I2C_IF_ON_OFF                 0x40
#define TNPI_NS_REG_MASK_NDEF_DATA_TRANS               0x80

#endif // TNPI_DEFINES_H
