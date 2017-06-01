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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.andrewkilpatrick.elmGen.ElmProgram;


public class AudioCardOutput implements AudioSink {
	SourceDataLine line = null;
	
	public AudioCardOutput() throws LineUnavailableException {
		AudioFormat audioFormat = new AudioFormat(ElmProgram.SAMPLERATE, 
				16, 2, true, false);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		line = (SourceDataLine)AudioSystem.getLine(info);
		line.open(audioFormat);
		line.start();
	}

	public void writeAdc(int buf[], int len) {
		// not implemented
	}
	
	public void writeDac(int buf[], int len) {
		if(len < 1 || len > buf.length) {
			return;
		}
		byte outBuf[] = new byte[len * 2];
		int outBufCount = 0;
		for(int i = 0; i < len; i ++) {
			outBuf[outBufCount ++] = (byte)((buf[i]  & 0x00ff00) >> 8);
			outBuf[outBufCount ++] = (byte)((buf[i] & 0xff0000) >> 16);
		}
		line.write(outBuf, 0, outBufCount);
	}
	
	public void close() {
		line.drain();
		line.close();
	}
}
