package ir;

import ir.base.BinaryInstruction;
import ir.base.Instruction;
import ir.base.Opcode;
import ir.base.Value;
import support.Identifier;

public class Store extends BinaryInstruction {
    private Identifier variable;
    public Store(Identifier var, Value a1, Value a2) {
	super(Opcode.store, a1, a2);
	variable = var;
    }

    public Identifier getVariable() {
	return variable;
    }

    @Override
    public boolean isCommonSubexpression(Instruction i) {
	return false;		// stores can never be common subexpressions
    }

    @Override
    public void performCSE() {
	return;			// no op, stores can never be common subexpressions
    }

}
