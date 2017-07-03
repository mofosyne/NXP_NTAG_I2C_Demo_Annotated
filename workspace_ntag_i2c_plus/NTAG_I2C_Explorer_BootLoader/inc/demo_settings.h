#ifndef DEMO_SETTINGS_H
#define DEMO_SETTINGS_H

// This file is intended to change the behavior of the Demo Application

// Version of Board and FW, change if needed
// Notice that only on char per define is allowed

#define CHAR_FW_MAJ '3'
#define CHAR_FW_MIN '4'


#define DISPLAY_DEFAULT_MESSAGE		0x00
#define DISPLAY_NDEF_MESSAGE		0x01

// Write back the timing as a NDEF Message(useful when you have no display
// to look at the timing and want to read it out later
// #define WRITE_TIME_VIA_NDEF

// Following I2C NFC devices are supported:
// NTAG I2C (standard)
// TNPI, switched via following define instead of NTAG I2C
// #define TNPI_DEVICE

// Following Boards are supported:
// #define Board_11U68
// #define Board_Demo_v1_4
// note that the NTAG I2C Explorer board is defined in the board libraries so it
// does not need a separate defintion

// Following Polling methods are supported
// Polling (standard)
// Interrupted, switched via following define instead of Polling
#define INTERRUPT

#endif
