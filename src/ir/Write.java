package ir;

import ir.base.Opcode;
import ir.base.Value;

public class Write extends ir.base.UnaryInstruction {

    public Write(Value arg) {
        super(Opcode.write, arg);
    }
}
