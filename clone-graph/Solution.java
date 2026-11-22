
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
// Definition for a Node.
class Node {
    public int val;
    public List<Node> neighbors;
    public Node() {
        val = 0;
        neighbors = new ArrayList<Node>();
    }
    public Node(int _val) {
        val = _val;
        neighbors = new ArrayList<Node>();
    }
    public Node(int _val, ArrayList<Node> _neighbors) {
        val = _val;
        neighbors = _neighbors;
    }
}
*/
// Time:  O(E + V) where E is the number of edges and V is the number of nodes
// Space: O(V)


class Solution {
    Map<Node, Node> cloneMap = new HashMap<>();

    public Node cloneGraph(Node node) {
        if (node == null) return null;

        Node clone = cloneMap.get(node);
        if (clone == null) {
            clone = new Node(node.val, new ArrayList<>());
            cloneMap.put(node, clone);
        }

        for (Node originalNeighbor : node.neighbors) {
            Node cloneNeighbor = cloneMap.get(originalNeighbor);
            if (cloneNeighbor == null) {
                cloneNeighbor = cloneGraph(originalNeighbor);
            } 
            clone.neighbors.add(cloneNeighbor);
        }

        return clone;
    }
}
