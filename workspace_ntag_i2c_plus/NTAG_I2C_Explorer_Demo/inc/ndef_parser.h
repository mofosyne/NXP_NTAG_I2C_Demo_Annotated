/*
 * ndef_parser.h
 *
 *  Created on: 10/7/2015
 *      Author: Jordi
 */

#ifndef INC_NDEF_PARSER_H_
#define INC_NDEF_PARSER_H_
#endif /* INC_NDEF_PARSER_H_ */

#include <stdint.h>
#include <stdbool.h>

/*
 * TODO
 */
void parse_ndef( uint8_t rxbuffer[], uint8_t messagebuffer[],uint8_t *messagebuffer_size);

/*
 * TODO
 */
bool check_ndef_presence(uint8_t rxbuffer[]);

/*
 * TODO
 */
void getUriType(uint8_t uriType, uint8_t uriText[], uint8_t* uriLength);
