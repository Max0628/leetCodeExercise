package org.leetCodeExercise.practice;

import java.util.HashSet;
import java.util.Set;

public class DpCoinChange {

  public int coinChange(int[] coins, int amount) {
    // edge case.
    if(coins==null || coins.length==0 || amount<=0){
      return 0;
    }
    // dp array
    //
    //
    /**
     * dp[i] 代表湊出金額 i 所需的最少硬幣數。
     * 例如：dp[100] = 4，表示湊出 100 元最少需要 4 枚硬幣。
     * 如果有一個硬幣面額為 x，且 i-x >= 0，則可以用這個硬幣來嘗試湊出 i 元。
     * 狀態轉移公式：dp[i] = min(dp[i], dp[i-x] + 1)
     * 這樣可以保證每次都選用最少硬幣的組合。
     * 舉個例子:
     * 假設 coins = [1, 2, 5]，amount = 11
     * 初始狀態：dp[0] = 0，其他 dp[i] = amount + 1 (表示無法湊出)
     * 計算過程：
     * 1. 使用硬幣 1：
     *   dp[1] = min(dp[1], dp[0] + 1) = 1
     *   dp[2] = min(dp[2], dp[1] + 1) = 2
     *   dp[3] = min(dp[3], dp[2] + 1) = 3
     *   ...
     *   dp[11] = min(dp[11], dp[10] + 1) = 11
     */
    int [] dp = new int[amount+1]; // plus 1 is to include case dp[0] =0.
    dp[0] = 0;
    for(int i=0;i<dp.length;i++){
      dp[i] = amount+1; // set to max value.
    }



    return -1;
  }
}

//
//322. Coin Change
//Medium
//    Topics
//premium lock icon
//    Companies
//You are given an integer array coins representing coins of different denominations and an
// integer amount representing a total amount of money.
//
//Return the fewest number of coins that you need to make up that amount. If that amount of money cannot
// be made up by any combination of the coins, return -1.
//
//You may assume that you have an infinite number of each kind of coin.
//
//
//
//Example 1:
//
//Input: coins = [1,2,5], amount = 11
//Output: 3
//Explanation: 11 = 5 + 5 + 1
//Example 2:
//
//Input: coins = [2], amount = 3
//Output: -1
//Example 3:
//
//Input: coins = [1], amount = 0
//Output: 0
//
//
//Constraints:
//
//    1 <= coins.length <= 12
//    1 <= coins[i] <= 231 - 1
//    0 <= amount <= 104
//
//
//Accepted
//2,526,358/5.4M
//Acceptance Rate
//47.2%
