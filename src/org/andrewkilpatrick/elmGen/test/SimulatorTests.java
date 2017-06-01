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
import org.andrewkilpatrick.elmGen.instructions.Absa;
import org.andrewkilpatrick.elmGen.instructions.And;
import org.andrewkilpatrick.elmGen.instructions.ChorusReadValue;
import org.andrewkilpatrick.elmGen.instructions.Clear;
import org.andrewkilpatrick.elmGen.instructions.Exp;
import org.andrewkilpatrick.elmGen.instructions.LoadAccumulator;
import org.andrewkilpatrick.elmGen.instructions.LoadSinLFO;
import org.andrewkilpatrick.elmGen.instructions.Log;
import org.andrewkilpatrick.elmGen.instructions.Maxx;
import org.andrewkilpatrick.elmGen.instructions.Mulx;
import org.andrewkilpatrick.elmGen.instructions.Not;
import org.andrewkilpatrick.elmGen.instructions.Or;
import org.andrewkilpatrick.elmGen.instructions.ReadRegister;
import org.andrewkilpatrick.elmGen.instructions.ReadRegisterFilter;
import org.andrewkilpatrick.elmGen.instructions.ScaleOffset;
import org.andrewkilpatrick.elmGen.instructions.WriteRegister;
import org.andrewkilpatrick.elmGen.instructions.WriteRegisterHighshelf;
import org.andrewkilpatrick.elmGen.instructions.WriteRegisterLowshelf;
import org.andrewkilpatrick.elmGen.instructions.Xor;
import org.andrewkilpatrick.elmGen.simulator.SimulatorState;
import org.andrewkilpatrick.elmGen.util.Util;


import junit.framework.TestCase;

public class SimulatorTests extends TestCase {
	
	public void testUtils() {
		int inl[] = new int[9];
		inl[0] = 0x000000;
		inl[1] = 0x100000;
		inl[2] = 0x3fffff;
		inl[3] = 0x400000;
		inl[4] = 0x700000;
		inl[5] = 0x7fffff;
		inl[6] = 0xffffff;
		inl[7] = 0xc00000;
		inl[8] = 0x800000;
		
		for(int i = 0; i < inl.length; i ++) {
			double doub = Util.regToDouble(inl[i]);
//			System.out.println("regToDouble - reg: " + String.format("0x%08x", inl[i]) +
//					" - double: " + doub);
//			System.out.println("doubletoReg - reg: " + String.format("0x%08x", Util.doubleToScale(doub)));
		}
	}
	
	public void testScaleOffset() {
		SimulatorState state = new SimulatorState();
		
		// check range
		for(int i = -0x800000; i < 0x800000; i += 10000) {
			state.getACC().clear();
			state.setACCVal(i);
			ScaleOffset so = new ScaleOffset(1.0, 0.0);
			so.simulate(state);
			assertEquals(i, state.getACCVal());
		}
		
		// check values
		state.setACCVal(0x400000);
		ScaleOffset so = new ScaleOffset(1.0, 0.0);
		so.simulate(state);
		assertEquals(0x400000, state.getACCVal());
		
		state.setACCVal(0x400000);
		so = new ScaleOffset(0.5, 0.0);
		so.simulate(state);
		assertEquals(0x200000, state.getACCVal());
		
		state.setACCVal(0x400000);
		so = new ScaleOffset(0.25, 0.0);
		so.simulate(state);
		assertEquals(0x100000, state.getACCVal());
		
		state.setACCVal(0x400000);
		so = new ScaleOffset(-1.0, 0.0);
		so.simulate(state);
		assertEquals(-0x400000, state.getACCVal());
		
		state.setACCVal(0x400000);
		so = new ScaleOffset(-2.0, 0.0);
		so.simulate(state);
		assertEquals(-0x800000, state.getACCVal());

		state.setACCVal(-0x400000);
		so = new ScaleOffset(-2.0, 0.0);
		so.simulate(state);
		assertEquals(0x7fffff, state.getACCVal());

		state.setACCVal(-0x100000);
		so = new ScaleOffset(-2.0, 0.0);
		so.simulate(state);
		assertEquals(0x200000, state.getACCVal());
	}

	public void testAnd() {
		SimulatorState state = new SimulatorState();
		
		state.setACCVal(0x400000);
		And and = new And(0x000000);
		and.simulate(state);
		assertEquals(0x000000, state.getACCVal());
		
		state.setACCVal(0x7fffff);
		and = new And(0xffffff);
		and.simulate(state);
		assertEquals(0x7fffff, state.getACCVal());
		
		state.setACCVal(-0x7fffff);
		and = new And(0x7fffff);
		and.simulate(state);
		assertEquals(0x7fffff, state.getACCVal());
	}
	
	public void testOr() {
		SimulatorState state = new SimulatorState();

		state.setACCVal(0x3fffff);
		Or or = new Or(0x000000);
		or.simulate(state);
		assertEquals(0x3fffff, state.getACCVal());
		
		state.setACCVal(0x3fffff);
		or = new Or(0xf00000);
		or.simulate(state);
		assertEquals(-0x7fffff, state.getACCVal());
		
		state.setACCVal(0x000001);
		or = new Or(0x800000);
		or.simulate(state);
		assertEquals(-0x000001, state.getACCVal());
		
		state.setACCVal(0x0000ff);
		or = new Or(0x0001ff);
		or.simulate(state);
		assertEquals(0x0001ff, state.getACCVal());
		
		state.setACCVal(0x0000ff);
		or = new Or(0x800000);
		or.simulate(state);
		assertEquals(-0x0000ff, state.getACCVal());
	}

	public void testXor() {
		SimulatorState state = new SimulatorState();
		
		state.setACCVal(0x0000ff);
		Xor xor = new Xor(0x0000ff);
		xor.simulate(state);
		assertEquals(0x000000, state.getACCVal());
	
		state.setACCVal(0x00000f);
		xor = new Xor(0x0000f0);
		xor.simulate(state);
		assertEquals(0x0000ff, state.getACCVal());

		state.setACCVal(0x0000ff);
		xor = new Xor(0x800000);
		xor.simulate(state);
		assertEquals(-0x0000ff, state.getACCVal());
		
	}
	
	public void testLog() {
		SimulatorState state = new SimulatorState();
		Log log = new Log(1.0, 0.0);

		double in[] = new double[9];
		in[0] = 0.0;
		in[1] = 0.03125;
		in[2] = 0.5;
		in[3] = 0.75;
		in[4] = 0.9999998807907104;
		in[5] = -0.001;
		in[6] = -0.02;
		in[7] = -0.5;
		in[8] = -1.0;
		   
		for(int i = 0; i < in.length; i ++) {
			state.setACCVal(Util.doubleToScale(in[i]));
			log.simulate(state);
//			System.out.println("log - in: " + in[i] + 
//					" - result: " + Util.regToDouble(state.getACCVal()));	
		}
	}
	
	public void testExp() {
		SimulatorState state = new SimulatorState();
		Exp exp = new Exp(1.0, 0.0);

		double in[] = new double[7];
		in[0] = 0.0;
		in[1] = 0.5;
		in[2] = 0.9999998807907104;
		in[3] = -0.001;
		in[4] = -0.02;
		in[5] = -0.5;
		in[6] = -1.0;
		   
		for(int i = 0; i < in.length; i ++) {
			state.setACCVal(Util.doubleToScale(in[i]));
			exp.simulate(state);
//			System.out.println("exp - in: " + in[i] + 
//					" - result: " + Util.regToDouble(state.getACCVal()));
		}
	}
	
	public void testSkip() {
		// XXX - write Skip test
	}
	
	public void testReadRegister() {
		SimulatorState state = new SimulatorState();
		
		state.setACCVal(0);
		state.setRegVal(11, 0xff00);
		ReadRegister readReg = new ReadRegister(11, 1.0);
		readReg.simulate(state);
		assertEquals(state.getACCVal(), 0xff00);

		state.setACCVal(0);
		readReg = new ReadRegister(11, 0.5);
		readReg.simulate(state);
		assertEquals(state.getACCVal(), 0x7f80);
	}
	
	public void testWriteRegister() {
		SimulatorState state = new SimulatorState();

		state.setACCVal(0x400000);
		WriteRegister writeReg = new WriteRegister(22, 1.0);
		writeReg.simulate(state);
		assertEquals(0x400000, state.getRegVal(22));
		
		writeReg = new WriteRegister(22, 0.5);
		writeReg.simulate(state);
		assertEquals(0x400000, state.getRegVal(22));
		assertEquals(0x200000, state.getACCVal());
	}
	
	public void testMaxx() {
		SimulatorState state = new SimulatorState();

		state.setACCVal(-0x100000);
		state.setRegVal(22, 0x200000);
		Maxx maxx = new Maxx(22, 1.0);
		maxx.simulate(state);
		assertEquals(0x200000, state.getACCVal());
		
		state.setACCVal(-0x100000);
		state.setRegVal(22, 0x200000);
		maxx = new Maxx(22, 0.5);
		maxx.simulate(state);
		assertEquals(0x100000, state.getACCVal());
		
		state.setACCVal(0x100000);
		state.setRegVal(22, -0x200000);
		maxx = new Maxx(22, 0.5);
		maxx.simulate(state);
		assertEquals(0x100000, state.getACCVal());		
	}
	
	public void testMulx() {
		SimulatorState state = new SimulatorState();
	
		state.setACCVal(Util.doubleToScale(0.1));
		state.setRegVal(ElmProgram.REG0, Util.doubleToScale(1.0));
		Mulx mulx = new Mulx(ElmProgram.REG0);
		mulx.simulate(state);
//		System.out.println("mulx: " + Util.regToDouble(state.getACCVal()));
	}
	
	public void testReadRegisterFilter() {
		SimulatorState state = new SimulatorState();
		
		state.setACCVal(0x400000);
		state.setRegVal(23, 0x00ff);
		ReadRegisterFilter readRegFilt = new ReadRegisterFilter(23, 1.0);
		readRegFilt.simulate(state);
		assertEquals(0x400000, state.getACCVal());
		
		state.setACCVal(0x400000);
		state.setRegVal(23, 0x00ff);
		readRegFilt = new ReadRegisterFilter(23, 0.5);
		readRegFilt.simulate(state);
		assertEquals(0x20007f, state.getACCVal());
		
	}
	
	public void testWriteRegisterLowshelf() {
		SimulatorState state = new SimulatorState();
		
		state.setACCVal(0x00ff00);
		state.sampleIncrement();
		state.setACCVal(0x3fffff);
		state.setRegVal(22, 0xff00);
		WriteRegisterLowshelf writeLowshelf = new WriteRegisterLowshelf(22, 1.0);
		writeLowshelf.simulate(state);
		assertEquals(0x3fffff, state.getRegVal(22));
		assertEquals(((0xff00 - 0x3fffff) / 1) + 0xff00, state.getACCVal());

		state.setACCVal(0x00ff00);
		state.sampleIncrement();
		state.setACCVal(0x3fffff);
		state.setRegVal(22, 0xff00);
		writeLowshelf = new WriteRegisterLowshelf(22, 0.5);
		writeLowshelf.simulate(state);
		assertEquals(0x3fffff, state.getRegVal(22));		
		assertEquals(((0xff00 - 0x3fffff) / 2) + 0xff00 - 1, state.getACCVal());
	}
	
	public void testWriteRegisterHighshelf() {
		SimulatorState state = new SimulatorState();
		
		state.setACCVal(0x00ff00);
		state.sampleIncrement();
		state.setACCVal(0x3fffff);
		WriteRegisterHighshelf writeHighshelf = new WriteRegisterHighshelf(22, 1.0);
		writeHighshelf.simulate(state);
		assertEquals(0x3fffff, state.getRegVal(22));
		assertEquals((0x3fffff / 1) + 0x00ff00, state.getACCVal());

		state.setACCVal(0x00ff00);
		state.sampleIncrement();
		state.setACCVal(0x3fffff);
		writeHighshelf = new WriteRegisterHighshelf(22, 0.5);
		writeHighshelf.simulate(state);
		assertEquals(0x3fffff, state.getRegVal(22));
		assertEquals((0x3fffff / 2) + 0x00ff00, state.getACCVal());
	}
	
	public void testReadDelay() {
		// XXX - write ReadDelay test
	}
	
	public void testReadDelayPointer() {
		// XXX - write ReadDelayPointer test
	}
	
	public void testWriteDelay() {
		// XXX - write WriteDelay test
	}
	
	public void testWriteDelayPointer() {
		// XXX - write WriteDelayPointer test
	}
	
	public void testLoadSinLFO() {
		SimulatorState state = new SimulatorState();
		LoadSinLFO loadSin0 = new LoadSinLFO(0, 25, 32767);
		loadSin0.simulate(state);
		assertEquals(25, (state.getRegVal(ElmProgram.SIN0_RATE) >> 14));
		assertEquals(32767, (state.getRegVal(ElmProgram.SIN0_RANGE) >> 8));
		LoadSinLFO loadSin1 = new LoadSinLFO(1, 30, 1000);
		loadSin1.simulate(state);
		assertEquals(30, (state.getRegVal(ElmProgram.SIN1_RATE) >> 14));
		assertEquals(1000, (state.getRegVal(ElmProgram.SIN1_RANGE) >> 8));
	}
	
	public void testLoadRampLFO() {
		// XXX - write LoadRampLFO test
	}
	
	public void testJam() {
		// XXX - write JAM test
	}
	
	public void testChorusReadDelay() {
		// XXX - write ChorusReadDelay test
	}
	
	public void testChorusScaleOffset() {
		// XXX - write ChorusScaleOffset test
	}
	
	public void testChorusReadValue() {
		// sin LFO
		SimulatorState state = new SimulatorState();
		LoadSinLFO loadSin0 = new LoadSinLFO(0, 25, 32767);
		LoadSinLFO loadSin1 = new LoadSinLFO(1, 25, 16383);
		loadSin0.simulate(state);
		loadSin1.simulate(state);
		
		state.sampleIncrement();
		ChorusReadValue cho0 = new ChorusReadValue(ElmProgram.CHO_LFO_SIN0);
		ChorusReadValue cho1 = new ChorusReadValue(ElmProgram.CHO_LFO_SIN1);
		cho0.simulate(state);
		cho1.simulate(state);

		int prevVal = 0;
		for(int i = 0; i < 33000; i ++) {
			state.sampleIncrement();
			cho0.simulate(state);
//			System.out.println("i: " + i + " - sin: " + state.getACCVal());
			if(i < 16473) {
				assertEquals("value is not > 0", true, state.getACCVal() > 0);
				if(i < 8238) {
					assertEquals("value is not increasing", true, state.getACCVal() >= prevVal);
				}
				else {
					assertEquals("value is not decreasing", true, state.getACCVal() <= prevVal);					
				}
			}
			else if(i > 16473 && i < 32941) {
				assertEquals("value is not < 0", true, state.getACCVal() < 0);
			}
			else if(i > 32940) {
				assertEquals("value is not > 0", true, state.getACCVal() > 0);
			}
			prevVal = state.getACCVal();
			cho1.simulate(state);
//			System.out.println("temp: " + prevVal + " - sin1: " + state.getACCVal());
			assertEquals("value of sin1 is not half the amplitude of sin0", 
					true, (state.getACCVal() < (prevVal / 2) + 1));
		}
	}
	
	public void testClear() {
		SimulatorState state = new SimulatorState();
		
		state.setACCVal(0xff00);
		Clear clear = new Clear();
		clear.simulate(state);
		assertEquals(0, state.getACCVal());
	}
	
	public void testNot() {
		SimulatorState state = new SimulatorState();
		int in = 0x0011ff;
		state.setACCVal(in);
		Not not = new Not();
		not.simulate(state);
		assertEquals(0xffffee00, state.getACCVal());
	}

	public void testAbsa() {
		SimulatorState state = new SimulatorState();
		state.setACCVal(-0x30030);
		Absa absa = new Absa();
		absa.simulate(state);
		assertEquals(0x30030, state.getACCVal());
	}
	
	public void testLoadAccumulator() {
		SimulatorState state = new SimulatorState();
		state.setACCVal(0x330030);
		state.setRegVal(1, -0xff03);
		LoadAccumulator loadAccumulator = new LoadAccumulator(1);
		loadAccumulator.simulate(state);
		assertEquals(-0xff03, state.getACCVal());
	}
	
//	private double clampRegVal(double in) {
//		if(in > 0.99999988079071) {
//			return 0.99999988079071;
//		}
//		if(in < -1.0) {
//			return -1.0;
//		}
//		return in;
//	}
//	
//	private boolean checkDoubleRange(double in, double err) {
//		if(in > (in + err) || in < (in - err)) {
//			return false;
//		}
//		return true;
//	}
}
