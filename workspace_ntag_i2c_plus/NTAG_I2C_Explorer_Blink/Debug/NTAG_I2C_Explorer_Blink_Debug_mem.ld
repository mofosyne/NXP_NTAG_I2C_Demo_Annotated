/*
 * GENERATED FILE - DO NOT EDIT
 * (c) Code Red Technologies Ltd, 2008-2015
 * (c) NXP Semiconductors 2013-2015
 * Linker script memory definitions
 * Created from LinkMemoryTemplate
 * By LPCXpresso v7.7.2 [Build 379] [2015-03-31]  on Mon Aug 24 12:52:37 CEST 2015)
*/

MEMORY
{
  /* Define each memory region */
  MFlash32 (rx) : ORIGIN = 0x4000, LENGTH = 0x4000 /* 16K bytes */
  RamLoc8 (rwx) : ORIGIN = 0x10000200, LENGTH = 0x1de0 /* 7648 bytes */
  RamUsb2 (rwx) : ORIGIN = 0x20004000, LENGTH = 0x800 /* 2K bytes */


}
  /* Define a symbol for the top of each memory region */
  __top_MFlash32 = 0x4000 + 0x4000;
  __top_RamLoc8 = 0x10000200 + 0x1de0;
  __top_RamUsb2 = 0x20004000 + 0x800;



