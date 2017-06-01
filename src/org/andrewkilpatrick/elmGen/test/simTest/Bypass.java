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

public class Bypass extends ElmProgram {
	
	/**
	 * Creates a bypass program. POT0 and POT1 control 
	 * left and right channel volume.
	 */
	public Bypass() {
		super("Mute");
		readRegister(ADCL, 1.0);
		mulx(POT0);
		writeRegister(DACL, 0.0);
		readRegister(ADCR, 1.0);
		mulx(POT1);
		writeRegister(DACR, 0.0);
	}

}
