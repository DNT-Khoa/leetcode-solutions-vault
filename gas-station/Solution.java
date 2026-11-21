// Time:  O(N) where N is the number of stations
// Space: O(1)

class Solution {
    public int canCompleteCircuit(int[] gas, int[] cost) {
        int n = gas.length;
        int runningTotal = 0;
        int runningTankSum = 0;
        int station = 0;

        for (int i = 0; i < n; i++) {
            if (runningTankSum < 0) {
                runningTankSum = 0;
                station = i;
            }
            int diffNet = gas[i] - cost[i];
            runningTotal += diffNet;
            runningTankSum += diffNet;
        }

        return runningTotal >= 0 ? station : -1;
    }
}
