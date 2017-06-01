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
package org.andrewkilpatrick.elmGen.simulator;

public class DelayCompressor {

	/**
	 * Compresses a 24 bit signed value into a 14 bit floating point value.
	 * 
	 * bit 13 - sign
	 * bit 9-12 - exponent
	 * bit 0-8 - mantissa
	 * 
	 * @param val original 24 bit signed value
	 * @return a 14 bit floating point value
	 */
	static int compress(int val) {
		int testVal = val & 0x7fffff;
		if(testVal == 0) {
			return 0;
		}
		if(val < 0) {
			testVal = ((~testVal) + 1) & 0x7fffff;
		}
		int ret = 0;
		int exp = -8;
		for(long i = 0x200l; i < 0x800000l; i = i << 1) {
			if(i > testVal) {
				break;
			}
			exp ++;
		}
		long mant = testVal >> (exp + 8);
		ret = (int)(((exp & 0x0f) << 9) | (mant & 0x1ff));
		if(val < 0) ret |= 0x2000;
		return ret;
	}
	
	/**
	 * Decompresses a 14 bit floating point value into a 24 bit signed value.
	 * 
	 * @param val the 14 bit floating point value
	 * @return the decompressed 24 bit signed value
	 */
	static int decompress(int val) {
		int ret = 0;
		int exp = (val >> 9) & 0x0f;
		if((exp & 0x08) > 0) {
			exp = (exp & 0x07) - 8;
		}
		ret = (int) ((POWER_LOOKUP[(int) (exp + 8)] * 256.0) * (val & 0x1ff));
		if((val & 0x2000) != 0) {
			ret = (~ret + 1);
		}
		return ret;
	}
	
	private static double POWER_LOOKUP[] = {
		Math.pow (2, -8),
		Math.pow (2, -7),
		Math.pow (2, -6),
		Math.pow (2, -5),
		Math.pow (2, -4),
		Math.pow (2, -3),
		Math.pow (2, -2),
		Math.pow (2, -1),
		Math.pow (2, 0),
		Math.pow (2, 1),
		Math.pow (2, 2),
		Math.pow (2, 3),
		Math.pow (2, 4),
		Math.pow (2, 5),
		Math.pow (2, 6),
		Math.pow (2, 7)
	};
	


	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		for(int i = -0x7fffff; i < 0x7fffff; i += 100000) {
		for(int i = -6; i < 6; i += 1) {
			int ret = DelayCompressor.compress(i);
			int ret2 = DelayCompressor.decompress(ret);
			System.out.println("original value: " + String.format("0x%08x - %10d", i, i) +
				"\n      compress: " + String.format("0x%08x - %10d" , ret, ret) + 
				"\n    decompress: " + String.format("0x%08x - %10d", ret2, ret2) + "\n");
		}
	}
}
