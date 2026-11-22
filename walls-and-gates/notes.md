# Walls and Gates

**Difficulty:** Medium (Premium)  
**Link:** https://leetcode.com/problems/walls-and-gates/

---

You are given an m x n grid rooms initialized with these three possible values:

- -1 : a wall or an obstacle.
- 0 : a gate.
- INF : infinity, an empty room. The value 2^31 - 1 = 2147483647 is used to represent INF.

Fill each empty room with the distance to its nearest gate. If it is impossible to reach a gate, it should stay INF . Modify rooms in place.

Example 1:

```
Input:
 rooms = [[2147483647,-1,0,2147483647],[2147483647,2147483647,2147483647,-1],[2147483647,-1,2147483647,-1],[0,-1,2147483647,2147483647]]

Output:
 [[3,-1,0,1],[2,2,1,-1],[1,-1,2,-1],[0,-1,3,4]]
```

Example 2:

```
Input:
 rooms = [[-1]]

Output:
 [[-1]]
```

Constraints:

- m == rooms.length
- n == rooms[i].length
- 1 <= m, n <= 250
- rooms[i][j] is -1 , 0 , or 2^31 - 1 .
