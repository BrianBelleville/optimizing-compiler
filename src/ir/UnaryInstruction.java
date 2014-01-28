package ir;

import java.util.ArrayList;

public class UnaryInstruction extends Instruction {

    private Instruction argument;

    public UnaryInstruction(int num, BasicBlock b, Opcode o, Instruction arg) {
        super(num, b, o);
        argument = arg.getDeleted();
        argument.addUse(this);
    }

    @Override
    public ArrayList<Instruction> getArguments() {
        ArrayList<Instruction> r = new ArrayList<Instruction>();
        r.add(argument);
        return r;
    }

    @Override
    public void replaceArgument(Instruction replace, Instruction i) throws Exception {
        if(argument == replace) {
            argument = replace;
        } else {
            throw new Exception("error replacing argument, argument not present in instruction");
        }
    }
}
