#include <string.h>
#include "board.h"
#include <stdlib.h>
#include "global_variables.h"
#include "demo_settings.h"
#include "HW_functions.h"
#include "LCD.h"
#include "crc32.h"
#include "ndef_message.h"
#include "ndef_parser.h"
#include "nfc_device.h"

//---------------------------------------------------------------------
//               Helping functions declarations
//---------------------------------------------------------------------
/*
 * Waits till RF has written in the terminater page of the SRAM
 */
void wait_for_RF_write_in_SRAM();

/*
 * Implements the LED demo app functionality
 */
void LED_Demo(bool *LCD_initialized, uint32_t *LCDScrollCount);

/*
 * Sends board and FW version info
 */
void send_VersionInfo();

/*
 * Init timers for PWM for NFC Cube Demo
 */
void InitCubeDemo(void);

/*
 * Setup the Chip
 */
void Setup();

/*
 * Let a LED blink (blocking function ~ 700ms)
 * @param LED LED to blink
 */
void LED_blink(LED led);

/*
 * Implements the NFC cube demo
 */
void RGBcube_Demo(bool *LCD_initialized);

/*
 * Checks Buttons and gives them back as a bitfield
 * @param Buttons Bitfield in which the button state is written(first Button = LSB, true = pressed, false = not pressed)
 */
void check_Buttons(uint8_t *Buttons);

/*
 * Writes the Timing in the User Memory formated as NDEF Message
 * @param time1 first time to write
 * @param time2 second time to write
 */
short int write_back_Time_as_NDEF(int time1, int time2);

/*
 * Writes the Android Application Record of the NTAG I2C Demo Android App in the User Memory
 */
bool reset_AAR();

/*
 * Resets the memory to its default values
 */
void factory_reset_Tag();

/*
 * Performs the Speedtest
 * @return returns an NTAG_ERROR_CODE
 */
short int speedTest();

/*
 *
 */
void HW_setup_Speedtest();

/*
 * Copies the interrupt vector table from flash address USER_FLASH_START to SRAM address of 0x1000 0000
 */
void CopyInterruptToSRAM(void);

/**
 * @brief	Handle interrupt from 32-bit timer
 * @return	Nothing
 */

void TIMER32_1_IRQHandler(void)
{
	//interrupt when duty cycle value is reached
	if (Chip_TIMER_MatchPending(LPC_TIMER32_1, 0))
	{
		Chip_TIMER_ClearMatch(LPC_TIMER32_1, 0);
		Board_LED_Set(0, false);
	}
	//interrupt when duty cycle end of period is reached
	if (Chip_TIMER_MatchPending(LPC_TIMER32_1, 1))
	{
		Chip_TIMER_ClearMatch(LPC_TIMER32_1, 1);
		Chip_TIMER_Reset(LPC_TIMER32_1);
		if (LPC_TIMER32_1->MR[0] != 0)
			Board_LED_Set(0, true);
	}
}

//---------------------------------------------------------------------
//               Helping functions declarations end
//---------------------------------------------------------------------

#define SRAM_MEMORY_START 			0x10000000
#define USER_FLASH_START 			0x4000
#define INTERRUPT_VECTOR_TABLE_SIZE 0x200
#define NTAG_I2C_PLUS

//for making cube demo init only one time
bool init_cube_demo = false;

//for making led demo init only one time
bool init_led_demo = false;
/*
 * main Program
 * @return should never return
 */
int main(void) {


	//all interrupts are disabled prior to any interrupt vector table is moved
	__disable_irq();

	//relocate the interrupt vector table to SRAM
	CopyInterruptToSRAM();

	// The MAP bits in the SYSMEMREMAP register is set to 0x1,
	// indicating the vector table is located in the SRAM and not in the flash area
	Chip_SYSCTL_Map(REMAP_USER_RAM_MODE);

	//all interrupts are enabled after the interrupt vector table has been moved to SRAM
	__enable_irq();

	// Initialize main buffer used to read and write user memory
	uint8_t Buttons = 0;
	bool *LCD_initialized=FALSE;
	CurrentDisplay = DISPLAY_DEFAULT_MESSAGE;
	uint32_t *LCDScrollCount=0;

	check_Buttons(&Buttons);

	Setup();

	//If button 2 is pressed on start-up, reset the tag memory to the default (smart poster NDEF)
	if (Buttons == 0x02)
		factory_reset_Tag();


#ifdef INTERRUPT
	// If Interrupted Mode is enabled set the FD Pin to react on the SRAM
	NFC_SetFDOffFunction(ntag_handle,
			I2C_LAST_DATA_READ_OR_WRITTEN_OR_RF_SWITCHED_OFF_11b);
	NFC_SetFDOnFunction(ntag_handle, DATA_READY_BY_I2C_OR_DATA_READ_BY_RF_11b);
#endif

	InitTimer();

	// Main Loop
	while (1) {

		// Enable Passthrough Mode RF->I2C
		NFC_SetTransferDir(ntag_handle, RF_TO_I2C);
		NFC_SetPthruOnOff(ntag_handle, TRUE);

		// wait for RF Write in the SRAM terminator page
		wait_for_RF_write_in_SRAM();

		// get the SRAM Data
		memset(sram_buf, 0, NFC_MEM_SRAM_SIZE);
		NFC_ReadBytes(ntag_handle, NFC_MEM_ADDR_START_SRAM, sram_buf,
				NFC_MEM_SRAM_SIZE);

		// Check for requested action
		char command = sram_buf[NFC_MEM_SRAM_SIZE - 4];
		switch (command)
		{
			case 'S': {
				// run speed test
				error = speedTest();

				// let the Green LED blink if Speedtest was successful
				if (error) {
					HW_switchLEDs(REDLED);
					HAL_Timer_delay_ms(10);
					HW_switchLEDs(LEDOFF);
				} else {
					LED_blink(GREENLED);
				}
				break;
			}
			case 'L':{
				LED_Demo(&LCD_initialized,&LCDScrollCount);
				break;
			}
			case 'V':{
				send_VersionInfo();
				break;
			}
			case 'C':{
				RGBcube_Demo(&LCD_initialized);
				break;
			}
			case 'R':{
				factory_reset_Tag();
				break;
			}

		}
	}

// never leave this function
	while (1) {
		__WFE();
	}
	return 0;
}

//-----------------------------------------------------------------
//               Helping functions begin
//-----------------------------------------------------------------


void CopyInterruptToSRAM(void)
{
	unsigned int * flashPtr, * ramPtr;
	unsigned int * uLimit = (unsigned int *) (USER_FLASH_START+INTERRUPT_VECTOR_TABLE_SIZE);

	ramPtr = (unsigned int *) SRAM_MEMORY_START; //load SRAM starting at 0x1000 0000
	flashPtr = (unsigned int *) USER_FLASH_START;   //start of the interrupt vector table

	while (flashPtr < uLimit){

		*ramPtr = *flashPtr;
		ramPtr++;
		flashPtr++;
	}
}

//---------------------------------------------------------------------
void LED_Demo(bool *LCD_initialized, uint32_t *LCDScrollCount)
{
	//make a init only one time
	if(!init_led_demo)
	{
		HAL_BSP_BoardInit();
		init_led_demo = true;
	}

	uint8_t LCDmessagebuffer[160];

	// turn on the according LED
	uint8_t number = sram_buf[NFC_MEM_SRAM_SIZE - 3] - '0';
	HW_switchLEDs(number);

	//show energy harvesting voltage
	uint8_t Volt[2];
	DisplayVoltage();
	HW_getVolt(Volt);
	sram_buf[NFC_MEM_SRAM_SIZE - 8] = Volt[0];
	sram_buf[NFC_MEM_SRAM_SIZE - 7] = Volt[1];

	// App has enabled the temperature sensor
	if(sram_buf[NFC_MEM_SRAM_SIZE - 9] == 'E')
	{
		uint8_t Temperature[2];
		HW_getTemp(Temperature);
		sram_buf[NFC_MEM_SRAM_SIZE - 6] = Temperature[0];
		sram_buf[NFC_MEM_SRAM_SIZE - 5] = Temperature[1];

		DisplayTemp();
	}
	// App has disabled the temperature sensor
	if(sram_buf[NFC_MEM_SRAM_SIZE - 9] != 'E')
		DisplayEmptyTemp();

	// App has enabled the board LCD
	if(sram_buf[NFC_MEM_SRAM_SIZE - 10] == 'E')
	{
		if(*LCD_initialized==FALSE)
		{
			LCDInit();
			*LCD_initialized=TRUE;
		}
		else
			LCD_on();

//		DisplayVoltage();
//		DisplayTemp();

//		uint8_t Volt[2];
//		HW_getVolt(Volt);
//		sram_buf[NFC_MEM_SRAM_SIZE - 8] = Volt[0];
//		sram_buf[NFC_MEM_SRAM_SIZE - 7] = Volt[1];

		// App has enabled the display of the NDEF message on the LCD
		if(sram_buf[NFC_MEM_SRAM_SIZE - 11] == 'E')
		{
			uint8_t *NDEFmessagelen=0;
			uint32_t index=0;
			uint8_t rxbuffer[4 * NFC_I2C_BLOCK_SIZE];
			uint8_t txbuffer[2 * NFC_I2C_BLOCK_SIZE];
			memset(rxbuffer, 0, 4 * NFC_I2C_BLOCK_SIZE);
			memset(txbuffer, 0, 2 * NFC_I2C_BLOCK_SIZE);
			memset(LCDmessagebuffer, 0x20, 160);

			// prepare defined state for SRAM
			NFC_SetPthruOnOff(ntag_handle, FALSE);

			//Read NDEF from EEPROM
			NFC_ReadBytes(ntag_handle, NFC_MEM_ADDR_START_USER_MEMORY, rxbuffer,
					4 * NFC_I2C_BLOCK_SIZE);

			//NDEF message is parsed so it can be displayed on the LCD
			parse_ndef(rxbuffer, LCDmessagebuffer, &NDEFmessagelen);

			NFC_SetPthruOnOff(ntag_handle, TRUE);
			if(CurrentDisplay == DISPLAY_DEFAULT_MESSAGE)
			{
				LCDWriteNDEFmessage(&LCDmessagebuffer[16], 16);
				CurrentDisplay = DISPLAY_NDEF_MESSAGE;
				*LCDScrollCount = 16;  			// Reset the message scroll counter
				LPC_TIMER32_0->IR |= (1<<0);  	// clear timer interrupt
				LPC_TIMER32_0->TCR = (1<<0);	// Release CT32B_0 reset, and start
			}

			if ((LPC_TIMER32_0->IR & 1) && (NDEFmessagelen > 16))	// Timer match used for scrolling feature
			{
				LPC_TIMER32_0->IR |= (1<<0);  		// clear timer interrupt
				if (*LCDScrollCount < (NDEFmessagelen)+16)
					(*LCDScrollCount)++;
				else
					*LCDScrollCount=0;
				index=*LCDScrollCount;
				LCDWriteNDEFmessage(&LCDmessagebuffer[index], 16);
			}
		}
		else
		{
			//Display default message on the LCD
			memcpy(LCDmessagebuffer,"NTAGI2C Explorer",16);
			LCDWrite(0, LCDmessagebuffer, 16);
			CurrentDisplay = DISPLAY_DEFAULT_MESSAGE;
		}
	}
	// App has disabled the LCD
	else
		LCD_off();

	//Check pressed buttons
	uint8_t Buttons;
	check_Buttons(&Buttons);
	sram_buf[NFC_MEM_SRAM_SIZE - 2] = Buttons;

	// write back Data
	NFC_SetTransferDir(ntag_handle, I2C_TO_RF);
	NFC_WriteBytes(ntag_handle, NFC_MEM_ADDR_START_SRAM, sram_buf,
	NFC_MEM_SRAM_SIZE);

	// waiting till RF has read
	NFC_WaitForEvent(ntag_handle, NTAG_EVENT_RF_READ_SRAM,
	SRAM_TIMEOUT, FALSE);

}

//---------------------------------------------------------------------
void RGBcube_Demo(bool *LCD_initialized){
	//NVIC_EnableIRQ(TIMER_32_1_IRQn);
	if(!init_cube_demo)
	{
		InitCubeDemo();
		init_cube_demo = true;
	}

	if(*LCD_initialized==FALSE)
	{
		LCDInit();
		*LCD_initialized=TRUE;
	}
	else
		LCD_on();

	//0x00	green
	//0x01	blue
	//0x02	red
	HW_dimLEDs(0x02, sram_buf[NFC_MEM_SRAM_SIZE - 5]);
	HW_dimLEDs(0x01, sram_buf[NFC_MEM_SRAM_SIZE - 7]);
	HW_dimLEDs(0x00, sram_buf[NFC_MEM_SRAM_SIZE - 6]);

	DisplayRGBColorCode();

	uint8_t Buttons = 0;
	check_Buttons(&Buttons);
	if(Buttons != 0x0)
	{
		if((Buttons & 0x01) == 0x01)
			sram_buf[NFC_MEM_SRAM_SIZE - 5] = 0xFF;
		else
			sram_buf[NFC_MEM_SRAM_SIZE - 5] = 0x00;
		if((Buttons & 0x04) == 0x04)
			sram_buf[NFC_MEM_SRAM_SIZE - 6] = 0xFF;
		else
			sram_buf[NFC_MEM_SRAM_SIZE - 6] = 0x00;
		if((Buttons & 0x02) == 0x02)
			sram_buf[NFC_MEM_SRAM_SIZE - 7] = 0xFF;
		else
			sram_buf[NFC_MEM_SRAM_SIZE - 7] = 0x00;
	}

	// write back Data
	NFC_SetTransferDir(ntag_handle, I2C_TO_RF);
	NFC_WriteBytes(ntag_handle, NFC_MEM_ADDR_START_SRAM, sram_buf,
	NFC_MEM_SRAM_SIZE);

	// waiting till RF has read
	NFC_WaitForEvent(ntag_handle, NTAG_EVENT_RF_READ_SRAM,
	SRAM_TIMEOUT, FALSE);

}

//---------------------------------------------------------------------
#if defined (NTAG_I2C_PLUS)
void factory_reset_Tag()
{
	HW_switchLEDs(REDLED);
	HAL_Timer_delay_ms(100);

	//reset default eeprom memory values (smart poster)
	NFC_WriteBytes(ntag_handle, NTAG_MEM_ADRR_I2C_ADDRESS,
			Default_BeginingOfMemory, Default_BeginingOfMemory_length);


	//reset pages from 8 to 56
	uint8_t page = 8;
	while(page < 56)
	{
		NFC_WriteBlock(ntag_handle, page, Null_Block, NTAG_I2C_BLOCK_SIZE);
		page ++;
	}
	//reset pages 56,57,58
	NFC_WriteBlock(ntag_handle, 56, Default_Page_56, NTAG_I2C_BLOCK_SIZE);
	NFC_WriteBlock(ntag_handle, 57, Default_Page_57, NTAG_I2C_BLOCK_SIZE);
	NFC_WriteBlock(ntag_handle, 58, Default_Page_58, NTAG_I2C_BLOCK_SIZE);

	HW_switchLEDs(GREENLED);
	HAL_Timer_delay_ms(100);
}
#else
void factory_reset_Tag()
{
	// config registers memory address for NTAG I2C 1K version
	uint8_t config = NTAG_MEM_BLOCK_CONFIGURATION_1k;

	//default config register values as defined by the datasheet
	uint8_t default_config_reg[NTAG_I2C_BLOCK_SIZE]={0x01, 0x00, 0xF8, 0x48, 0x08,0x01, 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
	HW_switchLEDs(REDLED);
	HAL_Timer_delay_ms(100);

	//reset default eeprom memory values (smart poster)
	NFC_WriteBytes(ntag_handle, NFC_MEM_ADDR_START_USER_MEMORY,
		Default_NDEF_Message, Default_NDEF_Message_length);

	//reset default config registers
	NTAG_WriteBlock(ntag_handle, config, default_config_reg, NTAG_I2C_BLOCK_SIZE);

	HW_switchLEDs(GREENLED);
	HAL_Timer_delay_ms(100);
}
#endif

//---------------------------------------------------------------------
void wait_for_RF_write_in_SRAM() {

	uint32_t counter= HAL_Timer_getTime_ms();

	// wait for RF Write in the SRAM terminator page
	while (NFC_WaitForEvent(ntag_handle, NTAG_EVENT_RF_WROTE_SRAM,
	SRAM_TIMEOUT, FALSE)) {

		// check if PT is off(happens when NTAG is not in the field anymore)
		// and switch it back on
		uint8_t reg = 0;
		NFC_ReadRegister(ntag_handle, NFC_MEM_OFFSET_NC_REG, &reg);
		if (!(reg & NFC_NC_REG_MASK_PTHRU_ON_OFF)) {
			NFC_SetTransferDir(ntag_handle, RF_TO_I2C);
			NFC_SetPthruOnOff(ntag_handle, TRUE);
		}
		if((HAL_Timer_getTime_ms() - counter > 1000))
		{
			LCD_off();
			break;
		}
	}
}
//---------------------------------------------------------------------
void send_VersionInfo() {

	// ---- Version information ----
	memset(sram_buf, 0, NFC_MEM_SRAM_SIZE);
	int index = 0;
	memcpy(&sram_buf[index], "Board Ver.: ", 12);
	index = 12;
	sram_buf[index++] = CHAR_BOARD_MAJ;
	sram_buf[index++] = '.';
	sram_buf[index++] = CHAR_BOARD_MIN;
	sram_buf[index++] = '\n';

	memcpy(&sram_buf[index], "FW    Ver.: ", 12);
	index += 12;
	sram_buf[index++] = CHAR_FW_MAJ;
	sram_buf[index++] = '.';
	sram_buf[index++] = CHAR_FW_MIN;
	sram_buf[index++] = '\n';

	// write back Version information
	NFC_SetTransferDir(ntag_handle, I2C_TO_RF);
	NFC_WriteBytes(ntag_handle, NFC_MEM_ADDR_START_SRAM, sram_buf,
	NFC_MEM_SRAM_SIZE);

	// waiting till RF has read
	NFC_WaitForEvent(ntag_handle, NTAG_EVENT_RF_READ_SRAM,
	SRAM_TIMEOUT, FALSE);
}

//---------------------------------------------------------------------
void HW_setup_Speedtest() {
#if defined(Board_NTAG_I2C_Explorer)
        LPC_SYSCTL->PDRUNCFG &= ~((1 << 1) | (1 << 0)); // turn on the IRC
        Chip_Clock_SetMainClockSource(SYSCTL_MAINCLKSRC_IRC);
        SystemCoreClockUpdate();

        Chip_I2CM_SetBusSpeed(LPC_I2C, 400000);
        SysTick_Config(SystemCoreClock/1000);           // Use systick timer for timing
#else
        return;
#endif
}

//---------------------------------------------------------------------
short int speedTest() {
	uint32_t start_reader;
	uint32_t stop_reader;

	uint32_t start_tag;
	uint32_t stop_tag;

	//to ensure init system clock back to default for LED demo
	init_led_demo = false;

	int counter = 0;
	memset(sram_buf, 0, NFC_MEM_SRAM_SIZE);
	error = NFC_SetPthruOnOff(ntag_handle, TRUE);

	if (error != NTAG_ERR_OK)
		return error;

	HW_setup_Speedtest();

	// save start timer
	start_reader = HAL_Timer_getTime_ms();

	// Reset the seed to its initial value for the CRC32 calculation
	uint32_t seed = 0xFFFFFFFF;
	uint32_t crcRx = 0;

	int lastBlock = true;
	// Begin to Read Data
	do {

		//----------------------------------------------
		// wait for data write by RF
		if (NFC_WaitForEvent(ntag_handle, NTAG_EVENT_RF_WROTE_SRAM,
		SRAM_TIMEOUT, TRUE))
			return NTAG_ERR_COMMUNICATION;

		//----------------------------------------------
		// data is ready, read whole SRAM
		error = NFC_ReadBytes(ntag_handle, NFC_MEM_ADDR_START_SRAM, sram_buf,
		NFC_MEM_SRAM_SIZE);
		if (error)
			return NTAG_ERR_COMMUNICATION;
		counter++;

		// Set LED
		HW_switchLEDs(counter % 2 ? LEDOFF : BLUELED);

		// The last block contains the "finish_S" string to notify the microcontroller about the message transmission completion
		lastBlock = strncmp((char*) &sram_buf[0], "finish_S", 8);

		// Update content for the CRC32 calculation
		if (lastBlock != 0) {
			crcRx = crc32(seed, sram_buf, NFC_MEM_SRAM_SIZE);
			seed = crcRx;
		} else {
			crcRx = crc32(seed, sram_buf, NFC_MEM_SRAM_SIZE - 4);
		}
	} while (lastBlock != 0);

	// Get the CRC values in reception
	uint32_t crcReceived = (uint8_t) sram_buf[NFC_MEM_SRAM_SIZE - 1] << 24
			| (uint8_t) sram_buf[NFC_MEM_SRAM_SIZE - 2] << 16
			| (uint8_t) sram_buf[NFC_MEM_SRAM_SIZE - 3] << 8
			| (uint8_t) sram_buf[NFC_MEM_SRAM_SIZE - 4];

	// save stop timer
	stop_reader = HAL_Timer_getTime_ms();

	// switch PT direction I2C -> RF
	NFC_SetTransferDir(ntag_handle, I2C_TO_RF);

	//clear sram Buffer
	memset(sram_buf, 0, NFC_MEM_SRAM_SIZE);

	int pack = counter;

	// save start timer
	start_tag = HAL_Timer_getTime_ms();

	// Reset the seed to its initial value for the CRC32 calculation
	seed = 0xFFFFFFFF;
	uint32_t crcTx = 0;

	// Begin to Write Data
	for (; counter != 0; counter--) {
		// write number do identify blocks
		sram_buf[0] = pack - counter;

		// for last block write, write finish identifier
		// The last block contains the "finish_S" string to notify the LPC board about the message transmission completion
		if (counter == 1) {
			memcpy(sram_buf, "finish_S", 8);

			// Update content for the CRC32 calculation (last 4 bytes are the CRC32)
			crcTx = crc32(seed, sram_buf, NFC_MEM_SRAM_SIZE - 4);

			// Write the result of the prev CRC check
			if (crcRx != crcReceived)
				sram_buf[NFC_MEM_SRAM_SIZE - 5] = 0x01;
			else
				sram_buf[NFC_MEM_SRAM_SIZE - 5] = 0x00;

			// Append the CRC32 to the block as this is the last block to be transmitted
			sram_buf[NFC_MEM_SRAM_SIZE - 4] = (crcTx >> 0) & 0xFF;
			sram_buf[NFC_MEM_SRAM_SIZE - 3] = (crcTx >> 8) & 0xFF;
			sram_buf[NFC_MEM_SRAM_SIZE - 2] = (crcTx >> 16) & 0xFF;
			sram_buf[NFC_MEM_SRAM_SIZE - 1] = (crcTx >> 24) & 0xFF;
		} else {
			// Update content for the CRC32 calculation
			crcTx = crc32(seed, sram_buf, NFC_MEM_SRAM_SIZE);
			seed = crcTx;
		}

		//----------------------------------------------
		// Write Data to SRAM
		error = NFC_WriteBytes(ntag_handle, NFC_MEM_ADDR_START_SRAM, sram_buf,
		NFC_MEM_SRAM_SIZE);

		if (error)
			return NTAG_ERR_COMMUNICATION;
		//----------------------------------------------
		// wait for data read
		if (NFC_WaitForEvent(ntag_handle, NTAG_EVENT_RF_READ_SRAM,
		SRAM_TIMEOUT, TRUE))
			return NTAG_ERR_COMMUNICATION;

		//SetLED
		HW_switchLEDs(counter % 2 ? LEDOFF : BLUELED);

	}

	// stop time
	stop_tag = HAL_Timer_getTime_ms();

	// calculate Time
//	uint32_t time1 = stop_reader - start_reader;
//	uint32_t time2 = stop_tag - start_tag;

#ifdef WRITE_TIME_VIA_NDEF
	// Writes the Time in the UserMemory as a NDEF Message
	write_back_Time_as_NDEF(time1, time2);
#endif

	//HW_tear_down_Speedtest();
	// Check the integrity of the data received (not done before to avoid stopping the execution of the rest of the SpeedTest demo)
	if (crcRx != crcReceived)
		return NTAG_ERR_COMMUNICATION;

	return NTAG_ERR_OK;
}

//---------------------------------------------------------------------
void Setup() {
	HW_switchLEDs(LEDOFF);

	// Initialize peripherals
	HAL_BSP_BoardInit();

	// Setup Pins on the microcontroller
	HW_setup_Board_for_use_with_NTAG();

	// enable delay timer
	HAL_Timer_Init();

	// Initialize the Interrupt Service Routine
	HAL_ISR_Init();

	// Initialize I2C
	i2cHandleMaster = HAL_I2C_InitDevice(HAL_I2C_INIT_DEFAULT);
	SystemCoreClockUpdate();

	// Set interrupt for time measurement
	SysTick_Config(SystemCoreClock / 1000); // produce a timer interrupt every 1ms

	// Initialize the NTAG I2C components
	ntag_handle = NFC_InitDevice(NFC_TEST_DEVICE, i2cHandleMaster);
	HAL_ISR_RegisterCallback(0, ISR_LEVEL_LO, NULL, NULL);

	// Enable IRQ for BOD
	Chip_SYSCTL_EnableBODReset();

	HW_init_Peripherals();
}

void InitCubeDemo(void){

	InitTimer1();
	InitPWM();
	InitPWMPins(true);

	/* Enable timer interrupt */
	NVIC_ClearPendingIRQ(TIMER_32_1_IRQn);
	NVIC_EnableIRQ(TIMER_32_1_IRQn);

	//0x00	green
	//0x01	blue
	//0x02	red
	HW_dimLEDs(0x00, 0x00);
	HW_dimLEDs(0x01, 0x00);
	HW_dimLEDs(0x02, 0x00);
	EnableTimer1();
}

//---------------------------------------------------------------------
void LED_blink(LED led) {
	HW_switchLEDs(LEDOFF);
	HAL_Timer_delay_ms(100);
	HW_switchLEDs(led);
	HAL_Timer_delay_ms(100);
	HW_switchLEDs(LEDOFF);
	HAL_Timer_delay_ms(100);
	HW_switchLEDs(led);
	HAL_Timer_delay_ms(100);
	HW_switchLEDs(LEDOFF);
	HAL_Timer_delay_ms(100);
	HW_switchLEDs(led);
	HAL_Timer_delay_ms(100);
	HW_switchLEDs(LEDOFF);
	HAL_Timer_delay_ms(100);

}

//---------------------------------------------------------------------
void check_Buttons(uint8_t *Buttons) {
	if (HW_Get_Button_State(Button1)) {
		*Buttons |= 0x01;
	} else {
		*Buttons &= ~0x01;
	}

	if (HW_Get_Button_State(Button2)) {
		*Buttons |= 0x02;
	} else {
		*Buttons &= ~0x02;
	}

	if (HW_Get_Button_State(Button3)) {
		*Buttons |= 0x04;
	} else {
		*Buttons &= ~0x04;
	}
}

//---------------------------------------------------------------------
short int write_back_Time_as_NDEF(int time1, int time2) {
//check for overflow
	if (time1 >= 99999)
		time1 = 99999;
	if (time2 >= 99999)
		time2 = 99999;

//----------------------------------------------
// write back times as ndef
	int index = 0;

// Header of the NDEF Message
	sram_buf[index++] = 0x03;
	sram_buf[index++] = 0x1C; // Message length
	sram_buf[index++] = 0xd1;

	sram_buf[index++] = 0x01; // Text Record
	sram_buf[index++] = 0x18; // Record length
	sram_buf[index++] = 0x54;
	sram_buf[index++] = 0x02;

	sram_buf[index++] = 0x65;
	sram_buf[index++] = 0x6e;

	char temp[5];
	uint8_t k;

	memset(temp, 0, 5);
	itoa(time1, temp, 10);

	sram_buf[index++] = '(';

	for (k = 0; k < 5 - strlen(temp); k++)
		sram_buf[index++] = '0';

	for (k = 0; k < strlen(temp); k++)
		sram_buf[index++] = temp[k];

	sram_buf[index++] = ')';

	memset(temp, 0, 5);
	itoa(time2, temp, 10);

	sram_buf[index++] = '(';

	for (k = 0; k < 5 - strlen(temp); k++)
		sram_buf[index++] = '0';

	for (k = 0; k < strlen(temp); k++)
		sram_buf[index++] = temp[k];

	sram_buf[index++] = ')';

	memset(temp, 0, 5);

	sram_buf[index++] = '(';

	for (k = 0; k < 5 - strlen(temp); k++)
		sram_buf[index++] = '0';

	for (k = 0; k < strlen(temp); k++)
		sram_buf[index++] = temp[k];

	sram_buf[index++] = ')';

// end of NDEF Message
	sram_buf[index++] = 0xFE;
	sram_buf[index++] = 0x00;

// Disable the PT
	error = NFC_SetPthruOnOff(ntag_handle, FALSE);

// get the timeout time
	int current_loop = HAL_Timer_getTime_ms() + SRAM_TIMEOUT;

	uint8_t reg = 0;
// write back the Result in EEPROM as NDEF
	do {
		error = NFC_WriteBytes(ntag_handle, NFC_MEM_ADDR_START_USER_MEMORY,
				sram_buf, index);

		NFC_ReadRegister(ntag_handle, NFC_MEM_OFFSET_NS_REG, &reg);
		while (reg & NFC_NS_REG_MASK_I2C_LOCKED) {
			NFC_ReadRegister(ntag_handle, NFC_MEM_OFFSET_NS_REG, &reg);
		}

		if (current_loop < HAL_Timer_getTime_ms())
			return NTAG_ERR_COMMUNICATION;

	} while (error != NTAG_ERR_OK);

	return NTAG_ERR_OK;
}

//---------------------------------------------------------------------
bool reset_AAR() {

	error = NFC_WriteBytes(ntag_handle, NFC_MEM_ADDR_START_USER_MEMORY,
			Default_NDEF_Message, Default_NDEF_Message_length);

	return error;
}

//---------------------------------------------------------------------
/*
 * Display the voltage, returned by the mobile app
 */
void DisplayVoltage(void)
{
	uint8_t I2CMasterVoltageBuffer[6];

	if (sram_buf[57] != 0) {
	I2CMasterVoltageBuffer[0] = LCD_I2C_Address;
	I2CMasterVoltageBuffer[1] = COMMAND;
	I2CMasterVoltageBuffer[2] = Comm_SetDDRAMAddress | 0x40;
	Chip_I2CM_Write(LPC_I2C, &I2CMasterVoltageBuffer[0], 3);

	I2CMasterVoltageBuffer[1] = DATA;	// Control Byte: data write
	I2CMasterVoltageBuffer[2] = sram_buf[56];
	I2CMasterVoltageBuffer[3] = '.';
	I2CMasterVoltageBuffer[4] = sram_buf[57];
	I2CMasterVoltageBuffer[5] = 'V';

	Chip_I2CM_Write(LPC_I2C, &I2CMasterVoltageBuffer[0], 6);
	}

  return;
}
//---------------------------------------------------------------------
/*
 *The mobile application returns the temperature after it calculates the temperature
 */
void DisplayTemp(void)
{
	uint8_t I2CMasterTemperatureBuffer[14];

	I2CMasterTemperatureBuffer[0] = LCD_I2C_Address;
	I2CMasterTemperatureBuffer[1] = COMMAND;
	I2CMasterTemperatureBuffer[2] = Comm_SetDDRAMAddress | 0x45; // Set DDRAM to second line, character 5
	Chip_I2CM_Write(LPC_I2C, &I2CMasterTemperatureBuffer[0], 3);

	I2CMasterTemperatureBuffer[1] = DATA;	// Control Byte: data write
	I2CMasterTemperatureBuffer[2] = sram_buf[40];
	I2CMasterTemperatureBuffer[3] = sram_buf[41];
	I2CMasterTemperatureBuffer[4] = '.';
	I2CMasterTemperatureBuffer[5] = sram_buf[42];
	I2CMasterTemperatureBuffer[6] = 'C';

	I2CMasterTemperatureBuffer[7] = ' ';
	I2CMasterTemperatureBuffer[8] = sram_buf[46];
	I2CMasterTemperatureBuffer[9] = sram_buf[47];
	I2CMasterTemperatureBuffer[10] = '.';
	I2CMasterTemperatureBuffer[11] = sram_buf[48];
	I2CMasterTemperatureBuffer[12] = 'F';

	Chip_I2CM_Write(LPC_I2C, &I2CMasterTemperatureBuffer[0], 13);

}
//---------------------------------------------------------------------
/*
 *The mobile application has disabled the temperature, therefore, in the LCD is shown  --.- C --.- F
 */
void DisplayEmptyTemp(void)
{
	uint8_t I2CMasterTemperatureBuffer[14];

	I2CMasterTemperatureBuffer[0] = LCD_I2C_Address;
	I2CMasterTemperatureBuffer[1] = COMMAND;
	I2CMasterTemperatureBuffer[2] = Comm_SetDDRAMAddress | 0x45; // Set DDRAM to second line, character 5
	Chip_I2CM_Write(LPC_I2C, &I2CMasterTemperatureBuffer[0], 3);

	I2CMasterTemperatureBuffer[1] = DATA;	// Control Byte: data write
	I2CMasterTemperatureBuffer[2] = '-';
	I2CMasterTemperatureBuffer[3] = '-';
	I2CMasterTemperatureBuffer[4] = '.';
	I2CMasterTemperatureBuffer[5] = '-';
	I2CMasterTemperatureBuffer[6] = 'C';

	I2CMasterTemperatureBuffer[7] = ' ';
	I2CMasterTemperatureBuffer[8] = '-';
	I2CMasterTemperatureBuffer[9] = '-';
	I2CMasterTemperatureBuffer[10] = '.';
	I2CMasterTemperatureBuffer[11] = '-';
	I2CMasterTemperatureBuffer[12] = 'F';

	Chip_I2CM_Write(LPC_I2C, &I2CMasterTemperatureBuffer[0], 13);

}

void get_dec_str (uint8_t* str, size_t len, uint32_t val)
{
  uint8_t i;
  for(i=1; i<=len; i++)
  {
    str[len-i] = (uint8_t) ((val % 10UL) + '0');
    val/=10;
  }

  str[i-1] = '\0';
}

//---------------------------------------------------------------------
/*
 *The NFC cube return RGB code of current color
 */
void DisplayRGBColorCode(void)
{
	uint8_t I2CMasterRGBCode[17];

	I2CMasterRGBCode[0] = LCD_I2C_Address;
	I2CMasterRGBCode[1] = COMMAND;
	I2CMasterRGBCode[2] = Comm_SetDDRAMAddress; // Set DDRAM to first
	Chip_I2CM_Write(LPC_I2C, &I2CMasterRGBCode[0], 3);

	I2CMasterRGBCode[1] = DATA;	// Control Byte: data write
	I2CMasterRGBCode[2] = ' ';
	I2CMasterRGBCode[3] = 'R';
	I2CMasterRGBCode[4] = ' ';
	I2CMasterRGBCode[5] = ' ';
	I2CMasterRGBCode[6] = ' ';
	I2CMasterRGBCode[7] = ' ';
	I2CMasterRGBCode[8] = ' ';
	I2CMasterRGBCode[9] = 'G';
	I2CMasterRGBCode[10] = ' ';
	I2CMasterRGBCode[11] = ' ';
	I2CMasterRGBCode[12] = ' ';
	I2CMasterRGBCode[13] = ' ';
	I2CMasterRGBCode[14] = ' ';
	I2CMasterRGBCode[15] = 'B';
	I2CMasterRGBCode[16] = ' ';

	Chip_I2CM_Write(LPC_I2C, &I2CMasterRGBCode[0], 17);

	I2CMasterRGBCode[0] = LCD_I2C_Address;
	I2CMasterRGBCode[1] = COMMAND;
	I2CMasterRGBCode[2] = Comm_SetDDRAMAddress | 0x40; // Set DDRAM to second line
	Chip_I2CM_Write(LPC_I2C, &I2CMasterRGBCode[0], 3);

	uint8_t num1[4];
	uint8_t num2[4];
	uint8_t num3[4];
	get_dec_str(num1,3,sram_buf[NFC_MEM_SRAM_SIZE - 5]);
	get_dec_str(num2,3,sram_buf[NFC_MEM_SRAM_SIZE - 6]);
	get_dec_str(num3,3,sram_buf[NFC_MEM_SRAM_SIZE - 7]);

	I2CMasterRGBCode[1] = DATA;	// Control Byte: data write
	I2CMasterRGBCode[2] = num1[0];
	I2CMasterRGBCode[3] = num1[1];
	I2CMasterRGBCode[4] = num1[2];
	I2CMasterRGBCode[5] = ' ';
	I2CMasterRGBCode[6] = ' ';
	I2CMasterRGBCode[7] = ' ';
	I2CMasterRGBCode[8] = num2[0];
	I2CMasterRGBCode[9] = num2[1];
	I2CMasterRGBCode[10] = num2[2];
	I2CMasterRGBCode[11] = ' ';
	I2CMasterRGBCode[12] = ' ';
	I2CMasterRGBCode[13] = ' ';
	I2CMasterRGBCode[14] = num3[0];
	I2CMasterRGBCode[15] = num3[1];
	I2CMasterRGBCode[16] = num3[2];

	Chip_I2CM_Write(LPC_I2C, &I2CMasterRGBCode[0], 17);

}


//---------------------------------------------------------------------
void LCDWrite(int LCDrow, uint8_t Data[], int len)
{
	uint8_t I2CMasterLCDBuffer[0x42];  //maximum data is 0x40 plus I2C Address + command byte
	uint32_t i;

	I2CMasterLCDBuffer[0] = LCD_I2C_Address;
	I2CMasterLCDBuffer[1] = COMMAND;
	if (LCDrow == 0)
		I2CMasterLCDBuffer[2] = Comm_SetDDRAMAddress; 			// Write to data RAM at address 0x00
	else
		I2CMasterLCDBuffer[2] = Comm_SetDDRAMAddress | 0x40; 	// Write to data RAM at address 0x40, which is the starting address of the second line
	Chip_I2CM_Write(LPC_I2C, &I2CMasterLCDBuffer[0], 3);

	I2CMasterLCDBuffer[0] = LCD_I2C_Address;
	I2CMasterLCDBuffer[1] = DATA;
	for (i=0; i<len; i++)
		I2CMasterLCDBuffer[i+2] = Data[i];

	Chip_I2CM_Write(LPC_I2C, &I2CMasterLCDBuffer[0], len+2);
}
//---------------------------------------------------------------------
//               Helping functions end
//---------------------------------------------------------------------
