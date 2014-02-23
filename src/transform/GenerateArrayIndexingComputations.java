package transform;

import java.util.ListIterator;
import ir.ArrayIndex;
import ir.Adda;
import ir.BasicBlock;
import ir.base.Instruction;
import java.util.ArrayList;

public class GenerateArrayIndexingComputations implements Pass {
    public void run(ListIterator<Instruction> iter)
    throws Exception {
        Instruction i = iter.next();
        if(i instanceof ArrayIndex) {
            iter.remove();      // remove instruction i
            ArrayList<Instruction> code = ((ArrayIndex)i).getIndexingCalculation();
            BasicBlock containing = i.getContainingBB();
            // link into the dominator tree as we add it to the instruciton list
            Instruction dominator = i.getDominating();
            i.setDominating(null);
            for(Instruction c : code) {
                c.setDominating(dominator);
                c.setContainingBB(containing);
                dominator = c;
                iter.add(c);
            }
            // there will always be a load or store after the array indexing
            // can return with the iterator advanced since we are only
            // looking for ArrayIndex instructions
            Instruction ls = iter.next();
            ls.setDominating(code.get(code.size() - 1));
            // delete for the last instruction in code list. This
            // should always be an Adda, so put the cast to make this clear.
            i.delete((Adda)code.get(code.size() -1));
        }
    }
}
