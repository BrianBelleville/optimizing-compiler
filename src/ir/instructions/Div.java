package ir.instructions;

import ir.BasicBlock;
import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Div extends BinaryInstruction {

    public Div(int num, BasicBlock b, Instruction a1, Instruction a2) {
        super(num, b, Opcode.div, a1, a2);
    }

}
