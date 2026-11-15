
import java.util.PriorityQueue;

// Time:  O(Nlog(K)) where N = number of points and K = k
// Space: O(K)

class Solution {
    public int[][] kClosest(int[][] points, int k) {
        PriorityQueue<int[]> maxHeap = new PriorityQueue<>((e1, e2) -> Integer.compare(calculateDistance(e2), calculateDistance(e1)));
        
        for (int[] point : points) {
            maxHeap.offer(point);
            if (maxHeap.size() > k) maxHeap.poll();
        }

        int[][] result = new int[k][2];
        int i = 0;
        while (!maxHeap.isEmpty()) {
            result[i] = maxHeap.poll();
            i++;
        }

        return result;
    }

    int calculateDistance(int[] point) {
        return point[0] * point[0] + point[1] * point[1];
    }
}
