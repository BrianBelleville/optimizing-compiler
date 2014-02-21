package ir.base;


import ir.BasicBlock;
import java.io.Writer;
import java.util.ArrayList;

public abstract class Instruction extends Value {
    private static int instructionNum = 0;

    private BasicBlock bb;
    private int number;
    private Value deleted;
    private Instruction dominating;
    private Opcode opcode;
    public ArrayList<Instruction> uses;

    abstract public ArrayList<Value> getArguments();

    public Instruction(Opcode o) {
        number = getNextInstructionNum();
        opcode = o;
        uses = new ArrayList<Instruction>();
    }

    @Override
    public void printAsArg(Writer w) throws Exception {
        if(isDeleted()) {
            throw new Exception("Deleted instruction used as an argument");
        }
	w.write(" (" + Integer.toString(number) + ")");
    }

    public void printInstruction(Writer w) throws Exception {
	w.write(Integer.toString(number) + ": " + opcode.toString());
	ArrayList<Value> args = getArguments();
	for(Value v : args) {
	    v.printAsArg(w);
	}
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

    public final Instruction getDominating() {
        return dominating;
    }

    public final void setContainingBB(BasicBlock b) {
	bb = b;
    }

    public final BasicBlock getContainingBB() {
        return bb;
    }

    public final int getNumber() {
        return number;
    }

    // get the instruction that should be used in place of this
    // instruction. If this instruction has been deleted, return the
    // instruction it has been deleted for
    @Override
    public final Value getSubstitute() throws Exception {
        return isDeleted() ? deleted : this;
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
    public void delete(Value i) throws Exception {
        if(isDeleted()) {
            throw new Exception("Attempt to delete an instruction twice");
        }            
        // make sure if i has already been deleted we delete ourselves
        // with whatever i was deleted for
        deleted = i.getSubstitute(); 
    }

    public boolean isCommonSubexpression(Instruction i) {
        return this.opcode == i.opcode && this.getArguments().equals(i.getArguments());
    }

    public void performCSE() throws Exception {
        Instruction i = dominating;
        while(i != null) {
            if(this.isCommonSubexpression(i)) {
                this.delete(i.getSubstitute());
                return;
            }
            i = i.dominating;
        }
    }

    public void getArgumentSubstitutes() throws Exception {
        ArrayList<Value> args = getArguments();
        for(Value v : args) {
            replaceArgument(v, v.getSubstitute());            
        }
    }

    // replace the argument that is eq to replace with i, this will
    // need to be done if replace has been deleted for i.
    abstract public void replaceArgument(Value replace, Value i, boolean throwOnError) throws Exception;
    public void replaceArgument(Value replace, Value i) throws Exception {
	replaceArgument(replace, i, false);
    }

    public void removeVariableReferenceArguments() throws Exception {
        ArrayList<Value> args = getArguments();
        for(Value v : args) {
            // the argument should always be there, but we can't throw
            // on error since it may be present multiple times, and
            // the first replace would remove it, the second replace
            // would then result in an error
            replaceArgument(v, v.stripVariableReference());
        }
    }
}

