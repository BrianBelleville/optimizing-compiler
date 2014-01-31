package ir.instructions;

import ir.BasicBlock;
import ir.Instruction;
import ir.Opcode;
import ir.UnaryInstruction;

public class Loadi extends UnaryInstruction {

    public Loadi(BasicBlock b, Instruction arg) {
        super(b, Opcode.loadi, arg);
    }
}
