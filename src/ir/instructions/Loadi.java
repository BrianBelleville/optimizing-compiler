package ir.instructions;

import ir.BasicBlock;
import ir.Instruction;
import ir.Opcode;
import ir.UnaryInstruction;

public class Loadi extends UnaryInstruction {

    public Loadi(int num, BasicBlock b, Instruction arg) {
        super(num, b, Opcode.loadi, arg);
    }
}
