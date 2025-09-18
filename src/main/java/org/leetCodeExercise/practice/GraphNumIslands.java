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

// if node is connected each other top / bottom /left /right ,then they are on the same island.
// check one node if it's top / bottom / left / right is 1, if yes, then check that node's top / bottom / left / right
