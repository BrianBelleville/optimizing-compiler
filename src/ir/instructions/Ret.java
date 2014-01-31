package ir.instructions;

import ir.BasicBlock;
import ir.Instruction;
import ir.Opcode;
import ir.UnaryInstruction;

public class Ret extends UnaryInstruction {

    public Ret(BasicBlock b, Instruction arg) {
        super(b, Opcode.ret, arg);
    }
}
