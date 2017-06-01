/* ElmGen - DSP Development Tool
 * Copyright (C)2011 - Andrew Kilpatrick
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 	
 */
package org.andrewkilpatrick.elmGen;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;

/**
 * This class implements the host-side protocol, programming and verification
 * parts of a hardware EEPROM programmer for writing to an i2c EEPROM. The
 * protocol is compatible with Andrew Kilpatrick's Arduino-based EEPROM
 * programmer firmware. Native RXTX drivers must be installed on your
 * system for this to work.
 * 
 * @author andrew
 */
public class EEPromProgrammer {
	private final static int RX_TIMEOUT = 4000;
	private SerialPort port = null;
	private InputStream in = null;
	private OutputStream out = null;
	
	/*
	 * Protocol:
	 * 
	 * - SOH
	 * - CMD
	 * - DATA_LEN
	 * - DATA...
	 * - EOT
	 * 
	 * CMD_READ_DATA:
	 * - data 0 = start (MSB)
	 * - data 1 = start (LSB)
	 * - data 2 = length
	 * 
	 * CMD_WRITE_DATA:
	 * - data 0 = start (MSB)
	 * - data 1 = start (LSB)
	 * - data 2 = length
	 * - data 3-n = data
	 * 
	 * RET_READ_DATA:
	 * - data 0 = start (MSB)
	 * - data 1 = start (LSB)
	 * - data 2 = length
	 * - data 3-n = data
	 */
	// protocol bytes
	public static final byte CMD_READ_DATA = 0x01;
	public static final byte CMD_WRITE_DATA = 0x02;
	public static final byte RET_READ_DATA = (byte)0x81;
	public static final byte RET_WRITE_ACK = (byte)0x82;
	
	// framing characters
	public static final int SOH = 0x01;
	public static final int EOT = 0x04;
	public static final int ESC = 0x1b;
	public static final int ESC_SOH = 0x78;
	public static final int ESC_EOT = 0x79;
	public static final int ESC_ESC = 0x7a;

	/**
	 * Creates an EEPromProgrammer to communicate with a
	 * hardware programmer.
	 * 
	 * @param serialPortName the serial port name connected to the programmer
	 */
	public EEPromProgrammer(String serialPortName) {
		// open the serial port
        CommPortIdentifier portIdentifier;
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(serialPortName);
	        if(portIdentifier.isCurrentlyOwned()) {
	            System.err.println("Error: Port is currently in use: " +
	            		serialPortName);
	            System.exit(-1);
	        }
	        else {
	            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
	            if(commPort instanceof SerialPort) {
	                port = (SerialPort) commPort;
	                port.setSerialPortParams(9600,
	                		SerialPort.DATABITS_8,
	                		SerialPort.STOPBITS_1,
	                		SerialPort.PARITY_NONE);
	                port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
	                port.disableReceiveFraming();
	                port.disableReceiveTimeout();
	                port.disableReceiveThreshold();
	                in = port.getInputStream();
	                out = port.getOutputStream();
	            } else {
	                System.err.println("Port is not a serial port: " + serialPortName);
	            }
	        }
		} catch (NoSuchPortException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (PortInUseException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("EEPromProgrammer waiting 2s for startup...");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Programs a bank.
	 * 
	 * @param bank the bank to load into
	 * @param eeprom the EEPromFile to use for data
	 * @throws IOException if there was an error
	 */
	public void programBank(int bank, EEPromHandler eeprom) throws IOException {
		// program in 16 byte chunks
		System.out.println("Writing bank:   " + bank + " to EEPROM");
		for(int i = 0; i < 512; i += 16) {
			byte data[] = eeprom.getBytes(bank, i, 16);
			write((bank * 512) + i, data);
		}
		
		// verify in 16 byte chunks
		System.out.println("Verifying bank: " + bank + " from EEPROM");
		int verifyCount = 0;
		for(int i = 0; i < 512; i += 16) {
			byte data[] = eeprom.getBytes(bank, i, 16);
			byte rxData[];
			rxData = read((bank * 512) + i, 16);
			if(rxData == null) {
				throw new IOException("no data received from device!");
			}
			if(rxData.length != 16) {
				throw new ElmProgramException("verify failure: rxData " +
					"is not of length = 16");
			}
			for(int j = 0; j < 16; j ++) {
				if(rxData[j] != data[j]) {
					throw new ElmProgramException("verify failure at: " + (i + j) + 
						String.format("expecting: %02X - got: %02X", 
							data[j], rxData[j]));
				}
				else {
					verifyCount ++;
				}
			}
		}
	}
	
	/**
	 * Reads data from the EEPROM.
	 * 
	 * @param start the start offset
	 * @param length the number of bytes to read (must be 1-16)
	 * @return an array of bytes
	 * @throws IOException if there is a programmer error
	 */
	public byte[] read(int start, int length) throws IOException {
		if(length < 1 || length > 16) {
			throw new IllegalArgumentException("length is invalid: " + length +
					" - must be 1-16");
		}
		if(start < 0 || start > 32767) {
			throw new IllegalArgumentException("start is invalid: " + start +
					" - must be 0-32767");
		}
		byte txMsg[] = new byte[7];
		txMsg[0] = SOH;
		txMsg[1] = CMD_READ_DATA;
		txMsg[2] = 3;
		txMsg[3] = (byte)((start & 0xff00) >> 8);
		txMsg[4] = (byte)(start & 0x00ff);
		txMsg[5] = (byte)(length & 0xff);
		txMsg[6] = EOT;
		// send the message to the programmer
		byte rxMsg[];
		try {
			rxMsg = sendReceiveMessage(txMsg);
		} catch (ParseException e) {
			throw new IOException(e.getMessage());
		}
		if(rxMsg == null) {
			return null;
		}
		if(rxMsg[1] != RET_READ_DATA) {
			throw new IOException("bad command received: " + 
					String.format("%02X", rxMsg[1]));
		}
		if(rxMsg[2] < 4) {
			throw new IOException("not enough data received: " +
					rxMsg.length + " bytes - must be > 3");			
		}
		int eepromDataLen = rxMsg[2] - 3;
		byte eepromData[] = new byte[eepromDataLen];
		System.arraycopy(rxMsg, 6, eepromData, 0, eepromDataLen);
		return eepromData;
	}
	
	/**
	 * Writes data to the EEPROM.
	 * 
	 * @param start the start offset
	 * @param eepromData an array of bytes containing the data to write
	 * @throws IOException if there is a programmer error
	 */
	public void write(int start, byte eepromData[]) throws IOException {
		if(eepromData.length < 1 || eepromData.length > 16) {
			throw new IllegalArgumentException("data length is invalid: " + 
					eepromData.length + " - must be 1-16");
		}
		if(start < 0 || start > 32767) {
			throw new IllegalArgumentException("start is invalid: " + start +
					" - must be 0-32767");
		}
		byte txMsg[] = new byte[7 + eepromData.length];
		int txCount = 0;
		txMsg[txCount++] = SOH;
		txMsg[txCount++] = CMD_WRITE_DATA;
		txMsg[txCount++] = (byte)(eepromData.length + 3);
		txMsg[txCount++] = (byte)((start & 0xff00) >> 8);
		txMsg[txCount++] = (byte)(start & 0x00ff);
		txMsg[txCount++] = (byte)(eepromData.length & 0xff);
		for(int i = 0; i < eepromData.length; i ++) {
			txMsg[txCount++] = eepromData[i];
		}
		txMsg[txCount++] = EOT;
		// send the message to the programmer
		byte rxMsg[];
		try {
			rxMsg = sendReceiveMessage(txMsg);
		} catch (ParseException e) {
			throw new IOException(e.getMessage());
		}
		if(rxMsg == null) {
			return;
		}
		if(rxMsg[1] != RET_WRITE_ACK) {
			throw new IOException("bad command received: " + 
					String.format("%02X", rxMsg[1]));
		}
	}
	
	private byte[] sendReceiveMessage(byte txMsg[]) throws ParseException {
		byte txStuffMsg[] = stuffMessage(txMsg);
		try {
			// flush receiver
			while(in.available() > 0) {
				in.read();
			}
			// send message
			out.write(txStuffMsg);
			// receive message
			long startTime = System.currentTimeMillis();
			byte rxBuf[] = new byte[128];
			int inCount = 0;
			while((System.currentTimeMillis() - startTime) < RX_TIMEOUT) {
				while(in.available() > 0 && inCount < rxBuf.length - 1) {
					int rxByte = in.read();
					rxBuf[inCount++] = (byte)rxByte;
					if(rxByte == EOT) {
						byte shorterBuf[] = new byte[inCount];
						System.arraycopy(rxBuf, 0, shorterBuf, 0, inCount);
						return unstuffMessage(shorterBuf);
					}
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.err.println("no response received from device");
		return null;
	}
	
	private byte[] stuffMessage(byte msg[]) {
		byte stuff[] = new byte[msg.length * 2];
		stuff[0] = msg[0];
		int outCount = 1;
		for(int i = 1; i < (msg.length - 1); i ++) {
			if(msg[i] == SOH) {
				stuff[outCount++] = ESC;
				stuff[outCount++] = ESC_SOH;
			} else if(msg[i] == EOT) {
				stuff[outCount++] = ESC;
				stuff[outCount++] = ESC_EOT;				
			} else if(msg[i] == ESC) {
				stuff[outCount++] = ESC;
				stuff[outCount++] = ESC_ESC;				
			} else {
				stuff[outCount++] = msg[i];
			}
		}
		stuff[outCount++] = msg[msg.length - 1];
		byte retBuf[] = new byte[outCount];
		System.arraycopy(stuff, 0, retBuf, 0, outCount);
		return retBuf;
	}
	
	private byte[] unstuffMessage(byte msg[]) throws ParseException {
		if(msg.length < 4) {
			throw new ParseException("received message too short: " +
					msg.length, 0);
		}
		byte unstuff[] = new byte[msg.length];
		unstuff[0] = msg[0];
		boolean escape = false;
		int outCount = 1;
		for(int i = 1; i < (msg.length - 1); i ++) {
			if(msg[i] == ESC) {
				escape = true;
				continue;
			}
			if(escape == true) {
				if(msg[i] == ESC_SOH) {
					unstuff[outCount++] = SOH;
				}
				else if(msg[i] == ESC_EOT) {
					unstuff[outCount++] = EOT;
				}
				else if(msg[i] == ESC_ESC) {
					unstuff[outCount++] = ESC;
				}
				else {
					throw new ParseException("invalid escape code: " + 
							String.format("%02X", msg[i]), i);
				}
				escape = false;
			} else {
				unstuff[outCount++] = msg[i];
			}
		}
		unstuff[outCount++] = msg[msg.length - 1];
		byte retBuf[] = new byte[outCount];
		System.arraycopy(unstuff, 0, retBuf, 0, outCount);
		return retBuf;
	}
	
	/**
	 * Main for testing.
	 * 
	 * @param args supply the COM port name
	 */
	public static void main(String args[]) {
		if(args.length < 1) {
			System.err.println("com port must be specified");
			return;
		}
		EEPromProgrammer programmer = new EEPromProgrammer(args[0]);
		try {
			byte eepromData[] = new byte[4];
			for(int i = 0; i < eepromData.length; i ++) {
				eepromData[i] = (byte)(i + 10);
			}
			programmer.write(512, eepromData);
			System.out.println("Waiting to read...");
			Thread.sleep(2000);
			System.out.println("Reading...");
			programmer.read(0, 16);
			
			System.out.println("Done.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
