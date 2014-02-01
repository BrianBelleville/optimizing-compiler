package ir.instructions;

import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Move extends BinaryInstruction {

    public Move(Instruction a1, Instruction a2) {
        super(Opcode.move, a1, a2);
    }

}
