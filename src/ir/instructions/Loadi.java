package ir.instructions;

import ir.Opcode;
import ir.UnaryInstruction;

public class Loadi extends UnaryInstruction {

    public Loadi(Immediate arg) {
        super(Opcode.loadi, arg);
    }
}
