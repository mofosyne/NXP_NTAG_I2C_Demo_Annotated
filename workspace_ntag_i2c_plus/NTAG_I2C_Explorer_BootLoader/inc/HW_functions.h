#ifndef HW_FUNCTIONS_H
#define HW_FUNCTIONS_H

#include "demo_settings.h"

#ifdef Board_Demo_v1_4
#define VCC_SW  15
#define FD_PIN  4
#define BUTTON1_PIO IOCON_PIO13
#define BUTTON2_PIO IOCON_PIO17
#define BUTTON3_PIO IOCON_PIO8
#define TEMP_I2C_ADDRESS        (0x90)
typedef enum Button__
{
  Button1 = 13, Button2 = 17, Button3 = 8
} Button;

#define RED 0
#define GREEN 1
#define BLUE 2

#elif defined(Board_11U68)

typedef enum Button__
{
  Button1 = 0, Button2 = 0, Button3 = 0
} Button;


#define RED 0
#define GREEN 1
#define BLUE 2
#elif defined(Board_NTAG_I2C_Explorer)
// no Button define needed already in board lib

#else
typedef enum Button__
{
  Button1 = 0, Button2 = 0, Button3 = 0
} Button;

#endif

typedef enum LED__
{
  LEDOFF = 0, REDLED, BLUELED, GREENLED
}LED;

//---------------------------------------------------------------------
///
/// Get the Temp
/// @param led
void HW_switchLEDs(LED led);

//---------------------------------------------------------------------
///
/// sets up the Board so that the NTAG can be used
void HW_setup_Board_for_use_with_NTAG();

//---------------------------------------------------------------------
///
/// initilize the all Peripherals (ex.: Buttons, TempSensor, ...)
void HW_init_Peripherals();

//---------------------------------------------------------------------
///
/// Get button State
/// @param button button to check
/// @return the state of the Butten true = pressed, false = not pressed
//bool HW_Get_Button_State(Button button);

//---------------------------------------------------------------------
///
/// Get the Temp
/// @param Buffer Temp in encoded format, should return 0 when not implemented
//void HW_getTemp(uint8_t Buffer[]);

//---------------------------------------------------------------------
///
/// Get the Volt
/// @param Buffer Volt in encoded format, should return 0 when not implemented
void HW_getVolt(uint8_t Buffer[]);

//---------------------------------------------------------------------
///
/// Initialize Timer32_0 for a 1 ms tick rate
void InitTimer(void);

int hidmain(void);

bool HW_Get_Button_State(Button button);

#endif
