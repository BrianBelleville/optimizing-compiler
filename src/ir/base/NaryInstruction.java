package ir.base;


import java.util.ArrayList;

public class NaryInstruction extends Instruction {

    private ArrayList<Value> arguments;

    public NaryInstruction(Opcode o, ArrayList<Value> args) {
        super( o);
        arguments = new ArrayList<Value>();
        for(Value v: args) {
            Value a = v.getSubstitute();
            arguments.add(a);
            a.addUse(this);
        }
    }

    @Override
    public ArrayList<Value> getArguments() {
        return arguments;
    }

    @Override
    public void replaceArgument(Value replace, Value nval, boolean throwOnError)
        throws Exception {
	boolean found = false;
	int s = arguments.size();
	for(int i = 0; i < s; i++) {
	    if(arguments.get(i).equals(replace)) {
		arguments.set(i, nval);
		found = true;
	    }
	}
	if(throwOnError && !found) {
            throw new Exception("error replacing argument, argument not present in instruction");
        }
    }

}
