package ir.instructions;

import ir.BasicBlock;
import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;
import support.Identifier;

public class Store extends BinaryInstruction {
    private Identifier variable;
    public Store(BasicBlock b, Identifier var, Instruction a1, Instruction a2) {
	super(b, Opcode.store, a1, a2);
	variable = var;
    }

    public Identifier getVariable() {
	return variable;
    }
}
