package ir.instructions;

import ir.base.NoArgInstruction;
import ir.base.Opcode;

public class End extends NoArgInstruction {

    public End(){
        super(Opcode.end);
    }

}
