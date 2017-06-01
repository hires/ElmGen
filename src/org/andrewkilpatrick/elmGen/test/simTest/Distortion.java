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

import org.andrewkilpatrick.elmGen.ElmProgram;

public class Distortion extends ElmProgram {
	
	/**
	 * Creates a mute program.
	 */
	public Distortion() {
		super("Distortion");
		int filt = REG1;
		int filt2 = REG2;
		int filt3 = REG3;
		int filt4 = REG4;
		
		readRegister(ADCL, 1.0);
		mulx(POT0);
		
		scaleOffset(-2.0, 0.0);
		readRegister(filt, 0.9);
		writeRegister(filt, 1.0);
		scaleOffset(-2.0, 0.0);
		readRegister(filt2, 0.3);
		writeRegister(filt2, 1.0);
		scaleOffset(-2.0, 0.0);
		readRegister(filt3, 0.7);
		writeRegister(filt3, 1.0);
		readRegister(filt4, -0.3);
		writeRegister(filt4, 1.0);
		
		writeRegister(DACL, 1.0);
		writeRegister(DACR, 0.0);
	}

}
