package ir.base;


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
    public void replaceArgument(Value replace, Value i, boolean throwOnError)
	throws Exception {
	if(throwOnError) {
	    throw new Exception("Error, attempt to replace the argument of a no arg instruction");
	}
    }
}
