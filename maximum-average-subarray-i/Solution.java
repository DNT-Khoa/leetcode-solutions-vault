// Time:  O(n)
// Space: O(1)

class Solution {
    public double findMaxAverage(int[] nums, int k) {
        int n = nums.length;
        double maxAverage = Double.NEGATIVE_INFINITY;

        int left = 0, right = 0;
        int currentSum = 0;
        while (right < n) {
            currentSum += nums[right];
            if (right - left + 1 > k) {
                currentSum -= nums[left++];
            }
            if (right - left + 1 == k) maxAverage = Math.max(maxAverage, (double)currentSum / k);
            right++;
        }
        return maxAverage;
    }
}
