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
 * This class represents the CHO RDAL instruction.
 * 
 * @author andrew
 */
public class ChorusReadValue extends Instruction {
	final int lfo;
	
	/**
	 * Scale offset based on LFO position.
	 * 
	 * @param lfo the LFO to use (0 = SIN0, 1 = SIN1, 2 = RMP0, 3 = RMP1, 4 = COS0, 5 = COS1)
	 */
	public ChorusReadValue(int lfo) {
		if(lfo < 0 || lfo > 5) {
			throw new IllegalArgumentException("lfo out of range: " + lfo +
					" - valid values: 0 = SIN0, 1 = SIN1, 2 = RMP0, 3 = RMP1, 4 = COS0, 5 = COS1)");
		}
		this.lfo = lfo;
	}
	
	@Override
	public int getHexWord() {
		int ret = 0xc2000014;
		ret |= (lfo & 0x03) << 21;
		ret |= (lfo & 0x04) << 22;
		return ret;
	}

	@Override
	public String getInstructionString() {
		return "ChorusReadValue(" + lfo + ")";
	}

	@Override
	public void simulate(SimulatorState state) {
		if(lfo == 0) { // sin 0
			state.setACCVal(state.getSinLFOVal(0));
		}
		else if(lfo == 1) {  // sin 1
			state.setACCVal(state.getSinLFOVal(1));
		}
		else if(lfo == 2) {  // ramp 0
			state.setACCVal(state.getRampLFOVal(0));
		}
		else if(lfo == 3) {  // ramp 1
			state.setACCVal(state.getRampLFOVal(1));
		}
		else if(lfo == 4) {  // cos 0
			state.setACCVal(state.getSinLFOVal(2));
		}
		else if(lfo == 5) {  // cos 1
			state.setACCVal(state.getSinLFOVal(3));
		}
	}
}
