#include "HAL_I2C_lpc11u37h.h"
#include "ntag_defines.h"
#include "i2cm_11xx.h"

static I2CM_XFER_T xfer;


volatile Bool isTxCompleted =   FALSE;
volatile Bool isRxCompleted =   FALSE;
volatile Bool isTxRxCompleted = FALSE;

//---------------------------------------------------------------------
void Init_I2C_PinMux(void)
{

}

//---------------------------------------------------------------------
void I2C_IRQHandler(void)
{

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
/*void App_I2C_Callback_Receive(uint32_t err_code, uint32_t n)
{

} */

//---------------------------------------------------------------------
/*void App_I2C_Callback_Transmit(uint32_t err_code, uint32_t n)
{

} */

//---------------------------------------------------------------------
/*void App_I2C_Callback_MasterTxRx(uint32_t err_code, uint32_t n)
{

} */


//---------------------------------------------------------------------
int16_t I2CMasterInit(uint32_t bitrate)
{

  /* Generic Initialization */
//  Board_Init();
  Chip_I2CM_Init(I2C0);


  Chip_I2CM_SetBusSpeed(LPC_I2C, bitrate);

  return (NTAG_ERR_OK);
}


//---------------------------------------------------------------------
int16_t TransmitPoll(uint8_t *tx, uint8_t tx_len)
{

/*  xfer.slaveAddr = tx[0];

  xfer.txSz = tx_len - 1;
  xfer.txBuff = &tx[1];

  xfer.rxSz = 0; */

  return(Chip_I2CM_Write(LPC_I2C, &tx[0], tx_len));
//  return (Chip_I2CM_XferBlocking(LPC_I2C, &xfer);
}


//---------------------------------------------------------------------
int16_t ReceivePoll(uint8_t *rx, uint8_t rx_len)
{

/*  xfer.txSz = 0;
  xfer.slaveAddr = rx[0];

  xfer.rxSz = rx_len - 1;
  xfer.rxBuff = &rx[1];

  return (Chip_I2CM_XferBlocking(LPC_I2C, &xfer) & 0xFFFF);*/
  return(Chip_I2CM_Read(LPC_I2C, &rx[0], rx_len));
}


//---------------------------------------------------------------------
int16_t TransmitInvalid(void)
{
	uint8_t tx[2];
	tx[0] = 0x08;
	tx[1] = 0x00;
//  xfer.slaveAddr = 0x08;

//  xfer.rxSz = 0;

//  xfer.txSz = 1;

//  return (Chip_I2CM_XferBlocking(LPC_I2C, &xfer) & 0xFFFF);
  return(Chip_I2CM_Write(LPC_I2C, &tx[0], 2));

}

//---------------------------------------------------------------------
int16_t TransmitReceive(uint8_t *tx, uint8_t tx_len, uint8_t *rx, uint8_t rx_len)
{

  xfer.slaveAddr = tx[0];

  xfer.txSz = tx_len - 1;
  xfer.txBuff = &tx[1];

  xfer.rxSz = rx_len - 1;
  xfer.rxBuff = &rx[0];

  return (Chip_I2CM_XferBlocking(LPC_I2C, &xfer));

}
