// Time:  O(N) where N is the length of nums
// Space: O(1)

class Solution {
    public int jump(int[] nums) {
        int count = 0;
        
        int start = 0;
        int end = 0;

        while (end < nums.length - 1) {
            int nextEnd = end;
            while (start <= end) {
                nextEnd = Math.max(start + nums[start], nextEnd);
                start++;
            }
            count++;
            end = nextEnd;
        }

        return count;
    }
}
