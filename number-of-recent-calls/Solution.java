
import java.util.ArrayDeque;
import java.util.Deque;

// Time:  O(1) amortized 
// Space: O(n)

class RecentCounter {

    private Deque<Integer> dq;

    public RecentCounter() {
        dq = new ArrayDeque<>();
    }
    
    public int ping(int t) {
        while (!dq.isEmpty() && dq.peek() < t - 3000) dq.poll();
        dq.offer(t);
        return dq.size();
    }
}

/**
 * Your RecentCounter object will be instantiated and called as such:
 * RecentCounter obj = new RecentCounter();
 * int param_1 = obj.ping(t);
 */
