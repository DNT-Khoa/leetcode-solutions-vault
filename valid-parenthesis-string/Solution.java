// Time:  O(N)
// Space: O(1)

class Solution {
    public boolean checkValidString(String s) {

        int low = 0, high = 0;
        for (char c : s.toCharArray()) {
            switch (c) {
                case '(' -> {
                    low++;
                    high++;
                }
                case ')' -> {
                    low--;
                    high--;
                }
                default -> {
                    low--;
                    high++;
                }
            }

            if (high < 0) return false;
            if (low < 0) low = 0;
        }

        return low == 0;
    }
}
