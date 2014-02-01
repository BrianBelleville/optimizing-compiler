package ir;

import java.util.ArrayList;

public class NoArgInstruction extends Instruction {
    public NoArgInstruction(Opcode o) {
	super(o);
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
