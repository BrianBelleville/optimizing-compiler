package ir.instructions;

import ir.base.BinaryInstruction;
import ir.base.Opcode;
import ir.base.Value;
import support.Identifier;

public class Phi extends BinaryInstruction {
    private Identifier variable;
    public Phi(Identifier var, Value a1, Value a2) {
        super(Opcode.phi, a1, a2);
	variable = var;
    }

    public Identifier getVariable() {
	return variable;
    }
}
