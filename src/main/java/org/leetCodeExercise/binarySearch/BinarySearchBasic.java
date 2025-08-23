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
   * <p>
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
    if(target > numbers[numbers.length-1]){
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
}

