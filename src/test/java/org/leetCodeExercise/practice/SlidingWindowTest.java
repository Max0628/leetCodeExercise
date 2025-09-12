package org.leetCodeExercise.practice;

public class SlidingWindowTest {

  public static void main(String[] args) {
    SlidingWindow sw = new SlidingWindow();

//    test(sw, "", -1, "Empty string");
//    test(sw, "a", 1, "Single char");
//    test(sw, "abc", 3, "No repeat");
//    test(sw, "abcabcbb", 3, "With repeats");
//    test(sw, "bbbbb", 1, "All same char");
    test(sw, "pwwkew", 3, "Complex case");
  }

  private static void test(SlidingWindow sw, String input, int expected, String description) {
    int result = sw.lengthOfLongestSubstring(input);
    if (result == expected) {
      System.out.println("[PASS] " + description + ": input='" + input + "' result=" + result);
    } else {
      System.out.println("[FAIL] " + description + ": input='" + input + "' result=" + result + " expected=" + expected);
    }
  }
}
