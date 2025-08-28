package org.leetCodeExercise.binarySearch;

public class BinarySearchBasic {

  /**
   * Instructions:
   * 1. Target is in a sorted array of numbers.
   * 2. Find the index of the target and return it.
   * 3. Use two pointer approach.
   * 4. Use binary search concept; ensure time complexity is O(log N).
   *
   * @param numbers of sorted array.
   * @param target
   *
   * @return the index of the target int the array, return -1 if not found.
   */
  public int BinarySearchTwoPointerBasic(int numbers[], int target) {
    int left = 0;
    int right = numbers.length - 1; // right is inclusive; it is part of the search interval.
    while (left <= right) { // check if left == right as well; exit loop only when left > right.
      int middle = left + (right - left) / 2; // calculate mid without overflow.
      if (numbers[middle] > target) {
        right = middle - 1; // mid cannot be the answer; search left subarray.
      } else if (numbers[middle] < target) {
        left = middle + 1; // same as here.
      } else {
        return middle; // find out the answer.
      }
    }
    return -1;
  }

  /**
   * Instruction:
   * 1. If you can't find target in array, find the first element which is bigger than target.
   *
   * @param numbers
   * @param target
   *
   * @return
   */
  public int FindFirstGreaterOrEqualIndex(int numbers[], int target) {
    int left = 0;
    int right = numbers.length - 1;

    //Edge case: target is bigger than the biggest element in array.
    if (target > numbers[right]) {
      return -1;
    }

    while (left <= right) {
      int middle = left + (right - left) / 2;
      if (numbers[middle] > target) {
        // Current middle is greater than target
        // It could be the first >= target, but there might be a smaller one on the left
        right = middle - 1;
      } else if (numbers[middle] < target) {
        // Current middle is smaller than target
        // The first >= target must be to the right of middle
        left = middle + 1;
      } else {
        return middle;
      }
    }
    // At this point, left points to the first element >= target
    return left;
  }

  /**
   * Instructions:
   * 1. Find out the first element's value which is >= target.
   *
   * @param numbers
   * @param target
   *
   * @return
   */
  public int FindFirstGreaterOrEqualValue(int numbers[], int target) {
    int left = 0;

    // right close interval
    int right = numbers.length;

    // Edge case: target is bigger than the last element.
    if (target > numbers[numbers.length - 1]) {
      return -1;
    }

    // because right index is not in the interval.
    while (left < right) {
      int middle = left + (right - left) / 2;
      // in this condition, right maybe the answer.
      if (numbers[middle] >= target) {
        right = middle;
      } else {
        //in this case, left is smaller than middle, which means it will not be the answer.
        left = middle + 1;
      }
    }
    // answer will always be in the right pointer.
    return numbers[right];
  }

  /**
   * Instruction :
   * <p>
   * A certain country's bank calculates loan repayment as follows:
   * 1. Each month, the remaining loan balance is multiplied by (1 + monthly interest rate),
   * then the result is truncated to the nearest integer (discard any fractional part).
   * 2. After that, the monthly payment is subtracted from the remaining balance.
   * <p>
   * Example:
   * - Suppose the remaining balance is 10,000,000 and the monthly interest rate is 0.1%.
   * - If the customer pays 50,000 that month, the calculation is:
   * 10,000,000 × 1.001 = 10,010,000
   * 10,010,000 − 50,000 = 9,960,000
   * <p>
   * Task:
   * - A customer borrows 20,000,000 with a monthly interest rate of 0.15%.
   * - The loan must be fully repaid in 30 years (360 months).
   * - Calculate the minimum monthly payment required to pay off the loan completely.
   * - The last month may require a smaller payment.
   * <p>
   * Suggestion:
   * - It is recommended to simulate the repayment schedule with a program.
   *
   * <p>
   * Answer: 73940 / 70940 / 72940 / 71940
   * </p>
   * <p>
   * "A stupid solution first comes to my mind."
   */
  public long calculateMonthlyPayment() {
    long totalDebt = 20000000;
    long monthlyPayment = 50000;
    long currentLeft = 0;

    // We don't know how many times need to adjust the monthlyPayment. add until it can pay the debt. Using while foop for
    while (true) {
      currentLeft = totalDebt;

      // 360 month.
      for (int month = 0; month <= 360; month++) {
        currentLeft = applyMonthlyPayment(currentLeft, monthlyPayment);
      }


      if (currentLeft > 0) {

        // If still can't pay debt. plus 1 dollar per month.
        monthlyPayment += 1;
      } else {
        // 71789
        return monthlyPayment;
      }

    }
  }

  /**
   * Count every loop.
   *
   * @param previousMonthLeft
   * @param monthlyPayment
   *
   * @return
   */
  public long applyMonthlyPayment(long previousMonthLeft, long monthlyPayment) {
    double debtRate = 0.0015;
    long withInterest = (long) (previousMonthLeft * (1 + debtRate));
    return withInterest - monthlyPayment;
  }

  /**
   * Use Binary Search to do the issue.
   *
   * @return
   */
  public long calculateMonthlyPaymentBinarySearch() {
    long totalDebt = 20000000;

    // left pointer
    long minPerMonthPaymeny = totalDebt / 360;

    // right pointer
    long maxPerMonthPaymeny = totalDebt;

    // left > right exit while loop.
    while (minPerMonthPaymeny <= maxPerMonthPaymeny) {
      long mid = minPerMonthPaymeny + (maxPerMonthPaymeny - minPerMonthPaymeny) / 2;

      //run loop
      long result = run360times(totalDebt, mid);
      if (result <= 0) {
        maxPerMonthPaymeny = mid - 1;
      } else {
        minPerMonthPaymeny = mid + 1;
      }
    }
    return minPerMonthPaymeny;
  }

  public long run360times(long totalDebt, long monthlyPayment) {
    long currentLeft = totalDebt;
    // force to run 360 times.
    for (int month = 0; month <= 360; month++) {
      currentLeft = calculateMonthlyPayment(currentLeft, monthlyPayment);
    }
    return currentLeft;
  }

  public long calculateMonthlyPayment(long previousMonthLeft, long monthlyPayment) {
    double debtRate = 0.0015;
    long withInterest = (long) (previousMonthLeft * (1 + debtRate));
    return withInterest - monthlyPayment;
  }
}

