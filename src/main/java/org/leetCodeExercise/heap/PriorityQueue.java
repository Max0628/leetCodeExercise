package org.leetCodeExercise.heap;

public class PriorityQueue {

  private int[] heap; // use array to store heap.
  private int size;// current element count.
  private int capacity;// capacity of array.

  public PriorityQueue(int capacity) {
    this.heap = new int[capacity];
    this.size = 0;
    this.capacity = capacity;
  }

  // O(log n)
  public void insert(int val) {

    // Case: heap is full.
    if (size == capacity) {
      throw new IllegalArgumentException("Heap is full.");
    }

    // put value into heap number "size" index location.
    // size represent the index need to put into array.
    heap[size] = val;
    // update element's location to the correct way.
    siftUp(size);// add element in heap always add to the tail of array.
    size++;
  }

  // O(1)
  // took out min value, but don't delete it.
  public int peek() {
    if (size == 0) {
      throw new IllegalArgumentException("Heap is Empty.");
    }
    return heap[0];
  }

  //O(log n)
  public int poll() {
    if (size == 0) {
      throw new IllegalArgumentException("Heap is Empty");
    }
    int min = heap[0];
    heap[0] = heap[size - 1];
    size--;
    siftDown(0);
    return min;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  private void siftUp(int i) {
    while (i > 0) {// if added index is not root.
      int parent = (i - 1) / 2;// left, right child index are suitable.
      if (heap[parent] <= heap[i]) {
        break;
      }
      swap(i, parent);// exchange value.
      i = parent; // check parent's parent until root.
    }
  }

  private void siftDown(int i) {
    while (i * 2 + 1 < size) {
      int left = i * 2 + 1;
      int right = i * 2 + 2;
      int smaller = left;
      if (right < size && heap[right] < heap[left]) {
        smaller = right;
      }
      if (heap[i] <= heap[smaller]) break;
      swap(i, smaller);
      i = smaller;
    }
  }

  private void swap(int i, int j) {
    int tmp = heap[i];
    heap[i] = heap[j];
    heap[j] = tmp;
  }
}
