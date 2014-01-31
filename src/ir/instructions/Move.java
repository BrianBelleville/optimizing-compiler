package ir.instructions;

import ir.BasicBlock;
import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Move extends BinaryInstruction {

    public Move(BasicBlock b, Instruction a1, Instruction a2) {
        super(b, Opcode.move, a1, a2);
    }

}
