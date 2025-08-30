package org.leetCodeExercise.mergeSort;

import java.util.Arrays;

public class MergeSortByArrayTest {
  public static void main(String[] args) {

    MergeSortByArray mergeSortByArray = new MergeSortByArray();
    int testArray[] = {5, 3, 4, 2, 1, 8, 9, 7, 6};
    System.out.println("mergeSortByArray: " + Arrays.toString(mergeSortByArray.mergeSort(testArray)));
  }
}
