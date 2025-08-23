package org.leetCodeExercise.binarySearch;

public class BinarySearchBasicTest {
  public static void main(String[] args) {
    BinarySearchBasic bs = new BinarySearchBasic();
    int[] arr = {2, 5, 8, 12, 16, 23, 38, 56, 72, 91};

    // condition: target is in the array.
    int target1 = 23;
    System.out.println("Target " + target1 + " found at index: " + bs.BinarySearchTwoPointerBasic(arr, target1));

    int target2 = 38;
    System.out.println("Target " + target2 + " found at index: " + bs.FindFirstGreaterOrEqualIndex(arr, target2));

    int target3 = 38;
    System.out.println("Target " + target3 + " found at index: " + bs.FindFirstGreaterOrEqualValue(arr, target3));
  }
}