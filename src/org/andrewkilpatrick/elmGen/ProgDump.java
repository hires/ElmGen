package org.andrewkilpatrick.elmGen;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.andrewkilpatrick.elmGen.instructions.Clear;
import org.andrewkilpatrick.elmGen.instructions.Instruction;

public class ProgDump {

	public static void dumpProgram(ElmProgram prog, String outputFilename) throws FileNotFoundException {
		System.out.println("dumping program - len: " + prog.getCodeLen() + 
				" - outputFilename: " + outputFilename);
		PrintWriter out = new PrintWriter(outputFilename);
		// 
		for(int i = 0; i < prog.getCodeLen(); i ++) {
			Instruction inst = prog.getInstruction(i);
			String word = String.format("%08x", inst.getHexWord());
			String instStr = inst.getInstructionString();
			String outStr = word + "  // " + instStr;
			System.out.println(outStr);
			out.println(outStr);
		}
		// pad the file with blank
		for(int i = prog.getCodeLen(); i < 128; i ++) {
			Instruction inst = new Clear();
			String word = String.format("%08x", inst.getHexWord());
			String instStr = inst.getInstructionString();
			String outStr = word + "  // " + instStr;
			System.out.println(outStr);
			out.println(outStr);			
		}
		out.close();
	}
}
