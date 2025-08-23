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
  public int BinarySearchBasicTwoPointer(int numbers[], int target) {
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
}
