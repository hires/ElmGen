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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.andrewkilpatrick.elmGen.ElmProgram;


public class AudioFileWriter implements AudioSink {	
	RandomAccessFile out = null;
	int sampDataCount = 0;
	
	/**
	 * Creates an AudioFileWriter for writing a raw file.
	 * 
	 * @param filename the filename of the file to write
	 */
	public AudioFileWriter(String filename) {
		try {
			out = new RandomAccessFile(filename, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		byte header[] = new byte[44];
		try {
			out.write(header);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			int c = 0;
			byte header[] = new byte[44];
			// RIFF
			header[c++] = 'R';
			header[c++] = 'I';
			header[c++] = 'F';
			header[c++] = 'F';
			// chunk length
			int len = 44 + sampDataCount - 8;
			header[c++] = (byte)(len & 0x000000ff); 
			header[c++] = (byte)((len & 0x0000ff00) >> 8);
			header[c++] = (byte)((len & 0x00ff0000) >> 16);
			header[c++] = (byte)((len & 0xff000000) >> 24);
			// WAVE
			header[c++] = 'W';
			header[c++] = 'A';
			header[c++] = 'V';
			header[c++] = 'E';
			// format chunk
			header[c++] = 'f';
			header[c++] = 'm';
			header[c++] = 't';
			header[c++] = ' ';
			header[c++] = 0x10;
			header[c++] = 0x00;
			header[c++] = 0x00;
			header[c++] = 0x00;
			header[c++] = 0x01;
			header[c++] = 0x00;
			header[c++] = 0x02;
			header[c++] = 0x00;
			int samplerate = ElmProgram.SAMPLERATE;
			header[c++] = (byte)(samplerate & 0x000000ff); 
			header[c++] = (byte)((samplerate & 0x0000ff00) >> 8);
			header[c++] = (byte)((samplerate & 0x00ff0000) >> 16);
			header[c++] = (byte)((samplerate & 0xff000000) >> 24);
			int bytesec = samplerate * 4;
			header[c++] = (byte)(bytesec & 0x000000ff); 
			header[c++] = (byte)((bytesec & 0x0000ff00) >> 8);
			header[c++] = (byte)((bytesec & 0x00ff0000) >> 16);
			header[c++] = (byte)((bytesec & 0xff000000) >> 24);
			header[c++] = 0x04;
			header[c++] = 0x00;
			header[c++] = 0x10;
			header[c++] = 0x00;
			// data chunk
			header[c++] = 'd';
			header[c++] = 'a';
			header[c++] = 't';
			header[c++] = 'a';
			header[c++] = (byte)(sampDataCount & 0x000000ff); 
			header[c++] = (byte)((sampDataCount & 0x0000ff00) >> 8);
			header[c++] = (byte)((sampDataCount & 0x00ff0000) >> 16);
			header[c++] = (byte)((sampDataCount & 0xff000000) >> 24);
			out.seek(0);
			out.write(header);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeAdc(int[] buf, int len) {
		// not supported
	}
	
	public void writeDac(int[] buf, int len) {
		int outBufCount = 0;
		byte outBuf[] = new byte[len * 2];
		for(int i = 0; i < len; i ++) {
			outBuf[outBufCount ++] = (byte)((buf[i]  & 0x00ff00) >> 8);
			outBuf[outBufCount ++] = (byte)((buf[i] & 0xff0000) >> 16);
		}
		try {
			out.write(outBuf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		sampDataCount += outBufCount;
	}
}
