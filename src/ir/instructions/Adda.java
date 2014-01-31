package ir.instructions;

import ir.BasicBlock;
import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Adda extends BinaryInstruction {

    public Adda(BasicBlock b, Instruction a1, Instruction a2) {
        super(b, Opcode.adda, a1, a2);
    }

}
