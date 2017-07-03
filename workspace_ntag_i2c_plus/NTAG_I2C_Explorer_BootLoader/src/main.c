#include <string.h>
#include "board.h"
#include <stdlib.h>
#include "global_variables.h"
#include "demo_settings.h"
#include "HW_functions.h"
#include "LCD.h"
#include "flash_fw.h"

//---------------------------------------------------------------------
//               Helping functions declarations
//---------------------------------------------------------------------
/*
 * Waits till RF has written in the terminater page of the SRAM
 */
void wait_for_RF_write_in_SRAM();

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
 * Checks Buttons and gives them back as a bitfield
 * @param Buttons Bitfield in which the button state is written(first Button = LSB, true = pressed, false = not pressed)
 */
void check_Buttons(uint8_t *Buttons);

/*
 * Implements the IAP functions that enable to program the user application into the on-chip flash memory
 */
bool Flash_Demo();

/*
 * Shows on the LCD the NFC flashing status
 */
void flashing_status(char flash_status);

/*
 * Initializes the LCD
 */
void LCDinitialization();
//---------------------------------------------------------------------
//               Helping functions declarations end
//---------------------------------------------------------------------

bool LCD_initialized = false;
bool started=false;
bool flash_on_progress=false;
uint8_t LCDmessagebuffer[16];

#define USER_FLASH_START 0x4000

/*
 * The CPU execution jumps to the start of the user application
 * @param address: Memory address where the user application starts
 */
void boot_jump(uint32_t address) { 			// Starting address of user application
	__ASM volatile ("ldr r1, [r0]");  		// Get initial MSP value
	__ASM volatile ("mov sp, r1");    		// Set SP value
	__ASM volatile ("ldr r1, [r0, #4]"); 	// Get initial PC value
	__ASM volatile ("bx r1 "); 				// Jump to start address
}

/*
 * main Program
 * @return should never return
 */
int main(void) {

	// Initialize main buffer used to read and write user memory
	uint8_t Buttons = 0;
	bool VBUS_sense;

	// Enter peek and poke mode
	VBUS_sense = Chip_GPIO_ReadPortBit(LPC_GPIO, 0, 3);
	if (VBUS_sense && (Buttons == 0x00))
		hidmain();

	Setup();

#ifdef INTERRUPT
	// If Interrupted Mode is enabled set the FD Pin to react on the SRAM
	NFC_SetFDOffFunction(ntag_handle,
			I2C_LAST_DATA_READ_OR_WRITTEN_OR_RF_SWITCHED_OFF_11b);
	NFC_SetFDOnFunction(ntag_handle, DATA_READY_BY_I2C_OR_DATA_READ_BY_RF_11b);
#endif

	InitTimer();
	check_Buttons(&Buttons);

	/* If no button is pressed, the secondary boot loader will start the execution of the user application
	 * Execution of the user application is performed by updating the Stack pointer (SP) and the program counter (PC) registers
	 * The SP points to the new location where the user application has allocated the top of its stack
	 * The PC contains the location of the first executable instruction in the user application
	 * From here on, the CPU will continue normal execution and initialization specified on the user application
	 */
	/*
	 * If button 2 is pressed, we enter the user application and perform the Reset Tag functionality
	 */
	if ((Buttons == 0x00) || (Buttons == 0x02))
		boot_jump(USER_FLASH_START);

	/*
	 * If the third button is pressed, we enter into FLASH MODE functionality
	 */
	else if (Buttons == 0x04){
		// Main Loop
		while (1) {

			LCDinitialization();

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
			switch (command) {
				case 'F':{
					Flash_Demo();
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
	return 0;
}

//-----------------------------------------------------------------
//               Helping functions begin
//-----------------------------------------------------------------

//---------------------------------------------------------------------
bool Flash_Demo() {

	// Show in the LCD the flashing status
	char flash_status = sram_buf[NFC_MEM_SRAM_SIZE - 3];
	flashing_status(flash_status);

	// Flashing finished or failed
	if((flash_status=='S') || (flash_status=='F'))
		return false;

	started=true;

	//Get from Android app the size of the data to be received
	uint32_t size = sram_buf[NFC_MEM_SRAM_SIZE - 8] << 24;
	size |= sram_buf[NFC_MEM_SRAM_SIZE - 7] << 16;
	size |= sram_buf[NFC_MEM_SRAM_SIZE - 6] << 8;
	size |= sram_buf[NFC_MEM_SRAM_SIZE - 5];

	//Get from Android app the sector memory the data needs to be flashed
	uint32_t addresse = sram_buf[NFC_MEM_SRAM_SIZE - 12] << 24;
	addresse |= sram_buf[NFC_MEM_SRAM_SIZE - 11] << 16;
	addresse |= sram_buf[NFC_MEM_SRAM_SIZE - 10] << 8;
	addresse |= sram_buf[NFC_MEM_SRAM_SIZE - 9];

	if (size % 64)
		return true;

	//Store in RAM temporary the data to be flashed
	uint8_t* data = malloc(size);
	if (data == 0)
		return true;
	memset(data, 0, size);

	int blocks = size / (NFC_MEM_SRAM_SIZE);
	int counter = 0;

	//The Android app sends data in chunks of 64 bytes until all the data has been transmitted
	for (counter = 0; counter < blocks; counter++)
	{

		//----------------------------------------------
		// wait for data write by RF
		if (NFC_WaitForEvent(ntag_handle, NTAG_EVENT_RF_WROTE_SRAM,
		SRAM_TIMEOUT, TRUE))
			return true;

		//----------------------------------------------
		// data is ready, read whole SRAM
		error = NFC_ReadBytes(ntag_handle, NFC_MEM_ADDR_START_SRAM, sram_buf,
				NFC_MEM_SRAM_SIZE);
		if (error)
			return true;

		memcpy(data + counter * NFC_MEM_SRAM_SIZE, sram_buf, NFC_MEM_SRAM_SIZE);

//		char loop[33];
//		itoa (counter,loop,10);
//		puts (&loop);
//		printf("counter loop %d \n", counter);
		HW_switchLEDs(LEDOFF);
	}
	if (size % NFC_MEM_SRAM_SIZE) {
		//----------------------------------------------
		// wait for data write by RF
		if (NFC_WaitForEvent(ntag_handle, NTAG_EVENT_RF_WROTE_SRAM,
		SRAM_TIMEOUT, TRUE))
			return true;

		//----------------------------------------------
		// data is ready, read whole SRAM
		error = NFC_ReadBytes(ntag_handle, NFC_MEM_ADDR_START_SRAM, sram_buf,
				NFC_MEM_SRAM_SIZE);
		if (error)
			return true;

		memcpy(data + counter * NFC_MEM_SRAM_SIZE, sram_buf,
				size % NFC_MEM_SRAM_SIZE);
	}

	// Once all the data is stored in RAM, we flash it to the appropiate memory sector
	if (flash((void*) addresse, (void*) data, size)) {
		HW_switchLEDs(REDLED);
		HAL_Timer_delay_ms(10);
		HW_switchLEDs(LEDOFF);

		sram_buf[NFC_MEM_SRAM_SIZE - 4] = 'N';
		sram_buf[NFC_MEM_SRAM_SIZE - 3] = 'A';
		sram_buf[NFC_MEM_SRAM_SIZE - 2] = 'K';
	} else {
		sram_buf[NFC_MEM_SRAM_SIZE - 4] = 'A';
		sram_buf[NFC_MEM_SRAM_SIZE - 3] = 'C';
		sram_buf[NFC_MEM_SRAM_SIZE - 2] = 'K';
	}

	NFC_SetTransferDir(ntag_handle, I2C_TO_RF);
	NFC_SetPthruOnOff(ntag_handle, TRUE);

	// write back Data
	NFC_WriteBytes(ntag_handle, NFC_MEM_ADDR_START_SRAM, sram_buf,
			NFC_MEM_SRAM_SIZE);

	// waiting till RF has read
	NFC_WaitForEvent(ntag_handle, NTAG_EVENT_RF_READ_SRAM,
	SRAM_TIMEOUT, TRUE);

	// we free the temporary storage of the data
	free(data);

	return false;
}


//---------------------------------------------------------------------
void wait_for_RF_write_in_SRAM() {

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
	}
}
void flashing_status(char flash_status)
{

	if ((HAL_Timer_getTime_ms()>50) && (LCD_initialized==false))
	{
		LCDInit();
		LCD_initialized = true;
	}
	if(LCD_initialized)
	{
		//flashing is in progress
		if(flash_status == 'P')
		{
			flash_on_progress=true;
			memcpy(LCDmessagebuffer,"Flashing...     ",16);
			LCDWrite(0, LCDmessagebuffer, 16);
		}
		//flashing successful
		if(flash_status == 'S')
		{
			flash_on_progress=false;
			LED_blink(GREENLED);
			memcpy(LCDmessagebuffer,"Flashing OK     ",16);
			LCDWrite(0, LCDmessagebuffer, 16);
			HAL_Timer_delay_ms(500);
		}
		//flashing failed
		else if (flash_status == 'F')
		{
			flash_on_progress=false;
			LED_blink (REDLED);
			memcpy(LCDmessagebuffer,"Flashing FAILED ",16);
			LCDWrite(0, LCDmessagebuffer, 16);
			HAL_Timer_delay_ms(500);
		}
	}
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

void LCDinitialization()
{
	if (HAL_Timer_getTime_ms()>50)
	{
		LCDInit();
		LCD_initialized = true;
	}
	if((LCD_initialized)&&(started==false))
	{
		// Display on board LCD that we have enter into Flash mode
		memcpy(LCDmessagebuffer,"Flash Mode      ",16);
		LCDWrite(0, LCDmessagebuffer, 16);
	}
	if((LCD_initialized)&&(flash_on_progress==true))
	{
		memcpy(LCDmessagebuffer,"Flashing...     ",16);
		LCDWrite(0, LCDmessagebuffer, 16);
	}
}
//---------------------------------------------------------------------
//               Helping functions end
//---------------------------------------------------------------------
