package ir;

import ir.base.Opcode;
import ir.base.OrderIndependentBinaryInstruction;
import ir.base.Value;

public class Mul extends OrderIndependentBinaryInstruction {

    public Mul(Value a1, Value a2) {
        super(Opcode.mul, a1, a2);
    }

}
