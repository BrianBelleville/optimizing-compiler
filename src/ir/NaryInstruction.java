package ir;


import java.util.ArrayList;

public class NaryInstruction extends Instruction {

    private ArrayList<Value> arguments;

    public NaryInstruction(Opcode o, ArrayList<Value> args) {
        super( o);
        arguments = new ArrayList<Value>();
        for(Value v: args) {
            arguments.add(v);
        }
    }

    @Override
    public ArrayList<Value> getArguments() {
        ArrayList<Value> rval = new ArrayList<Value>(arguments.size());
        for(Value v : arguments) {
            rval.add(v);
        }
        return rval;
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
