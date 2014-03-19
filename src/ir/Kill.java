package ir;

import support.Identifier;

public class Kill extends NoArgInstruction {
    private Identifier varname;
    public Kill(Identifier var) {
        super(Opcode.kill);
        varname = var;
    }

    public Identifier getVariable() {
        return varname;
    }

    @Override
    public boolean isCommonSubexpression(Instruction i) {
	return false;		// can never be common subexpressions
    }

    @Override
    public void performCSE() {
	return;			// no op, can never be common subexpressions
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
    
