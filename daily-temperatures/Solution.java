// Time:  O(n)
// Space: O(n)

import java.util.ArrayDeque;
import java.util.Deque;

class Solution {
    public int[] dailyTemperatures(int[] temperatures) {
        Deque<Integer> stack = new ArrayDeque<>();
        int[] res = new int[temperatures.length];

        for (int i = 0; i < temperatures.length; i++) {
            while (!stack.isEmpty() && temperatures[stack.peek()] < temperatures[i]) {
                int toBeRemovedIdx = stack.poll();
                res[toBeRemovedIdx] = i - toBeRemovedIdx;
            }

            stack.offerFirst(i);
        }

        return res;
    }
}
