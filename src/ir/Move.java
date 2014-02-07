package ir;

import ir.base.BinaryInstruction;
import ir.base.Opcode;
import ir.base.Value;

public class Move extends BinaryInstruction {

    public Move(Value a1, Value a2) {
        super(Opcode.move, a1, a2);
    }

}
