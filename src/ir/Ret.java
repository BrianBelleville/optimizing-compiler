package ir;

import ir.base.Instruction;
import ir.base.Opcode;
import ir.base.UnaryInstruction;
import ir.base.Value;

public class Ret extends UnaryInstruction {

    public Ret(Value arg) {
        super(Opcode.ret, arg);
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
