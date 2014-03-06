package support;

import java.util.HashMap;

public class GlobalMemoryRegion extends MemoryRegion {
    private HashMap<Identifier, addressRecord> usedMem;
    private int freeAddress;

    public GlobalMemoryRegion() {
        freeAddress = 0;
        usedMem = new HashMap<Identifier, addressRecord>();
    }

    @Override
    public int getSize() {
        // Globals are allocated at negative offsets
        return 0 - freeAddress;
    }

    @Override
    public int getAddress(Identifier var, int size)
        throws Exception {
        addressRecord a = usedMem.get(var);
        if(a != null) {
            if(a.size != size) {
                throw new Exception("varaible accessed as different size than expected");
            }
            return a.address;
        }
        // Globals are allocated at negative offsets
        freeAddress = freeAddress - size;
        usedMem.put(var, new addressRecord(freeAddress, size));
        return freeAddress;
    }
}
