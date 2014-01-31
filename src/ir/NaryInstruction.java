package ir;

import java.util.ArrayList;

public class NaryInstruction extends Instruction {

    private ArrayList<Instruction> arguments;

    public NaryInstruction(BasicBlock b, Opcode o,
                           ArrayList<Instruction> args) {
        super( b, o);
        arguments = new ArrayList<Instruction>();
        for(Instruction i: args) {
            Instruction a = i.getDeleted();
            arguments.add(a);
            a.addUse(this);
        }
    }

    @Override
    public ArrayList<Instruction> getArguments() {
        return arguments;
    }

    @Override
    public void replaceArgument(Instruction replace, Instruction i)
        throws Exception {
        int index = arguments.indexOf(i);
        if(index == -1) {
            throw new Exception("error replacing argument, argument not present in instruction");
        }
        arguments.set(index, i);
    }

}
