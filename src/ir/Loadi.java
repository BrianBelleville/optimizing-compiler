package ir;

import ir.base.Opcode;
import ir.base.UnaryInstruction;

public class Loadi extends UnaryInstruction {

    public Loadi(Immediate arg) {
        super(Opcode.loadi, arg);
    }
}
