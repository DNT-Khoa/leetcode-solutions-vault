
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

// Time:  O(N + T·log(K)) where N is number of tasks, T is the total number of time steps, idle ones included, K is the number of distinct tasks (at most 26)
// Space: O(K) = O(26)

class Solution {
    record TaskSchedule(int count, int readyTime) {}

    public int leastInterval(char[] tasks, int n) {    
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());
        Deque<TaskSchedule> queue = new ArrayDeque<>();

        // count occurrence of each tasks 
        Map<Character, Integer> count = new HashMap<>();
        for (char c : tasks) {
            count.merge(c, 1, Integer::sum);
        }

        // add occurences to max heap
        for (var o : count.values()) {
            maxHeap.offer(o);
        }

        int time = 0;
        while (!maxHeap.isEmpty() || !queue.isEmpty()) {
            while (!queue.isEmpty() && queue.peek().readyTime() == time) {
                maxHeap.offer(queue.poll().count());
            }

            if (!maxHeap.isEmpty()) {
                int taskCount = maxHeap.poll();
                if (taskCount > 1) {
                    queue.offer(new TaskSchedule(taskCount - 1, time + n + 1));
                }
            } 

            time++;
        }

        return time;
    }
}
