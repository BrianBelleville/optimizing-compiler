package ir.base;


import java.util.ArrayList;

public class BinaryInstruction extends Instruction {

    private Value arg1;
    private Value arg2;

    public BinaryInstruction(Opcode o, Value a1, Value a2) {
        super(o);
        arg1 = a1.getSubstitute();
        arg2 = a2.getSubstitute();
        arg1.addUse(this);
        arg2.addUse(this);
    }

    @Override
    public ArrayList<Value> getArguments() {
        ArrayList<Value> r = new ArrayList<Value>();
        r.add(arg1);
        r.add(arg2);
        return r;
    }

    @Override
    public void replaceArgument(Value replace, Value i)
        throws Exception {
        boolean found = false;
        if(arg1.equals(replace)) {
            arg1 = i;
            found = true;
        }
        if(arg2.equals(replace)) {
            arg2 = i;
            found = true;
        }
        if(!found) {
            throw new Exception("error replacing argument, argument not present in instruction");
        }
    }

}