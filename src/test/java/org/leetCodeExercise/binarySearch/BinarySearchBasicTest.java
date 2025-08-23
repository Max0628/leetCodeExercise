package org.leetCodeExercise.binarySearch;

public class BinarySearchBasicTest {
  public static void main(String[] args) {
    BinarySearchBasic bs = new BinarySearchBasic();
    int[] arr = {2, 5, 8, 12, 16, 23, 38, 56, 72, 91};

    // condition: target is in the array.
    int target = 23;
    System.out.println("Target " + target + " found at index: " + bs.BinarySearchBasicTwoPointer(arr, target));
  }
}