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
#include "HAL_I2C_lpc8xx.h"
#include "ntag_defines.h"


volatile bool isTxCompleted = false;
volatile bool isRxCompleted = false;
volatile bool isTxRxCompleted = false;

I2C_PARAM_T param;
I2C_RESULT_T result;


/** Memory size for I2C ROM */
#define I2C_ROM_MEM_SIZE        (0x100UL)
static uint32_t i2cmem[I2C_ROM_MEM_SIZE];

static I2C_HANDLE_T *i2c_handle;
static int16_t error_code;

//---------------------------------------------------------------------
void Init_I2C_PinMux(void)
{
#if (defined(BOARD_NXP_XPRESSO_812) || defined(BOARD_LPC812MAX))
  /* Enable the clock to the Switch Matrix */
  Chip_Clock_EnablePeriphClock(SYSCTL_CLOCK_SWM);

  /* Connect the I2C_SDA and I2C_SCL signals to port pins(P0.10, P0.11) */
  Chip_SWM_MovablePinAssign(SWM_I2C_SDA_IO, 10);
  Chip_SWM_MovablePinAssign(SWM_I2C_SCL_IO, 11);

#if (I2C_BITRATE > 400000)
  /* Enable Fast Mode Plus for I2C pins */
  Chip_IOCON_PinSetI2CMode(LPC_IOCON, IOCON_PIO10, PIN_I2CMODE_FASTPLUS);
  Chip_IOCON_PinSetI2CMode(LPC_IOCON, IOCON_PIO11, PIN_I2CMODE_FASTPLUS);
#endif

  /* Disable the clock to the Switch Matrix to save power */
  Chip_Clock_DisablePeriphClock(SYSCTL_CLOCK_SWM);

#else
  /* Configure your own I2C pin muxing here if needed */
#warning "No I2C pin muxing defined"
#endif
}

//---------------------------------------------------------------------
void I2C_IRQHandler(void)
{
  LPC_I2CD_API ->i2c_isr_handler(i2c_handle);
}

//---------------------------------------------------------------------
void spin_on_error(void)
{
  while (1)
  {
    /* Show the red LED continuously due to error */
    Board_LED_Toggle(0);
  }
}

//---------------------------------------------------------------------
void App_I2C_Callback_Receive(uint32_t err_code, uint32_t n)
{
  isRxCompleted = true;
  error_code = err_code;
}

//---------------------------------------------------------------------
void App_I2C_Callback_Transmit(uint32_t err_code, uint32_t n)
{
  isTxCompleted = true;
  error_code = err_code;
}

//---------------------------------------------------------------------
void App_I2C_Callback_MasterTxRx(uint32_t err_code, uint32_t n)
{
  isTxRxCompleted = true;
  error_code = err_code;
}


//---------------------------------------------------------------------
int16_t I2CMasterInit(uint32_t bitrate)
{
  volatile uint32_t mem;

  /* Generic Initialization */
//  Board_Init();
  Chip_I2C_Init();

  Init_I2C_PinMux();

  /* Get the I2C memory size needed */
  mem = LPC_I2CD_API ->i2c_get_mem_size();

  /* Perform a sanity check on the storage allocation */
  if (I2C_ROM_MEM_SIZE < (mem / sizeof(uint32_t)))
  {
    spin_on_error();
  }

  /* Setup the I2C */
  i2c_handle = LPC_I2CD_API ->i2c_setup(LPC_I2C_BASE, i2cmem);

  /* Check the API return value for a valid handle */
  if (i2c_handle != NULL )
  {
    /* initialize the I2C with the configuration parameters */
    error_code = LPC_I2CD_API ->i2c_set_bitrate(i2c_handle,
        Chip_Clock_GetSystemClockRate(), bitrate);
    if (error_code)
    {
      return (NTAG_ERR_INIT_FAILED);
    }
  }

  return (NTAG_ERR_OK);
}

//---------------------------------------------------------------------
int16_t TransmitPoll(uint8_t *tx, uint8_t tx_len)
{
  param.num_bytes_send = tx_len;
  param.buffer_ptr_send = &tx[0];
  param.stop_flag = 1;
  param.func_pt = App_I2C_Callback_Transmit;
  error_code = LPC_I2CD_API ->i2c_master_transmit_poll(i2c_handle, &param,
      &result);

  return (error_code & 0xFFFF);
}

//---------------------------------------------------------------------
int16_t ReceivePoll(uint8_t *rx, uint8_t rx_len)
{
  param.num_bytes_rec = rx_len;
  param.buffer_ptr_rec = &rx[0];
  param.stop_flag = 1;
  param.func_pt = App_I2C_Callback_Receive;
  error_code = LPC_I2CD_API ->i2c_master_receive_poll(i2c_handle, &param,
      &result);

  return (error_code & 0xFFFF);
}

//---------------------------------------------------------------------
int16_t TransmitInvalid()
{
  uint8_t slave_addr[1];
  slave_addr[0] = 0x08;
  param.num_bytes_rec = 0;
  param.num_bytes_send = 1;
  param.buffer_ptr_send = slave_addr;
  param.stop_flag = 1;
  param.func_pt = App_I2C_Callback_Transmit;
  error_code = LPC_I2CD_API ->i2c_master_transmit_poll(i2c_handle, &param,
      &result);
  return (error_code & 0xFFFF);
}

//---------------------------------------------------------------------
int16_t TransmitReceive(uint8_t *tx, uint8_t tx_len, uint8_t *rx,
    uint8_t rx_len)
{
  param.buffer_ptr_send = &tx[0];
  param.num_bytes_send = tx_len;

  param.buffer_ptr_rec = &rx[0];
  param.num_bytes_rec = rx_len;

  param.stop_flag = 1;
  param.func_pt = App_I2C_Callback_MasterTxRx;

  error_code = LPC_I2CD_API ->i2c_master_tx_rx_poll(i2c_handle, &param,
      &result);
  return (error_code & 0xFFFF);
}
