package com.udea.co.demo;

import java.util.ArrayList;
import java.util.List;

public class Node {
    List<String> keys = new ArrayList<>();
    List<Node> children = new ArrayList<>();
    boolean isLeaf;
    Node next;

    public Node(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    @Override
    public String toString() {
        return "[" + String.join(" | ", keys) + "]";
    }
}
