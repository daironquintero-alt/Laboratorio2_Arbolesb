package com.udea.co.demo;

public class SplitResult {
    String promotedKey;
    Node left;
    Node right;

    SplitResult(String promotedKey, Node left, Node right) {
        this.promotedKey = promotedKey;
        this.left = left;
        this.right = right;
    }
}
