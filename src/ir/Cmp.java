package ir;

import ir.base.BinaryInstruction;
import ir.base.Opcode;
import ir.base.Value;

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
