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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.andrewkilpatrick.elmGen.ElmProgram;


import junit.framework.TestCase;

public class CodeComparator extends TestCase {
	/**
	 * Compares the machine code between SpinGen and SpinASM.
	 * 
	 * @param prog the program to compare
	 * @param comparisonFile the filename of the machine code dump from SpinASM
	 */
	public static void compare(ElmProgram prog, String comparisonFile) {
		String myCode[] = prog.getMachineCodeStrings();
		String spinCode[] = loadTest(comparisonFile);
		System.out.println("loaded code - length: " + spinCode.length);
		for(int i = 0; i < myCode.length; i ++) {
			System.out.println(i + ":" + myCode[i]);
			assertEquals(spinCode[i] + " - " + myCode[i], spinCode[i], myCode[i]);			
		}
	}
	
	private static String[] loadTest(String fileName) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));			
			String spinCode[] = new String[128];
			int lineCount = 0;
			while(in.ready()) {
				String line = in.readLine();
				String parts[] = line.trim().split("\\t");
				if(parts.length < 3) {
					System.err.println("too few parts to be real");
					return null;
				}
				spinCode[lineCount++] = parts[2];
			}
			String spinCode2[] = new String[lineCount];
			System.arraycopy(spinCode, 0, spinCode2, 0, lineCount);
			return spinCode2;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
