package ir;

import ir.base.Opcode;
import ir.base.UnaryInstruction;
import ir.base.Value;
import support.Identifier;

public class Load extends UnaryInstruction {
    private Identifier variable;
    private boolean global;
    public Load(Identifier var, boolean global, Value arg) {
	super(Opcode.load, arg);
        variable = var;
        this.global = global;
    }

    public Identifier getVariable() {
	return variable;
    }
}
