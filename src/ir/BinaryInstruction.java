package ir;

import java.util.ArrayList;

public class BinaryInstruction extends Instruction {

    private Instruction arg1;
    private Instruction arg2;

    public BinaryInstruction(int num, BasicBlock b, Opcode o, Instruction a1, Instruction a2) {
        super(num, b, o);
        arg1 = a1.getDeleted();
        arg2 = a2.getDeleted();
        arg1.addUse(this);
        arg2.addUse(this);
    }

    @Override
    public ArrayList<Instruction> getArguments() {
        ArrayList<Instruction> r = new ArrayList<Instruction>();
        r.add(arg1);
        r.add(arg2);
        return r;
    }

    @Override
    public void replaceArgument(Instruction replace, Instruction i)
        throws Exception {
        boolean found = false;
        if(arg1 == replace) {
            arg1 = i;
            found = true;
        }
        if(arg2 == replace) {
            arg2 = i;
            found = true;
        }
        if(!found) {
            throw new Exception("error replacing argument, argument not present in instruction");
        }
    }

}
