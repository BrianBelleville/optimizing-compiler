package ir.instructions;

import ir.BasicBlock;
import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Div extends BinaryInstruction {

    public Div(BasicBlock b, Instruction a1, Instruction a2) {
        super(b, Opcode.div, a1, a2);
    }

}
