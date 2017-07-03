################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../src/board.c \
../src/board_sysinit.c \
../src/usb_board_sysinit.c 

OBJS += \
./src/board.o \
./src/board_sysinit.o \
./src/usb_board_sysinit.o 

C_DEPS += \
./src/board.d \
./src/board_sysinit.d \
./src/usb_board_sysinit.d 


# Each subdirectory must supply rules for building sources it contributes
src/%.o: ../src/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: MCU C Compiler'
	arm-none-eabi-gcc -D__REDLIB__ -DDEBUG -D__CODE_RED -DCORE_M0 -DREV_G_HARDWARE -I"C:\Users\nxp91080\Documents\LPCXpresso_7.9.2_493\NTAG_I2C\workspace_ntag_i2c_new\lpc_chip_11uxx_lib\inc" -I"C:\Users\nxp91080\Documents\LPCXpresso_7.9.2_493\NTAG_I2C\workspace_ntag_i2c_new\nxp_lpcxpresso_11u24h_board_lib\inc" -O3 -g -Wall -c -fmessage-length=0 -fno-builtin -ffunction-sections -fdata-sections -mcpu=cortex-m0 -mthumb -D__REDLIB__ -specs=redlib.specs -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.o)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


