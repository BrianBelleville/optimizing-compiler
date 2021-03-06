package ir;

import java.io.Writer;
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
    public void printInstruction(Writer w) throws Exception {
	super.printInstruction(w);
	w.write(" " + getTarget().getNodeName());
    }

    @Override
    public void replaceArgument(Value replace, Value i, boolean throwOnError)
	throws Exception {
	if(throwOnError) {
	    throw new Exception("Error, attempt to replace arguments of a branch instruction");
	}
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
    public void delete(Value i) throws Exception {
	throw new Exception("Error, attempt to delete a branch instruction");
    }

    @Override
    public ArrayList<Value> getArguments() {
	return new ArrayList<Value>();		// branch instructions real argument is the target
    }

    @Override
    public boolean isDeadCode() {
        return false;           // can not be dead code
    }

    @Override
    public boolean needsRegister() {
        return false;
    }
}
