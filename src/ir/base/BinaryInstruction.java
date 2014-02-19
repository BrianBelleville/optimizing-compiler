package ir.base;


import java.util.ArrayList;

public class BinaryInstruction extends Instruction {

    private Value arg1;
    private Value arg2;

    public BinaryInstruction(Opcode o, Value a1, Value a2) {
        super(o);
        arg1 = a1;
        arg2 = a2;
    }

    public Value getArg1() {
        return arg1;
    }

    public Value getArg2() {
        return arg2;
    }

    @Override
    public boolean isCommonSubexpression(Instruction i) {
        if(i instanceof BinaryInstruction) {
            BinaryInstruction b = (BinaryInstruction)i;
            return this.getOpcode() == b.getOpcode()
                && this.arg1.equals(b.arg1)
                && this.arg2.equals(b.arg2);
        }
        return false;
    }

    @Override
    public ArrayList<Value> getArguments() {
        ArrayList<Value> r = new ArrayList<Value>();
        r.add(arg1);
        r.add(arg2);
        return r;
    }

    @Override
    public void replaceArgument(Value replace, Value i, boolean throwOnError)
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
        if(!found && throwOnError) {
            throw new Exception("error replacing argument, argument not present in instruction");
        }
    }

}
