package ir.instructions;

import ir.base.BinaryInstruction;
import ir.base.Opcode;
import ir.base.Value;

public class Mul extends BinaryInstruction {

    public Mul(Value a1, Value a2) {
        super(Opcode.mul, a1, a2);
    }

}
