package ir;

import ir.base.Opcode;
import ir.base.UnaryInstruction;
import ir.base.Value;
import support.Identifier;

public class Load extends UnaryInstruction {
    private Identifier variable;
    public Load(Identifier var, Value arg) {
	super(Opcode.load, arg);
	variable = var;
    }

    public Identifier getVariable() {
	return variable;
    }
}
