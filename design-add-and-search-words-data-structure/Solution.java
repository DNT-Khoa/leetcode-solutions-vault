// Time:  O(?)
// Space: O(?)

import java.util.HashMap;
import java.util.Map;

class WordDictionary {
    static class TrieNode {
        boolean isLeaf;
        Map<Character, TrieNode> children = new HashMap<>();
    }

    private TrieNode root;

    public WordDictionary() {
        root = new TrieNode();
    }
    
    public void addWord(String word) {
        TrieNode curr = root;
        for (char c : word.toCharArray()) {
            if (!curr.children.containsKey(c)) {
                curr.children.put(c, new TrieNode());
            }
            curr = curr.children.get(c);
        }
        curr.isLeaf = true;
    }
    
    // Time:  addWord: O(w). search: O(w) with no wildcards,
    //        O(26^d * w) worst case where d = number of '.' in the query.
    // Space: O(total characters added) for the trie,
    //        plus O(w) recursion stack per search.

    public boolean search(String word) {
        return backtrack(word, 0, root);
    }

    boolean backtrack(String word, int currIdx, TrieNode node) {
        if (currIdx == word.length()) return node.isLeaf;

        char c = word.charAt(currIdx);
        if (c == '.') {
            for (TrieNode child : node.children.values()) {
                if (backtrack(word, currIdx + 1, child)) return true;
            }
            return false;
        }

        TrieNode next = node.children.get(c);
        if (next == null) return false;
        return backtrack(word, currIdx + 1, next);
    }
}

/**
 * Your WordDictionary object will be instantiated and called as such:
 * WordDictionary obj = new WordDictionary();
 * obj.addWord(word);
 * boolean param_2 = obj.search(word);
 */
