package ir;


public class Bra extends BranchInstruction {

    public Bra(BasicBlock target) {
        super(Opcode.bra, target);
    }
}
