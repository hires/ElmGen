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
package org.andrewkilpatrick.elmGen.instructions;

import org.andrewkilpatrick.elmGen.simulator.SimulatorState;

/**
 * This class represents the WRAX instruction.
 * 
 * @author andrew
 */
public class WriteRegister extends Instruction {
	final int addr;
	final double scale;
	
	/**
	 * Writes ACC to a register file, then multiply ACC by scale.
	 * 
	 * @param addr the register address to write
	 * @param scale the amount to scale the ACC after the write
	 */
	public WriteRegister(int addr, double scale) {
		if(addr < 0 || addr > 63) {
			throw new IllegalArgumentException("addr out of range: " + addr +
					" - valid range: 0 - 63");
		}
		checkS114(scale);
		this.addr = addr;
		this.scale = scale;
	}
	
	@Override
	public int getHexWord() {
		return ((convS114(scale) & 0xffff) << 16) | 
			((addr & 0x3f) << 5) | 0x06;
	}

	@Override
	public String getInstructionString() {
		return "WriteRegister(" + addr + "," + scale + ")";
	}

	@Override
	public void simulate(SimulatorState state) {
		state.setRegVal(addr, state.getACCVal());
		state.getACC().scale(scale);
	}
}
