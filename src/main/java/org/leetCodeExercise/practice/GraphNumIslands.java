package org.leetCodeExercise.practice;

public class GraphNumIslands {

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
}

// If a cell is connected to other '1's (land) in the top, bottom, left, or right direction, they all belong to the same island.
// When checking a cell, if its neighbor is '1', keep checking that neighbor's neighbors, until all connected land is found.

/** Thinking process:
 1. The input is a 2D array (matrix), which we can treat as a grid map.
 2. The goal is to count how many "islands" are in the grid. An island is a group of '1's (land) connected up, down, left, or right.
 3. To find all land in the same island, start from a '1' and check its four neighbors. If a neighbor is also '1', keep exploring from there.
 4. This process is called "finding connected components" in graph theory. We can use DFS (Depth-First Search) to do this, which is easy to write with recursion.
 5. Edge case: If the grid is null or empty, just return 0.
 6. Time complexity: O(m*n), where m is the number of rows and n is the number of columns. Every cell is visited at most once.
 7. Space complexity: O(m*n) in the worst case, because of the recursion stack if the whole grid is land.
**/