package ir;

import ir.base.BinaryInstruction;
import ir.base.Opcode;
import ir.base.Value;

public class Sub extends BinaryInstruction {

    public Sub(Value a1, Value a2) {
        super(Opcode.sub, a1, a2);
    }

}
