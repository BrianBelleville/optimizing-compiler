package ir.instructions;

import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Div extends BinaryInstruction {

    public Div(Instruction a1, Instruction a2) {
        super(Opcode.div, a1, a2);
    }

}
