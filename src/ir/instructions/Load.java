package ir.instructions;

import ir.BasicBlock;
import ir.Instruction;
import ir.Opcode;
import ir.UnaryInstruction;
import support.Identifier;

public class Load extends UnaryInstruction {
    private Identifier variable;
    public Load(BasicBlock b, Identifier var, Instruction arg) {
	super(b, Opcode.load, arg);
	variable = var;
    }

    public Identifier getVariable() {
	return variable;
    }
}
