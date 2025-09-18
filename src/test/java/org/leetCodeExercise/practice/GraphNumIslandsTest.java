package org.leetCodeExercise.practice;

public class GraphNumIslandsTest {
  public static void main(String[] args) {
    GraphNumIslands graphNumIslands = new GraphNumIslands();

    char[][] grid1 = {
        {'1','1','1','1','0'},
        {'1','1','0','1','0'},
        {'1','1','0','0','0'},
        {'0','0','0','0','0'}
    };

    char[][] grid2 = {
        {'1','1','0','0','0'},
        {'1','1','0','0','0'},
        {'0','0','1','0','0'},
        {'0','0','0','1','1'}
    };

    int result1 = graphNumIslands.numIslands(grid1);
    int result2 = graphNumIslands.numIslands(grid2);

    System.out.println("Test case 1 result: " + result1); // expect: 1
    System.out.println("Test case 2 result: " + result2); // expect: 3
  }
}