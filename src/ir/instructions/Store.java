package ir.instructions;

import ir.BasicBlock;
import ir.BinaryInstruction;
import ir.Instruction;
import ir.Opcode;

public class Store extends BinaryInstruction {

    public Store(BasicBlock b, Instruction a1, Instruction a2) {
        super(b, Opcode.store, a1, a2);
    }

}
