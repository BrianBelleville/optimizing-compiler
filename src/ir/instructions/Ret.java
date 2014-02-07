package ir.instructions;

import ir.base.Opcode;
import ir.base.UnaryInstruction;
import ir.base.Value;

public class Ret extends UnaryInstruction {

    public Ret(Value arg) {
        super(Opcode.ret, arg);
    }
}
