package ir.base;


import ir.BasicBlock;

import java.util.ArrayList;

public abstract class Instruction extends Value {
    private static int instructionNum = 0;

    public BasicBlock bb;
    private int number;
    private Instruction deleted;
    private Instruction dominating;

    private Opcode opcode;
    public ArrayList<Instruction> uses;

    abstract public ArrayList<Value> getArguments();

    public Instruction(Opcode o) {
        number = getNextInstructionNum();
        opcode = o;
        uses = new ArrayList<Instruction>();
    }
    
    // this is obviously horribly not thread safe, but doesn't matter
    // for this
    private final int getNextInstructionNum() {
	instructionNum += 1;
	return instructionNum;
    }

    public final void setDominating(Instruction i) {
        dominating = i;
    }

    public final void setContainingBB(BasicBlock b) {
	bb = b;
    }

    public final int getNumber() {
        return number;
    }

    // get the instruction that should be used in place of this
    // instruction. If this instruction has been deleted, return the
    // instruction it has been deleted for
    public final Value getSubstitute() {
	Instruction r = this;
        while(r.deleted != null) {
             r = r.deleted;
        }
        return r;
    }

    public final boolean isDeleted() {
        return deleted != null;
    }

    public final Opcode getOpcode() {
        return opcode;
    }

    public final void addUse(Instruction i) {
        uses.add(i);
    }

    // delete this instruction in favor of instruction i, in other
    // words perform common subexpression elimination.
    public void delete(Instruction i) throws Exception {
        deleted = i;

        // this may not be necessary, depending on how I generate my
        // SSA, and when I perform CSE, also may not be necessary if I
        // just make sure to not emit deleted instructions, and if an
        // argument is marked as deleted, use that instead.
        for(Instruction use : uses) {
            use.replaceArgument(this, i);
        }
    }

    public boolean isCommonSubexpression(Instruction i) {
        return this.opcode == i.opcode && this.getArguments().equals(i.getArguments());
    }

    public void performCSE() {
        Instruction i = dominating;
        while(i != null) {
            if(this.isCommonSubexpression(i)) {
                this.deleted = (Instruction)i.getSubstitute(); // should always be an instruction, not ideal
                return;
            }
            i = i.dominating;
        }
    }

    // replace the argument that is eq to replace with i, this will
    // need to be done if replace has been deleted for i.
    abstract public void replaceArgument(Value replace, Value i) throws Exception;
}

