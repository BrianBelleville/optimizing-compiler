package support;

import ir.Value;
import ir.Instruction;
import ir.Fetch;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.io.Writer;

public class InterferenceGraph {
    private HashMap<Value, HashSet<Value>> adjacencyList;
    private ArrayList<Value> deletedNodes;
    private ArrayList<HashSet<Value>> deletedEdges;

    public InterferenceGraph () {
        adjacencyList = new HashMap<Value, HashSet<Value>>();
        deletedNodes = new ArrayList<Value>();
        deletedEdges = new ArrayList<HashSet<Value>>();
    }

    private Integer getAvailableColor(Value node) {
        HashSet<Integer> forbiddenColors = getForbiddenColors(node);
        // start coloring from 0, these are abstract registers, so it
        // doesn't matter that the 0 register can't be used in DLX.
        Integer i = 0;
        while(forbiddenColors.contains(i)) {
            i++;
        }
        return i;
    }

    private HashSet<Integer> getForbiddenColors(Value n) {
        HashSet<Value> edges = adjacencyList.get(n);
        HashSet<Integer> rval = new HashSet<Integer>();
        for(Value v : edges) {
            Integer c = v.getColor();
            if(c != null && c != -1) {
                rval.add(c);
            }
        }
        return rval;
    }

    // doesn't seek to answer the question in general, just for the
    // assumptions made by our algorithm, just checks if there are
    // nodes with >= k edges.
    private boolean isKColorable(int k) {
        for(HashSet<Value> edges : adjacencyList.values()) {
            if(edges.size() >= k) {
                return false;
            }
        }
        return true;
    }

    private void selectAndSpillValue() {
        Value spill = null;
        int maxEdges = 0;
        for(Entry<Value, HashSet<Value>> v  : adjacencyList.entrySet()) {
            if(v.getValue().size() > maxEdges) {
                spill = v.getKey();
                maxEdges = v.getValue().size();
            }
        }
        deleteNode(spill);
    }

    // fetch instructions that are used once (or never) can be spilled
    // for free, just access the value from the memory location when
    // it is needed. Other instructions that are never used don't need
    // a register either.
    public void freeSpills() {
        ArrayList<Value> permanentDeletions = new ArrayList<Value>();
        for(Value v : adjacencyList.keySet()) {
            if(v instanceof Fetch) {
                if(((Instruction)v).getUseCount() <= 1) {
                    // placeholder color to access it directly
                    // from its memory location
                    v.setColor(-1);
                    permanentDeletions.add(v);
                }
            } else if(v instanceof Instruction) {
                if(((Instruction)v).getUseCount() == 0) {
                    v.setColor(-1);
                    permanentDeletions.add(v);
                }
            }
        }
        // These nodes will have already been given their color now,
        // and can be permanantly removed from the graph, they will no
        // longer interfer with resources needed by any other nodes,
        // so we don't ever need to add them back in.
        for(Value v : permanentDeletions) {
            permanentlyDeleteNode(v);
        }
    }


    public void colorGraph() {
        colorGraph(8);
    }

    public void colorGraph(int k) {
        if(!isKColorable(k)) {
            freeSpills();
        }
        while(!isKColorable(k)) {
            selectAndSpillValue();
        }
        for(Value v  : adjacencyList.keySet()) {
            v.setColor(getAvailableColor(v));
        }
        // add back spilled variables and give them a color
        for(int i = 0; i < deletedNodes.size(); i++) {
            Value v = deletedNodes.get(i);
            restoreNode(v, deletedEdges.get(i));
            if(v instanceof Fetch) {
                // fetches will just be accessed from the location
                // they were passed on the stack, no need to give them
                // a separate memory cell
                v.setColor(-1);
            }
            v.setColor(getAvailableColor(v));
        }
    }

    public void addNode(Value n) {
        if(!adjacencyList.containsKey(n)) {
            adjacencyList.put(n, new HashSet<Value>());
        }
    }

    // remove a node and its edges from the graph, but store them so
    // that they can be restored to the interference graph
    private void deleteNode(Value n) {
        HashSet<Value> edges = adjacencyList.get(n);
        adjacencyList.remove(n);
        for(Value e : edges) {
            adjacencyList.get(e).remove(n);
        }
        deletedNodes.add(n);
        deletedEdges.add(edges);
    }

    private void permanentlyDeleteNode(Value n) {
        deleteNode(n);
        deletedNodes.remove(deletedNodes.size() - 1);
        deletedEdges.remove(deletedEdges.size() - 1);
    }

    private void restoreNode(Value n, HashSet<Value> edges) {
        addNode(n);
        for(Value v : edges) {
            addEdge(n, v);
        }
    }

    public void addEdge(Value v1, Value v2) {
        HashSet<Value> v1Edges = adjacencyList.get(v1);
        HashSet<Value> v2Edges = adjacencyList.get(v2);
        if(v1Edges == null) {
            v1Edges = new HashSet<Value>();
            adjacencyList.put(v1, v1Edges);
        }
        if(v2Edges == null) {
            v2Edges = new HashSet<Value>();
            adjacencyList.put(v2, v2Edges);
        }
        v1Edges.add(v2);
        v2Edges.add(v1);
    }

    public void printGraph(Writer w) throws Exception {
        for(Entry<Value, HashSet<Value>> v  : adjacencyList.entrySet()) {
            Value node = v.getKey();
            HashSet<Value> edges = v.getValue();
            // first write the node so that nodes that have no edges
            // connecting them will be drawn.
            w.write("\"");
            node.printAsArg(w);
            w.write("\";\n");
            for(Value endpoint : edges) {
                w.write("\"");
                node.printAsArg(w);
                w.write("\" -- \"");
                endpoint.printAsArg(w);
                w.write("\";\n");
            }
        }
    }
}
