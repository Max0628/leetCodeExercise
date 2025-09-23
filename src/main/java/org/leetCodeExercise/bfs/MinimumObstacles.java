package org.leetCodeExercise.bfs;

public class MinimumObstacles {

  public int minimumObstacles(int[][] grid) {

    // edge case
    if(grid==null||grid.length==0){
      return 0;
    }

    // left top corner: (0,0),right bottom corner(m-1,n-1)
    int m = grid.length;
    int n = grid[0].length;

    // first try to write code for walking from (0,0) to (m-1,n-1).

    
    return -1;
  }
}
/*
2290. Minimum Obstacle Removal to Reach Corner[hard]
You are given a 0-indexed 2D integer array grid of size m x n.
Each cell has one of two values:
0 represents an empty cell,
1 represents an obstacle that may be removed.
You can move up, down, left, or right from and to an empty cell.
Return the minimum number of obstacles to remove so you can move from
the upper left corner (0, 0) to the lower right corner (m - 1, n - 1).

Example 1:
Input: grid = [
               [0,1,1],
               [1,1,0],
               [1,1,0]
              ]
Output: 2
Explanation: We can remove the obstacles at (0, 1) and (0, 2) to create a path from (0, 0) to (2, 2).
It can be shown that we need to remove at least 2 obstacles, so we return 2.
Note that there may be other ways to remove 2 obstacles to create a path.

 Example 2:
Input: grid = [
               [0,1,0,0,0],
               [0,1,0,1,0],
               [0,0,0,1,0]
              ]
Output: 0
Explanation: We can move from (0, 0) to (2, 4) without removing any obstacles, so we return 0.

Constraints:
m == grid.length
n == grid[i].length
1 <= m, n <= 105
2 <= m * n <= 105
grid[i][j] is either 0 or 1.
grid[0][0] == grid[m - 1][n - 1] == 0

Strategy:
Note:
Code:
Analysis:
Compile error:
 */

/*
// java
public class Solution {
    static int[] dx = {0, 0, 1, -1};
    static int[] dy = {1, -1, 0, 0};
    static int directionCount = 4;

    public int minimumObstacle(int[][] grid) {
        int n = grid.length;
        int m = grid[0].length;

        int[][] id = new int[n][m];
        int[] dis = new int[n * m + 1];
        int[] vis = new int[n * m + 1];
        int[] locationX = new int[n * m + 1];
        int[] locationY = new int[n * m + 1];

        int index = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                id[i][j] = ++index;
                dis[index] = -1;
                locationX[index] = i;
                locationY[index] = j;
            }
        }

        Deque<Integer> queue = new ArrayDeque<>();
        queue.addFirst(1);
        dis[1] = 0;

        while (!queue.isEmpty()) {
            int currentNode = queue.pollFirst();
            if (vis[currentNode] == 1) {
                continue;
            }
            vis[currentNode] = 1;

            int x = locationX[currentNode];
            int y = locationY[currentNode];

            for (int i = 0; i < directionCount; i++) {
                int targetX = x + dx[i];
                int targetY = y + dy[i];
                if (targetX >= 0 && targetX < n && targetY >= 0 && targetY < m) {
                    int newDistance = dis[currentNode];
                    int targetId = id[targetX][targetY];
                    if (grid[targetX][targetY] == 1) {
                        newDistance++;
                    }

                    if (dis[targetId] == -1 || dis[targetId] > newDistance) {
                        dis[targetId] = newDistance;
                        if (grid[targetX][targetY] == 1) {
                            queue.addLast(targetId);
                        } else {
                            queue.addFirst(targetId);
                        }
                    }
                }
            }
        }

        return dis[id[n - 1][m - 1]];
    }
}
 */