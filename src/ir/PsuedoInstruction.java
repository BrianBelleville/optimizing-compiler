package ir;

import java.util.ArrayList;

// this class is to represent things that aren't actually
// instructions, but need to be substituted for instructions ins some
// cases, such as immediate values in arithmetic operations, or
// addresses for loads and stores.
public class PsuedoInstruction extends Instruction {
    // a PsuedoInstruction should not actually be emited into a basic
    // block, it has the instruction interface so that it may be used
    // as an argument for other instructions.
    public PsuedoInstruction(int num, Opcode o) {
	super(num, null, o);
    }

    @Override
    public ArrayList<Instruction> getArguments() {
	return null;
    }

    @Override
    public void replaceArgument(Instruction replace, Instruction i)
	throws Exception {
	throw new Exception("Error, attempt to replace the argument of a psuedo instruction");
    }
}
