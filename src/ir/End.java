package ir;

import ir.base.Instruction;
import ir.base.NoArgInstruction;
import ir.base.Opcode;

public class End extends NoArgInstruction {

    public End(){
        super(Opcode.end);
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
}
