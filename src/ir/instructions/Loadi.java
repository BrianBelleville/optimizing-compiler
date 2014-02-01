package ir.instructions;

import ir.Instruction;
import ir.Opcode;
import ir.UnaryInstruction;

public class Loadi extends UnaryInstruction {

    public Loadi(Instruction arg) {
        super(Opcode.loadi, arg);
    }
}
