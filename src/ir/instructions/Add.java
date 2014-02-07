package ir.instructions;

import ir.BinaryInstruction;
import ir.Value;
import ir.Opcode;

public class Add extends BinaryInstruction {

    public Add(Value a1, Value a2) {
        super(Opcode.add, a1, a2);
    }

}

