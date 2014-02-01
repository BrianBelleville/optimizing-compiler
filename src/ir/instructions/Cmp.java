package ir.instructions;

import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Cmp extends BinaryInstruction {
    public enum CmpType {
	eq,
	neq,
	lt,
	lte,
	gt,
	gte
    }
    private CmpType cmpType;
    public Cmp(CmpType type, Instruction a1, Instruction a2) {
        super(Opcode.cmp, a1, a2);
	cmpType = type;
    }

    public CmpType getType() {
	return cmpType;
    }
}
