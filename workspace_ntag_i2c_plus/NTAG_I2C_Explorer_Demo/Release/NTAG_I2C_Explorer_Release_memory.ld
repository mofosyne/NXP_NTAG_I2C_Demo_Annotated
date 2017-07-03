/*
 * GENERATED FILE - DO NOT EDIT
 * (c) Code Red Technologies Ltd, 2008-2013
 * (c) NXP Semiconductors 2013-2016
 * Generated linker script file for LPC11U24/401
 * Created from memory.ldt by FMCreateLinkMemory
 * Using Freemarker v2.3.23
 * LPCXpresso v7.9.2 [Build 493] [2015-09-14]  on Jan 12, 2016 9:31:57 AM
 */

MEMORY
{
  /* Define each memory region */
  MFlash32 (rx) : ORIGIN = 0x4000, LENGTH = 0x4000 /* 16K bytes (alias Flash) */  
  RamLoc8 (rwx) : ORIGIN = 0x10000200, LENGTH = 0x1de0 /* 7648 bytes (alias RAM) */  
  RamUsb2 (rwx) : ORIGIN = 0x20004000, LENGTH = 0x800 /* 2K bytes (alias RAM2) */  
}

  /* Define a symbol for the top of each memory region */
  __base_MFlash32 = 0x4000  ; /* MFlash32 */  
  __base_Flash = 0x4000 ; /* Flash */  
  __top_MFlash32 = 0x4000 + 0x4000 ; /* 16K bytes */  
  __top_Flash = 0x4000 + 0x4000 ; /* 16K bytes */  
  __base_RamLoc8 = 0x10000200  ; /* RamLoc8 */  
  __base_RAM = 0x10000200 ; /* RAM */  
  __top_RamLoc8 = 0x10000200 + 0x1de0 ; /* 7648 bytes */  
  __top_RAM = 0x10000200 + 0x1de0 ; /* 7648 bytes */  
  __base_RamUsb2 = 0x20004000  ; /* RamUsb2 */  
  __base_RAM2 = 0x20004000 ; /* RAM2 */  
  __top_RamUsb2 = 0x20004000 + 0x800 ; /* 2K bytes */  
  __top_RAM2 = 0x20004000 + 0x800 ; /* 2K bytes */  
