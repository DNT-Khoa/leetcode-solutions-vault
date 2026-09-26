
import java.util.ArrayDeque;
import java.util.Deque;

// Time:  O(N)
// Space: O(K)

class Solution {
    public int[] maxSlidingWindow(int[] nums, int k) {
        int N = nums.length;
        Deque<Integer> dq = new ArrayDeque<>();
        int[] res = new int[N - k + 1];

        for (int i = 0; i < N; i++) {
            while (!dq.isEmpty() && i - dq.peekFirst() + 1 > k) dq.pollFirst();
            while (!dq.isEmpty() && nums[dq.peekLast()] < nums[i]) dq.pollLast();
            dq.offerLast(i);

            if (i >= k - 1) {
                res[i - k + 1] = nums[dq.peekFirst()];
            }
        }

        return res;
    }
}
