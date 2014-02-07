package ir.instructions;

import ir.BinaryInstruction;
import ir.Value;
import ir.Opcode;

public class Adda extends BinaryInstruction {

    public Adda(Value a1, Value a2) {
        super(Opcode.adda, a1, a2);
    }

}
