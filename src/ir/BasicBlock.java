package ir;

import java.util.ArrayList;

public class BasicBlock {
    public BasicBlock dominator;
    public ArrayList<Instruction> instructions;
    private BasicBlock fallThrough;
    public BasicBlock getFallThrough() {
        return fallThrough;
    }
    public BasicBlock getBranchTarget() {
        // todo: get the branch target of the last instruction
        // a branch instruction will always end a basic block,
        // but not every basic block must end with a branch
        return null;
    }
    public void emit(Instruction i) {
        instructions.add(i);
        i.performCSE();
    }
}
