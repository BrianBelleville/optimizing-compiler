package ir.instructions;

import ir.Opcode;
import ir.NoArgInstruction;

public class Immediate extends NoArgInstruction {

    public Immediate(){
        super(Opcode.immediate);
    }

}
