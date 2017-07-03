#include <string.h>
#include "board.h"
#include <stdlib.h>
#include "global_variables.h"
#include "HW_functions.h"
#include "LCD.h"

//---------------------------------------------------------------------
//               Helping functions declarations
//---------------------------------------------------------------------

/*
 * Setup the Chip
 */
void Setup();

/*
 *  Let a LED blink (blocking function ~ 700ms)
 *  @param LED LED to blink
 */
void LED_blink(LED led);

/*
 * Copies the interrupt vector table from flash address USER_FLASH_START to SRAM address of 0x1000 0000
 */
void CopyInterruptToSRAM(void);


//---------------------------------------------------------------------
//               Helping functions declarations end
//---------------------------------------------------------------------
#define SRAM_MEMORY_START 			0x10000000
#define USER_FLASH_START 			0x4000
#define INTERRUPT_VECTOR_TABLE_SIZE 0x200

/*
 * main Program
 * @return should never return
 */
int main(void)
{
	//all interrupts are disabled prior to any interrupt vector table is moved
	__disable_irq();

	//relocate the interrupt vector table to SRAM
	CopyInterruptToSRAM();

	// The MAP bits in the SYSMEMREMAP register is set to 0x1,
	// indicating the vector table is located in the SRAM and not in the flash area
	Chip_SYSCTL_Map(REMAP_USER_RAM_MODE);

	//all interrupts are enabled after the interrupt vector table has been moved to SRAM
	__enable_irq();


	uint8_t reg = 0;
	uint8_t LCDmessagebuffer[160];

	Setup();
	InitTimer();

	memcpy(LCDmessagebuffer, " Field detected ",16);

	//we configure to active the field detection signal on the presence of the RF field.
	NFC_SetFDOnFunction(ntag_handle, RF_SWITCHED_ON_00b);

	// Main Loop
	while (1) {

		//Read NTAG I2C session registers. The last bit of NS_REG indicates whether RF field is detected
		NFC_ReadRegister(ntag_handle, NTAG_MEM_OFFSET_NS_REG, &reg);

		//If RF field is detected, we blink a green led + show a message on the LCD
		while((reg & NTAG_NS_REG_MASK_RF_FIELD_PRESENT)) {
			LCDInit();
			LCDWrite(0, LCDmessagebuffer, 16);
			LED_blink(GREENLED);

			//we check again if RF field is available
			NFC_ReadRegister(ntag_handle, NTAG_MEM_OFFSET_NS_REG, &reg);
		}
		LCD_off();
	}
	return 0;
}
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
void LED_blink(LED led)
{
	HW_switchLEDs(GREENLED);
	HAL_Timer_delay_ms(500);
	HW_switchLEDs(LEDOFF);
	HAL_Timer_delay_ms(500);
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

