package ir;

import java.util.ArrayList;

public class NoArgInstruction extends Instruction {
    public NoArgInstruction(Opcode o) {
	super(o);
    }

    @Override
    public ArrayList<Value> getArguments() {
	return null;
    }

    @Override
    public void replaceArgument(Value replace, Value i)
	throws Exception {
	throw new Exception("Error, attempt to replace the argument of a psuedo instruction");
    }
}
