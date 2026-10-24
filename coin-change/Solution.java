// Time:  O(amount * C) where C is number of coins
// Space: O(amount)

class Solution {
    public int coinChange(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        for (int i = 0; i < dp.length; i++) {
            dp[i] = amount + 1;
        }
        dp[0] = 0;

        for (int a = 0; a <= amount; a++) {
            for (int c : coins) {
                if (a - c >= 0) {
                    dp[a] = Math.min(dp[a], dp[a - c] + 1);
                }
            }
        }

        return dp[amount] != amount + 1 ? dp[amount] : -1;
    }
}
