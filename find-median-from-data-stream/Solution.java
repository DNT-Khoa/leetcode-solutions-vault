
import java.util.Comparator;
import java.util.PriorityQueue;

// Time:  addNum is O(log(N)) and findMedian is O(1)
// Space: O(N)

class MedianFinder {

    PriorityQueue<Integer> smallHeap;
    PriorityQueue<Integer> largeHeap;

    public MedianFinder() {
        this.smallHeap = new PriorityQueue<>(Comparator.reverseOrder());
        this.largeHeap = new PriorityQueue<>();
    }
    
    public void addNum(int num) {
        smallHeap.offer(num);

        // ensuring all elements in small heap <= large heap
        if (!largeHeap.isEmpty() && smallHeap.peek() > largeHeap.peek()) {
            largeHeap.offer(smallHeap.poll());
        }

        // ensuring both heap have approximately equal size (no more than 1 item difference)
        if (smallHeap.size() - largeHeap.size() > 1) {
            largeHeap.offer(smallHeap.poll());
        }
        if (largeHeap.size() - smallHeap.size() > 1) {
            smallHeap.offer(largeHeap.poll());
        }
    }
    
    public double findMedian() {
        if (smallHeap.size() == largeHeap.size()) {
            return (smallHeap.peek() + largeHeap.peek()) / 2.0;
        }

        if (smallHeap.size() > largeHeap.size()) {
            return smallHeap.peek();
        }

        return largeHeap.peek();
    }
}

/**
 * Your MedianFinder object will be instantiated and called as such:
 * MedianFinder obj = new MedianFinder();
 * obj.addNum(num);
 * double param_2 = obj.findMedian();
 */
