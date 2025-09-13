package org.leetCodeExercise.practice;

import java.util.HashMap;
import java.util.Map;

public class SlidingWindow {

  public int lengthOfLongestSubstring(String s) {

    if (s.length() == 0 || s == null) {
      return -1;
    }

    int left = 0;
    int max = 0;

    HashMap<Character, Integer> map = new HashMap<>();
    // if right touch string's end, it's traverse all subString of string.
    for (int right = 0; right < s.length(); right++) {
      char c = s.charAt(right);
      // if map already have this key, check it index
      // if the index is smaller than left, represent it is not in current window.
      // if it's bigger than left, it means it's in the window.
      // we need to update the index to index +1's location.
      if (map.containsKey(c) && map.get(c) >= left) {
        left = map.get(c) + 1;
      }
      map.put(c, right);
      max = Math.max(max, right - left + 1);
    }
    return max;
  }

  public int lengthOfLongestSubstringV2(String s) {
    if (s == null || s.length() == 0) {
      return 0;
    }

    Map<Character, Integer> map = new HashMap<>();
    int left = 0;
    int right = 0;
    int maxSize = 0;

    // if right pointer is equals to last element of string, exit loop.
    while (right < s.length()) {
      char c = s.charAt(right);

      // if map doesn't contain element, just go and put current element in.
      // if map contain current element, and this element is in the current windows interval.
      if (map.containsKey(c) && map.get(c) >= left) {
        // first move the left index to current element's index in the map.
        // and move the index + 1, because old index contain the element.
        left = map.get(c) + 1;
      }

      map.put(c, right);
      maxSize = Math.max(maxSize, right - left + 1);

      // every time move right pointer +1;
      right++;
    }

    return maxSize;
  }
}
