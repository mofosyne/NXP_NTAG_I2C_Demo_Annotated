#ifndef GLOBAL_VARIABLES_H
#define GLOBAL_VARIABLES_H

#include "rfid_api_full.h"
#include "nfc_device.h"

static HAL_I2C_HANDLE_T *i2cHandleMaster;
uint8_t error;

static uint8_t sram_buf[NTAG_MEM_SRAM_SIZE];
NFC_HANDLE_T ntag_handle;
//static uint16_t error_code;

#endif
