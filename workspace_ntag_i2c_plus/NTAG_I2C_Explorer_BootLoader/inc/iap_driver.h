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

#ifndef IAP_DRIVER_H_
#define IAP_DRIVER_H_

/*
* IAP status codes
*/
typedef enum {
    CMD_SUCCESS = 0,
    INVALID_COMMAND,
    SRC_ADDR_ERROR,
    DST_ADDR_ERROR,
    SRC_ADDR_NOT_MAPPED,
    DST_ADDR_NOT_MAPPED,
    COUNT_ERROR,
    INVALID_SECTOR,
    SECTOR_NOT_BLANK,
    SECTOR_NOT_PREPARED_FOR_WRITE_OPERATION,
    COMPARE_ERROR,
    BUSY,
} __e_iap_status;

/**
* Init IAP driver
* @return    0 for success
*/
int iap_init(void);

/**
* Erase flash sector(s)
*
* @param sector_start  The start of the sector to be erased
* @param sector_end    The end of the sector to be erased
*
* @return CMD_SUCCESS, BUSY, SECTOR_NOT_PREPARED_FOR_WRITE_OPERATION,
*         or INVALID_SECTOR
*/
int iap_erase_sector(unsigned int sector_start, unsigned int sector_end);

/**
 * Erase flash page(s)
 *
 * @param page_start  The start of the page to be erased
 * @param page_end    The end of the page to be erased
 *
 * @return CMD_SUCCESS, BUSY, SECTOR_NOT_PREPARED_FOR_WRITE_OPERATION,
 *         or INVALID_SECTOR
 */
int iap_erase_page(unsigned int page_start, unsigned int page_end);

/**
 * Prepare flash sector(s) / page(s) for erase / writing
 *
 * @param sector_start  The start of the sector to be prepared
 * @param sector_end    The end of the sector to be prepared
 *
 * @return CMD_SUCCESS, BUSY, or INVALID_SECTOR
 */
int iap_prepare_sector(unsigned int sector_start, unsigned int sector_end);

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
        unsigned int count);

/**
 * Read part ID
 *
 * @param part_id Part ID
 *
 * @return CMD_SUCCESS
 */
int iap_read_part_id(uint32_t *part_id);

/**
 * Read Bootcode revision no
 *
 * @param bootcode_rev Bootcode revision no
 *
 * @return CMD_SUCCESS
 */
int iap_read_bootcode_rev(uint8_t *major, uint8_t* minor);

/**
 * Read device's unique ID no
 *
 * @param unique_id Unique ID no (4 x 32-bits)
 *
 * @return CMD_SUCCESS
 */
int iap_read_unique_id(uint32_t *u_id);

#endif /* IAP_DRIVER_H_ */
