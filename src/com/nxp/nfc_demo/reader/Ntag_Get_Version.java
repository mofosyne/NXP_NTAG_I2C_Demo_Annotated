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


/**
 * Class for Get version Command response.
 */
public class Ntag_Get_Version {

	/**
	 * Enum for different Products.
	 */
	public enum Prod {
		NTAG_I2C_1k(888), NTAG_I2C_2k(1904), NTAG_I2C_1k_T(888), NTAG_I2C_2k_T(1904), NTAG_I2C_1k_V(888), NTAG_I2C_2k_V(1904),
		NTAG_I2C_1k_Plus(888), NTAG_I2C_2k_Plus(1912), 
		Unknown(0), MTAG_I2C_1k(720), MTAG_I2C_2k(1440);

		private int mem_size;

		private Prod(int mem_size) {
			this.mem_size = mem_size;
		}

		/**
		 * gets the Memsize of a Tag.
		 *
		 * @return Memsize of a Tag
		 */
		public int getMemsize() {
			return mem_size;
		}
	}

	private byte vendor_ID;
	private byte product_type;
	private byte product_subtype;
	private byte major_product_version;
	private byte minor_product_version;
	private byte storage_size;
	private byte protocol_type;

	/**
	 * Get version Response of a NTAG_I2C_1K.
	 */
	public static final Ntag_Get_Version NTAG_I2C_1k;
	public static final Ntag_Get_Version NTAG_I2C_1k_T;
	public static final Ntag_Get_Version NTAG_I2C_1k_V;

	/**
	 * Get version Response of a NTAG_I2C_2K.
	 */
	public static final Ntag_Get_Version NTAG_I2C_2k;
	public static final Ntag_Get_Version NTAG_I2C_2k_T;
	public static final Ntag_Get_Version NTAG_I2C_2k_V;
	
	/**
	 * Get version Response for NTAG I2C Plus products.
	 */
	public static final Ntag_Get_Version NTAG_I2C_1k_Plus;
	public static final Ntag_Get_Version NTAG_I2C_2k_Plus;

	public static final Ntag_Get_Version MTAG_I2C_1k;
	public static final Ntag_Get_Version MTAG_I2C_2k;
	public static final Ntag_Get_Version TNPI_6230;
	public static final Ntag_Get_Version TNPI_3230;

	static {
		NTAG_I2C_1k = new Ntag_Get_Version(new byte[] { 0x00, 0x04, 0x04, 0x05, 0x01, 0x01, 0x13, 0x03 });
		NTAG_I2C_2k = new Ntag_Get_Version(new byte[] { 0x00, 0x04, 0x04, 0x05, 0x01, 0x01, 0x15, 0x03 });
		
		NTAG_I2C_1k_V = new Ntag_Get_Version(new byte[] { 0x00, 0x04, 0x04, 0x05, 0x02, 0x00, 0x13, 0x03 });
		NTAG_I2C_2k_V = new Ntag_Get_Version(new byte[] { 0x00, 0x04, 0x04, 0x05, 0x02, 0x00, 0x15, 0x03 });
		
		NTAG_I2C_1k_T = new Ntag_Get_Version(new byte[] { 0x00, 0x04, 0x04, 0x05, 0x02, 0x01, 0x13, 0x03 });
		NTAG_I2C_2k_T = new Ntag_Get_Version(new byte[] { 0x00, 0x04, 0x04, 0x05, 0x02, 0x01, 0x15, 0x03 });
		
		NTAG_I2C_1k_Plus = new Ntag_Get_Version(new byte[] { 0x00, 0x04, 0x04, 0x05, 0x02, 0x02, 0x13, 0x03 });
		NTAG_I2C_2k_Plus = new Ntag_Get_Version(new byte[] { 0x00, 0x04, 0x04, 0x05, 0x02, 0x02, 0x15, 0x03 });
		
		MTAG_I2C_1k = new Ntag_Get_Version(new byte[] { 0x00, 0x04, 0x05, 0x07, 0x02, 0x02, 0x13, 0x03 });
		MTAG_I2C_2k = new Ntag_Get_Version(new byte[] { 0x00, 0x04, 0x05, 0x07, 0x02, 0x02, 0x15, 0x03 });

		TNPI_6230 = new Ntag_Get_Version(new byte[] { 0x00, 0x04, 0x05, 0x05, 0x01, 0x01, 0x15, 0x03 });
		TNPI_3230 = new Ntag_Get_Version(new byte[] { 0x00, 0x04, 0x05, 0x05, 0x01, 0x01, 0x13, 0x03 });
	}

	/**
	 * Returns the Product to which this get Version Response belongs.
	 *
	 * @return Product
	 */
	public Prod Get_Product() {
		if (this.equals(NTAG_I2C_1k))
			return Prod.NTAG_I2C_1k;
		if (this.equals(NTAG_I2C_2k))
			return Prod.NTAG_I2C_2k;
		if (this.equals(NTAG_I2C_1k_T))
			return Prod.NTAG_I2C_1k;
		if (this.equals(NTAG_I2C_2k_T))
			return Prod.NTAG_I2C_2k;
		if (this.equals(NTAG_I2C_1k_V))
			return Prod.NTAG_I2C_1k;
		if (this.equals(NTAG_I2C_2k_V))
			return Prod.NTAG_I2C_2k;
		if (this.equals(NTAG_I2C_1k_Plus))
			return Prod.NTAG_I2C_1k_Plus;
		if (this.equals(NTAG_I2C_2k_Plus))
			return Prod.NTAG_I2C_2k_Plus;
		if (this.equals(MTAG_I2C_1k))
			return Prod.MTAG_I2C_1k;
		if (this.equals(MTAG_I2C_2k))
			return Prod.MTAG_I2C_2k;
		else
			return Prod.Unknown;
	}

	/**
	 * Constructor.
	 * 
	 * @param Data
	 *            Data from the Get Version Command
	 */
	public Ntag_Get_Version(byte[] Data) {	
		vendor_ID = Data[1];
		product_type = Data[2];
		product_subtype = Data[3];
		major_product_version = Data[4];
		minor_product_version = Data[5];
		storage_size = Data[6];
		protocol_type = Data[7];
	}

	@Override
	/**
	 * Compares the Response by means of VendorID, Product Type, Product Subtype and Storage Size.
	 */
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other.getClass() != this.getClass()) {
			return false;
		}
		Ntag_Get_Version temp = (Ntag_Get_Version) other;
		
		if (temp.vendor_ID == this.vendor_ID
				&& temp.product_type == this.product_type
				&& temp.product_subtype == this.product_subtype
				&& temp.major_product_version == this.major_product_version
				&& temp.minor_product_version == this.minor_product_version
				&& temp.storage_size == this.storage_size) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the Vendor ID.
	 *
	 * @return Vendor ID
	 */
	public byte getVendor_ID() {
		return vendor_ID;
	}

	/**
	 * Returns the Product Type.
	 *
	 * @return Product Type
	 */
	public byte getProduct_type() {
		return product_type;
	}

	/**
	 * Returns the Product Subtype.
	 *
	 * @return Product Subtype
	 */
	public byte getProduct_subtype() {
		return product_subtype;
	}

	/**
	 * Returns the Major Product Version.
	 *
	 * @return Major Product Version
	 */
	public byte getMajor_product_version() {
		return major_product_version;
	}

	/**
	 * Returns the Minor Product Version.
	 *
	 * @return Minor Product Version
	 */
	public byte getMinor_product_version() {
		return minor_product_version;
	}

	/**
	 * Returns the Storage Size.
	 *
	 * @return Storage Size
	 */
	public byte getStorage_size() {
		return storage_size;
	}

	/**
	 * Returns the Protocol Type.
	 *
	 * @return Protocol Type
	 */
	public byte getProtocol_type() {
		return protocol_type;
	}

}
