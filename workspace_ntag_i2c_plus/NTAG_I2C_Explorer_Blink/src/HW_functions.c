/*
 * HW_functions.c
 *
 *  Created on: 23.06.2014
 *      Author: NXP67729
 */

#include "chip.h"
#include <string.h>
#include "board.h"
#include "HW_functions.h"
#include "LCD.h"
#include "global_variables.h"

uint8_t defaultstring[16] = "NTAGI2C EXPLORER";

void init_TempSensor(void) {
#if defined(Board_NTAG_I2C_Explorer)
	uint8_t I2CMasterTXBuffer[3];

	I2CMasterTXBuffer[0] = TEMP_I2C_ADDRESS;
	I2CMasterTXBuffer[1] = 0x04;  // TIDLE register
	I2CMasterTXBuffer[2] = 0x05;  // read temperature every 500ms to save power

	Chip_I2CM_Write(LPC_I2C, I2CMasterTXBuffer, 3);
	//TransmitPoll( &I2CMasterTXBuffer[0], 3 );

	return;
#else
	return;
#endif
}

// sets up the Board so that the NTAG can be used
void HW_setup_Board_for_use_with_NTAG() {
#ifdef Board_Demo_v1_4
	Chip_GPIO_SetPinDIROutput(LPC_GPIO_PORT, 0, VCC_SW);
	Chip_GPIO_SetPinDIRInput(LPC_GPIO_PORT, 0, FD_PIN);
	Chip_GPIO_SetPinState(LPC_GPIO_PORT, 0, VCC_SW, true);
#elif defined(Board_NTAG_I2C_Explorer)
	// Setup the pins on the microcontroller
//	Chip_GPIO_SetPinDIROutput(LPC_GPIO, 0, VCC_SW);//switch on power for NTAG I2C
	Chip_GPIO_SetPinDIRInput(LPC_GPIO, 1, FD_PIN);
//	Chip_GPIO_SetPinState(LPC_GPIO, 0, VCC_SW, true);

	LPC_SYSCTL->SYSAHBCLKCTRL &=
			~((1 << 27) | (1 << 26) | (1 << 14) | (1 << 11)); // Disable USB RAM, USB, and SSP0 to minimize power in energy harvesting mode
	LPC_SYSCTL->PDRUNCFG |= ((1 << 10) | (1 << 8) | (1 << 3)); // turn off USBPAD, USB PLL, and BOD

	LPC_PMU->PCON |= (1 << 0);  			// Enter deep-sleep for __WFI()
	LPC_SYSCTL->PDWAKECFG &= ~(1 << 4); // Configure the wake-up state from deep-sleep.  Enable ADC.
#else

#endif
}

/// Switches LEDs off
static void ledsOFF() {
#if defined(Board_Demo_v1_4) || defined(Board_11U68) || defined(Board_NTAG_I2C_Explorer)
	Board_LED_Set(RED, false);
	Board_LED_Set(GREEN, false);
	Board_LED_Set(BLUE, false);
#else

#endif
}

//---------------------------------------------------------------------
///
/// Switches only red LED on
static void Red() {
#if defined(Board_Demo_v1_4) || defined(Board_11U68) || defined(Board_NTAG_I2C_Explorer)
	Board_LED_Set(RED, true);
	Board_LED_Set(GREEN, false);
	Board_LED_Set(BLUE, false);
#else

#endif
}

//---------------------------------------------------------------------
///
/// Switches only green LED on
static void Green() {
#if defined(Board_Demo_v1_4) || defined(Board_11U68) || defined(Board_NTAG_I2C_Explorer)
	Board_LED_Set(RED, false);
	Board_LED_Set(GREEN, true);
	Board_LED_Set(BLUE, false);
#else

#endif
}

//---------------------------------------------------------------------
///
/// Switches only blue LED on
static void Blue() {
#if defined(Board_Demo_v1_4) || defined(Board_11U68) || defined(Board_NTAG_I2C_Explorer)
	Board_LED_Set(RED, false);
	Board_LED_Set(GREEN, false);
	Board_LED_Set(BLUE, true);
#else

#endif
}

//---------------------------------------------------------------------
void HW_switchLEDs(LED led) {
	if (led == LEDOFF)
		ledsOFF();
	else if (led == REDLED)
		Red();
	else if (led == GREENLED)
		Green();
	else if (led == BLUELED)
		Blue();
}

//---------------------------------------------------------------------
void init_Buttons() {
#if defined(Board_Demo_v1_4)
	// Setting pins to input pull-up
	Chip_GPIO_SetPinState(LPC_GPIO_PORT, 0, Button1, false);
	Chip_GPIO_SetPinState(LPC_GPIO_PORT, 0, Button2, false);
	Chip_GPIO_SetPinState(LPC_GPIO_PORT, 0, Button3, false);

	Chip_GPIO_SetPinDIRInput(LPC_GPIO_PORT, 0, Button1);
	Chip_GPIO_SetPinDIRInput(LPC_GPIO_PORT, 0, Button2);
	Chip_GPIO_SetPinDIRInput(LPC_GPIO_PORT, 0, Button3);

	Chip_IOCON_PinSetMode(
			((LPC_IOCON_T *) LPC_IOCON_BASE), BUTTON1_PIO, PIN_MODE_PULLUP);
	Chip_IOCON_PinSetMode(
			((LPC_IOCON_T *) LPC_IOCON_BASE), BUTTON2_PIO, PIN_MODE_PULLUP);
	Chip_IOCON_PinSetMode(
			((LPC_IOCON_T *) LPC_IOCON_BASE), BUTTON3_PIO, PIN_MODE_PULLUP);
#elif defined(Board_NTAG_I2C_Explorer)
	// Setting pins to input with pull-up
	Chip_GPIO_SetPinState(LPC_GPIO, 0, Button1, true);
	Chip_GPIO_SetPinState(LPC_GPIO, 0, Button2, true);
	Chip_GPIO_SetPinState(LPC_GPIO, 0, Button3, true);

	Chip_GPIO_SetPinDIRInput(LPC_GPIO, 0, Button1);
	Chip_GPIO_SetPinDIRInput(LPC_GPIO, 0, Button2);
	Chip_GPIO_SetPinDIRInput(LPC_GPIO, 0, Button3);
#else

#endif
}

//---------------------------------------------------------------------
bool HW_Get_Button_State(Button button) {
#if defined(Board_Demo_v1_4)
	return !Chip_GPIO_GetPinState(LPC_GPIO_PORT, 0, button);
#elif defined(Board_NTAG_I2C_Explorer)
	return !Chip_GPIO_GetPinState(LPC_GPIO, 0, button);
#else
	return false;
#endif

}

//---------------------------------------------------------------------
void HW_getTemp(uint8_t Buffer[]) {
#if defined(Board_Demo_v1_4) || defined(Board_NTAG_I2C_Explorer)
	uint8_t RX_Buffer[HAL_I2C_RX_RESERVED_BYTES + 2];
	uint8_t TX_Buffer[HAL_I2C_TX_RESERVED_BYTES + 1];

	/* get temperature value from sensor */
	TX_Buffer[HAL_I2C_TX_RESERVED_BYTES + 0] = 0;
	HAL_I2C_SendBytes(i2cHandleMaster, TEMP_I2C_ADDRESS >> 1, TX_Buffer, 1);
	HAL_I2C_RecvBytes(i2cHandleMaster, TEMP_I2C_ADDRESS >> 1, RX_Buffer, 2);

	Buffer[0] = RX_Buffer[HAL_I2C_RX_RESERVED_BYTES + 0];
	Buffer[1] = RX_Buffer[HAL_I2C_TX_RESERVED_BYTES + 1];
#else
	Buffer[0] = 0;
	Buffer[1] = 0;
	return;
#endif
}

void HW_getVolt(uint8_t Buffer[]) {
#if defined(Board_NTAG_I2C_Explorer)
	uint16_t ADCresult;

	LPC_ADC->CR |= (1 << 24);					// Set START bit
	while ((LPC_ADC->DR[5] & (1 << 31)) == 0)
		;  // wait until done
	readAdcVal(LPC_ADC, 5, &ADCresult);
	Buffer[0] = ADCresult & 0x00FF;
	Buffer[1] = (ADCresult & 0xFF00) >> 8;

#else
	Buffer[0] = 0;
	Buffer[1] = 0;
	return;
#endif
}

//---------------------------------------------------------------------
/* Initialize the ADC peripheral and the ADC setup structure to default value */

void ADC_Init(int ADCclock) {
#if defined(Board_NTAG_I2C_Explorer)
	uint32_t div;
	uint32_t cr = 0;

	LPC_SYSCTL->SYSAHBCLKCTRL |= (1 << 13); 	// Enable clock to ADC
	LPC_SYSCTL->PDRUNCFG &= ~(1 << 4);		// Enable ADC

	LPC_ADC->INTEN = 0;						// Disable all interrupts
	div = (SystemCoreClock / ADCclock) - 1;
	cr = (div << 8);
	cr |= (1 << 5);							// only using ADC 5
	LPC_ADC->CR = cr;
#else
	return;
#endif
}

void HW_init_Peripherals() {
	init_Buttons();
	init_TempSensor();
	ADC_Init(2300000);
}

//---------------------------------------------------------------------
/* Initialize Timer32_0 to be used with scrolling the ndef message   */
void InitTimer(void) {
	LPC_SYSCTL->SYSAHBCLKCTRL |= (1<<9);	// Enable clock to CT32B0
	LPC_TIMER32_0->TCR = (1<<1); 			// reset CT32B0.  Timer not running.
	LPC_TIMER32_0->PR  = 12000;				// tick every millisecond
	LPC_TIMER32_0->MR[0] = 20; 			    // Time, in milliseconds (when clock running)
	LPC_TIMER32_0->MCR |= (1<<1) | (1<<0);	// Reset the timer on a match.  Interrupt
}


//---------------------------------------------------------------------
/* Initialize the LCD peripheral                                       */

void LCDInit(void)
{
	uint8_t I2CMasterLCDBuffer[0x42];  //maximum data is 0x40 plus I2C Address + command byte
	uint8_t SupplyVoltage[2];
	short int ADCValue;

	HW_getVolt(SupplyVoltage);
	ADCValue = (SupplyVoltage[1] << 8) | SupplyVoltage[0];


	if (ADCValue > 0x308)      I2CMasterLCDBuffer[5] = 0x7F; // Contrast set for 2.7V
	else if (ADCValue > 0x2EC) I2CMasterLCDBuffer[5] = 0x7D; // Contrast set for 2.8V
	else if (ADCValue > 0x2D2) I2CMasterLCDBuffer[5] = 0x77; // Contrast set for 2.9V
	else if (ADCValue > 0x2BA) I2CMasterLCDBuffer[5] = 0x74; // Contrast set for 3.0V
	else if (ADCValue > 0x2A4) I2CMasterLCDBuffer[5] = 0x72; // Contrast set for 3.1V
	else if (ADCValue <= 0x2A4) I2CMasterLCDBuffer[5] = 0x70; // Contrast set for 3.2V

	I2CMasterLCDBuffer[0] = LCD_I2C_Address;
	I2CMasterLCDBuffer[1] = COMMAND;					// Control Byte: instruction write
	I2CMasterLCDBuffer[2] = Comm_FunctionSet_Normal;	// Function set: instruction set 0
	I2CMasterLCDBuffer[3] = Comm_FunctionSet_Extended;
	I2CMasterLCDBuffer[4] = Comm_InternalOscFrequency;	// Internal oscillator frequency: BS=1 (bias=1/4), frame frequency=180Hz
//	I2CMasterLCDBuffer[5] = 0x40;	// Contrast set:

	I2CMasterLCDBuffer[6] = 0x5E;	// Power/ICON/Contrast control
	I2CMasterLCDBuffer[7] = 0x6D;	// Follower control
	I2CMasterLCDBuffer[8] = Comm_DisplayOnOff;	// Display ON/OFF control
	I2CMasterLCDBuffer[9] = Comm_ClearDisplay;
	I2CMasterLCDBuffer[10] = 0x06;	// Entry mode set: shift right entry; do not shift entire display
	Chip_I2CM_Write(LPC_I2C, &I2CMasterLCDBuffer[0], 11);
	HAL_Timer_delay_ms(1);

	I2CMasterLCDBuffer[2] = Comm_SetDDRAMAddress;
	Chip_I2CM_Write(LPC_I2C, &I2CMasterLCDBuffer[0], 3);
	HAL_Timer_delay_ms(1);

//	I2CMasterLCDBuffer[1] = DATA;	// Control Byte: data write
//	memcpy(&I2CMasterLCDBuffer[2], "NTAGI2C EXPLORER", 16);
//	Chip_I2CM_Write(LPC_I2C, &I2CMasterLCDBuffer[0], 18);

}

/**********************************************************/
void LCD_on(void)
{
	uint8_t I2CMasterLCDBuffer[3];  //maximum data is 0x40 plus I2C Address + command byte

	I2CMasterLCDBuffer[0] = LCD_I2C_Address;
	I2CMasterLCDBuffer[1] = COMMAND;
	I2CMasterLCDBuffer[2] = Comm_DisplayOnOff;


	Chip_I2CM_Write(LPC_I2C, &I2CMasterLCDBuffer[0], 3);
	HAL_Timer_delay_ms(1);
}
/**********************************************************/
void LCD_off(void)
{
	uint8_t I2CMasterLCDBuffer[3];  //maximum data is 0x40 plus I2C Address + command byte

	I2CMasterLCDBuffer[0] = LCD_I2C_Address;
	I2CMasterLCDBuffer[1] = COMMAND;
	I2CMasterLCDBuffer[2] = Comm_DisplayOff;


	Chip_I2CM_Write(LPC_I2C, &I2CMasterLCDBuffer[0], 3);
	HAL_Timer_delay_ms(1);
}
/**********************************************************/

void LCDInitUSB(void)
{
	uint8_t I2CMasterLCDBuffer[0x42];  //maximum data is 0x40 plus I2C Address + command byte
	uint32_t LCDdelay;

	I2CMasterLCDBuffer[0] = LCD_I2C_Address;
	I2CMasterLCDBuffer[1] = COMMAND;					// Control Byte: instruction write
	I2CMasterLCDBuffer[2] = Comm_FunctionSet_Normal;	// Function set: instruction set 0
	I2CMasterLCDBuffer[3] = Comm_FunctionSet_Extended;
	I2CMasterLCDBuffer[4] = Comm_InternalOscFrequency;	// Internal oscillator frequency: BS=1 (bias=1/4), frame frequency=180Hz
	I2CMasterLCDBuffer[5] = 0x50;	// Contrast set:
	I2CMasterLCDBuffer[6] = 0x5E;	// Power/ICON/Contrast control
	I2CMasterLCDBuffer[7] = 0x6D;	// Follower control
	I2CMasterLCDBuffer[8] = Comm_DisplayOnOff;	// Display ON/OFF control
	I2CMasterLCDBuffer[9] = Comm_ClearDisplay;
	I2CMasterLCDBuffer[10] = 0x06;	// Entry mode set: shift right entry; do not shift entire display
	Chip_I2CM_Write(LPC_I2C, &I2CMasterLCDBuffer[0], 11);

	LCDdelay = 10000;
		while (LCDdelay) {
			LCDdelay--;
		};

	I2CMasterLCDBuffer[1] = COMMAND;	// Control Byte: data write
	I2CMasterLCDBuffer[2] = Comm_SetDDRAMAddress;
	Chip_I2CM_Write(LPC_I2C, &I2CMasterLCDBuffer[0], 3);
	LCDdelay = 10000;
		while (LCDdelay) {
			LCDdelay--;
		};

	I2CMasterLCDBuffer[1] = DATA;	// Control Byte: data write
	memcpy(&I2CMasterLCDBuffer[2], "NTAGI2C EXPLORER", 16);
	Chip_I2CM_Write(LPC_I2C, &I2CMasterLCDBuffer[0], 18);

	LCDdelay = 10000;
		while (LCDdelay) {
			LCDdelay--;
		};

	I2CMasterLCDBuffer[1] = COMMAND;	// Control Byte: data write
	I2CMasterLCDBuffer[2] = Comm_SetDDRAMAddress | 0x40;  // second line
	Chip_I2CM_Write(LPC_I2C, &I2CMasterLCDBuffer[0], 3);

	LCDdelay = 10000;
		while (LCDdelay) {
			LCDdelay--;
		};

	I2CMasterLCDBuffer[1] = DATA;	// Control Byte: data write
	memcpy(&I2CMasterLCDBuffer[2], "*** USB MODE ***", 16);
	Chip_I2CM_Write(LPC_I2C, &I2CMasterLCDBuffer[0], 18);

}


/**********************************************************/


void LCDWriteNDEFmessage(uint8_t Data[], int len)
{
	int i;
	uint8_t I2CMasterLCDBuffer[0x42];  //maximum data is 0x40 plus I2C Address + command byte

	/* first, set up the DDRAM pointer in the LCD */
	I2CMasterLCDBuffer[0] = LCD_I2C_Address;
	I2CMasterLCDBuffer[1] = COMMAND;
	I2CMasterLCDBuffer[2] = Comm_ReturnHome;
	Chip_I2CM_Write(LPC_I2C, &I2CMasterLCDBuffer[0], 3);

	/* now, send the data */
	I2CMasterLCDBuffer[1] = DATA;	// Control Byte: data write
	for (i=0; i<len; i++)
	{
		I2CMasterLCDBuffer[i+2] = Data[i];
	}
	Chip_I2CM_Write(LPC_I2C, &I2CMasterLCDBuffer[0], len+2);

}

