#ifndef __LCD__H_
#define __LCD_H_

#ifdef __cplusplus
extern "C" {
#endif

#define LCD_I2C_Address              0x7C
#define COMMAND                      0x00
#define DATA	                     0x40

#define Comm_ClearDisplay            0x01
#define Comm_ReturnHome              0x02
#define Comm_EntryModeSet            0x04
#define Comm_DisplayOnOff            0x0C
#define Comm_DisplayOff              0x08
#define Comm_InternalOscFrequency    0x14
#define Comm_Left_Shift              0x18
#define default_Contrast             0x35
#define Comm_FunctionSet_Normal      0x38
#define Comm_FunctionSet_Extended    0x39
#define Comm_PwrIconContrast         0x5C
#define Comm_FollowerCtrl            0x60
#define Comm_ContrastSet             0x70
#define Comm_SetDDRAMAddress         0x80

void DisplayVoltage(void);

void DisplayTemp(void);

void LCDInit(void);

void LCDInitUSB(void);

void LCDWrite(int LCDrow, uint8_t Data[], int len);

void LCDClearScreen(void);

void LCDWriteNDEFmessage(uint8_t Data[], int len);

void LCD_on(void);

void LCD_off(void);

#ifdef __cplusplus
}
#endif

#endif /* __LCD__H_ */
