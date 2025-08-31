package org.leetCodeExercise.mergeSort;

import java.util.Arrays;

public class MergeSortByArrayTest {
  public static void main(String[] args) {

    MergeSortByArray mergeSortByArray = new MergeSortByArray();
    int testArray1[] = {5, 3, 4, 2, 1, 8, 9, 7, 6};
    System.out.println("mergeSortByArray: " + Arrays.toString(mergeSortByArray.mergeSort(testArray1)));
    int testArray2[] = {3, 8, 1, 4, 7, 2, 5};
    int testArray3[] = {1, 2, 3, 4, 5, 6, 7, 8};
    System.out.println("mergeSortEvenOdd: " + Arrays.toString(mergeSortByArray.mergeSortEvenOdd(testArray3)));
  }
}
