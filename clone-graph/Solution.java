
import java.util.HashMap;

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
// Time:  O(V + E)
// Space: O(V)
class Solution {
    public Node cloneGraph(Node node) {
        if (node == null) return null;
        Map<Integer, Node> map = new HashMap<>();
        return clone(node, map);
    }

    Node clone(Node originalNode, Map<Integer, Node> map) {
        if (!map.containsKey(originalNode.val)) {
            map.put(originalNode.val, new Node(originalNode.val));
        }
        
        for (Node neighbor : originalNode.neighbors) {
            if (!map.containsKey(neighbor.val)) {
                clone(neighbor, map);
            }
            map.get(originalNode.val).neighbors.add(map.get(neighbor.val));
        }

        return map.get(originalNode.val);
    }
}
