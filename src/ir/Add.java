package ir;

import ir.base.Opcode;
import ir.base.OrderIndependentBinaryInstruction;
import ir.base.Value;

public class Add extends OrderIndependentBinaryInstruction {

    public Add(Value a1, Value a2) {
        super(Opcode.add, a1, a2);
    }

}

