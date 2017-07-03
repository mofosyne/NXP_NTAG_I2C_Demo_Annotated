/*
****************************************************************************
* Copyright(c) 2014 NXP Semiconductors                                     *
* All rights are reserved.                                                 *
*                                                                          *
* Software that is described herein is for illustrative purposes only.     *
* This software is supplied "AS IS" without any warranties of any kind,    *
* and NXP Semiconductors disclaims any and all warranties, express or      *
* implied, including all implied warranties of merchantability,            *
* fitness for a particular purpose and non-infringement of intellectual    *
* property rights.  NXP Semiconductors assumes no responsibility           *
* or liability for the use of the software, conveys no license or          *
* rights under any patent, copyright, mask work right, or any other        *
* intellectual property rights in or to any products. NXP Semiconductors   *
* reserves the right to make changes in the software without notification. *
* NXP Semiconductors also makes no representation or warranty that such    *
* application will be suitable for the specified use without further       *
* testing or modification.                                                 *
*                                                                          *
* Permission to use, copy, modify, and distribute this software and its    *
* documentation is hereby granted, under NXP Semiconductors' relevant      *
* copyrights in the software, without fee, provided that it is used in     *
* conjunction with NXP Semiconductor products(UCODE I2C, NTAG I2C).        *
* This  copyright, permission, and disclaimer notice must appear in all    *
* copies of this code.                                                     *
****************************************************************************
*/
package com.nxp.nfc_demo.reader;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;

import com.nxp.nfc_demo.exceptions.CC_differException;
import com.nxp.nfc_demo.exceptions.CommandNotSupportedException;
import com.nxp.nfc_demo.exceptions.DynamicLockBitsException;
import com.nxp.nfc_demo.exceptions.NotPlusTagException;
import com.nxp.nfc_demo.exceptions.StaticLockBitsException;
import com.nxp.nfc_demo.listeners.WriteEEPROMListener;
import com.nxp.nfc_demo.listeners.WriteSRAMListener;
import com.nxp.nfc_demo.reader.Ntag_Get_Version.Prod;

public abstract class I2C_Enabled_Commands {

	/**
	 * This method returns the reader to be used based on the NFC Phone capabilities
	 * This distinction is needed because in the field we can find old NFC Phones that do not support 
	 * Sector Select or Get Version commands.
	 *
	 * @param tag
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static I2C_Enabled_Commands get(Tag tag) throws IOException,
			InterruptedException {
		byte[] answer;
		byte[] command = new byte[2];
		NfcA nfca = NfcA.get(tag);
		Prod prod;

		// Check for support of setTimout to be able to send an efficient
		// sector_select - select minimal implementation if not supported
		nfca.setTimeout(20);
		// check for timeout
		if (nfca.getTimeout() < 50) {		
			// check if GetVersion is supported
			try {
				nfca.connect();
				command = new byte[1];
				command[0] = (byte) 0x60; // GET_VERSION
				answer = nfca.transceive(command);
				prod = (new Ntag_Get_Version(answer)).Get_Product();
				nfca.close();
				if (prod == Prod.NTAG_I2C_1k || prod == Prod.NTAG_I2C_2k
						|| prod == Prod.NTAG_I2C_1k_T || prod == Prod.NTAG_I2C_2k_T
						|| prod == Prod.NTAG_I2C_1k_V || prod == Prod.NTAG_I2C_2k_V
						|| prod == Prod.NTAG_I2C_1k_Plus || prod == Prod.NTAG_I2C_2k_Plus) {
					return new Ntag_I2C_Commands(tag);
				}
			} catch (Exception e) {
				e.printStackTrace();
				nfca.close();
								
				// check if sector select is supported
				try {
					nfca.connect();
					command = new byte[2];
					command[0] = (byte) 0xC2; //SECTOR_SELECT
					command[1] = (byte) 0xFF;
					answer = nfca.transceive(command);
					nfca.close();
					return new Ntag_I2C_Commands(tag);
					
				} catch (Exception e2) {
					e.printStackTrace();
					nfca.close();
				}
			}
		}

		//check if we can use the minimal Version
		MifareUltralight mfu = MifareUltralight.get(tag);
		try {
			mfu.connect();
			command = new byte[1];
			command[0] = (byte) 0x60; // GET_VERSION
			answer = mfu.transceive(command);
			prod = (new Ntag_Get_Version(answer)).Get_Product();
			mfu.close();
			if (prod == Prod.NTAG_I2C_1k || prod == Prod.NTAG_I2C_2k
					|| prod == Prod.NTAG_I2C_1k_T || prod == Prod.NTAG_I2C_2k_T
					|| prod == Prod.NTAG_I2C_1k_V || prod == Prod.NTAG_I2C_2k_V
					|| prod == Prod.NTAG_I2C_1k_Plus || prod == Prod.NTAG_I2C_2k_Plus) {
				return new MinimalNtag_I2C_Commands(tag, prod);
			}
		} catch (Exception e) {
            e.printStackTrace();
            mfu.close();
            try {
                  mfu.connect();
                  answer = mfu.readPages(0);
                  // no exception is thrown so the phone can use the mfu.readPages
                  // function
                  // also check if:
                  // - tag is from NXP (byte 0 == 0x04)
                  // - CC corresponds to a NTAG I2C 1K
                  if (answer[0] == (byte) 0x04 && answer[12] == (byte) 0xE1
                                && answer[13] == (byte) 0x10 && answer[14] == (byte) 0x6D
                                && answer[15] == (byte) 0x00) {
                	  // check if Config is readable (distinguish from NTAG216), if
                      // not exception is thrown, and tag is not an
                      // NTAG I2C 1k
                      answer = mfu.readPages(0xE8);
                      
                      // Try to read session registers to differentiate between standard and PLUS products
                	  try {	 
                		  answer = mfu.readPages(0xEC);
                		  mfu.close();

                		  for(int i = 0; i < 4; i++) {
                			  if(answer[i] != 0x00) {
                				  prod = Prod.NTAG_I2C_1k_Plus;
                				  return new MinimalNtag_I2C_Commands(tag, prod);
                			  }
                		  }
                		  prod = Prod.NTAG_I2C_1k;
                		  return new MinimalNtag_I2C_Commands(tag, prod);
                	  } catch (Exception e2) {
                		  e2.printStackTrace();
                		  mfu.close();
                		  prod = Prod.NTAG_I2C_1k;
                		  return new MinimalNtag_I2C_Commands(tag, prod);
                	  }
                  } else if (answer[0] == (byte) 0x04 && answer[12] == (byte) 0xE1
                                && answer[13] == (byte) 0x10 && answer[14] == (byte) 0xEA
                                && answer[15] == (byte) 0x00) {
                	  // Try to read session registers to differentiate between standard and PLUS products
                	  try {	 
                		  answer = mfu.readPages(0xEC);
                		  mfu.close();
                		  for(int i = 0; i < 4; i++) {
                			  if(answer[i] != 0x00) {
                				  prod = Prod.NTAG_I2C_2k_Plus;
                				  return new MinimalNtag_I2C_Commands(tag, prod);
                			  }
                		  }
                		  prod = Prod.NTAG_I2C_2k;
                		  return new MinimalNtag_I2C_Commands(tag, prod);
                	  } catch (Exception e2) {
                		  e2.printStackTrace();
                		  mfu.close();
                		  prod = Prod.NTAG_I2C_2k;
                		  return new MinimalNtag_I2C_Commands(tag, prod);
                	  }
                  } else {
                	  mfu.close();
                	  return new MinimalNtag_I2C_Commands(tag, Prod.NTAG_I2C_1k_Plus);
                  }
            } catch (Exception e1) {
                  e1.printStackTrace();
                  mfu.close();
            }
		}
		return new MinimalNtag_I2C_Commands(tag, Prod.NTAG_I2C_1k_Plus);
	}

	protected int SRAMSize;

	public int getSRAMSize() {
		return SRAMSize;
	}

	protected int blockSize;

	public int getBlockSize() {
		return blockSize;
	}

	/**
	 * Different Read Methods which are possible with the NTAG I2C.
	 *
	 */
	public enum R_W_Methods {
		Fast_Mode, Polling_Mode, Error
	}

	/**
	 * Bits of the NS_REG Register.
	 *
	 */
	public enum NS_Reg_Func {
		RF_FIELD_PRESENT((byte) (0x01 << 0)), EEPROM_WR_BUSY((byte) (0x01 << 1)), EEPROM_WR_ERR(
				(byte) (0x01 << 2)), SRAM_RF_READY((byte) (0x01 << 3)), SRAM_I2C_READY(
				(byte) (0x01 << 4)), RF_LOCKED((byte) (0x01 << 5)), I2C_LOCKED(
				(byte) (0x01 << 6)), NDEF_DATA_READ((byte) (0x01 << 7)), ;
		private byte value;
		private NS_Reg_Func(byte value) {
			this.value = value;
		}
		public byte getValue() {
			return value;
		}
	}

	/**
	 * Bits of the NC_REG Register.
	 *
	 */
	public enum NC_Reg_Func {
		PTHRU_DIR((byte) (0x01 << 0)), SRAM_MIRROR_ON_OFF((byte) (0x01 << 1)), FD_ON(
				(byte) (0x03 << 2)), FD_OFF((byte) (0x03 << 4)), PTHRU_ON_OFF(
				(byte) (0x01 << 6)), I2C_RST_ON_OFF((byte) (0x01 << 7)), ;
		private byte value;
		private NC_Reg_Func(byte value) {
			this.value = value;
		}
		public byte getValue() {
			return value;
		}
	}

	/**
	 * Offset of the Config Registers.
	 *
	 */
	public enum CR_Offset {
		NC_REG((byte) 0x00), LAST_NDEF_PAGE((byte) 0x01), SM_REG((byte) 0x02), WDT_LS(
				(byte) 0x03), WDT_MS((byte) 0x04), I2C_CLOCK_STR((byte) 0x05), REG_LOCK(
				(byte) 0x06), FIXED((byte) 0x07);
		private byte value;
		private CR_Offset(byte value) {
			this.value = value;
		}
		public byte getValue() {
			return value;
		}
	}

	/**
	 * Offset of the Session Registers.
	 *
	 */
	public enum SR_Offset {
		NC_REG((byte) 0x00), LAST_NDEF_PAGE((byte) 0x01), SM_REG((byte) 0x02), WDT_LS(
				(byte) 0x03), WDT_MS((byte) 0x04), I2C_CLOCK_STR((byte) 0x05), NS_REG(
				(byte) 0x06), FIXED((byte) 0x07);

		private byte value;
		private SR_Offset(byte value) {
			this.value = value;
		}
		public byte getValue() {
			return value;
		}
	}
	
	/**
	 * Offset of the ACCESS Register.
	 *
	 */
	public enum Access_Offset {
		NFC_PROT((byte) 0x07), NFC_DIS_SEC1((byte) 0x05), AUTH_LIM((byte) 0x00);
		private byte value;
		private Access_Offset(byte value) {
			this.value = value;
		}
		public byte getValue() {
			return value;
		}
	}
		
	/**
	 * Offset of the PT_I2C Register.
	 *
	 */
	public enum PT_I2C_Offset {
		K2_PROT((byte) 0x03), SRAM_PROT((byte) 0x02), I2C_PROT((byte) 0x00);
		private byte value;
		private PT_I2C_Offset(byte value) {
			this.value = value;
		}
		public byte getValue() {
			return value;
		}
	}

	/**
	 * Closes the connection.
	 *
	 * @throws IOException
	 */
	public abstract void close() throws IOException;

	/**
	 * reopens the connection.
	 *
	 * @throws IOException
	 */
	public abstract void connect() throws IOException;

	/**
	 * reopens the connection.
	 *
	 * @throws IOException
	 */
	public abstract boolean isConnected();

	/**
	 * returns the last answer as Byte Array.
	 *
	 * @return Byte Array of the last Answer
	 */
	public abstract byte[] getLastAnswer();

	/**
	 * Gets the Product of the current Tag.
	 *
	 * @return Product of the Tag
	 * @throws IOException
	 */
	public abstract Prod getProduct() throws IOException;

	/**
	 * Gets all Session Registers as Byte Array.
	 *
	 * @return all Session Registers
	 * @throws IOException
	 * @throws FormatException
	 * @throws CommandNotSupportedException 
	 */
	public abstract byte[] getSessionRegisters() throws IOException,
			FormatException, CommandNotSupportedException;

	/**
	 * Gets all Config Registers as Byte Array.
	 *
	 * @return all Config Registers
	 * @throws IOException
	 * @throws FormatException
	 * @throws CommandNotSupportedException 
	 */
	public abstract byte[] getConfigRegisters() throws IOException,
			FormatException, CommandNotSupportedException;

	/**
	 * Gets a specific Config Register.
	 *
	 * @param off
	 *            Offset of the Config Register
	 * @return Register
	 * @throws IOException
	 * @throws FormatException
	 * @throws CommandNotSupportedException 
	 */
	public abstract byte getConfigRegister(CR_Offset off) throws IOException,
			FormatException, CommandNotSupportedException;

	/**
	 * Gets a specific Session Register.
	 *
	 * @param off
	 *            Offset of the Session Register
	 * @return Register
	 * @throws IOException
	 * @throws FormatException
	 * @throws CommandNotSupportedException 
	 */
	public abstract byte getSessionRegister(SR_Offset off) throws IOException,
			FormatException, CommandNotSupportedException;

	/**
	 * Writes the Config registers.
	 *
	 * @param NC_R
	 * @param LD_R
	 * @param SM_R
	 * @param WD_LS_R
	 * @param WD_MS_R
	 * @param I2C_CLOCK_STR
	 * @throws IOException
	 * @throws FormatException
	 * @throws CommandNotSupportedException
	 */
	public abstract void writeConfigRegisters(byte NC_R, byte LD_R, byte SM_R,
			byte WD_LS_R, byte WD_MS_R, byte I2C_CLOCK_STR) throws IOException,
			FormatException, CommandNotSupportedException;

	/**
	 * Checks if the Phone can write in the SRAM when PT is enabled.
	 *
	 * @return
	 * @throws FormatException
	 * @throws IOException
	 */
	public abstract Boolean checkPTwritePossible() throws IOException,
			FormatException;

	/**
	 * Waits till the I2C has written in the SRAM.
	 * @param timeoutMS Time to wait
	 *
	 * @throws IOException
	 * @throws FormatException
	 * @throws TimeoutException
	 */
	public abstract void waitforI2Cwrite(int timeoutMS) throws IOException, FormatException, TimeoutException;

	/**
	 * Waits till the I2C has read the SRAM.
	 * @param timeoutMS Time to wait
	 *
	 * @throws IOException
	 * @throws FormatException
	 * @throws TimeoutException
	 */
	public abstract void waitforI2Cread(int timeoutMS) throws IOException, FormatException, TimeoutException;

	/**
	 * Writes Data to the EEPROM as long as enough space is on the Tag.
	 *
	 * @param data
	 *            Raw Data to write
	 * @throws IOException
	 * @throws FormatException
	 * @throws CommandNotSupportedException
	 */
	public abstract void writeEEPROM(byte[] data, WriteEEPROMListener listener) throws IOException,
			FormatException, CommandNotSupportedException;

	/**
	 * Writes Data to the EEPROM as long as enough space is on the Tag.
	 *
	 * @param data
	 *            Raw Data to write
	 * @param startAddr
	 *            Start Address from which the write begins
	 * @throws IOException
	 * @throws FormatException
	 */
	public abstract void writeEEPROM(int startAddr, byte[] data)
			throws IOException, FormatException;

	/**
	 * Read Data from the EEPROM.
	 *
	 * @param absStart
	 *            Start of the read
	 * @param absEnd
	 *            End of the read(included in the Answer)
	 * @return Data read
	 * @throws IOException
	 * @throws FormatException
	 * @throws CommandNotSupportedException
	 * 
	 */
	public abstract byte[] readEEPROM(int absStart, int absEnd)
			throws IOException, FormatException, CommandNotSupportedException;

	/**
	 * Writes on SRAM Block Only 64 Bytes are transfered.
	 *
	 * @param data
	 *            Data to write
	 * @throws IOException
	 * @throws FormatException
	 * @throws CommandNotSupportedException
	 */
	public abstract void writeSRAMBlock(byte[] data, WriteSRAMListener listener) throws IOException,
			FormatException, CommandNotSupportedException;

	/**
	 * Writes Data in the SRAM, when 64 Bytes exceeded more Blocks are written.
	 *
	 * @param data
	 *            Data to write
	 * @throws IOException
	 * @throws FormatException
	 * @throws TimeoutException 
	 * @throws CommandNotSupportedException
	 */
	public abstract void writeSRAM(byte[] data, R_W_Methods method, WriteSRAMListener listener)
			throws IOException, FormatException, TimeoutException, CommandNotSupportedException;

	/**
	 * Reads one SRAM Block.
	 *
	 * @return Byte Array of the read
	 * @throws IOException
	 * @throws FormatException
	 * @throws CommandNotSupportedException
	 */
	public abstract byte[] readSRAMBlock() throws IOException, FormatException, CommandNotSupportedException;

	/**
	 * Reads as many Blocks as Specified.
	 *
	 * @param blocks
	 *            Blocks to read
	 * @param method
	 *            Method with which the SRAM is read
	 * @return Returns the Byte Array of the Read
	 * @throws IOException
	 * @throws FormatException
	 * @throws TimeoutException 
	 * @throws CommandNotSupportedException
	 */
	public abstract byte[] readSRAM(int blocks, R_W_Methods method)
			throws IOException, FormatException, TimeoutException, CommandNotSupportedException;

	/**
	 * Write an Empty NDEF Message to the NTAG.
	 *
	 * @throws IOException
	 * @throws FormatException
	 */
	public abstract void writeEmptyNdef() throws IOException, FormatException;
	
	/**
	 * Write Default NDEF to the NTAG.
	 *
	 * @throws IOException
	 * @throws FormatException
	 */
	public abstract void writeDefaultNdef() throws IOException, FormatException;

	/**
	 * Resets the Tag, this includes: Capability Container and User Memory.
	 * @return 
	 *
	 * @throws IOException
	 * @throws FormatException
	 * @throws CC_differException
	 * @throws StaticLockBitsException
	 * @throws DynamicLockBitsException
	 */
	public abstract int writeDeliveryNdef() throws IOException,
			FormatException, CC_differException, StaticLockBitsException,
			DynamicLockBitsException;

	/**
	 * Read a NDEF Message from the tag - not an official NFC Forum NDEF
	 * detection routine.
	 *
	 * @param message
	 *            NDEF message to write on the tag
	 * @throws IOException
	 * @throws FormatException
	 * @throws CommandNotSupportedException
	 */
	public abstract void writeNDEF(NdefMessage message, WriteEEPROMListener listener) throws IOException,
			FormatException, CommandNotSupportedException;

	/**
	 * Authenticate using PWD_AUTH command against NTAG I2C Plus product.
	 *
	 * @param pwd
	 *            4Byte password to authenticate with
	 * @throws IOException
	 * @throws NotPlusTagException
	 *            
	 */
	public abstract byte[] authenticatePlus(byte[] pwd) throws IOException, NotPlusTagException;
	
	/**
	 * Protect NTAG I2C Plus product memory map.
	 *
	 * @param pwd
	 *            4Byte password to authenticate with
	 * @param staretAddr
	 *            Page to lock the memory from
	 * @throws IOException
	 * @throws FormatException
	 *            
	 */
	public abstract void protectPlus(byte[] pwd, byte startAddr) throws IOException, FormatException, NotPlusTagException;
	
	/**
	 * Unprotect NTAG I2C Plus product memory map.
	 *
	 * @throws IOException
	 * @throws FormatException
	 * @throws NotPlusTagException
	 *            
	 */
	public abstract void unprotectPlus() throws IOException, FormatException, NotPlusTagException;
	
	/**
	 * Returns whether the NTAG I2C tag is protected or not.
	 *
	 * @throws IOException
	 * @throws FormatException
	 * @return Returns the protection status
	 *            
	 */
	public abstract int getProtectionPlus();
	
	/**
	 * Gets NTAG I2C Plus Access Register.
	 *
	 * @return ACCESS Plus Register
	 * @throws IOException
	 * @throws FormatException
	 * @throws CommandNotSupportedException
	 */
	public abstract byte[] getAccessRegister() throws IOException,
			FormatException, CommandNotSupportedException;
	
	/**
	 * Gets NTAG I2C Plus PT_I2C Register.
	 *
	 * @return PT_I2C Plus Register
	 * @throws IOException
	 * @throws FormatException
	 * @throws CommandNotSupportedException
	 */
	public abstract byte[] getPTI2CRegister() throws IOException,
			FormatException, CommandNotSupportedException;
	
	/**
	 * Gets NTAG I2C Plus Auth Register.
	 *
	 * @return auth0 Plus Register
	 * @throws IOException
	 * @throws FormatException
	 * @throws CommandNotSupportedException
	 */
	public abstract byte[] getAuth0Register() throws IOException,
			FormatException, CommandNotSupportedException;
	
	/**
	 * Writes Auth registers.
	 *
	 * @param auth0
	 * @param access
	 * @param pt_i2c
	 * @throws IOException
	 * @throws FormatException
	 * @throws CommandNotSupportedException
	 */
	public abstract void writeAuthRegisters(byte auth0, byte access, byte pt_i2c) throws IOException,
			FormatException, CommandNotSupportedException;
	
	/**
	 * Read a NDEF Message from the tag - not an official NFC Forum NDEF
	 * detection routine.
	 *
	 * @throws IOException
	 * @throws FormatException
	 * @throws CommandNotSupportedException
	 */
	public abstract NdefMessage readNDEF() throws IOException, FormatException, CommandNotSupportedException;

	protected byte[] concat(byte[] one, byte[] two) {
		if (one == null) {
			one = new byte[0];
		}
		if (two == null) {
			two = new byte[0];
		}
		byte[] combined = new byte[one.length + two.length];
		System.arraycopy(one, 0, combined, 0, one.length);
		System.arraycopy(two, 0, combined, one.length, two.length);
		return combined;
	}
}
