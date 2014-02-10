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
    public void replaceArgument(Value replace, Value i, boolean throwOnError)
        throws Exception {
        int index = arguments.indexOf(i);
        if(index == -1 && throwOnError) {
            throw new Exception("error replacing argument, argument not present in instruction");
        }
        arguments.set(index, i);
    }

}
