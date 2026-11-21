
import java.util.TreeMap;

// Time:  O(NlogN) where N is the number of cards
// Space: O(N)

class Solution {
    public boolean isNStraightHand(int[] hand, int groupSize) {
        if (hand.length % groupSize != 0) return false;

        TreeMap<Integer, Integer> treeMap = new TreeMap<>();
        for (int card : hand) {
            treeMap.merge(card, 1, Integer::sum);
        }

        while (!treeMap.isEmpty()) {
            // pick the first card in the group
            int firstCard = treeMap.firstKey();
            for (int i = firstCard; i < firstCard + groupSize; i++) {
                if (!treeMap.containsKey(i)) return false;

                int count = treeMap.get(i);
                if (count == 1) {
                    treeMap.remove(i);
                } else {
                    treeMap.put(i, count - 1);
                }
            }
        }

        return true;
    }
}
