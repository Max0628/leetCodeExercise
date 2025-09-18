package org.leetCodeExercise.practice;

import java.util.ArrayDeque;
import java.util.Deque;

public class GraphNumIslands {

// DFS version
  /** Thinking process:
   1. The input is a 2D array (matrix), which we can treat as a grid map.
   2. The goal is to count how many "islands" are in the grid. An island is a group of '1's (land) connected up, down, left, or right.
   3. To find all land on the same island, start from a '1' and check its four neighbors. If a neighbor is also '1', keep exploring from there.
   4. This process is called "finding connected components" in graph theory. We can use DFS (Depth-First Search) to do this, which is easy to write with recursion.
   5. Edge case: If the grid is null or empty, just return 0.
   6. Time complexity: O(m*n), where m is the number of rows and n is the number of columns. Every cell is visited at most once.
   7. Space complexity: O(m*n) in the worst case, because of the recursion stack if the whole grid is land.
   **/
  public int numIslands(char[][] grid) {
    // because paras is a two detention array, use Adjacent Matrix.
    // edge case.
    if (grid == null || grid.length == 0) {
      return 0;
    }

    // island count.
    int count = 0;
    // matrix's row
    int m = grid.length;
    // matrix's column
    int n = grid[0].length;

    // traverse the matrix.
    for (int i = 0; i < m; i++) {
      for (int j = 0; j < n; j++) {
        // if we find an island, then do dfs to find all the connected island, and mark them as visited.
        if (grid[i][j] == '1') {
          dfs(grid, i, j, m, n);
          count++;
        }
      }
    }

    return count;
  }

  // recursive
  private void dfs(char[][] grid, int i, int j, int m, int n) {
    // base case, if current index is touch the border, or the node is water, then return.
    if (i < 0 || i >= m || j < 0 || j >= n || grid[i][j] == '0') {
      return;
    }
    // trun current grid to warter.
    grid[i][j] = '0';
    // check the top / bottom / left / right node.
    dfs(grid, i - 1, j, m, n); // top
    dfs(grid, i + 1, j, m, n); // bottom
    dfs(grid, i, j - 1, m, n); // left
    dfs(grid, i, j + 1, m, n); // right
  }


  //BFS version
  /**
   1. use for loop to traverse to an element.
   2. if current is '1', it means we find a new island, count ++.
   3. we change this element to '0', and push this element to queue.
   4. checking if the queue is not empty.
   5. poll the head element from queue, and :
   6. checking the current element's neighbor, if it's '1', change it to '0'.
   */
  public int numIslandsV2(char[][] grid) {
    //edge case.
    if (grid == null || grid.length == 0) {
      return 0;
    }

    // queue to store neighbor element.
    Deque<Coordinate> queue = new ArrayDeque<>();
    int row = grid.length - 1;
    int col = grid[0].length - 1;
    int count = 0;
    for (int i = 0; i <= row; i++) {
      for (int j = 0; j <= col; j++) {
        char curr = grid[i][j];
        if (curr == '1') {
          count++;
          grid[i][j] = '0';
          Coordinate coordinate = new Coordinate(i, j);
          queue.push(coordinate);
          while (queue.peek() != null) {
            bfs(grid, queue, row, col);
          }
        }
      }
    }
    return count;
  }

  public void bfs(char[][] grid, Deque<Coordinate> queue, int row, int col) {
    Coordinate headElement = queue.poll();
    int x = headElement.getX();
    int y = headElement.getY();
    if (x + 1 <= row && grid[x + 1][y] == '1') {
      queue.push(new Coordinate(x + 1, y));
      grid[x + 1][y] = '0';
    }
    if (x - 1 >= 0 && grid[x - 1][y] == '1') {
      queue.push(new Coordinate(x - 1, y));
      grid[x - 1][y] = '0';
    }
    if (y - 1 > 0 && grid[x][y - 1] == '1') {
      queue.push(new Coordinate(x, y - 1));
      grid[x][y - 1] = '0';
    }
    if (y + 1 <= col && grid[x][y + 1] == '1') {
      queue.push(new Coordinate(x, y + 1));
      grid[x][y + 1] = '0';
    }
  }

  class Coordinate {
    private int x;
    private int y;

    public Coordinate(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public int getX() {
      return x;
    }

    public int getY() {
      return y;
    }
  }
}