package ir.instructions;

import ir.BinaryInstruction;
import ir.Value;
import ir.Opcode;

public class Mul extends BinaryInstruction {

    public Mul(Value a1, Value a2) {
        super(Opcode.mul, a1, a2);
    }

}
