// Time:  O(Nlog(N))
// Space: O(N)
import java.util.Comparator;
import java.util.PriorityQueue;

class Solution {
    public int lastStoneWeight(int[] stones) {
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        for (int stone : stones) {
            maxHeap.offer(stone);
        }

        while (maxHeap.size() > 1) {
            int largest = maxHeap.poll();
            int secondLargest = maxHeap.poll();

            int remained = largest - secondLargest;
            if (remained > 0) {
                maxHeap.offer(remained);
            }
        }

        return maxHeap.isEmpty() ? 0 : maxHeap.poll();
    }
}
