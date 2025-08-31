package org.leetCodeExercise.mergeSort;

import java.util.Arrays;

public class MergeSortByArray {

  /**
   * merge sort practice by array & change the originalArray.
   *
   * @param array
   *
   * @return
   */
  public int[] mergeSort(int[] array) {

    // if current only have 1 element, return it.
    if (array.length <= 1) {
      return array;
    }

    int mid = array.length / 2;

    // left array is always smaller or equal to right array.
    int[] leftArray = new int[mid];//4
    int[] rightArray = new int[array.length - mid];//5

    // put element into arrays.
    for (int i = 0; i < mid; i++) { //0,1,2,3
      leftArray[i] = array[i];
    }

    // mid =4, array.length = 9, index: [4,5,6,7,8]
    for (int j = mid; j < array.length; j++) { //4,5,6,7,8
      //but rightArray start from index 0;
      rightArray[j - mid] = array[j];
    }

    // sorted subArrays.
    mergeSort(leftArray);
    mergeSort(rightArray);
    merge(array, leftArray, rightArray);

    return array;
  }

  /**
   * Description:
   * conquer part of "divide and conquer"，return the result of merge left & right array.
   *
   * @param originalArray
   * @param leftArray
   * @param rightArray
   *
   * @return
   */
  public static int[] merge(int[] originalArray, int[] leftArray, int[] rightArray) {

    int i = 0;
    int j = 0;
    int k = 0;

    // loop until one of array is empty. if an array is empty will exit.
    while (i < leftArray.length && j < rightArray.length) {
      if (leftArray[i] < rightArray[j]) {
        originalArray[k] = leftArray[i];
        i++;
      } else {
        originalArray[k] = rightArray[j];
        j++;
      }
      k++;
    }

    // array have leftover element need to put into originalArray.
    while (leftArray.length > i) {
      originalArray[k] = leftArray[i];
      i++;
      k++;
    }

    while (rightArray.length > j) {
      originalArray[k] = rightArray[j];
      j++;
      k++;
    }
    return originalArray;
  }


  /**
   * DescriptionL:
   * another case for practice.
   *
   * @param array
   *
   * @return
   */
  public int[] mergeSortEvenOdd(int[] array) {

    if (array.length <= 1) {
      return array;
    }

    int mid = array.length / 2;
    int[] left = new int[mid];
    int[] right = new int[array.length - mid];

    for (int i = 0; i < mid; i++) {
      left[i] = array[i];
    }

    for (int j = mid; j < array.length; j++) {
      right[j - mid] = array[j];
    }

    mergeSortEvenOdd(left);
    mergeSortEvenOdd(right);
    mergeWithEvenOddRule(array, left, right);


    return array;
  }

  public int[] mergeWithEvenOddRule(int[] arr, int[] left, int[] right) {

    int i = 0;
    int j = 0;
    int k = 0;
    while (left.length > i && right.length > j) {
      // compare even or odd
      if ((left[i] % 2 == 0) && (right[j] % 2 != 0)) {
        arr[k] = left[i];
        i++;
      } else if ((right[j] % 2 == 0) && (left[i] % 2 != 0)) {
        arr[k] = right[j];
        j++;
      }
      //compare big or small
      else if (left[i] > right[j]) {
        arr[k] = left[i];
        i++;
      } else {
        arr[k] = right[j];
        j++;
      }
      k++;
    }

    while (left.length > i) {
      arr[k] = left[i];
      i++;
      k++;
    }

    while (right.length > j) {
      arr[k] = right[j];
      j++;
      k++;
    }
    return arr;
  }
}
