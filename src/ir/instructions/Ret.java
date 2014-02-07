package ir.instructions;

import ir.Value;
import ir.Opcode;
import ir.UnaryInstruction;

public class Ret extends UnaryInstruction {

    public Ret(Value arg) {
        super(Opcode.ret, arg);
    }
}
