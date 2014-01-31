package ir.instructions;

import ir.BasicBlock;
import ir.Instruction;
import ir.Opcode;
import ir.UnaryInstruction;

public class Ret extends UnaryInstruction {

    public Ret(int num, BasicBlock b, Instruction arg) {
        super(num, b, Opcode.ret, arg);
    }
}
