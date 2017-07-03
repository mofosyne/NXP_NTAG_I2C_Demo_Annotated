################################################################################
# Automatically-generated file. Do not edit!
################################################################################

-include ../makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include src/HAL_TMR/subdir.mk
-include src/HAL_NTAG/subdir.mk
-include src/HAL_ISR/subdir.mk
-include src/HAL_I2C/subdir.mk
-include src/HAL_BSPLPC11U37H/subdir.mk
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
all: libNTAG_I2C_API.a

# Tool invocations
libNTAG_I2C_API.a: $(OBJS) $(USER_OBJS)
	@echo 'Building target: $@'
	@echo 'Invoking: MCU Archiver'
	arm-none-eabi-ar -r  "libNTAG_I2C_API.a" $(OBJS) $(USER_OBJS) $(LIBS)
	@echo 'Finished building target: $@'
	@echo ' '
	$(MAKE) --no-print-directory post-build

# Other Targets
clean:
	-$(RM) $(OBJS)$(C_DEPS)$(ARCHIVES) libNTAG_I2C_API.a
	-@echo ' '

post-build:
	-@echo 'Performing post-build steps'
	-arm-none-eabi-size "libNTAG_I2C_API.a" ; # arm-none-eabi-objdump -h -S "libNTAG_I2C_API.a" >"libNTAG_I2C_API.lss"
	-@echo ' '

.PHONY: all clean dependents
.SECONDARY: post-build

-include ../makefile.targets
