package ir.instructions;

import ir.base.BinaryInstruction;
import ir.base.Opcode;
import ir.base.Value;

public class Div extends BinaryInstruction {

    public Div(Value a1, Value a2) {
        super(Opcode.div, a1, a2);
    }

}
