package ir.instructions;

import ir.BinaryInstruction;
import ir.Value;
import ir.Opcode;
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
}
