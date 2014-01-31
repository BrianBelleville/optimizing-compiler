package ir.instructions;

import ir.BasicBlock;
import ir.Instruction;
import ir.Opcode;

public class Write extends ir.UnaryInstruction {

    public Write(BasicBlock b, Instruction arg) {
        super(b, Opcode.write, arg);
    }
}
