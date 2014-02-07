package ir.instructions;

import ir.base.BinaryInstruction;
import ir.base.Opcode;
import ir.base.Value;

public class Adda extends BinaryInstruction {

    public Adda(Value a1, Value a2) {
        super(Opcode.adda, a1, a2);
    }

}
