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

import org.andrewkilpatrick.elmGen.ElmProgram;
import org.andrewkilpatrick.elmGen.simulator.SimulatorState;


/**
 * This class represents the SKP instruction.
 * 
 * @author andrew
 */
public class Skip extends Instruction {
	final int flags;
	final int nskip;
	
	/**
	 * Conditional program execution.
	 * 
	 * @param flags the flags
	 * @param nskip the number of instructions to skip (1-63)
	 */
	public Skip(int flags, int nskip) {
		this.flags = flags;
		if(nskip < 1 || nskip > 63) {
			throw new IllegalArgumentException("nskip invalid: " + nskip + 
					" - must be 1 - 63");
		}
		this.nskip = nskip;
	}
	
	@Override
	public int getHexWord() {
		return (int)(((flags & 0x1f) << 27) | ((nskip & 0x3f) << 21) | 0x11);
	}

	@Override
	public String getInstructionString() {
		return "Skip(" + String.format("%02X", flags) + "," + nskip + ")";
	}

	@Override
	public void simulate(SimulatorState state) {
		boolean skip = false;
		if((flags & ElmProgram.SKP_RUN) > 0) {
			if(state.isFirstRun()) {
				skip = false;
			}
			else {
				skip = true;
			}
		}
		else if((flags & ElmProgram.SKP_ZRC) > 0) {
			if((state.getACCVal() < 0 && state.getPACCVal() >= 0) ||
					state.getACCVal() >= 0 && state.getPACCVal() < 0) {
				skip = true;
			}
			else {
				skip = false;
			}
		}
		else if((flags & ElmProgram.SKP_ZRO) > 0) {
			if(state.getACCVal() == 0) {
				skip = true;
			}
			else {
				skip = false;
			}
		}
		else if((flags & ElmProgram.SKP_GEZ) > 0) {
			if(state.getACCVal() > 0) {
				skip = true;
			}
			else {
				skip = false;
			}
		}
		else if((flags & ElmProgram.SKP_NEG) > 0) {
			if(state.getACCVal() < 0) {
				skip = true;
			}
			else {
				skip = false;
			}
		}
		if(skip) {
			state.skipInst(nskip);
		}
	}
}
