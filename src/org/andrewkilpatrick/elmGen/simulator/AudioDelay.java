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

public class AudioDelay {
	int buf[];
	int pos;
	
	public AudioDelay() {
		buf = new int[262144];
		pos = 0;
	}
	
	int[] process(int inBuf[], int delay) {
		int outBuf[] = new int[delay];
		for(int i = 0; i < inBuf.length; i ++) {		
			buf[(pos + delay) % buf.length] = inBuf[i];
			outBuf[i] = buf[(pos) % buf.length];
			pos ++;
		}
		return outBuf;
	}
}
