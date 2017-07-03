package com.nxp.nfc_demo.listeners;

public interface WriteSRAMListener {
	/**
	 * It informs the listener about new data written in the SRAM
	 * Used to inform about the progress during the SpeedTest
	 *
	 */
    public abstract void onWriteSRAM();
}
