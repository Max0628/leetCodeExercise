package org.leetCodeExercise.practice;

public class GraphNumIslandsTest {
  public static void main(String[] args) {
    GraphNumIslands graphNumIslands = new GraphNumIslands();

    char[][] grid1 = {
            {'1', '1', '1', '1', '0'},
            {'1', '1', '0', '1', '0'},
            {'1', '1', '0', '0', '0'},
            {'0', '0', '0', '0', '0'}
    };

    char[][] grid2 = {
            {'1', '1', '0', '0', '0'},
            {'1', '1', '0', '0', '0'},
            {'0', '0', '1', '0', '0'},
            {'0', '0', '0', '1', '1'}
    };
    char[][] grid3 = {
            {'1', '1', '1', '1', '0'},
            {'1', '1', '0', '1', '0'},
            {'1', '1', '0', '0', '0'},
            {'0', '0', '0', '0', '0'}
    };
    char[][] grid4 = {
            {'1', '1', '0', '0', '0'},
            {'1', '1', '0', '0', '0'},
            {'0', '0', '1', '0', '0'},
            {'0', '0', '0', '1', '1'}
    };
    // 不能使用同一組 matrix,會修改原本的 matrix

    int result1 = graphNumIslands.numIslands(grid1);
    int result2 = graphNumIslands.numIslands(grid2);

    System.out.println("Test case 1 result: " + result1); // expect: 1
    System.out.println("Test case 2 result: " + result2); // expect: 3

    // 呼叫 BFS 方法
    int result3 = graphNumIslands.numIslandsV2(grid3);
    int result4 = graphNumIslands.numIslandsV2(grid4);

    // 印出結果
    System.out.println("Test case 3 result: " + result3 + " | expected: 1");
    System.out.println("Test case 4 result: " + result4 + " | expected: 3");
  }
}