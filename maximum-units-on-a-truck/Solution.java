// Time:  O(NLogN) where N is box types 
// Space: O(1)

import java.util.Arrays;

class Solution {
    public int maximumUnits(int[][] boxTypes, int truckSize) {
        Arrays.sort(boxTypes, (a, b) -> b[1] - a[1]);
        
        int res = 0;
        for (int i = 0; i < boxTypes.length; i++) {
            int[] boxType = boxTypes[i];
            int numOfBoxes = boxType[0];
            int numOfUnits = boxType[1];

            if (truckSize > numOfBoxes) {
                res += numOfBoxes * numOfUnits;
                truckSize -= numOfBoxes;
            } else {
                res += truckSize * numOfUnits;
                return res;
            }
        }

        return res;
    }
}
