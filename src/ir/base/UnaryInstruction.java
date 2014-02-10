package ir.base;


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
    public void replaceArgument(Value replace, Value i, boolean throwOnError) throws Exception {
        if(argument.equals(replace)) {
            argument = replace;
        } else if (throwOnError) {
            throw new Exception("error replacing argument, argument not present in instruction");
        }
    }
}
