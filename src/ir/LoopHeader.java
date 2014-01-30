package ir;

// the difference between this and a normal BasicBlock is that this
// one will search through all of the instructions that it dominates
// when a phi is added to replace the variable used in the phi.
public class LoopHeader extends BasicBlock {
    public LoopHeader(BasicBlock dominator) {
	super(dominator);
    }
}
