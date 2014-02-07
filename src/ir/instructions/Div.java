package ir.instructions;

import ir.BinaryInstruction;
import ir.Value;
import ir.Opcode;

public class Div extends BinaryInstruction {

    public Div(Value a1, Value a2) {
        super(Opcode.div, a1, a2);
    }

}
