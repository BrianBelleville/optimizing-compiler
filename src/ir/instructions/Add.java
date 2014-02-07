package ir.instructions;

import ir.base.BinaryInstruction;
import ir.base.Opcode;
import ir.base.Value;

public class Add extends BinaryInstruction {

    public Add(Value a1, Value a2) {
        super(Opcode.add, a1, a2);
    }

}

