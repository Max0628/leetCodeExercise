package org.leetCodeExercise.practice;

public class isValidStackTest {

  public static void main(String[] args) {
    isValidStack isValidStack= new isValidStack();

    test(isValidStack, "()", true, "Single pair parentheses");
    test(isValidStack, "()[]{}", true, "Multiple types valid");
    test(isValidStack, "(]", false, "Mismatched pair");
    test(isValidStack, "([])", true, "Nested valid");
    test(isValidStack, "([)]", false, "Nested invalid");
    test(isValidStack, "([}}])", false, "Nested invalid");
  }

  private static void test(isValidStack isValidStack, String input, boolean expected, String description) {
    boolean result = isValidStack.isValid(input);
    if (result == expected) {
      System.out.println("[PASS] " + description + ": input='" + input + "' result=" + result);
    } else {
      System.out.println("[FAIL] " + description + ": input='" + input + "' result=" + result + " expected=" + expected);
    }
  }
}
