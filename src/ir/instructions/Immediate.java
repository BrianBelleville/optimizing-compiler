package ir.instructions;

import ir.Opcode;
import ir.NoArgInstruction;

public class Immediate extends NoArgInstruction {
    private int value;

    public Immediate(int value){
	super(Opcode.immediate);
	this.value = value;
    }

    public int getValue() {
	return value;
    }
}
