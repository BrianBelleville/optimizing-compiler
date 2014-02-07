package ir.instructions;

import ir.BinaryInstruction;
import ir.Value;
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
    public Cmp(CmpType type, Value a1, Value a2) {
        super(Opcode.cmp, a1, a2);
	cmpType = type;
    }

    public CmpType getType() {
	return cmpType;
    }
}
