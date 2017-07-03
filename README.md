# NTAGI2CDemo Annotated By NXP

This is mostly untouched source code, (just updated to run on android studio 2017).

I am interested in how this app works in communicating with the NTAG I2C plus tags, but it can be a bit hard to grok. So I'm annotating the code as needed to do what I'm trying to do.

* Source of source code http://www.nxp.com/products/identification-and-security/nfc-and-reader-ics/connected-tag-solutions/ntag-ic-plus-explorer-kit-development-kit:OM5569-NT322E?tab=Design_Tools_Tab


## Area where I have annotated functions:

* How does the phone firmware flash a device via the SRAM?
    - com/nxp/nfc_demo/reader/Ntag_I2C_Demo.java `public Boolean Flash(byte[] bytesToFlash)`