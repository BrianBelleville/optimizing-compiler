package ir.instructions;

import ir.NoArgInstruction;
import ir.Opcode;

public class Wln extends NoArgInstruction {

    public Wln(int num) {
        super(num, Opcode.wln);
    }

}
