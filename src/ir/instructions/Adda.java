package ir.instructions;

import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Adda extends BinaryInstruction {

    public Adda(Instruction a1, Instruction a2) {
        super(Opcode.adda, a1, a2);
    }

}
