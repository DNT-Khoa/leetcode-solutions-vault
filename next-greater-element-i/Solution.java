
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

// Time:  O(n) where n is the size of nums2
// Space: O(n)

class Solution {
    public int[] nextGreaterElement(int[] nums1, int[] nums2) {
        Deque<Integer> stack = new ArrayDeque<>();
        Map<Integer, Integer> numToNextGreater = new HashMap<>();

        for (int num : nums2) {
            while (!stack.isEmpty() && stack.peek() < num) {
                numToNextGreater.put(stack.poll(), num);
            }
            stack.offerFirst(num);
        }

        while (!stack.isEmpty()) {
            numToNextGreater.put(stack.poll(), -1);
        }

        for (int i = 0; i < nums1.length; i++) {
            nums1[i] = numToNextGreater.get(nums1[i]);
        }
        
        return nums1;
    }
}
