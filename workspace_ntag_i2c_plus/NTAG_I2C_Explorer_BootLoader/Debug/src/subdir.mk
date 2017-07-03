################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../src/HW_functions.c \
../src/flash_fw.c \
../src/hid_desc.c \
../src/hid_i2c.c \
../src/hid_main.c \
../src/iap_driver.c \
../src/main.c 

OBJS += \
./src/HW_functions.o \
./src/flash_fw.o \
./src/hid_desc.o \
./src/hid_i2c.o \
./src/hid_main.o \
./src/iap_driver.o \
./src/main.o 

C_DEPS += \
./src/HW_functions.d \
./src/flash_fw.d \
./src/hid_desc.d \
./src/hid_i2c.d \
./src/hid_main.d \
./src/iap_driver.d \
./src/main.d 


# Each subdirectory must supply rules for building sources it contributes
src/%.o: ../src/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: MCU C Compiler'
	arm-none-eabi-gcc -D__REDLIB__ -DBoard_NTAG_I2C_Explorer -D__LPC11U37H__ -DHAVE_NTAG_INTERRUPT -DHAVE_STDINT_H -DCORE_M0 -D__MTB_BUFFER_SIZE=2048 -DHAVE_STDBOOL_H -D__USE_LPC -DNDEBUG -D__CODE_RED -DBOARD_NTAG_I2C_EXPLORER -I"C:\Users\nxp91080\Documents\LPCXpresso_7.9.2_493\workspace_ntag_i2c_new\NTAG_I2C_API\src\inc" -I"C:\Users\nxp91080\Documents\LPCXpresso_7.9.2_493\workspace_ntag_i2c_new\NTAG_I2C_Explorer_BootLoader\inc" -I"C:\Users\nxp91080\Documents\LPCXpresso_7.9.2_493\workspace_ntag_i2c_new\nxp_lpcxpresso_11u24h_board_lib\inc" -I"C:\Users\nxp91080\Documents\LPCXpresso_7.9.2_493\workspace_ntag_i2c_new\lpc_chip_11uxx_lib\inc" -I"C:\Users\nxp91080\Documents\LPCXpresso_7.9.2_493\workspace_ntag_i2c_new\lpc_chip_11uxx_lib\inc\usbd" -Os -g3 -Wall -c -fmessage-length=0 -fno-builtin -ffunction-sections -fdata-sections -mcpu=cortex-m0 -mthumb -D__REDLIB__ -specs=redlib.specs -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.o)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


