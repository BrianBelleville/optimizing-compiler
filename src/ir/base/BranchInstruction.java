package ir.base;


import java.util.ArrayList;

public class BranchInstruction extends Instruction {
    private BasicBlock target;

    public BranchInstruction(Opcode o, BasicBlock target) {
	super(o);
	this.target = target;
    }

    public BasicBlock getTarget() {
	return target;
    }

    @Override
    public void replaceArgument(Value replace, Value i)
	throws Exception {
	throw new Exception("Error, attempt to replace arguments of a branch instruction");
    }

    @Override
    public boolean isCommonSubexpression(Instruction i) {
	return false;		// branches can never be common subexpressions
    }

    @Override
    public void performCSE() {
	return;			// no op, branches can never be common subexpressions
    }

    @Override
    public void delete(Instruction i) throws Exception {
	throw new Exception("Error, attempt to delete a branch instruction");
    }

    @Override
    public ArrayList<Value> getArguments() {
	return null;		// branch instructions real argument is the target
    }

}
