package ir;

import java.util.ArrayList;
// todo: make setting the dominator part of the constructor
// todo: have a table containing the most recent instance of a
//       opcode, this can be used to perform CSE faster
// todo: have a separate option for adding phi instructions,
//       maybe just search for a phi with the on the correct identifier
//       and allow the parser to modify that instruction.
// todo: maybe have a special basic block for a loop header that will
//       search through it's instructions and instructions it dominates
//       in order to replace variable uses when a phi is added.
public class BasicBlock {
    private BasicBlock dominator;
    public ArrayList<Instruction> instructions;
    private BasicBlock fallThrough;

    public BasicBlock(BasicBlock dominator) {
	this.dominator = dominator;
    }

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
