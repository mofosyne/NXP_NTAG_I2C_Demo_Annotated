/*
 * flash_fw.h
 *
 *  Created on: 13/7/2015
 *      Author: Jordi
 */

#ifndef INC_FLASH_FW_H_
#define INC_FLASH_FW_H_
#endif /* INC_FLASH_FW_H_ */

#include <stdint.h>
#include <stdbool.h>
#include "iap_driver.h"

bool flash(void* flash_address, void* ram_address, int size);
bool flash_test(void* flash_address, void* ram_address, int size);
void iap_test();
void IAP_Read_Addr_0x2800_Demo();
bool flash_bootindicator(void* ram_address);


