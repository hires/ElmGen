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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.andrewkilpatrick.elmGen.util.Util;


public class LevelLogger implements AudioSink {
	JFrame frame;
	JPanel panel;
	int windowCount = 0;
	double maxL = 0.0;
	double maxR = 0.0;
	int xPos = 0;
	int oldL = -96;
	int oldR = -96;
	
	AudioDelay delay;
	
	public LevelLogger() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame = new JFrame("ElmGen - Level Logger");
				frame.setLocation(300, 0);
				panel = new JPanel();
				panel.setPreferredSize(new Dimension(600, 200));
				frame.getContentPane().add(panel);
				frame.pack();
				frame.setVisible(true);			
			}
		});
		delay = new AudioDelay();
	}

	public void close() {
		
	}

	public void writeAdc(int[] buf, int len) {
		
	}
	
	public void writeDac(int[] buf, int len) {
		int dbuf[] = delay.process(buf, 50000);
	
		for(int i = 0; i < len; i += 2) {			
			double left = Math.abs(Util.regToDouble(dbuf[i]));
			double right = Math.abs(Util.regToDouble(dbuf[i + 1]));
			
			if(left > maxL) {
				maxL = left;
			}
			if(right > maxR) {
				maxR = right;
			}
			
			windowCount ++;
			if(windowCount == 512) {
				updateLevels();
				windowCount = 0;
//				maxL = 0.0;
//				maxR = 0.0;
			}
			maxL *= 0.999;
			maxR *= 0.999;
		}
	}

	private void updateLevels() {
		Graphics2D g2 = (Graphics2D) panel.getGraphics();
		if(g2 == null) return;
		if(xPos < 10) {
			g2.setColor(Color.BLACK);
			g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());
		}
		
		int newL = (int)sampleToDB(maxL);
		int newR = (int)sampleToDB(maxR);
		if(newL < -96) newL = -96;
		if(newR < -96) newR = -96;
		
//		double db = sampleToDB(maxL);
//		System.out.println("dB - L: " + newL + " - R: " + newR);
//		System.out.println("height: " + heightL);
//		g2.setColor(Color.BLACK);
//		g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());
		
		g2.setColor(Color.MAGENTA);
		g2.drawLine(xPos, -(oldL * 2), xPos + 1, -(newL * 2));
		g2.setColor(Color.CYAN);
		g2.drawLine(xPos, -(oldR * 2), xPos + 1, -(newR * 2));
		oldL = newL;
		oldR = newR;
		xPos ++;
		if(xPos == panel.getWidth()) {
			xPos = 0;
		}
	}
		
	private double sampleToDB(double sampleLevel) {
		return 20 * Math.log10(sampleLevel);
	}

}
