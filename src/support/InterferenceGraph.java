package support;

import ir.Value;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.io.Writer;

public class InterferenceGraph {
    private HashMap<Value, HashSet<Value>> adjacencyList;
    public InterferenceGraph () {
        adjacencyList = new HashMap<Value, HashSet<Value>>();
    }

    public void addNode(Value n) {
        if(!adjacencyList.containsKey(n)) {
            adjacencyList.put(n, new HashSet<Value>());
        }
    }

    public void deleteNode(Value n) {
        HashSet<Value> edges = adjacencyList.get(n);
        adjacencyList.remove(n);
        for(Value e : edges) {
            adjacencyList.get(e).remove(n);
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
        w.write("strict graph Interference {\n");
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
        w.write("}");
    }
}
