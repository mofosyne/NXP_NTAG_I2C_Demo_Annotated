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

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;

import com.nxp.nfc_demo.activities.AuthActivity.AuthStatus;
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
public class MinimalNtag_I2C_Commands extends I2C_Enabled_Commands {

	private final int firstSectorMemsize = (0xFF - 0x4) * 4;
	private MifareUltralight mfu;
	private Prod tagType;
	private byte[] answer;
	private static int waitTime = 20;

	/**
	 * Special Registers of the NTAG I2C.
	 *
	 */
	public enum Register {
		Session((byte) 0xF8), Configuration((byte) 0xE8), SRAM_Begin(
				(byte) 0xF0), User_memory_Begin((byte) 0x04), UID((byte) 0x00);

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
	public MinimalNtag_I2C_Commands(Tag tag, Prod prod) throws IOException {
		tagType = prod;
		blockSize = 4;
		SRAMSize = 64;
		this.mfu = MifareUltralight.get(tag);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#close()
	 */
	@Override
	public void close() throws IOException {
		mfu.close();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#connect()
	 */
	@Override
	public void connect() throws IOException {
		mfu.connect();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#isConnected()
	 */
	@Override
	public boolean isConnected() {
		return mfu.isConnected();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getLastAnswer()
	 */
	@Override
	public byte[] getLastAnswer() {
		return answer;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getProduct()
	 */
	@Override
	public Prod getProduct() throws IOException {
		// returns generic NTAG_I2C_1k, because getVersion is not possible
		return tagType;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getSessionRegisters()
	 */
	@Override
	public byte[] getSessionRegisters() throws IOException, FormatException,
			CommandNotSupportedException {
		if (tagType == Prod.NTAG_I2C_1k || tagType == Prod.NTAG_I2C_2k) {
			throw new CommandNotSupportedException(
					"getSessionRegisters not supported");
		}
		answer = mfu.readPages(0xEC);
		return answer;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#getConfigRegisters()
	 */
	@Override
	public byte[] getConfigRegisters() throws IOException, FormatException,
			CommandNotSupportedException {
		if (tagType == Prod.NTAG_I2C_2k) {
			throw new CommandNotSupportedException(
					"getConfigRegisters is not Supported for this Phone with NTAG I2C 2k");
		}
		answer = mfu.readPages(0xE8);
		return answer;
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
			FormatException, CommandNotSupportedException {
		if (tagType == Prod.NTAG_I2C_2k) {
			throw new CommandNotSupportedException(
					"getConfigRegister is not Supported for this Phone with NTAG I2C 2k");
		}
		return getConfigRegisters()[off.getValue()];
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
			FormatException, CommandNotSupportedException {
		if (tagType == Prod.NTAG_I2C_1k || tagType == Prod.NTAG_I2C_2k) {
			throw new CommandNotSupportedException(
					"getSessionRegister not supported");
		}
		return getSessionRegisters()[off.getValue()];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeConfigRegisters(byte,
	 * byte, byte, byte, byte, byte)
	 */
	@Override
	public void writeConfigRegisters(byte ncR, byte ldR, byte smR,
			byte wdLsR, byte wdMsR, byte i2CClockStr) throws IOException,
			FormatException, CommandNotSupportedException {
		if (tagType == Prod.NTAG_I2C_2k) {
			throw new CommandNotSupportedException(
					"writeConfigRegisters is not Supported for this Phone with NTAG I2C 2k");
		}
		byte[] data = new byte[4];

		// Write the Config Regs
		data[0] = ncR;
		data[1] = ldR;
		data[2] = smR;
		data[3] = wdLsR;
		mfu.writePage(0xE8, data);

		data[0] = wdMsR;
		data[1] = i2CClockStr;
		data[2] = (byte) 0x00;
		data[3] = (byte) 0x00;
		mfu.writePage(0xE9, data);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#waitforI2Cwrite()
	 */
	@Override
	public void waitforI2Cwrite(int timeoutMS) throws IOException,
			FormatException {
		// just wait a little
		try {
			Thread.sleep(waitTime);
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
	public void waitforI2Cread(int timeoutMS) throws IOException,
			FormatException {
		// just wait a little
		try {
			Thread.sleep(waitTime);
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
	public void writeEEPROM(byte[] data, WriteEEPROMListener listener) throws IOException, FormatException,
			CommandNotSupportedException {
		if ((tagType == Prod.NTAG_I2C_2k || tagType == Prod.NTAG_I2C_2k_Plus)
				&& data.length > firstSectorMemsize) {
			throw new CommandNotSupportedException(
					"writeEEPROM is not Supported for this Phone, with Data bigger then First Sector("
							+ firstSectorMemsize + " Bytes)");
		}
		
		if (data.length > getProduct().getMemsize()) {
			throw new IOException("Data is too long");
		}

		byte[] temp;
		int blockNr = Register.User_memory_Begin.getValue();

		// write till all Data is written
		for (int i = 0; i < data.length; i += 4) {
			temp = Arrays.copyOfRange(data, i, i + 4);
			mfu.writePage(blockNr, temp);
			blockNr++;
			
			// Inform the listener about the writing
			if(listener != null) {
				listener.onWriteEEPROM(i + 4);
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
		// Nothing will be done for now
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#readEEPROM(int, int)
	 */
	@Override
	public byte[] readEEPROM(int absStart, int absEnd) throws IOException,
			FormatException, CommandNotSupportedException {

		if ((tagType == Prod.NTAG_I2C_2k && absEnd > 0xFF)
				|| tagType == Prod.NTAG_I2C_2k_Plus && absEnd > 0xE1)
			throw new CommandNotSupportedException(
					"readEEPROM is not Supported for this Phone on Second Sector");

		byte[] temp = new byte[0];
		answer = new byte[0];

		if (absStart > 0xFF) {
			absStart = 0xFF;
		}

		if (absEnd > 0xFF) {
			absEnd = 0xFF;
		}

		int i;
		for (i = absStart; i <= (absEnd - 3); i += 4) {
			temp = mfu.readPages(i);
			answer = concat(answer, temp);
		}

		if (i < absEnd) {
			temp = mfu.readPages(absEnd - 3);
			byte[] bla = Arrays.copyOfRange(temp, (i - (absEnd - 3)) * 4, 16);
			answer = concat(answer, bla);
		}
		return answer;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeSRAMBlock(byte[])
	 */
	@Override
	public void writeSRAMBlock(byte[] data, WriteSRAMListener listener) throws IOException,
			FormatException, CommandNotSupportedException {
		
		if (tagType == Prod.NTAG_I2C_2k) {
			throw new CommandNotSupportedException(
					"writeSRAMBlock is not Supported for this Phone with NTAG I2C 2k");
		}
		byte[] txBuffer = new byte[4];
		int index = 0;

		for (int i = 0; i < 16; i++) {
			for (int dI = 0; dI < 4; dI++) {
				if (index < data.length) {
					txBuffer[dI] = data[index++];
				} else {
					txBuffer[dI] = (byte) 0x00;
				}
			}
			mfu.writePage(0xF0 + i, txBuffer);

		}
		// Inform the listener about the writing
		if(listener != null) {
			listener.onWriteSRAM();
		}
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
			FormatException, CommandNotSupportedException {
		
		if (tagType == Prod.NTAG_I2C_2k) {
			throw new CommandNotSupportedException(
					"writeSRAM is not Supported for this Phone with NTAG I2C 2k");
		}
		int blocks = (int) Math.ceil(data.length / 64.0);
		for (int i = 0; i < blocks; i++) {
			writeSRAMBlock(data, listener);
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
			if (data.length > 64) {
				data = Arrays.copyOfRange(data, 64, data.length);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#readSRAMBlock()
	 */
	@Override
	public byte[] readSRAMBlock() throws IOException, FormatException, CommandNotSupportedException {
		if (tagType == Prod.NTAG_I2C_2k) {
			throw new CommandNotSupportedException(
					"readSRAMBlock is not Supported for this Phone with NTAG I2C 2k");
		}
		answer = new byte[0];
		for (int i = 0; i < 0x0F; i += 4) {
			answer = concat(answer, mfu.readPages(0xF0 + i));
		}
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
			FormatException, CommandNotSupportedException {		
		if (tagType == Prod.NTAG_I2C_2k) {
			throw new CommandNotSupportedException(
					"readSRAM is not Supported for this Phone with NTAG I2C 2k");
		}
		byte[] response = new byte[0];
		byte[] temp;
		answer = new byte[0];
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
		// Nothing done for now
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeDefaultNdef()
	 */
	@Override
	public void writeDefaultNdef() throws IOException, FormatException {
		byte[] data = new byte[4];

		data[0] = (byte) 0x03;
		data[1] = (byte) 0x60;
		data[2] = (byte) 0x91;
		data[3] = (byte) 0x02;
		
		mfu.writePage((byte) 0x04, data);
		
		data[0] = (byte) 0x35;
		data[1] = (byte) 0x53;
		data[2] = (byte) 0x70;
		data[3] = (byte) 0x91;
		
		mfu.writePage((byte) 0x05, data);
		
		data[0] = (byte) 0x01;
		data[1] = (byte) 0x14;
		data[2] = (byte) 0x54;
		data[3] = (byte) 0x02;
		
		mfu.writePage((byte) 0x06, data);
		
		data[0] = (byte) 0x65;
		data[1] = (byte) 0x6E;
		data[2] = (byte) 0x4E;
		data[3] = (byte) 0x54;
		
		mfu.writePage((byte) 0x07, data);
		
		data[0] = (byte) 0x41;
		data[1] = (byte) 0x47;
		data[2] = (byte) 0x20;
		data[3] = (byte) 0x49;
		
		mfu.writePage((byte) 0x08, data);

		data[0] = (byte) 0x32;
		data[1] = (byte) 0x43;
		data[2] = (byte) 0x20;
		data[3] = (byte) 0x45;
		
		mfu.writePage((byte) 0x09, data);

		data[0] = (byte) 0x58;
		data[1] = (byte) 0x50;
		data[2] = (byte) 0x4C;
		data[3] = (byte) 0x4F;
		
		mfu.writePage((byte) 0x0A, data);
		
		data[0] = (byte) 0x52;
		data[1] = (byte) 0x45;
		data[2] = (byte) 0x52;
		data[3] = (byte) 0x51;
		
		mfu.writePage((byte) 0x0B, data);
		
		data[0] = (byte) 0x01;
		data[1] = (byte) 0x19;
		data[2] = (byte) 0x55;
		data[3] = (byte) 0x01;
		
		mfu.writePage((byte) 0x0C, data);
		
		data[0] = (byte) 0x6E;
		data[1] = (byte) 0x78;
		data[2] = (byte) 0x70;
		data[3] = (byte) 0x2E;
		
		mfu.writePage((byte) 0x0D, data);
		
		data[0] = (byte) 0x63;
		data[1] = (byte) 0x6F;
		data[2] = (byte) 0x6D;
		data[3] = (byte) 0x2F;
		
		mfu.writePage((byte) 0x0E, data);

		data[0] = (byte) 0x64;
		data[1] = (byte) 0x65;
		data[2] = (byte) 0x6D;
		data[3] = (byte) 0x6F;
		
		mfu.writePage((byte) 0x0F, data);
		
		data[0] = (byte) 0x62;
		data[1] = (byte) 0x6F;
		data[2] = (byte) 0x61;
		data[3] = (byte) 0x72;
		
		mfu.writePage((byte) 0x10, data);
		
		data[0] = (byte) 0x64;
		data[1] = (byte) 0x2F;
		data[2] = (byte) 0x4F;
		data[3] = (byte) 0x4D;
		
		mfu.writePage((byte) 0x11, data);
		
		data[0] = (byte) 0x35;
		data[1] = (byte) 0x35;
		data[2] = (byte) 0x36;
		data[3] = (byte) 0x39;
		
		mfu.writePage((byte) 0x12, data);
		
		data[0] = (byte) 0x54;
		data[1] = (byte) 0x0F;
		data[2] = (byte) 0x14;
		data[3] = (byte) 0x61;
		
		mfu.writePage((byte) 0x13, data);
		
		data[0] = (byte) 0x6E;
		data[1] = (byte) 0x64;
		data[2] = (byte) 0x72;
		data[3] = (byte) 0x6F;
		
		mfu.writePage((byte) 0x14, data);

		data[0] = (byte) 0x69;
		data[1] = (byte) 0x64;
		data[2] = (byte) 0x2E;
		data[3] = (byte) 0x63;
		
		mfu.writePage((byte) 0x15, data);
		
		data[0] = (byte) 0x6F;
		data[1] = (byte) 0x6D;
		data[2] = (byte) 0x3A;
		data[3] = (byte) 0x70;
		
		mfu.writePage((byte) 0x16, data);

		data[0] = (byte) 0x6B;
		data[1] = (byte) 0x67;
		data[2] = (byte) 0x63;
		data[3] = (byte) 0x6F;
		
		mfu.writePage((byte) 0x17, data);
		
		data[0] = (byte) 0x6D;
		data[1] = (byte) 0x2E;
		data[2] = (byte) 0x6E;
		data[3] = (byte) 0x78;
		
		mfu.writePage((byte) 0x18, data);

		data[0] = (byte) 0x70;
		data[1] = (byte) 0x2E;
		data[2] = (byte) 0x6E;
		data[3] = (byte) 0x74;
		
		mfu.writePage((byte) 0x19, data);
		
		data[0] = (byte) 0x61;
		data[1] = (byte) 0x67;
		data[2] = (byte) 0x69;
		data[3] = (byte) 0x32;
		
		mfu.writePage((byte) 0x1A, data);
		
		data[0] = (byte) 0x63;
		data[1] = (byte) 0x64;
		data[2] = (byte) 0x65;
		data[3] = (byte) 0x6D;
		
		mfu.writePage((byte) 0x1B, data);
		
		data[0] = (byte) 0x6F;
		data[1] = (byte) 0x5F;
		data[2] = (byte) 0xFE;
		data[3] = (byte) 0x00;
		
		mfu.writePage((byte) 0x1C, data);
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
		// Nothing done for now
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#writeNDEF()
	 */
	@Override
	public void writeNDEF(NdefMessage message, WriteEEPROMListener listener) throws IOException,
			FormatException, CommandNotSupportedException {
		byte[] ndefMessageByte = createRawNdefTlv(message);
		writeEEPROM(ndefMessageByte, listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#readNDEF()
	 */
	@Override
	public NdefMessage readNDEF() throws IOException, FormatException, CommandNotSupportedException {
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

		// Interpret Bytes
		NdefMessage message = new NdefMessage(data);
		return message;
	}

	// -------------------------------------------------------------------
	// Helping function
	// -------------------------------------------------------------------

	/**
	 * create a Raw NDEF TLV from a NDEF Message
	 *
	 * @param ndefMessage
	 *            NDEF Message to put in the NDEF TLV
	 * @return Byte Array of NDEF Message
	 * @throws UnsupportedEncodingException
	 */
	private byte[] createRawNdefTlv(NdefMessage ndefMessage)
			throws UnsupportedEncodingException {
		// creating NDEF
		byte[] ndefMessageByte = ndefMessage.toByteArray();
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
			int tlvSize = ndefMessageSize;
			tlvSize |= 0xFF0000;
			message[0] = (byte) 0x03;
			message[1] = (byte) ((tlvSize >> 16) & 0xFF);
			message[2] = (byte) ((tlvSize >> 8) & 0xFF);
			message[3] = (byte) (tlvSize & 0xFF);
			message[message.length - 1] = (byte) 0xFE;
			System.arraycopy(ndefMessageByte, 0, message, 4,
					ndefMessageByte.length);
		}
		return message;
	}

	@Override
	public Boolean checkPTwritePossible() throws IOException, FormatException {
		// Just wait some time
		try {
			Thread.sleep(waitTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
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
		byte[] command = new byte[5];
		command[0] = (byte) 0x1B;
		command[1] = pwd[0];
		command[2] = pwd[1];
		command[3] = pwd[2];
		command[4] = pwd[3];
		return mfu.transceive(command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#protectPlus()
	 * 
	 */
	@Override
	public void protectPlus(byte[] pwd, byte startAddr)
			throws IOException, FormatException, NotPlusTagException  {
		byte[] data = new byte[4];
		
		if(getProduct() != Prod.NTAG_I2C_1k_Plus && getProduct() != Prod.NTAG_I2C_2k_Plus) {
			throw new NotPlusTagException(
					"Auth Operations are not supported by non NTAG I2C PLUS products");
		} 
		
		// Set the password indicated by the user
		mfu.writePage(0xE5, pwd);
		
		byte access = (byte) 0x00;
		byte authLim = 0x00; 							// Don't limit the number of auth attempts
		
		access ^= 1 << Access_Offset.NFC_PROT.getValue();			// NFC_Prot
		access ^= 0 << Access_Offset.NFC_DIS_SEC1.getValue();		// NFC_DIS_SEC1
		access |= authLim << Access_Offset.AUTH_LIM.getValue();	// AUTHLIM
		
		// Write the ACCESS configuration
		data[0] = access;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = 0x00;
		mfu.writePage(0xE4, data);
		
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
		mfu.writePage(0xE7, data);
				
		// Write the AUTH0 lock starting page
		data[0] = 0x00;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = startAddr;
		mfu.writePage(0xE3, data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nxp.nfc_demo.reader.I2C_Enabled_Commands#unprotectPlus()
	 * 
	 */
	@Override
	public void unprotectPlus() throws IOException, FormatException, NotPlusTagException  {
		byte[] data = new byte[4];
		
		if(getProduct() != Prod.NTAG_I2C_1k_Plus && getProduct() != Prod.NTAG_I2C_2k_Plus) {
			throw new NotPlusTagException(
					"Auth Operations are not supported by non NTAG I2C PLUS products");
		}
		data[0] = 0x00;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = (byte) 0xFF;
		mfu.writePage(0xE3, data);
		
		// Set the password to FFs
		data[0] = (byte) 0xFF;
		data[1] = (byte) 0xFF;
		data[2] = (byte) 0xFF;
		data[3] = (byte) 0xFF;
		mfu.writePage(0xE5, data);
		
		// Write the ACCESS configuration
		data[0] = 0x00;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = 0x00;
		mfu.writePage(0xE4, data);
		
		// Write the PT I2C configuration
		data[0] = 0x00;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = 0x00;
		mfu.writePage(0xE7, data);
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
					
					if (((0x0000080 & access[0]) >> Access_Offset.NFC_PROT.getValue() == 1)
					 && ((0x0000004 & pti2c[0]) >> PT_I2C_Offset.SRAM_PROT.getValue() == 1)) {
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
		} catch (CommandNotSupportedException e) {
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
		return mfu.readPages(0xE3);
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
		return mfu.readPages(0xE4);
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
		return mfu.readPages(0xE7);
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
		
		// Write the ACCESS configuration
		data[0] = access;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = 0x00;
		mfu.writePage(0xE4, data);
		
		// Write the PT I2C configuration
		data[0] = ptI2C;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = 0x00;
		mfu.writePage(0xE7, data);
		
		// Set the password to FFs
		data[0] = (byte) 0xFF;
		data[1] = (byte) 0xFF;
		data[2] = (byte) 0xFF;
		data[3] = (byte) 0xFF;
		mfu.writePage(0xE5, data);
		
		// Set the pack to 00s
		data[0] = (byte) 0x00;
		data[1] = (byte) 0x00;
		data[2] = (byte) 0x00;
		data[3] = (byte) 0x00;
		mfu.writePage(0xE6, data);
		
		data[0] = 0x00;
		data[1] = 0x00;
		data[2] = 0x00;
		data[3] = auth0;
		mfu.writePage(0xE3, data);
	}
}
