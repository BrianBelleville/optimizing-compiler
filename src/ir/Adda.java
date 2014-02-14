package ir;

import ir.base.Opcode;
import ir.base.OrderIndependentBinaryInstruction;
import ir.base.Value;

public class Adda extends OrderIndependentBinaryInstruction {

    public Adda(Value a1, Value a2) {
        super(Opcode.adda, a1, a2);
    }

}
