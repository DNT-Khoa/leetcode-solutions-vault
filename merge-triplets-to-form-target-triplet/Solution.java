// Time:  O(N) where N is the number of triplets
// Space: O(N)

import java.util.ArrayList;
import java.util.List;

class Solution {
    public boolean mergeTriplets(int[][] triplets, int[] target) {
        List<int[]> validTriplets = new ArrayList<>();

        // filter out all triplets with x, y, z larger than target[x, y, z]
        for (int[] triplet : triplets) {
            if (triplet[0] > target[0] || triplet[1] > target[1] || triplet[2] > target[2]) continue;
            validTriplets.add(triplet);
        }

        for (int i = 0; i < target.length; i++) {
            if (!atLeastOneTripletWithTarget(target[i], i, validTriplets)) return false;
        }

        return true;
    }

    boolean atLeastOneTripletWithTarget(int target, int index, List<int[]> triplets) {
        for (int[] triplet : triplets) {
            if (triplet[index] == target) return true;
        }

        return false;
    }
}
