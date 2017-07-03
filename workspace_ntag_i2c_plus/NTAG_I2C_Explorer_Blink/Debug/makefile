################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include src/subdir.mk
-include subdir.mk
-include objects.mk

ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
endif

-include ../makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 

# All Target
all: NTAG_I2C_Explorer_Blink.axf

dependents:
	-cd C:/Users/nxp91080/Documents/LPCXpresso_7.9.2_493/workspace_ntag_i2c_new/NTAG_I2C_API/Debug && $(MAKE) clean all
	-cd C:/Users/nxp91080/Documents/LPCXpresso_7.9.2_493/workspace_ntag_i2c_new/lpc_chip_11uxx_lib/Debug && $(MAKE) clean all

# Tool invocations
NTAG_I2C_Explorer_Blink.axf: $(OBJS) $(USER_OBJS) C:/Users/nxp91080/Documents/LPCXpresso_7.9.2_493/workspace_ntag_i2c_new/NTAG_I2C_API/Debug/libNTAG_I2C_API.a C:/Users/nxp91080/Documents/LPCXpresso_7.9.2_493/workspace_ntag_i2c_new/lpc_chip_11uxx_lib/Debug/liblpc_chip_11uxx_lib.a
	@echo 'Building target: $@'
	@echo 'Invoking: MCU Linker'
	arm-none-eabi-gcc -nostdlib -L"C:\Users\nxp91080\Documents\LPCXpresso_7.9.2_493\workspace_ntag_i2c_new\nxp_lpcxpresso_11u24h_board_lib\Debug" -L"C:\Users\nxp91080\Documents\LPCXpresso_7.9.2_493\workspace_ntag_i2c_new\lpc_chip_11uxx_lib\Debug" -L"C:\Users\nxp91080\Documents\LPCXpresso_7.9.2_493\workspace_ntag_i2c_new\NTAG_I2C_API\Debug" -Xlinker -Map="NTAG_I2C_Explorer_Blink.map" -Xlinker --gc-sections -mcpu=cortex-m0 -mthumb -T "NTAG_I2C_Explorer_Blink_Debug.ld" -o "NTAG_I2C_Explorer_Blink.axf" $(OBJS) $(USER_OBJS) $(LIBS)
	@echo 'Finished building target: $@'
	@echo ' '
	$(MAKE) --no-print-directory post-build

# Other Targets
clean:
	-$(RM) $(OBJS)$(C_DEPS)$(EXECUTABLES) NTAG_I2C_Explorer_Blink.axf
	-@echo ' '

post-build:
	-@echo 'Performing post-build steps'
	-arm-none-eabi-size NTAG_I2C_Explorer_Blink.axf ; arm-none-eabi-objcopy -O binary NTAG_I2C_Explorer_Blink.axf NTAG_I2C_Explorer_Blink.bin ; checksum -p LPC11U24_401 -d NTAG_I2C_Explorer_Blink.bin ; arm-none-eabi-objcopy -I binary -O ihex NTAG_I2C_Explorer_Blink.bin NTAG_I2C_Explorer_Blink.hex
	-@echo ' '

.PHONY: all clean dependents
.SECONDARY: post-build
C:/Users/nxp91080/Documents/LPCXpresso_7.9.2_493/workspace_ntag_i2c_new/NTAG_I2C_API/Debug/libNTAG_I2C_API.a:
C:/Users/nxp91080/Documents/LPCXpresso_7.9.2_493/workspace_ntag_i2c_new/lpc_chip_11uxx_lib/Debug/liblpc_chip_11uxx_lib.a:

-include ../makefile.targets
