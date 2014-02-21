package ir;

import ir.base.Instruction;
import ir.base.NoArgInstruction;
import ir.base.Opcode;

public class Wln extends NoArgInstruction {

    public Wln(){
        super(Opcode.wln);
    }

    @Override
    public boolean isCommonSubexpression(Instruction i) {
	return false;		// can never be common subexpressions
    }

    @Override
    protected void performCSE() {
	return;			// no op, can never be common subexpressions
    }
    
}
