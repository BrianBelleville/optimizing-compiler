package support;

import ir.Value;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.io.Writer;

public class InterferenceGraph {
    public class EdgeInfo {
        public HashSet<Value> edges;
        public HashSet<Integer> forbiddenColors;
        public EdgeInfo() {
            edges = new HashSet<Value>();
            forbiddenColors = new HashSet<Integer>();
        }
    }

    private HashMap<Value, EdgeInfo> adjacencyList;
    public InterferenceGraph () {
        adjacencyList = new HashMap<Value, EdgeInfo>();
    }

    private void colorNode(Value node, Integer color) {
        EdgeInfo e = adjacencyList.get(node);
        assert(!e.forbiddenColors.contains(color));
        node.setColor(color);
        for(Value v : e.edges) {
            adjacencyList.get(v).forbiddenColors.add(color);
        }
    }

    private Integer getAvailableColor(Value node) {
        HashSet<Integer> forbiddenColors = adjacencyList.get(node).forbiddenColors;
        // start coloring from 0, these are abstract registers, so it
        // doesn't matter that the 0 register can't be used in DLX.
        Integer i = 0;          
        while(forbiddenColors.contains(i)) {
            i++;
        }
        return i;
    }

    // Very very stupid graph coloring strategy
    public void colorGraph() {
        for(Value v  : adjacencyList.keySet()) {
            colorNode(v, getAvailableColor(v));
        }
    }
                                               

    public void addNode(Value n) {
        if(!adjacencyList.containsKey(n)) {
            adjacencyList.put(n, new EdgeInfo());
        }
    }

    public void deleteNode(Value n) {
        HashSet<Value> edges = adjacencyList.get(n).edges;
        adjacencyList.remove(n);
        for(Value e : edges) {
            adjacencyList.get(e).edges.remove(n);
        }
    }

    public void addEdge(Value v1, Value v2) {
        EdgeInfo v1EdgeInfo = adjacencyList.get(v1);
        EdgeInfo v2EdgeInfo = adjacencyList.get(v2);
        if(v1EdgeInfo == null) {
            v1EdgeInfo = new EdgeInfo();
            adjacencyList.put(v1, v1EdgeInfo);
        }
        if(v2EdgeInfo == null) {
            v2EdgeInfo = new EdgeInfo();
            adjacencyList.put(v2, v2EdgeInfo);
        }
        v1EdgeInfo.edges.add(v2);
        v2EdgeInfo.edges.add(v1);
    }

    public void printGraph(Writer w) throws Exception {
            for(Entry<Value, EdgeInfo> v  : adjacencyList.entrySet()) {
            Value node = v.getKey();
            HashSet<Value> edges = v.getValue().edges;
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
