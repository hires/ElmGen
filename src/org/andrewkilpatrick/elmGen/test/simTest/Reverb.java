/*
 * This code is a port of mini-rev1.spn from the
 * Spin Semiconductors website. 	
 */
package org.andrewkilpatrick.elmGen.test.simTest;

import org.andrewkilpatrick.elmGen.ElmProgram;

public class Reverb extends ElmProgram {
	
	/**
	 * Creates a reverb effect. A port of the 
	 * mini-rev1.spn effect from Spin Semi.
	 */
	public Reverb() {
		super("Reverb");
		setSamplerate(48000);
		clear();
		
		allocDelayMem("api1", 122);
		allocDelayMem("api2", 303);
		allocDelayMem("api3", 553);
		allocDelayMem("api4", 922);
		
		allocDelayMem("ap1", 3823);
		allocDelayMem("del1", 8500);

		allocDelayMem("ap2", 4732);
		allocDelayMem("del2", 7234);

		// coefficients
		double krt = 0.7;  // reverb time
		double kap = 0.625;  // AP coeff
		
		int apout = REG0;  // holding reg input AP signal

		// input all-passes
		readRegister(ADCL, 0.25);  // read left 25%
		readRegister(ADCR, 0.25);  // read right 25%

		readDelay("api1", 1.0, kap);  // read from the end of api1
		writeAllpass("api1", 0.0, -1.0);  // write back in inverted
		
		readDelay("api2", 1.0, kap);  // read from the end of api1
		writeAllpass("api2", 0.0, -1.0);  // write back in inverted

		readDelay("api3", 1.0, kap);  // read from the end of api1
		writeAllpass("api3", 0.0, -1.0);  // write back in inverted

		readDelay("api4", 1.0, kap);  // read from the end of api1
		writeAllpass("api4", 0.0, -1.0);  // write back in inverted

		writeRegister(apout, 1.0);  // write apout, keep ACC
		
		// first loop delay
		readDelay("del2", 1.0, krt);  // read del2, scale by krt
		readDelay("ap1", 1.0, -kap);  // do loop ap
		writeAllpass("ap1", 0.0, kap);
		writeDelay("del1", 0.0, 1.99);
		writeRegister(DACL, 0.0);
	
		// second loop delay
		readRegister(apout, 1.0);
		readDelay("del1", 1.0, krt);  // read del2, scale by krt
		readDelay("ap2", 1.0, -kap);  // do loop ap
		writeAllpass("ap2", 0.0, kap);
		writeDelay("del2", 0.0, 1.99);
		writeRegister(DACR, 0.0);
	}

}
