package ir;

import java.util.ArrayList;

public class UnaryInstruction extends Instruction {

    private Value argument;

    public UnaryInstruction(Opcode o, Value arg) {
        super( o);
        argument = arg.getSubstitute();
        argument.addUse(this);
    }

    @Override
    public ArrayList<Value> getArguments() {
        ArrayList<Value> r = new ArrayList<Value>();
        r.add(argument);
        return r;
    }

    @Override
    public void replaceArgument(Value replace, Value i) throws Exception {
        if(argument == replace) {
            argument = replace;
        } else {
            throw new Exception("error replacing argument, argument not present in instruction");
        }
    }
}
