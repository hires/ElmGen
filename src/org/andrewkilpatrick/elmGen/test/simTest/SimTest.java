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
package org.andrewkilpatrick.elmGen.test.simTest;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.andrewkilpatrick.elmGen.EEPromHandler;
import org.andrewkilpatrick.elmGen.ProgDump;
import org.andrewkilpatrick.elmGen.simulator.SpinSimulator;


public class SimTest {
	String programmerPort = "COM5";  // serial port for the Arduino
	
	/**
	 * Creates code for testing.
	 */
	public SimTest(String args[]) {
		boolean flash = false;  // run the flash tool
		boolean simulate = false;  // run the simulator
		boolean progdump = true;  // run the program dumper 
		
		// generate the code
		Distortion distortion = new Distortion();
		Mute mute = new Mute();
		Bypass bypass = new Bypass();
		Reverb reverb = new Reverb();
		System.out.println("Code generation complete!");
		
		// build/write the EEPROM
		if(flash) {
			try {
				EEPromHandler eeprom = new EEPromHandler("COM5");
				eeprom.fillBank(distortion, 0);
				eeprom.fillBank(bypass, 1);
				eeprom.fillBank(reverb, 2);
				eeprom.fillBank(mute, 3);
				eeprom.fillBank(mute, 4);
				eeprom.fillBank(mute, 5);
				eeprom.fillBank(mute, 6);
				eeprom.fillBank(mute, 7);
//				eeprom.writeAllBanks();
				eeprom.writeBank(2);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
				
		// run the simulator
		if(simulate) {
			String testWav = "c:\\temp\\test1.wav";  // input test file name
//			String outputFile = "c:\\temp\\out.wav";  // write out to a file
			String outputFile = null;  // play out through the sound card
			SpinSimulator sim = new SpinSimulator(reverb, testWav, outputFile, 0.5, 0.6, 0.25);
			sim.showInteractiveControls();  // 
			sim.showLevelLogger();
			sim.setLoopMode(true);
			sim.run();
		}
		
		// run the program dumper
		if(progdump) {
			String outputFile = "c:\\temp\\reverb.prg";
			try {
				ProgDump.dumpProgram(reverb, outputFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		System.exit(0);
	}		
	
	public static void main(String args[]) {
 		new SimTest(args);		
	}
}
