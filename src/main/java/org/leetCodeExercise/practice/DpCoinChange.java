package org.leetCodeExercise.practice;


public class DpCoinChange {

  public int coinChange(int[] coins, int amount) {
    //edge case.
    if (coins == null || coins.length == 0 || amount < 0) {
      return 0;
    }

    // declare dp array.
    int[] dp = new int[amount + 1];
    dp[0] = 0;

    //put -1 for every amount as a placeholder.
    for (int k = 1; k <= amount; k++) {
      dp[k] = -1;
    }

    // loop for amount.
    for (int i = 1; i <= amount; i++) {
      // loop for every coin possibility.
      for (int j = 0; j < coins.length; j++) {

        int currentDenomination = coins[j];
        // check if the amount is bigger than current coin denomination.
        if (i >= currentDenomination) {
          // if former amount is been calculated.
          if ((dp[i - currentDenomination]) != -1) {
            // if current amount not been calculated.
            if (dp[i] == -1) {
              dp[i] = dp[i - currentDenomination] + 1;
            } else {
              // if current amount been calculated, take the minimum
              dp[i] = Math.min(dp[i], dp[i - coins[j]] + 1);
            }
          }
        }
      }
    }
    for (int x = 0; x < dp.length; x++) {
    }
    return dp[amount];
  }
}

/*
322. Coin Change
Medium
    Topics
premium lock icon
    Companies
You are given an integer array coins representing coins of different denominations and an
 integer amount representing a total amount of money.

Return the fewest number of coins that you need to make up that amount. If that amount of money cannot
 be made up by any combination of the coins, return -1.

You may assume that you have an infinite number of each kind of coin.



Example 1:

Input: coins = [1,2,5], amount = 11
Output: 3
Explanation: 11 = 5 + 5 + 1
Example 2:

Input: coins = [2], amount = 3
Output: -1
Example 3:

Input: coins = [1], amount = 0
Output: 0


Constraints:

    1 <= coins.length <= 12
    1 <= coins[i] <= 231 - 1
    0 <= amount <= 104


Accepted
2,526,358/5.4M
Acceptance Rate
47.2%

dp[0]=0;
dp[1]=1; -> [1]
dp[2]=1; -> [2]
dp[3]= min(dp[3-1]+1,dp[3-2]+1);->[1,2]+1 or [1]+1 -> [1]+1 is smaller -> 1+1=2.dp[3]=2
formula
*/