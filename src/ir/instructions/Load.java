package ir.instructions;

import ir.Instruction;
import ir.Opcode;
import ir.UnaryInstruction;
import support.Identifier;

public class Load extends UnaryInstruction {
    private Identifier variable;
    public Load(Identifier var, Instruction arg) {
	super(Opcode.load, arg);
	variable = var;
    }

    public Identifier getVariable() {
	return variable;
    }
}
