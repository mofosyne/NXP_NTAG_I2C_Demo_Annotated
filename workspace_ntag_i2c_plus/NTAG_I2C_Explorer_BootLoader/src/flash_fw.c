/*
 * flash_fw.c
 *
 *  Created on: 13/7/2015
 *      Author: Jordi
 */

#include "flash_fw.h"
#include "string.h"


/*
 * Flash programming is based on a sector-by-sector basis.
 * This means that the code for the user application should not
 * be stored in any of the same flash sectors as the secondary boot loader
 */
bool flash(void* flash_address, void* ram_address, int size) {
	__e_iap_status iap_status;
//	unsigned int page = ((unsigned int) flash_address >> 6);
	unsigned int page = ((unsigned int) flash_address >> 8);
	unsigned int sector = (page >> 4);

//	unsigned int sector = 5;
//	uint32_t my_flash_address= 0x5000;
//	unsigned int pages_to_flash = size / 64;
//	unsigned int pages_to_flash = size / 256;
//	unsigned int sector_to_flash = (pages_to_flash >> 4);

	iap_init();

	//prepare sector for erase
	iap_status = (__e_iap_status) iap_prepare_sector(sector, sector);
	if (iap_status != CMD_SUCCESS) {
		return true;
	}

	//erase sector
	iap_status = (__e_iap_status) iap_erase_sector(sector, sector);
	if (iap_status != CMD_SUCCESS) {
		return true;
	}

	//prepare sector for writing
	iap_status = (__e_iap_status) iap_prepare_sector(sector, sector);
	if (iap_status != CMD_SUCCESS) {
		return true;
	}

	//writing to FLASH
	iap_status = (__e_iap_status ) iap_copy_ram_to_flash(ram_address,
			(void*) flash_address, size);
	if (iap_status != CMD_SUCCESS) {
		return true;
	}
//	puts ("sector flashed");

	return false;
}
