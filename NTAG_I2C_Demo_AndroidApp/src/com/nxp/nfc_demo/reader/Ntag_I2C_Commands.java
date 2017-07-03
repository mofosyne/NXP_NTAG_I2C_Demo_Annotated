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
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;

import com.nxp.nfc_demo.activities.AuthActivity.AuthStatus;
import com.nxp.nfc_demo.activities.MainActivity;
import com.nxp.nfc_demo.exceptions.CC_differException;
import com.nxp.nfc_demo.exceptions.CommandNotSupportedException;
import com.nxp.nfc_demo.exceptions.DynamicLockBitsException;
import com.nxp.nfc_demo.exceptions.NotPlusTagException;
import com.nxp.nfc_demo.exceptions.StaticLockBitsException;
import com.nxp.nfc_demo.listeners.WriteEEPROMListener;
import com.nxp.nfc_demo.listeners.WriteSRAMListener;
import com.nxp.nfc_demo.reader.Ntag_Get_Version.Prod;

/**
 * Class specific for the functions of The NTAG I2C.
 * 
 * @author NXP67729
 * 
 */
public class Ntag_I2C_Commands extends I2C_Enabled_Commands {
	private static final int DEFAULT_NDEF_MESSAGE_SIZE = 0;
	private static final int EMPTY_NDEF_MESSAGE_SIZE = 104;
	private static final int SRAM_SIZE = 64;
	private static final int SRAM_BLOCK_SIZE = 4;

	private Ntag_Commands reader;
	private Tag tag;
	private byte[] answer;
	private Ntag_Get_Version getVersionResponse;
	private byte sramSector;
	private boolean TimeOut = false;
	private Object lock = new Object();

	/**
	 * Special Registers of the NTAG I2C.
	 *
	 */
	public enum Register {
		Session((byte) 0xF8), Session_PLUS((byte) 0xEC), Configuration((byte) 0xE8), SRAM_Begin((byte) 0xF0), 
		Capability_Container((byte) 0x03), User_memory_Begin((byte) 0x04), UID((byte) 0x00),
		AUTH0((byte) 0xE3), ACCESS((byte) 0xE4), PWD((byte) 0xE5), PACK((byte) 0xE6), PT_I2C((byte) 0xE7);
		private byte value;
		private Register(byte value) {
			this.value = value;
		}
		public byte getValue() {
			return value;
		}
	}

	// ---------------------------------------------------------------------------------
	// Begin Public Functions
	// ---------------------------------------------------------------------------------

	/**
	 * Constructor.
	 * 
	 * @param tag
	 *            Tag to connect
	 * @throws IOException
	 */
	public Ntag_I2C_Commands(Tag tag) throws IOException {
		blockSize = SRAM_BLOCK_SIZE;
		SRAMSize = SRAM_SIZE;
		this.reader = new Ntag_Commands(tag);
		this.tag = tag;
		connect();
		if (getProduct() == Prod.NTAG_I2C_2k) {
			sramSector = 1;
		} else {
			sramSector = 0;
		}
		close();

	}

	/*
	 * (non-Javadoc).
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#close()
	 */
	@Override
	public void close() throws IOException {
		reader.close();
	}

	/*
	 * (non-Javadoc).
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#connect()
	 */
	@Override
	public void connect() throws IOException {
		reader.connect();
	}

	/*
	 * (non-Javadoc).
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#isConnected()
	 */
	@Override
	public boolean isConnected() {
		return reader.isConnected();
	}

	/*
	 * (non-Javadoc).
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getLastAnswer()
	 */
	@Override
	public byte[] getLastAnswer() {
		return answer;
	}

	/*
	 * (non-Javadoc).
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getProduct()
	 */
	@Override
	public Prod getProduct() throws IOException {
		if (getVersionResponse == null) {
			try {
				getVersionResponse = new Ntag_Get_Version(reader.getVersion());
			} catch (Exception e) {
				e.printStackTrace();
				try {
					reader.close();
					reader.connect();
					byte[] temp = reader.read((byte) 0x00);

					if (temp[0] == (byte) 0x04 && temp[12] == (byte) 0xE1
							&& temp[13] == (byte) 0x10
							&& temp[14] == (byte) 0x6D
							&& temp[15] == (byte) 0x00) {

						temp = reader.read((byte) 0xE8);
						getVersionResponse = Ntag_Get_Version.NTAG_I2C_1k;

					} else if (temp[0] == (byte) 0x04
							&& temp[12] == (byte) 0xE1
							&& temp[13] == (byte) 0x10
							&& temp[14] == (byte) 0xEA
							&& temp[15] == (byte) 0x00) {
						getVersionResponse = Ntag_Get_Version.NTAG_I2C_2k;
					}
				} catch (FormatException e2) {
					reader.close();
					reader.connect();
					e2.printStackTrace();
					getVersionResponse = Ntag_Get_Version.NTAG_I2C_1k;
				}

			}
		}

		return getVersionResponse.Get_Product();
	}

	/*
	 * (non-Javadoc).
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getSessionRegisters()
	 */
	@Override
	public byte[] getSessionRegisters() throws IOException, FormatException {
		if (getProduct() == Prod.NTAG_I2C_1k_Plus || getProduct() == Prod.NTAG_I2C_2k_Plus) {
			reader.SectorSelect((byte) 0);
			return reader.read(Register.Session_PLUS.getValue());
		} else {
			reader.SectorSelect((byte) 3);
			return reader.read(Register.Session.getValue());
		}		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getConfigRegisters()
	 */
	@Override
	public byte[] getConfigRegisters() throws IOException, FormatException {

		if (getProduct() == Prod.NTAG_I2C_1k || getProduct() == Prod.NTAG_I2C_1k_Plus  || getProduct() == Prod.NTAG_I2C_2k_Plus)
			reader.SectorSelect((byte) 0);
		else if (getProduct() == Prod.NTAG_I2C_2k)
			reader.SectorSelect((byte) 1);
		else
			throw new IOException();

		return reader.read(Register.Configuration.getValue());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getConfigRegister(com.nxp
	 * .nfc_demo.reader.Ntag_I2C_Commands.CR_Offset)
	 */
	@Override
	public byte getConfigRegister(CR_Offset off) throws IOException,
			FormatException {
		byte[] register = getConfigRegisters();
		return register[off.getValue()];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getSessionRegister(com.nxp
	 * .nfc_demo.reader.Ntag_I2C_Commands.SR_Offset)
	 */
	@Override
	public byte getSessionRegister(SR_Offset off) throws IOException,
			FormatException {
		byte[] register = getSessionRegisters();
		return register[off.getValue()];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeConfigRegisters(byte,
	 * byte, byte, byte, byte, byte)
	 */
	@Override
	public void writeConfigRegisters(byte NC_R, byte LD_R, byte SM_R,
			byte WD_LS_R, byte WD_MS_R, byte I2C_CLOCK_STR) throws IOException,
			FormatException {
		byte[] Data = new byte[4];

		if (getProduct() == Prod.NTAG_I2C_1k || getProduct() == Prod.NTAG_I2C_1k_Plus || getProduct() == Prod.NTAG_I2C_2k_Plus)
			reader.SectorSelect((byte) 0);
		else if (getProduct() == Prod.NTAG_I2C_2k)
			reader.SectorSelect((byte) 1);
		else
			throw new IOException();

		// Write the Config Regs
		Data[0] = NC_R;
		Data[1] = LD_R;
		Data[2] = SM_R;
		Data[3] = WD_LS_R;
		reader.write(Data, Register.Configuration.getValue());

		Data[0] = WD_MS_R;
		Data[1] = I2C_CLOCK_STR;
		Data[2] = (byte) 0x00;
		Data[3] = (byte) 0x00;
		reader.write(Data, (byte) (Register.Configuration.getValue() + 1));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#waitforI2Cwrite()
	 */
	@Override
	public void waitforI2Cwrite(int timeoutMS) throws IOException,
			FormatException, TimeoutException {
//		reader.SectorSelect((byte) 3);
//		TimeOut = false;
//		// interrupts the wait after timoutMS milliseconds
//		Timer mTimer = new Timer();
//		mTimer.schedule(new TimerTask() {
//			@Override
//			public void run() {
//				synchronized (lock) {
//					TimeOut = true;
//				}
//			}
//		}, timeoutMS);
//		// if SRAM_RF_RDY is set the Reader can Read
//		while ((getSessionRegister(SR_Offset.NS_REG) & NS_Reg_Func.SRAM_RF_READY
//				.getValue()) == 0) {
//			synchronized (lock) {
//				if (TimeOut)
//					throw new TimeoutException("waitforI2Cwrite had a Timout");
//			}
//		}
//		mTimer.cancel();
//		synchronized (lock) {
//			TimeOut = true;
//		}
		try {
			Thread.sleep(timeoutMS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#waitforI2Cread()
	 */
	@Override
	public void waitforI2Cread(int timeoutMS) throws IOException, FormatException, TimeoutException {
//		reader.SectorSelect((byte) 3);
//		TimeOut = false;
//		// interrupts the wait after timoutMS milliseconds
//		Timer mTimer = new Timer();
//		mTimer.schedule(new TimerTask() {
//			@Override
//			public void run() {
//				synchronized (lock) {
//					TimeOut = true;
//				}
//			}
//		}, timeoutMS);
//		// if SRAM_I2C_READY is set the Reader can write
//		while (((getSessionRegister(SR_Offset.NS_REG) & NS_Reg_Func.SRAM_I2C_READY
//				.getValue()) == NS_Reg_Func.SRAM_I2C_READY.getValue())) {
//			if (TimeOut)
//				throw new TimeoutException("waitforI2Cread had a Timout");
//		}
//		mTimer.cancel();
//		synchronized (lock) {
//			TimeOut = true;
//		}
		try {
			Thread.sleep(timeoutMS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeEEPROM(byte[])
	 */
	@Override
	public void writeEEPROM(byte[] data, WriteEEPROMListener listener) throws IOException, FormatException {
		if (data.length > getProduct().getMemsize()) {
			throw new IOException("Data is to long");
		}
		reader.SectorSelect((byte) 0);
		byte[] temp;
		int index = 0;
		byte blockNr = Register.User_memory_Begin.getValue();

		// write till all Data is written or the Block 0xFF was written(BlockNr
		// should be 0 then, because of the type byte)
		for (index = 0; index < data.length && blockNr != 0; index += 4) {
			// NTAG I2C Plus sits the Config registers in Sector 0
			if(getProduct() == Prod.NTAG_I2C_2k_Plus && blockNr == (byte) 0xE2) {
				break;
			}

			temp = Arrays.copyOfRange(data, index, index + 4);
			reader.write(temp, blockNr);
			blockNr++;
			
			// Inform the listener about the writing
			if(listener != null) {
				listener.onWriteEEPROM(index + 4);
			}
		}

		// If Data is left write to the 1. Sector
		if (index < data.length) {
			reader.SectorSelect((byte) 1);
			blockNr = 0;
			for (; index < data.length; index += 4) {
				temp = Arrays.copyOfRange(data, index, index + 4);
				reader.write(temp, blockNr);
				blockNr++;
				
				// Inform the listener about the writing
				if(listener != null) {
					listener.onWriteEEPROM(index + 4);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeEEPROM(int,
	 * byte[])
	 */
	@Override
	public void writeEEPROM(int startAddr, byte[] data) throws IOException,
			FormatException {

		if ((startAddr & 0x100) != 0x000 && (startAddr & 0x200) != 0x100) {
			throw new FormatException("Sector not supported");
		}
		reader.SectorSelect((byte) ((startAddr & 0x200) >> 16));
		byte[] temp;
		int index = 0;
		byte blockNr = (byte) (startAddr & 0xFF);

		// write till all Data is written or the Block 0xFF was written(BlockNr
		// should be
		// 0 then, because of the type byte)
		for (index = 0; index < data.length && blockNr != 0; index += 4) {
			temp = Arrays.copyOfRange(data, index, index + 4);
			reader.write(temp, blockNr);
			blockNr++;
		}

		// If Data is left write and the first Sector was not already written
		// switch to the first
		if (index < data.length && (startAddr & 0x100) != 0x100) {
			reader.SectorSelect((byte) 1);
			blockNr = 0;
			for (; index < data.length; index += 4) {
				temp = Arrays.copyOfRange(data, index, index + 4);
				reader.write(temp, blockNr);
				blockNr++;
			}
		} else if ((startAddr & 0x100) == 0x100) {
			throw new IOException("Data is to long");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#readEEPROM(int, int)
	 */
	@Override
	public byte[] readEEPROM(int absStart, int absEnd) throws IOException,
			FormatException {
		int maxfetchsize = reader.getMaxTransceiveLength();
		int maxFastRead = (maxfetchsize - 2) / 4;
		int fetchStart = absStart;
		int fetchEnd = 0;
		byte[] data = null;
		byte[] temp = null;

		reader.SectorSelect((byte) 0);

		while (fetchStart <= absEnd) {
			fetchEnd = fetchStart + maxFastRead - 1;
			// check for last read, fetch only rest
			if (fetchEnd > absEnd) {
				fetchEnd = absEnd;
			}

			// check for sector change in between and reduce fast_read to stay within sector
			if (getProduct() != Prod.NTAG_I2C_2k_Plus) {
				if ((fetchStart & 0xFF00) != (fetchEnd & 0xFF00)) {
					fetchEnd = (fetchStart & 0xFF00) + 0xFF;
				}
			} else {
				if ((fetchStart & 0xFF00) == 0 && (fetchEnd > 0xE2)) {
					fetchEnd = (fetchStart & 0xFF00) + 0xE1;
				}
			}
			temp = reader.fast_read((byte) (fetchStart & 0x00FF), (byte) (fetchEnd & 0x00FF));
			data = concat(data, temp);
			
			// calculate next fetch_start
			fetchStart = fetchEnd + 1;

			// check for sector change in between and reduce fast_read to stay within sector
			if (getProduct() != Prod.NTAG_I2C_2k_Plus) {
				if ((fetchStart & 0xFF00) != (fetchEnd & 0xFF00)) {
					reader.SectorSelect((byte) 1);
				}
			} else {
				if ((fetchStart & 0xFF00) == 0 && (fetchEnd >= 0xE1)) {
					reader.SectorSelect((byte) 1);
					fetchStart = 0x100;

					// Update the absEnd with pages not read on Sector 0
					absEnd = absEnd + (0xFF - 0xE2);
				}
			}
		}
		// Let's go back to Sector 0
		reader.SectorSelect((byte) 0);
		return data;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeSRAMBlock(byte[])
	 */
	@Override
	public void writeSRAMBlock(byte[] data, WriteSRAMListener listener) throws IOException, FormatException {
		byte[] txBuffer = new byte[4];
		int index = 0;
		reader.SectorSelect(sramSector);

		/**
		 * Samsung controllers do not like NfcA Transceive method,
		 * so it is better using MUL writePage command when possible
		 * For NTAG_I2C_2k it is not possible to use MUL commands because when
		 * establishing the connection the Sector is moved back to 0
		 */
		
		if((getProduct() == Prod.NTAG_I2C_1k_Plus || getProduct() == Prod.NTAG_I2C_2k_Plus)
				&& MainActivity.getAuthStatus() != AuthStatus.Authenticated.getValue()) {
			reader.fast_write(data, (byte) Register.SRAM_Begin.getValue(),
					(byte) (Register.SRAM_Begin.getValue() + 0x0F));
		} else {
			if (getProduct() == Prod.NTAG_I2C_1k) {
				reader.close();
				MifareUltralight ul = MifareUltralight.get(tag);
				if (ul != null) {
					ul.connect();
					int SRAM_Begin = (int) Register.SRAM_Begin.getValue() & 0xFF;
					for (int i = 0; i < 16; i++) {
						for (int dI = 0; dI < 4; dI++) {
							if (index < data.length) {
								txBuffer[dI] = data[index++];
							} else {
								txBuffer[dI] = (byte) 0x00;
							}
						}
						ul.writePage(SRAM_Begin + i, txBuffer);
					}
					ul.close();
				}
				reader.connect();
			} else {
				if(MainActivity.getAuthStatus() == AuthStatus.Authenticated.getValue()) {
					try {
						authenticatePlus(MainActivity.getPassword());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				for (int i = 0; i < 16; i++) {
					for (int dI = 0; dI < 4; dI++) {
						if (index < data.length) {
							txBuffer[dI] = data[index++];
						} else {
							txBuffer[dI] = (byte) 0x00;
						}
					}
					reader.write(txBuffer, (byte) (Register.SRAM_Begin.getValue() + i));
				}
			}
		}
			
		// Inform the listener about the writing
		if(listener != null) {
			listener.onWriteSRAM();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#authenticatePlus()
	 * 
	 */
	@Override
	public byte[] authenticatePlus(byte[] pwd) throws IOException, NotPlusTagException {
		if(getProduct() != Prod.NTAG_I2C_1k_Plus && getProduct() != Prod.NTAG_I2C_2k_Plus) {
			throw new NotPlusTagException(
					"Auth Operations are not supported by non NTAG I2C PLUS products");
		}
		return reader.pwdAuth(pwd);
	}
		
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#protectPlus()
	 * 
	 */
	@Override
	public void protectPlus(byte[] pwd, byte startAddr) throws IOException,
			FormatException, NotPlusTagException {
		byte[] data = new byte[4];
		
		if(getProduct() != Prod.NTAG_I2C_1k_Plus && getProduct() != Prod.NTAG_I2C_2k_Plus) {
			throw new NotPlusTagException(
					"Auth Operations are not supported by non NTAG I2C PLUS products");
		}
		reader.SectorSelect((byte) 0);
		
		// Set the password indicated by the user
		reader.write(pwd, Register.PWD.getValue());
		
		byte access = (byte) 0x00;
		byte authLimit = 0x00; 							// Don't limit the number of auth attempts
		
		access ^= 1 << Access_Offset.NFC_PROT.getValue();			// NFC_Prot
		access ^= 0 << Access_Offset.NFC_DIS_SEC1.getValue();		// NFC_DIS_SEC1
		access |= authLimit << Access_Offset.AUTH_LIM.getValue();	// AUTHLIM
		
		// Write the ACCESS configuration
		data[0] = access;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = 0x00;
		reader.write(data, Register.ACCESS.getValue());
		
		byte ptI2C = 0x00;
		byte i2CProt = 0x00;

		ptI2C ^= 0 << PT_I2C_Offset.K2_PROT.getValue();			// 2K Prot
		ptI2C ^= 1 << PT_I2C_Offset.SRAM_PROT.getValue();			// SRAM Prot
		ptI2C |= i2CProt << PT_I2C_Offset.I2C_PROT.getValue();	// I2C Prot

		// Write the PT_I2C configuration
		data[0] = ptI2C;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = 0x00;
		reader.write(data, Register.PT_I2C.getValue());
				
		// Write the AUTH0 lock starting page
		data[0] = 0x00;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = startAddr;
		reader.write(data, Register.AUTH0.getValue());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#unprotectPlus()
	 * 
	 */
	@Override
	public void unprotectPlus() throws IOException, FormatException, NotPlusTagException {
		byte[] data = new byte[4];
		
		if(getProduct() != Prod.NTAG_I2C_1k_Plus && getProduct() != Prod.NTAG_I2C_2k_Plus) {
			throw new NotPlusTagException(
					"Auth Operations are not supported by non NTAG I2C PLUS products");
		}
		reader.SectorSelect((byte) 0);
		
		data[0] = 0x00;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = (byte) 0xFF;
		reader.write(data, Register.AUTH0.getValue());
		
		// Set the password to FFs
		data[0] = (byte) 0xFF;
		data[1] = (byte) 0xFF;
		data[2] = (byte) 0xFF;
		data[3] = (byte) 0xFF;
		reader.write(data, Register.PWD.getValue());
		
		// Write the ACCESS configuration
		data[0] = 0x00;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = 0x00;
		reader.write(data, Register.ACCESS.getValue());
		
		// Write the PT I2C configuration
		data[0] = 0x00;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = 0x00;
		reader.write(data, Register.PT_I2C.getValue());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getProtectionPlus()
	 * 
	 */
	@Override
	public int getProtectionPlus() {
		try {
			reader.SectorSelect((byte) 0);
			byte[] auth0 = getAuth0Register();
			if(auth0 != null && auth0.length < 4) {
				try {
					readSRAMBlock();
					return AuthStatus.Protected_RW.getValue();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (FormatException e) {
					e.printStackTrace();
				}
				return AuthStatus.Protected_RW_SRAM.getValue();
			} else {
				if((auth0[3] & 0xFF) <= 0xEB) { 
					byte[] access = getAccessRegister();
					byte[] pti2c = getPTI2CRegister();
					
					if (((0x0000080 & access[0]) >> Access_Offset.NFC_PROT.getValue() == 1) &&
							((0x0000004 & pti2c[0]) >> PT_I2C_Offset.SRAM_PROT.getValue() == 1)) {
						return AuthStatus.Protected_RW_SRAM.getValue();
					} else if (((0x0000080 & access[0]) >> Access_Offset.NFC_PROT.getValue() == 1)
					       && ((0x0000004 & pti2c[0]) >> PT_I2C_Offset.SRAM_PROT.getValue() == 0)) {
						return AuthStatus.Protected_RW.getValue();
					} else if (((0x0000080 & access[0]) >> Access_Offset.NFC_PROT.getValue() == 0)
						   && ((0x0000004 & pti2c[0]) >> PT_I2C_Offset.SRAM_PROT.getValue() == 1)) {
						return AuthStatus.Protected_W_SRAM.getValue();
					} else if (((0x0000080 & access[0]) >> Access_Offset.NFC_PROT.getValue() == 0)
						   && ((0x0000004 & pti2c[0]) >> PT_I2C_Offset.SRAM_PROT.getValue() == 0)) {
						return AuthStatus.Protected_W.getValue();
					}
				}
			}
			return AuthStatus.Unprotected.getValue();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		} catch (CommandNotSupportedException e) {
			e.printStackTrace();
		}
		
		// Check if the SRAM is lock
		try {
			readSRAMBlock();
			return AuthStatus.Protected_RW.getValue();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}
		return AuthStatus.Protected_RW_SRAM.getValue();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getAuth0Register()
	 * 
	 */
	@Override
	public byte[] getAuth0Register() throws IOException, FormatException,
			CommandNotSupportedException {
		reader.SectorSelect((byte) 0);
		return reader.read(Register.AUTH0.getValue());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getAccessRegister()
	 * 
	 */
	@Override
	public byte[] getAccessRegister() throws IOException, FormatException,
			CommandNotSupportedException {
		reader.SectorSelect((byte) 0);
		return reader.read(Register.ACCESS.getValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getPTI2CRegister()
	 * 
	 */
	@Override
	public byte[] getPTI2CRegister() throws IOException, FormatException,
			CommandNotSupportedException {
		reader.SectorSelect((byte) 0);
		return reader.read(Register.PT_I2C.getValue());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeAuthRegisters()
	 * 
	 */
	@Override
	public void writeAuthRegisters(byte auth0, byte access, byte ptI2C) throws IOException, FormatException,
			CommandNotSupportedException {
		byte[] data = new byte[4];
				
		reader.SectorSelect((byte) 0);

		// Write the ACCESS configuration
		data[0] = access;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = 0x00;
		reader.write(data, Register.ACCESS.getValue());
		
		// Write the PT I2C configuration
		data[0] = ptI2C;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = 0x00;
		reader.write(data, Register.PT_I2C.getValue());
		
		// Set the password to FFs
		data[0] = (byte) 0xFF;
		data[1] = (byte) 0xFF;
		data[2] = (byte) 0xFF;
		data[3] = (byte) 0xFF;
		reader.write(data, Register.PWD.getValue());
		
		// Set the pack to 00s
		data[0] = (byte) 0x00;
		data[1] = (byte) 0x00;
		data[2] = (byte) 0x00;
		data[3] = (byte) 0x00;
		reader.write(data, Register.PACK.getValue());
		
		data[0] = 0x00;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = auth0;
		reader.write(data, Register.AUTH0.getValue());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeSRAM(byte[],
	 * com.nxp.nfc_demo.reader.Ntag_I2C_Commands.R_W_Methods)
	 * 
	 * @throws InterruptedException
	 */
	@Override
	public void writeSRAM(byte[] data, R_W_Methods method, WriteSRAMListener listener) throws IOException,
			FormatException, TimeoutException {

		int blocks = (int) Math.ceil(data.length / 64.0);
		for (int i = 0; i < blocks; i++) {
			byte[] dataBlock = new byte[64];
			if (data.length - (i + 1) * 64 < 0) {					
				Arrays.fill(dataBlock, (byte) 0);
				System.arraycopy(data, i * 64, dataBlock, 0, data.length % 64);
			} else {
				System.arraycopy(data, i * 64, dataBlock, 0, 64);
			}
			writeSRAMBlock(dataBlock, listener);
			if (method == R_W_Methods.Polling_Mode) {
				waitforI2Cread(100);
			} else {
				try {
					// else wait
					Thread.sleep(6);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#readSRAMBlock()
	 */
	@Override
	public byte[] readSRAMBlock() throws IOException, FormatException {	
		answer = new byte[0];
		if(MainActivity.getAuthStatus() == AuthStatus.Authenticated.getValue()) {
			try {
				authenticatePlus(MainActivity.getPassword());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		reader.SectorSelect(sramSector);
		answer = reader.fast_read((byte) 0xF0, (byte) 0xFF);
		return answer;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#readSRAM(int,
	 * com.nxp.nfc_demo.reader.Ntag_I2C_Commands.R_W_Methods)
	 */
	@Override
	public byte[] readSRAM(int blocks, R_W_Methods method) throws IOException,
			FormatException, TimeoutException {
		byte[] response = new byte[0];
		byte[] temp;

		for (int i = 0; i < blocks; i++) {
			if (method == R_W_Methods.Polling_Mode) {
				waitforI2Cwrite(100);
			} else {
				try {
					// else wait
					Thread.sleep(6);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			temp = readSRAMBlock();

			// concat read block to the full response
			response = concat(response, temp);
		}
		answer = response;
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeEmptyNdef()
	 */
	@Override
	public void writeEmptyNdef() throws IOException, FormatException {
		int index = 0;
		byte[] data = new byte[4];
		index = 0;

		reader.SectorSelect((byte) 0);

		data[index++] = (byte) 0x03;
		data[index++] = (byte) 0x00;
		data[index++] = (byte) 0xFE;
		data[index++] = (byte) 0x00;

		reader.write(data, (byte) 0x04);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeEmptyNdef()
	 */
	@Override
	public void writeDefaultNdef() throws IOException, FormatException {
		byte[] data = new byte[4];

		reader.SectorSelect((byte) 0);

		data[0] = (byte) 0x03;
		data[1] = (byte) 0x60;
		data[2] = (byte) 0x91;
		data[3] = (byte) 0x02;
		
		reader.write(data, (byte) 0x04);
		
		data[0] = (byte) 0x35;
		data[1] = (byte) 0x53;
		data[2] = (byte) 0x70;
		data[3] = (byte) 0x91;
		
		reader.write(data, (byte) 0x05);
		
		data[0] = (byte) 0x01;
		data[1] = (byte) 0x14;
		data[2] = (byte) 0x54;
		data[3] = (byte) 0x02;
		
		reader.write(data, (byte) 0x06);
		
		data[0] = (byte) 0x65;
		data[1] = (byte) 0x6E;
		data[2] = (byte) 0x4E;
		data[3] = (byte) 0x54;
		
		reader.write(data, (byte) 0x07);
		
		data[0] = (byte) 0x41;
		data[1] = (byte) 0x47;
		data[2] = (byte) 0x20;
		data[3] = (byte) 0x49;
		
		reader.write(data, (byte) 0x08);

		data[0] = (byte) 0x32;
		data[1] = (byte) 0x43;
		data[2] = (byte) 0x20;
		data[3] = (byte) 0x45;
		
		reader.write(data, (byte) 0x09);

		data[0] = (byte) 0x58;
		data[1] = (byte) 0x50;
		data[2] = (byte) 0x4C;
		data[3] = (byte) 0x4F;
		
		reader.write(data, (byte) 0x0A);
		
		data[0] = (byte) 0x52;
		data[1] = (byte) 0x45;
		data[2] = (byte) 0x52;
		data[3] = (byte) 0x51;
		
		reader.write(data, (byte) 0x0B);
		
		data[0] = (byte) 0x01;
		data[1] = (byte) 0x19;
		data[2] = (byte) 0x55;
		data[3] = (byte) 0x01;
		
		reader.write(data, (byte) 0x0C);
		
		data[0] = (byte) 0x6E;
		data[1] = (byte) 0x78;
		data[2] = (byte) 0x70;
		data[3] = (byte) 0x2E;
		
		reader.write(data, (byte) 0x0D);
		
		data[0] = (byte) 0x63;
		data[1] = (byte) 0x6F;
		data[2] = (byte) 0x6D;
		data[3] = (byte) 0x2F;
		
		reader.write(data, (byte) 0x0E);

		data[0] = (byte) 0x64;
		data[1] = (byte) 0x65;
		data[2] = (byte) 0x6D;
		data[3] = (byte) 0x6F;
		
		reader.write(data, (byte) 0x0F);
		
		data[0] = (byte) 0x62;
		data[1] = (byte) 0x6F;
		data[2] = (byte) 0x61;
		data[3] = (byte) 0x72;
		
		reader.write(data, (byte) 0x10);
		
		data[0] = (byte) 0x64;
		data[1] = (byte) 0x2F;
		data[2] = (byte) 0x4F;
		data[3] = (byte) 0x4D;
		
		reader.write(data, (byte) 0x11);
		
		data[0] = (byte) 0x35;
		data[1] = (byte) 0x35;
		data[2] = (byte) 0x36;
		data[3] = (byte) 0x39;
		
		reader.write(data, (byte) 0x12);
		
		data[0] = (byte) 0x54;
		data[1] = (byte) 0x0F;
		data[2] = (byte) 0x14;
		data[3] = (byte) 0x61;
		
		reader.write(data, (byte) 0x13);
		
		data[0] = (byte) 0x6E;
		data[1] = (byte) 0x64;
		data[2] = (byte) 0x72;
		data[3] = (byte) 0x6F;
		
		reader.write(data, (byte) 0x14);

		data[0] = (byte) 0x69;
		data[1] = (byte) 0x64;
		data[2] = (byte) 0x2E;
		data[3] = (byte) 0x63;
		
		reader.write(data, (byte) 0x15);
		
		data[0] = (byte) 0x6F;
		data[1] = (byte) 0x6D;
		data[2] = (byte) 0x3A;
		data[3]= (byte) 0x70;
		
		reader.write(data, (byte) 0x16);

		data[0] = (byte) 0x6B;
		data[1] = (byte) 0x67;
		data[2] = (byte) 0x63;
		data[3] = (byte) 0x6F;
		
		reader.write(data, (byte) 0x17);
		
		data[0] = (byte) 0x6D;
		data[1] = (byte) 0x2E;
		data[2] = (byte) 0x6E;
		data[3] = (byte) 0x78;
		
		reader.write(data, (byte) 0x18);

		data[0] = (byte) 0x70;
		data[1] = (byte) 0x2E;
		data[2] = (byte) 0x6E;
		data[3] = (byte) 0x74;
		
		reader.write(data, (byte) 0x19);
		
		data[0] = (byte) 0x61;
		data[1] = (byte) 0x67;
		data[2] = (byte) 0x69;
		data[3] = (byte) 0x32;
		
		reader.write(data, (byte) 0x1A);
		
		data[0] = (byte) 0x63;
		data[1] = (byte) 0x64;
		data[2] = (byte) 0x65;
		data[3] = (byte) 0x6D;
		
		reader.write(data, (byte) 0x1B);
		
		data[0] = (byte) 0x6F;
		data[1] = (byte) 0x5F;
		data[2] = (byte) 0xFE;
		data[3] = (byte) 0x00;
		
		reader.write(data, (byte) 0x1C);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeDeliveryNdef()
	 */
	@Override
	public int writeDeliveryNdef() throws IOException, FormatException,
			CC_differException, StaticLockBitsException,
			DynamicLockBitsException {
		int index = 0;
		byte[] data = new byte[4];
		byte[] eq;
		index = 0;
		
		reader.SectorSelect((byte) 0);

		// checking Capability Container
		if (getProduct() == Prod.NTAG_I2C_1k || getProduct() == Prod.NTAG_I2C_1k_Plus) {
			// CC for NTAG 1k
			data[index++] = (byte) 0xE1;
			data[index++] = (byte) 0x10;
			data[index++] = (byte) 0x6D;
			data[index++] = (byte) 0x00;

		} else if (getProduct() == Prod.NTAG_I2C_2k || getProduct() == Prod.NTAG_I2C_2k_Plus) {
			// CC for NTAG 2k
			data[index++] = (byte) 0xE1;
			data[index++] = (byte) 0x10;
			data[index++] = (byte) 0xEA;
			data[index++] = (byte) 0x00;
		} 

		// write CC
		try {
			reader.write(data, (byte) 0x03);
		} catch (IOException e) {
			e.printStackTrace();
			throw new CC_differException(
					"Capability Container cannot be written (use I2C instead to reset)");
		}

		// check if CC are set correctly
		eq = reader.read((byte) 0x03);
		if (!(eq[0] == data[0] && eq[1] == data[1] && eq[2] == data[2] && eq[3] == data[3])) {
			throw new CC_differException(
					"Capability Container wrong (use I2C instead to reset)");
		}

		// checking static Lock bits
		eq = reader.read((byte) 0x02);
		if (!(eq[2] == 0 && eq[3] == 0)) {
			throw new StaticLockBitsException(
					"Static Lockbits set, cannot reset (use I2C instead to reset)");
		}

		// checking dynamic Lock bits
		if (getProduct() == Prod.NTAG_I2C_1k || getProduct() == Prod.NTAG_I2C_1k_Plus) {
			eq = reader.read((byte) 0xE2);
		} else if (getProduct() == Prod.NTAG_I2C_2k) {
			reader.SectorSelect((byte) 1);
			eq = reader.read((byte) 0xE0);
		} else if (getProduct() == Prod.NTAG_I2C_2k_Plus) {
			eq = reader.read((byte) 0xE2);
		}

		if (!(eq[0] == 0 && eq[1] == 0 && eq[2] == 0)) {
			throw new DynamicLockBitsException(
					"Dynamic Lockbits set, cannot reset (use I2C instead to reset)");
		}

		// write all zeros
		reader.SectorSelect((byte) 0);

		byte[] d = new byte[getProduct().getMemsize()];
		writeEEPROM(d, null);

		// Write empty NDEF TLV in User Memory
		writeDefaultNdef();
		
		// Bytes Written: Product Memory + Default Ndef (104 bytes)
		int bytesWritten = getProduct().getMemsize() + DEFAULT_NDEF_MESSAGE_SIZE;
		return bytesWritten;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeNDEF()
	 */
	@Override
	public void writeNDEF(NdefMessage message, WriteEEPROMListener listener) throws IOException,
			FormatException {
		byte[] Ndef_message_byte = createRawNdefTlv(message);
		writeEEPROM(Ndef_message_byte, listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#readNDEF()
	 */
	@Override
	public NdefMessage readNDEF() throws IOException, FormatException {
		int ndefsize;
		int tlvsize;
		int tlvPlusNdef;

		// get TLV
		byte[] tlv = readEEPROM(Register.User_memory_Begin.getValue(),
				Register.User_memory_Begin.getValue() + 3);

		// checking TLV - maybe there are other TLVs on the tag
		if (tlv[0] != 0x03) {
			throw new FormatException("Format on Tag not supported");
		}
		if (tlv[1] != (byte) 0xFF) {
			ndefsize = (tlv[1] & 0xFF);
			tlvsize = 2;
			tlvPlusNdef = tlvsize + ndefsize;
		} else {
			ndefsize = (tlv[3] & 0xFF);
			ndefsize |= ((tlv[2] << 8) & 0xFF00);
			tlvsize = 4;
			tlvPlusNdef = tlvsize + ndefsize;
		}
		// Read NDEF Message
		byte[] data = readEEPROM(Register.User_memory_Begin.getValue(),
				Register.User_memory_Begin.getValue() + (tlvPlusNdef / 4));

		// delete TLV
		data = Arrays.copyOfRange(data, tlvsize, data.length);
		// delete end of String which is not part of the NDEF Message
		data = Arrays.copyOf(data, ndefsize);

		// get the String out of the Message
		NdefMessage message = new NdefMessage(data);
		return message;
	}

	// -------------------------------------------------------------------
	// Helping function
	// -------------------------------------------------------------------

	/**
	 * create a Raw NDEF TLV from a NDEF Message.
	 * 
	 * @param NdefMessage
	 *            NDEF Message to put in the NDEF TLV
	 * @return Byte Array of NDEF Message
	 * @throws UnsupportedEncodingException
	 */
	private byte[] createRawNdefTlv(NdefMessage NDEFmessage)
			throws UnsupportedEncodingException {
		// creating NDEF
		byte[] ndefMessageByte = NDEFmessage.toByteArray();
		int ndefMessageSize = ndefMessageByte.length;
		byte[] message;

		if (ndefMessageSize < 0xFF) {
			message = new byte[ndefMessageSize + 3];
			byte tlvSize = 0;
			tlvSize = (byte) ndefMessageSize;
			message[0] = (byte) 0x03;
			message[1] = (byte) tlvSize;
			message[message.length - 1] = (byte) 0xFE;
			System.arraycopy(ndefMessageByte, 0, message, 2,
					ndefMessageByte.length);
		} else {
			message = new byte[ndefMessageSize + 5];
			int TLV_size = ndefMessageSize;
			TLV_size |= 0xFF0000;
			message[0] = (byte) 0x03;
			message[1] = (byte) ((TLV_size >> 16) & 0xFF);
			message[2] = (byte) ((TLV_size >> 8) & 0xFF);
			message[3] = (byte) (TLV_size & 0xFF);
			message[message.length - 1] = (byte) 0xFE;
			System.arraycopy(ndefMessageByte, 0, message, 4,
					ndefMessageByte.length);
		}
		return message;
	}

	@Override
	public Boolean checkPTwritePossible() throws IOException, FormatException {
		byte nc_reg = getSessionRegister(SR_Offset.NC_REG);
		if ((nc_reg & NC_Reg_Func.PTHRU_ON_OFF.getValue()) == 0
			|| (nc_reg & NC_Reg_Func.PTHRU_DIR.getValue()) == 0) {
			return false;
		}

		byte ns_reg = getSessionRegister(SR_Offset.NS_REG);
		if ((ns_reg & NS_Reg_Func.RF_LOCKED.getValue()) == 0) {
			return false;
		}

		return true;
	}
}
