// Time:  O(N)
// Space: O(1)

class Solution {
    public boolean canJump(int[] nums) {
        int targetIndex = nums.length - 1;

        for (int i = targetIndex - 1; i >= 0; i--) {
            if (i + nums[i] >= targetIndex) {
                targetIndex = i;
            }
        }

        return targetIndex == 0;
    }
}
