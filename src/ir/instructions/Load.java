package ir.instructions;

import ir.BasicBlock;
import ir.Instruction;
import ir.Opcode;
import ir.UnaryInstruction;

public class Load extends UnaryInstruction {

    public Load(int num, BasicBlock b, Instruction arg) {
        super(num, b, Opcode.load, arg);
    }
}
