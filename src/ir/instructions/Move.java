package ir.instructions;

import ir.BinaryInstruction;
import ir.Value;
import ir.Opcode;

public class Move extends BinaryInstruction {

    public Move(Value a1, Value a2) {
        super(Opcode.move, a1, a2);
    }

}
