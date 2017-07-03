/****************************************************************************
 *   Project: NXP LPC8xx IAP example
 *
 *   Description:
 *     This file contains In-Application driver
 *
 ****************************************************************************
 * Software that is described herein is for illustrative purposes only
 * which provides customers with programming information regarding the
 * products. This software is supplied "AS IS" without any warranties.
 * NXP Semiconductors assumes no responsibility or liability for the
 * use of the software, conveys no license or title under any patent,
 * copyright, or mask work right to the product. NXP Semiconductors
 * reserves the right to make changes in the software without
 * notification. NXP Semiconductors also make no representation or
 * warranty that such application will be suitable for the specified
 * use without further testing or modification.
 ****************************************************************************/

#include <board.h>
#include "iap_driver.h"

#define IRQ_DISABLE

/*
 * The IAP function address in LPC11xx ROM
 */
#define IAP_ADDRESS            0x1FFF1FF1

int j;
/*
 * Command codes for IAP
 */
#define PREPARE_SECTOR      50
#define COPY_RAM_TO_FLASH   51
#define ERASE_SECTOR        52
#define BLANK_CHECK_SECTOR  53
#define READ_PART_ID        54
#define READ_BOOT_CODE_REV  55
#define COMPARE             56
#define REINVOKE_ISP        57
#define READ_UID            58
#define ERASE_PAGE          59

/* The command table */
struct __cmd_table {
	uint32_t cmd_code;
	uint32_t param[4];
};

static struct __cmd_table cmd_table;

/* The result table */
struct __result_table {
	uint32_t ret_code;
	uint32_t result[4];
};

static struct __result_table result_table;

/* The IAP entry function */
typedef unsigned int (*IAP)(struct __cmd_table*, struct __result_table*);
static const IAP iap_call = (IAP) IAP_ADDRESS;

/*---------------------------------------------------------------------------
 * Public functions
 */

/**
 * Init IAP driver
 * @return    0 for success
 */
int iap_init(void) {
	/* Need to update 'SystemCoreClock' according to the current clock settings
	 * It's needed as IAP parameter
	 */
	SystemCoreClockUpdate();
	return 0;
}

/**
 * Erase flash sector(s)
 *
 * @param sector_start  The start of the sector to be erased
 * @param sector_end    The end of the sector to be erased
 *
 * @return CMD_SUCCESS, BUSY, SECTOR_NOT_PREPARED_FOR_WRITE_OPERATION,
 *         or INVALID_SECTOR
 */
int iap_erase_sector(unsigned int sector_start, unsigned int sector_end) {
	cmd_table.cmd_code = ERASE_SECTOR;
	cmd_table.param[0] = sector_start;
	cmd_table.param[1] = sector_end;
	cmd_table.param[2] = SystemCoreClock/1000;

	#if defined IRQ_HANDLER_IN_SRAM
	iap_call(&cmd_table, &result_table);
	#endif

	#if defined IRQ_DISABLE
	__disable_irq();
	iap_call(&cmd_table, &result_table);
	__enable_irq();
	#endif

	return (int)result_table.ret_code;
}

/**
 * Erase flash page(s)
 *
 * @param page_start  The start of the page to be erased
 * @param page_end    The end of the page to be erased
 *
 * @return CMD_SUCCESS, BUSY, SECTOR_NOT_PREPARED_FOR_WRITE_OPERATION,
 *         or INVALID_SECTOR
 */
int iap_erase_page(unsigned int page_start, unsigned int page_end) {

	cmd_table.cmd_code = ERASE_PAGE;
	cmd_table.param[0] = page_start;
	cmd_table.param[1] = page_end;
	cmd_table.param[2] = SystemCoreClock/1000;

	#if defined IRQ_HANDLER_IN_SRAM
	iap_call(&cmd_table, &result_table);
	#endif

	#if defined IRQ_DISABLE
	__disable_irq();
	iap_call(&cmd_table, &result_table);
	__enable_irq();
	#endif

	return (int)result_table.ret_code;
}

/**
 * Prepare flash sector(s) / page(s) for erase / writing
 *
 * @param sector_start  The start of the sector to be prepared
 * @param sector_end    The end of the sector to be prepared
 *
 * @return CMD_SUCCESS, BUSY, or INVALID_SECTOR
 */
int iap_prepare_sector(unsigned int sector_start, unsigned int sector_end) {
	cmd_table.cmd_code = PREPARE_SECTOR;
	cmd_table.param[0] = sector_start;
	cmd_table.param[1] = sector_end;

	#if defined IRQ_HANDLER_IN_SRAM
	iap_call(&cmd_table, &result_table);
	#endif

	#if defined IRQ_DISABLE
	__disable_irq();
	iap_call(&cmd_table, &result_table);
	__enable_irq();
	#endif

	return (int)result_table.ret_code;
}

/**
 * Copy RAM contents into flash
 *
 * @param ram_address    RAM address to be copied
 *                       It should be in word boundary
 * @param flash_address  Flash address where the contents are to be copied
 *                       It should be within 64bytes boundary
 * @param count          Number of data to be copied (in bytes)
 *                       The options: 64, 128, 256, 512, 1024
 *
 * @return CMD_SUCCESS, BUSY, or INVALID_SECTOR
 */
int iap_copy_ram_to_flash(void* ram_address, void* flash_address,
		unsigned int count) {
	cmd_table.cmd_code = COPY_RAM_TO_FLASH;
	cmd_table.param[0] = (uint32_t) flash_address;
	cmd_table.param[1] = (uint32_t) ram_address;
	cmd_table.param[2] = count;
	cmd_table.param[3] = SystemCoreClock/1000;


	#if defined IRQ_HANDLER_IN_SRAM
	iap_call(&cmd_table, &result_table);
	#endif

	#if defined IRQ_DISABLE
	__disable_irq();
	iap_call(&cmd_table, &result_table);
	__enable_irq();
	#endif

	return (int)result_table.ret_code;
}

/**
 * Read part ID
 *
 * @param part_id Part ID
 *
 * @return CMD_SUCCESS
 */
int iap_read_part_id(uint32_t *part_id) {
	cmd_table.cmd_code = READ_PART_ID;


	#if defined IRQ_HANDLER_IN_SRAM
	iap_call(&cmd_table, &result_table);
	#endif

	#if defined IRQ_DISABLE
	__disable_irq();
	iap_call(&cmd_table, &result_table);
	__enable_irq();
	#endif

	*part_id = result_table.result[0];

	return (int)result_table.ret_code;
}

/**
 * Read Bootcode revision no
 *
 * @param bootcode_rev Bootcode revision no
 *
 * @return CMD_SUCCESS
 */
int iap_read_bootcode_rev(uint8_t *major, uint8_t* minor) {
	cmd_table.cmd_code = READ_BOOT_CODE_REV;

	#if defined IRQ_HANDLER_IN_SRAM
	iap_call(&cmd_table, &result_table);
	#endif

	#if defined IRQ_DISABLE
	__disable_irq();
	iap_call(&cmd_table, &result_table);
	__enable_irq();
	#endif
      if(major != 0x00)
         *major = (result_table.result[0] >> 8) & 0xFF;
      if(minor != 0x00)
         *minor = (result_table.result[0]) & 0xFF;


	return (int)result_table.ret_code;
}

/**
 * Read device's unique ID no
 *
 * @param unique_id Unique ID no (4 x 32-bits)
 *
 * @return CMD_SUCCESS
 */
int iap_read_unique_id(uint32_t *u_id) {

	cmd_table.cmd_code = READ_UID;

	#if defined IRQ_HANDLER_IN_SRAM
	iap_call(&cmd_table, &result_table);
	#endif

	#if defined IRQ_DISABLE
	__disable_irq();
	iap_call(&cmd_table, &result_table);
	__enable_irq();
	#endif

	u_id[0] = result_table.result[0];
	u_id[1] = result_table.result[1];
	u_id[2] = result_table.result[2];
	u_id[3] = result_table.result[3];

	return (int)result_table.ret_code;
}
