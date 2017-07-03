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

import android.nfc.FormatException;
import android.nfc.Tag;
import android.nfc.tech.NfcA;

/**
 * Class for Basic Communication with a 14443 Tag.
 *
 * @author NXP67729
 *
 */
public class Ntag_Commands {

	/**
	 * current Sector in which the Tag is.
	 */
	private byte currentSec;
	private int sectorSelectTimout;
	private final int timeout = 20;

	private byte[] answer;
	private byte[] command;
	private NfcA nfca;

	/**
	 * Constructor connects the Tag also.
	 *
	 * @return tag tag which should be connected
	 * @throws IOException
	 */
	public Ntag_Commands(Tag tag) throws IOException {
		nfca = NfcA.get(tag);
		sectorSelectTimout = timeout;
		nfca.setTimeout(timeout);
		currentSec = 0;
	}


	/**
	 * Close the Connection to the Tag.
	 *
	 * @throws IOException
	 */
	public void close() throws IOException {
		nfca.close();
		currentSec = 0;
	}

	/**
	 * Reopens the connection to the Tag.
	 *
	 * @throws IOException
	 */
	public void connect() throws IOException {
		nfca.connect();
		currentSec = 0;
	}

	/**
	 * Checks if the tag is still connected.
	 */
	public boolean isConnected() {
		return nfca.isConnected();
	}

	/**
	 * Returns Byte Code of last Command.
	 *
	 * @return Byte Code of last Command
	 */
	public byte[] getLastCommand() {
		return command;
	}

	/**
	 * Returns Byte Code of last Answer.
	 *
	 * @return Byte Code of last Answer
	 */
	public byte[] getLastAnswer() {
		return answer;
	}

	/**
	 * Performs a Sector Select if necessary.
	 *
	 * @param sector
	 *            Sector which should be selected
	 * @throws IOException
	 * @throws FormatException
	 */
	public void SectorSelect(byte sector) throws IOException, FormatException {
		// When card is already in this sector do nothing
		if (currentSec == sector) {
			return;
		}
		command = new byte[2];
		command[0] = (byte) 0xc2;
		command[1] = (byte) 0xff;
		nfca.transceive(command);
		command = new byte[4];
		command[0] = (byte) sector;
		command[1] = (byte) 0x00;
		command[2] = (byte) 0x00;
		command[3] = (byte) 0x00;
		nfca.setTimeout(sectorSelectTimout);

		// catch exception, passive ack
		try {
			nfca.transceive(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
		nfca.setTimeout(timeout);
		currentSec = sector;
	}
	
	/**
	 * Performs a Fast Write Command.
	 *
	 * @param data
	 *            Data to write
	 * @param startAddr
	 *            Start Address
	 * @param endAddr
	 *            End Address
	 * @throws IOException
	 * @throws FormatException
	 */
	public void fast_write(byte[] data, byte startAddr, byte endAddr) throws IOException,
			FormatException {
		// no answer
		answer = new byte[0];
		command = new byte[3 + data.length];
		command[0] = (byte) 0xA6;
		command[1] = (byte) startAddr;
		command[2] = (byte) endAddr;
		System.arraycopy(data, 0, command, 3, data.length);
		nfca.setTimeout(500);
		nfca.transceive(command);
		nfca.setTimeout(timeout);
	}

	/**
	 * Writes Data on the Tag.
	 *
	 * @param data
	 *            Data to write
	 * @param blockNr
	 *            Block Number to write
	 * @throws IOException
	 * @throws FormatException
	 */
	public void write(byte[] data, byte blockNr) throws IOException,
			FormatException {
		// no answer
		answer = new byte[0];
		command = new byte[6];
		command[0] = (byte) 0xA2;
		command[1] = blockNr;
		command[2] = data[0];
		command[3] = data[1];
		command[4] = data[2];
		command[5] = data[3];
		nfca.transceive(command);
	}

	/**
	 * Performs a Fast Read Command.
	 *
	 * @param startAddr
	 *            Start Address
	 * @param endAddr
	 *            End Address
	 * @return Answer of the Fast Read Command
	 * @throws IOException
	 * @throws FormatException
	 */
	public byte[] fast_read(byte startAddr, byte endAddr) throws IOException,
			FormatException {

		command = new byte[3];
		command[0] = (byte) 0x3A;
		command[1] = (byte) startAddr;
		command[2] = (byte) endAddr;
		nfca.setTimeout(500);
		answer = nfca.transceive(command);
		nfca.setTimeout(timeout);
		return answer;
	}

	/**
	 * Performs a Read Command.
	 *
	 * @param blockNr
	 *            Block Number to begin Read
	 * @return Answer of the Read (always 16Byte)
	 * @throws IOException
	 * @throws FormatException
	 */
	public byte[] read(byte blockNr) throws IOException, FormatException {
		command = new byte[2];
		command[0] = (byte) 0x30;
		command[1] = blockNr;
		answer = nfca.transceive(command);
		return answer;
	}

	/**
	 * Performs a Get Version Command.
	 *
	 * @return Get Version Response
	 * @throws IOException
	 */
	public byte[] getVersion() throws IOException {
		command = new byte[1];
		command[0] = (byte) 0x60;
		answer = nfca.transceive(command);
		return answer;
	}
	
	/**
	 * Performs a PWD AUTH Command.
	 *
	 * @param pwd
	 *            Password to authenticate with
	 * @return PWD Auth Response
	 * @throws IOException
	 */
	public byte[] pwdAuth(byte[] pwd) throws IOException {
		command = new byte[5];
		command[0] = (byte) 0x1B;
		command[1] = pwd[0];
		command[2] = pwd[1];
		command[3] = pwd[2];
		command[4] = pwd[3];
		answer = nfca.transceive(command);
		return answer;
	}

	/**
	 * returns the maximum Transceive length.
	 *
	 * @return Maximum Transceive length
	 */
	public int getMaxTransceiveLength() {
		return nfca.getMaxTransceiveLength();
	}

}
