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
package org.andrewkilpatrick.elmGen.test;

import org.andrewkilpatrick.elmGen.ElmProgram;

import junit.framework.TestCase;

public class CodeGenTests extends TestCase {
	
	public void testSimple() {
		ElmProgram p = new ElmProgram("testSimple");
		p.clear();
		p.readRegister(ElmProgram.ADCL, 0.5);
		p.readRegister(ElmProgram.ADCR, 0.5);
		p.writeRegister(ElmProgram.DACL, 1.0);
		p.writeRegister(ElmProgram.DACR, 0.0);

		CodeComparator.compare(p, "tests/simple");
	}
	
	public void testAccumulator() {
		ElmProgram p = new ElmProgram("testAccumulator");
		p.scaleOffset(1.0, 0.0);
		p.scaleOffset(0.5, 0.25);
		p.scaleOffset(0.0, 0.9990234375);
		p.scaleOffset(-2.0, -1.0);
		p.and(0xffffff);
		p.and(0x000000);
		p.and(0x123456);
		p.or(0xffffff);
		p.or(0x000000);
		p.or(0x123456);
		p.xor(0xffffff);
		p.xor(0x000000);
		p.xor(0x123456);
		p.log(0.7, -1.0);		
		p.log(1.0, 0.0);
		p.log(-1.0, 0.3);
		p.log(0.24, 0.75);
		p.log(0.0, 0.9990234375);
		p.exp(-1.0, 0.9990234375);
		p.exp(-1.0, 0.25);
		p.exp(0.25, -1.0);
		p.skip(ElmProgram.SKP_RUN, 10);
		p.skip(ElmProgram.SKP_ZRC, 63);
		p.skip(ElmProgram.SKP_RUN | ElmProgram.SKP_GEZ, 4);
		p.skip(ElmProgram.SKP_ZRO | ElmProgram.SKP_NEG, 1);		

		CodeComparator.compare(p, "tests/accumulator");
	}

	public void testRegister() {
		ElmProgram p = new ElmProgram("testRegister");

		p.readRegister(0, -2.0);
		p.readRegister(63, 1.99993896484);
		p.readRegister(2, 0.0);
		p.readRegister(24, 0.234);
		p.writeRegister(0, -2.0);
		p.writeRegister(63, 1.99993896484);
		p.writeRegister(2, 0.0);
		p.writeRegister(24, 0.234);
		p.maxx(0, -2.0);
		p.maxx(63, 1.99993896484);
		p.maxx(2, 0.0);
		p.maxx(24, 0.234);	
		p.mulx(0);
		p.mulx(4);
		p.mulx(63);
		p.readRegisterFilter(0, -2.0);
		p.readRegisterFilter(63, 1.99993896484);
		p.readRegisterFilter(2, 0.0);
		p.readRegisterFilter(24, 0.234);			
		p.writeRegisterLowshelf(0, -2.0);
		p.writeRegisterLowshelf(63, 1.99993896484);
		p.writeRegisterLowshelf(2, 0.0);
		p.writeRegisterLowshelf(24, 0.234);		
		p.writeRegisterHighshelf(0, -2.0);
		p.writeRegisterHighshelf(63, 1.99993896484);
		p.writeRegisterHighshelf(2, 0.0);
		p.writeRegisterHighshelf(24, 0.234);	

		CodeComparator.compare(p, "tests/register");
	}
	
	public void testDelay() {
		ElmProgram p = new ElmProgram("testDelay");
		
		p.readDelay(0, -2.0);
		p.readDelay(32767, 1.998046875);
		p.readDelay(1225, 0.0);
		p.readDelay(5321, 1.0);
		p.readDelay(22, -1.0);
		p.readDelayPointer(-2.0);
		p.readDelayPointer(1.998046875);
		p.readDelayPointer(0.0);
		p.readDelayPointer(1.0);
		p.readDelayPointer(-1.0);
		p.writeDelay(0, -2.0);
		p.writeDelay(32767, 1.998046875);
		p.writeDelay(1225, 0.0);
		p.writeDelay(5321, 1.0);
		p.writeDelay(22, -1.0);
		p.writeAllpass(0, -2.0);
		p.writeAllpass(32767, 1.998046875);
		p.writeAllpass(1225, 0.0);
		p.writeAllpass(5321, 1.0);
		p.writeAllpass(22, -1.0);		
		
		CodeComparator.compare(p, "tests/delay");
	}
	
	public void testLFO() {
		ElmProgram p = new ElmProgram("testLFO");

		p.loadSinLFO(0, 0, 0);
		p.loadSinLFO(1, 100, 32767);
		p.loadSinLFO(0, 511, 3000);
		p.loadSinLFO(1, 0, 200);
		p.loadSinLFO(0, 200, 10);
		p.loadSinLFO(1, 500, 400);
		p.loadRampLFO(0, -16384, 512);
		p.loadRampLFO(0, -1200, 1024);
		p.loadRampLFO(0, 0, 2048);
		p.loadRampLFO(1, 200, 4096);
		p.loadRampLFO(1, 1000, 512);
		p.loadRampLFO(1, 32767, 2048);
		p.jam(0);
		p.jam(1);	
		
		p.chorusReadDelay(ElmProgram.CHO_LFO_SIN0, 
				ElmProgram.CHO_SIN |
				ElmProgram.CHO_REG |
				ElmProgram.CHO_COMPA, 0);
		p.chorusReadDelay(ElmProgram.CHO_LFO_SIN1,
				ElmProgram.CHO_COS |
				ElmProgram.CHO_COMPC, 32767);
		p.chorusReadDelay(ElmProgram.CHO_LFO_SIN0, 
				ElmProgram.CHO_COS |
				ElmProgram.CHO_COMPC |
				ElmProgram.CHO_COMPA, 100);
		p.chorusReadDelay(ElmProgram.CHO_LFO_SIN1, 
				ElmProgram.CHO_SIN |
				ElmProgram.CHO_REG, 2300);
		p.chorusReadDelay(ElmProgram.CHO_LFO_RMP0,
				ElmProgram.CHO_COMPC |
				ElmProgram.CHO_RPTR2 |
				ElmProgram.CHO_NA, 1000);
		p.chorusReadDelay(ElmProgram.CHO_LFO_RMP1,
				ElmProgram.CHO_COMPA, 20);
		p.chorusReadDelay(ElmProgram.CHO_LFO_RMP0,
				ElmProgram.CHO_REG, 32767);
		p.chorusReadDelay(ElmProgram.CHO_LFO_RMP1,
				ElmProgram.CHO_REG |
				ElmProgram.CHO_RPTR2 |
				ElmProgram.CHO_NA, 30000);
		p.chorusReadDelay(ElmProgram.CHO_LFO_SIN0, 
				ElmProgram.CHO_SIN, 356);
		p.chorusReadDelay(ElmProgram.CHO_LFO_SIN1,
				ElmProgram.CHO_COS |
				ElmProgram.CHO_COMPC |
				ElmProgram.CHO_COMPA, 2345);
		p.chorusScaleOffset(ElmProgram.CHO_LFO_SIN0, 
				ElmProgram.CHO_SIN |
				ElmProgram.CHO_REG |
				ElmProgram.CHO_COMPA, 0);
		p.chorusScaleOffset(ElmProgram.CHO_LFO_SIN1,
				ElmProgram.CHO_COS |
				ElmProgram.CHO_COMPC, 0.999969482421875);
		p.chorusScaleOffset(ElmProgram.CHO_LFO_SIN0, 
				ElmProgram.CHO_COS |
				ElmProgram.CHO_COMPC |
				ElmProgram.CHO_COMPA, -1.0);
		p.chorusScaleOffset(ElmProgram.CHO_LFO_SIN1, 
				ElmProgram.CHO_SIN |
				ElmProgram.CHO_REG, 0.2345);
		p.chorusScaleOffset(ElmProgram.CHO_LFO_RMP0,
				ElmProgram.CHO_COMPC |
				ElmProgram.CHO_RPTR2 |
				ElmProgram.CHO_NA, -0.999);
		p.chorusScaleOffset(ElmProgram.CHO_LFO_RMP1,
				ElmProgram.CHO_COMPA, 0);
		p.chorusScaleOffset(ElmProgram.CHO_LFO_RMP0,
				ElmProgram.CHO_REG, 0.999969482421875);
		p.chorusScaleOffset(ElmProgram.CHO_LFO_RMP1,
				ElmProgram.CHO_REG |
				ElmProgram.CHO_RPTR2 |
				ElmProgram.CHO_NA, 0.234234);
		p.chorusScaleOffset(ElmProgram.CHO_LFO_SIN0, 
				ElmProgram.CHO_SIN, -0.5);
		p.chorusScaleOffset(ElmProgram.CHO_LFO_SIN1,
				ElmProgram.CHO_COS |
				ElmProgram.CHO_COMPC |
				ElmProgram.CHO_COMPA, -1.0);
		p.chorusReadValue(ElmProgram.CHO_LFO_SIN0);
		p.chorusReadValue(ElmProgram.CHO_LFO_SIN1);
		p.chorusReadValue(ElmProgram.CHO_LFO_COS0);
		p.chorusReadValue(ElmProgram.CHO_LFO_COS1);
		p.chorusReadValue(ElmProgram.CHO_LFO_RMP0);
		p.chorusReadValue(ElmProgram.CHO_LFO_RMP1);
		
		CodeComparator.compare(p, "tests/lfo");
	}
	
	public void testPseudo() {
		ElmProgram p = new ElmProgram("testPseudo");
		
		p.clear();
		p.not();
		p.absa();
		p.loadAccumulator(0);
		p.loadAccumulator(34);
		p.loadAccumulator(63);
		
		CodeComparator.compare(p, "tests/pseudo");
	}
}
