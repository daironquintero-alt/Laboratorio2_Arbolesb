package com.udea.co.demo;

import java.util.LinkedList;
import java.util.Queue;

public class BPlusTree {
    private final int order;
    private Node root;

    public BPlusTree(int order) {
        if (order < 4 || order > 9) {
            throw new IllegalArgumentException("El orden debe estar entre 4 y 9.");
        }
        this.order = order;
        this.root = null;
    }

    public boolean isEmpty() {
        return root == null;
    }

    // Comparación lexicográfica de Java: distingue mayúsculas y tildes.
    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        return name.strip();
    }

    private int findPosition(Node node, String key) {
        int i = 0;
        while (i < node.keys.size() && key.compareTo(node.keys.get(i)) > 0) {
            i++;
        }
        return i;
    }

    // La igualdad con un separador SIEMPRE conduce al hijo derecho.
    private int findChild(Node node, String key) {
        int i = 0;
        while (i < node.keys.size() && key.compareTo(node.keys.get(i)) >= 0) {
            i++;
        }
        return i;
    }

    private void traceChild(Node node, int index) {
        System.out.println("Nodo interno " + node + ": bajar al hijo " + index + ".");
    }

    public boolean search(String name) {
        String key = validateName(name);
        Node node = root;
        if (node == null) {
            System.out.println("Búsqueda: árbol vacío.");
            return false;
        }
        while (!node.isLeaf) {
            int index = findChild(node, key);
            traceChild(node, index);
            node = node.children.get(index);
        }
        int pos = findPosition(node, key);
        boolean found = pos < node.keys.size() && node.keys.get(pos).equals(key);
        System.out.println("Buscar en hoja " + node + ": " + (found ? "encontrado." : "no existe."));
        return found;
    }

    public InsertResult insert(String name) {
        String key = validateName(name);
        if (root == null) {
            root = new Node(true);
            root.keys.add(key);
            System.out.println("Inserción normal: se crea la raíz hoja " + root + ".");
            return new InsertResult(true, false);
        }
        InsertState state = new InsertState();
        SplitResult split = insertRecursive(root, key, state);
        if (split != null) {
            Node newRoot = new Node(false);
            newRoot.keys.add(split.promotedKey);
            newRoot.children.add(split.left);
            newRoot.children.add(split.right);
            root = newRoot;
            System.out.println("Split de raíz: nueva raíz " + root + ". Aumenta la altura.");
        }
        return new InsertResult(state.inserted, state.hadOverflow);
    }

    private SplitResult insertRecursive(Node node, String key, InsertState state) {
        if (node.isLeaf) {
            int pos = findPosition(node, key);
            if (pos < node.keys.size() && node.keys.get(pos).equals(key)) {
                System.out.println("Nombre duplicado en " + node + ". No se inserta.");
                return null;
            }
            node.keys.add(pos, key);
            state.inserted = true;
            System.out.println("Insertar '" + key + "' en hoja: " + node + ".");
            if (node.keys.size() >= order) {
                state.hadOverflow = true;
                return splitNode(node);
            }
            return null;
        }
        int index = findChild(node, key);
        traceChild(node, index);
        SplitResult split = insertRecursive(node.children.get(index), key, state);
        if (split != null) {
            node.keys.add(index, split.promotedKey);
            node.children.set(index, split.left);
            node.children.add(index + 1, split.right);
            if (node.keys.size() >= order) {
                state.hadOverflow = true;
                return splitNode(node);
            }
        }
        return null;
    }

    private SplitResult splitNode(Node node) {
        int mid = (node.keys.size() - 1) / 2;
        // Se conserva el nodo izquierdo para no romper el enlace de la hoja anterior.
        Node right = new Node(node.isLeaf);
        String promotedKey;
        System.out.println("Overflow en " + node + ". Split " + (node.isLeaf ? "de hoja." : "interno."));
        if (node.isLeaf) {
            right.keys.addAll(node.keys.subList(mid + 1, node.keys.size()));
            node.keys.subList(mid + 1, node.keys.size()).clear();
            right.next = node.next;
            node.next = right;
            promotedKey = right.keys.get(0);
            System.out.println("Copiar '" + promotedKey + "' al padre; permanece en la hoja derecha.");
        } else {
            promotedKey = node.keys.get(mid);
            right.keys.addAll(node.keys.subList(mid + 1, node.keys.size()));
            right.children.addAll(node.children.subList(mid + 1, node.children.size()));
            node.keys.subList(mid, node.keys.size()).clear();
            node.children.subList(mid + 1, node.children.size()).clear();
            System.out.println("Promover mediana inferior '" + promotedKey + "' y repartir los hijos.");
        }
        System.out.println("Resultado del split: " + node + " y " + right + ".");
        return new SplitResult(promotedKey, node, right);
    }

    public boolean delete(String name) {
        String key = validateName(name);
        if (root == null) {
            System.out.println("Eliminación: árbol vacío. El nombre no existe.");
            return false;
        }
        if (!deleteRecursive(root, key)) {
            return false;
        }
        if (!root.isLeaf && root.children.size() == 1) {
            root = root.children.get(0);
            System.out.println("Caso 3: la raíz perdió su último separador; su único hijo es la nueva raíz. Disminuye la altura.");
        }
        if (root.isLeaf && root.keys.isEmpty()) {
            root = null;
            System.out.println("Se eliminó el último nombre. El árbol queda vacío.");
        }
        return true;
    }

    private boolean deleteRecursive(Node node, String key) {
        if (node.isLeaf) {
            int pos = findPosition(node, key);
            if (pos == node.keys.size() || !node.keys.get(pos).equals(key)) {
                System.out.println("Nombre ausente en hoja " + node + ". No se elimina.");
                return false;
            }
            node.keys.remove(pos);
            System.out.println("Eliminar '" + key + "' de la hoja: " + node + ".");
            if (node == root || node.keys.size() >= minKeys(node)) {
                System.out.println("Caso 1: eliminación simple, sin underflow (la raíz tiene mínimo especial).");
            }
            return true;
        }
        int index = findChild(node, key);
        traceChild(node, index);
        Node child = node.children.get(index);
        if (!deleteRecursive(child, key)) {
            return false;
        }
        if (child.keys.size() < minKeys(child)) {
            repairUnderflow(node, index);
        }
        updateSeparators(node);
        return true;
    }

    private int minKeys(Node node) {
        // Hojas: ceil((m - 1) / 2). Internos: ceil(m / 2) - 1.
        return node.isLeaf ? order / 2 : (order + 1) / 2 - 1;
    }

    private void repairUnderflow(Node parent, int index) {
        Node node = parent.children.get(index);
        Node left = index > 0 ? parent.children.get(index - 1) : null;
        Node right = index + 1 < parent.children.size() ? parent.children.get(index + 1) : null;
        System.out.println("Underflow " + (node.isLeaf ? "de hoja " : "interno ") + node + ". Mínimo: " + minKeys(node) + ".");
        if (left != null && left.keys.size() > minKeys(left)) {
            if (node.isLeaf) {
                node.keys.add(0, left.keys.remove(left.keys.size() - 1));
            } else {
                node.children.add(0, left.children.remove(left.children.size() - 1));
                updateSeparators(left);
                updateSeparators(node);
            }
            System.out.println("Caso 2a: préstamo del hermano izquierdo. Resultado: " + left + " y " + node + ".");
        } else if (right != null && right.keys.size() > minKeys(right)) {
            if (node.isLeaf) {
                node.keys.add(right.keys.remove(0));
            } else {
                node.children.add(right.children.remove(0));
                updateSeparators(right);
                updateSeparators(node);
            }
            System.out.println("Caso 2b: préstamo del hermano derecho. Resultado: " + node + " y " + right + ".");
        } else if (left != null) {
            mergeNodes(left, node);
            parent.children.remove(index);
            System.out.println("Caso 3: fusión con hermano izquierdo. Resultado: " + left + ". El padre pierde un hijo y un separador.");
        } else if (right != null) {
            mergeNodes(node, right);
            parent.children.remove(index + 1);
            System.out.println("Caso 3: fusión con hermano derecho. Resultado: " + node + ". El padre pierde un hijo y un separador.");
        }
    }

    private void mergeNodes(Node left, Node right) {
        if (left.isLeaf) {
            left.keys.addAll(right.keys);
            left.next = right.next;
            System.out.println("Enlace de hojas actualizado. El separador del padre no se agrega como dato.");
        } else {
            left.children.addAll(right.children);
            // Reconstruir desde los hijos incluye el separador que desciende del padre.
            updateSeparators(left);
        }
    }

    private String minimum(Node node) {
        while (!node.isLeaf) {
            node = node.children.get(0);
        }
        return node.keys.get(0);
    }

    // Sólo actualiza el nodo de la ruta visitada, sin recorrer todo el árbol.
    private void updateSeparators(Node node) {
        var previous = new java.util.ArrayList<>(node.keys);
        node.keys.clear();
        for (int i = 1; i < node.children.size(); i++) {
            node.keys.add(minimum(node.children.get(i)));
        }
        if (!previous.equals(node.keys)) {
            System.out.println("Actualizar separadores: " + previous + " -> " + node + ".");
        }
    }

    public void printByLevels() {
        if (root == null) {
            System.out.println("El árbol está vacío.");
            return;
        }
        Queue<NodeLevel> queue = new LinkedList<>();
        queue.add(new NodeLevel(root, 0));
        int currentLevel = -1;
        while (!queue.isEmpty()) {
            NodeLevel current = queue.poll();
            if (current.level != currentLevel) {
                currentLevel = current.level;
                System.out.print("Nivel " + currentLevel + ": ");
            }
            System.out.print((current.node.isLeaf ? "Hoja " : "Interno ") + current.node + " ");
            for (Node child : current.node.children) {
                queue.add(new NodeLevel(child, current.level + 1));
            }
            if (queue.isEmpty() || queue.peek().level != currentLevel) {
                System.out.println();
            }
        }
        System.out.println("Jerarquía (los hijos aparecen de izquierda a derecha):");
        printHierarchy(root, "", "Raíz");
        System.out.print("Hojas enlazadas: ");
        Node leaf = root;
        while (!leaf.isLeaf) {
            leaf = leaf.children.get(0);
        }
        while (leaf != null) {
            System.out.print(leaf + " -> ");
            leaf = leaf.next;
        }
        System.out.println("null");
    }

    private void printHierarchy(Node node, String indent, String label) {
        System.out.println(indent + label + ": " + node + (node.isLeaf ? " (hoja)" : ""));
        for (int i = 0; i < node.children.size(); i++) {
            printHierarchy(node.children.get(i), indent + "    ", "Hijo " + i);
        }
    }
}
