package ir;

import ir.base.Instruction;
import ir.base.Opcode;
import ir.base.Value;

public class Write extends ir.base.UnaryInstruction {

    public Write(Value arg) {
        super(Opcode.write, arg);
    }
    
    @Override
    public boolean isCommonSubexpression(Instruction i) {
	return false;		// can never be common subexpressions
    }

    @Override
    public void performCSE() {
	return;			// no op, can never be common subexpressions
    }

}
