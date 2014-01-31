package ir.instructions;

import ir.Opcode;
import ir.NoArgInstruction;

public class End extends NoArgInstruction {

    public End(){
        super(Opcode.end);
    }

}
