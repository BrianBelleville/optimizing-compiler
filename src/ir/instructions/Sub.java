package ir.instructions;

import ir.BinaryInstruction;
import ir.Value;
import ir.Opcode;

public class Sub extends BinaryInstruction {

    public Sub(Value a1, Value a2) {
        super(Opcode.sub, a1, a2);
    }

}
