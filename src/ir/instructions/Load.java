package ir.instructions;

import ir.BasicBlock;
import ir.Instruction;
import ir.Opcode;
import ir.UnaryInstruction;

public class Load extends UnaryInstruction {

    public Load(BasicBlock b, Instruction arg) {
        super(b, Opcode.load, arg);
    }
}
