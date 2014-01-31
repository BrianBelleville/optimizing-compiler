package ir.instructions;

import ir.BasicBlock;
import ir.Instruction;
import ir.Opcode;

public class Write extends ir.UnaryInstruction {

    public Write(int num, BasicBlock b, Instruction arg) {
        super(num, b, Opcode.write, arg);
    }
}
