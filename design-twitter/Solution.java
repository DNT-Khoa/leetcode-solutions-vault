// Time:  O(?)
// Space: O(?)

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

class Twitter {
    record TweetWithTime(int tweetId, int time) {}
    private Map<Integer, List<TweetWithTime>> userTweets = new HashMap<>();
    private Map<Integer, Set<Integer>> userFollowing = new HashMap<>();

    static int TIME = 1;
    static int K = 10;

    public Twitter() {
        
    }
    
    public void postTweet(int userId, int tweetId) {
        userTweets.computeIfAbsent(userId, k -> new ArrayList<>()).add(new TweetWithTime(tweetId, TIME++));
    }
    
    public List<Integer> getNewsFeed(int userId) {
        PriorityQueue<TweetWithTime> minHeap = new PriorityQueue<>(Comparator.comparingInt(TweetWithTime::time));
        List<Integer> newsFeed = new ArrayList<>();

        // get tweets from userId
       getTweets(userId, minHeap);

        // get tweets of user following
        Set<Integer> followingIds = userFollowing.get(userId);
        if (followingIds != null && !followingIds.isEmpty()) {
            for (int followingId : followingIds) {
                getTweets(followingId, minHeap);
            }
        }

        while (!minHeap.isEmpty()) {
            newsFeed.add(minHeap.poll().tweetId());
        }

        Collections.reverse(newsFeed);
        return newsFeed;
    }

    private void getTweets(int userId, PriorityQueue<TweetWithTime> minHeap) {
         if (userTweets.containsKey(userId)) {
            for (TweetWithTime t : userTweets.get(userId)) {
                minHeap.offer(t);
                if (minHeap.size() > K) {
                    minHeap.poll();
                }
            }
        }
    }
    
    public void follow(int followerId, int followeeId) {
        userFollowing.computeIfAbsent(followerId, k -> new HashSet<>()).add(followeeId);
    }
    
    public void unfollow(int followerId, int followeeId) {
        if (userFollowing.containsKey(followerId)) {
            userFollowing.get(followerId).remove(followeeId);
        }
    }
}

/**
 * Your Twitter object will be instantiated and called as such:
 * Twitter obj = new Twitter();
 * obj.postTweet(userId,tweetId);
 * List<Integer> param_2 = obj.getNewsFeed(userId);
 * obj.follow(followerId,followeeId);
 * obj.unfollow(followerId,followeeId);
 */
