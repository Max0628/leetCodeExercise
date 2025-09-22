package org.leetCodeExercise.practice;

public class DpCoinChangeTest {
  public static void main(String[] args) {
    DpCoinChange solver = new DpCoinChange();

    // Example 1
//    int[] coins1 = {1, 2, 5};
//    int amount1 = 11;
//    int result1 = solver.coinChange(coins1, amount1);
//    System.out.println("Test 1: " + (result1 == 3 ? "PASS" : "FAIL") + " (Output: " + result1 + ")");

//    // Example 2
//    int[] coins2 = {2};
//    int amount2 = 3;
//    int result2 = solver.coinChange(coins2, amount2);
//    System.out.println("Test 2: " + (result2 == -1 ? "PASS" : "FAIL") + " (Output: " + result2 + ")");
//
//    // Example 3
//    int[] coins3 = {1};
//    int amount3 = 0;
//    int result3 = solver.coinChange(coins3, amount3);
//    System.out.println("Test 3: " + (result3 == 0 ? "PASS" : "FAIL") + " (Output: " + result3 + ")");

      // Example 4
    int [] coins4 = {2,5,10,1};
    int amount4 = 27;
    int result4 = solver.coinChange(coins4, amount4);
    System.out.println("Test 4: " + (result4 == 4 ? "PASS" : "FAIL") + " (Output: " + result4 + ")");
  }
}