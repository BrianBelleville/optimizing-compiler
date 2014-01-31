package ir.instructions;

import ir.BasicBlock;
import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Move extends BinaryInstruction {

    public Move(int num, BasicBlock b, Instruction a1, Instruction a2) {
        super(num, b, Opcode.move, a1, a2);
    }

}
