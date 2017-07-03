# NTAGI2CDemo Annotated By NXP

This is mostly untouched source code, (just updated to run on android studio 2017).

I am interested in how this app works in communicating with the NTAG I2C plus tags, but it can be a bit hard to grok. So I'm annotating the code as needed to do what I'm trying to do.

* Source of source code http://www.nxp.com/products/identification-and-security/nfc-and-reader-ics/connected-tag-solutions/ntag-ic-plus-explorer-kit-development-kit:OM5569-NT322E?tab=Design_Tools_Tab


## Area where I have annotated functions:

* How does the phone firmware flash a device via the SRAM?
    - com/nxp/nfc_demo/reader/Ntag_I2C_Demo.java `public Boolean Flash(byte[] bytesToFlash)`

* How does the bootloader enter flash mode


## Bootloader Main Function Structure Summary

```
int main(void) {

	Initialize main buffer used to read and write user memory

	Enter peek and poke mode (If Power is sensed via USB port)

#ifdef INTERRUPT
	If Interrupted Mode is enabled set the FD Pin to react on the SRAM
#endif

	Setup Timer and Check Button State

	## No Button Pressed

	If no button is pressed, the secondary boot loader will start the execution of the user application
	Execution of the user application is performed by updating the Stack pointer (SP) and the program counter (PC) registers

	The Stack Pointer points to the new location where the user application has allocated the top of its stack
	The Program Counter contains the location of the first executable instruction in the user application
	From here on, the CPU will continue normal execution and initialization specified on the user application

	## Button 2 Is Pressed

	Is same as no button pressed for the bootloader. However this will enter the user application and perform the Reset Tag functionality, as the user application is already checking for this button.

	## Button 3 Is Pressed
	If the third button is pressed, we enter into FLASH MODE functionality

	// Main Loop when Button 3 is Pressed
	while (1) 
	{
		Initalise LCD

		Enable Passthrough Mode RF->I2C

		wait for RF Write in the SRAM terminator page

		get the SRAM Data

		Check for requested action by checking the 4th last character.

		If 4th last character has the value of 'F', then start in system programming mode via NFC `Flash_Demo()`
	}

}
```