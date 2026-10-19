package com.udea.co.demo;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

// Pruebas independientes del menú, sin librerías externas ni datos precargados en Main.
public class BPlusTreeTest {
    private static final Field ROOT;
    private static int checks;
    static {
        try {
            ROOT = BPlusTree.class.getDeclaredField("root");
            ROOT.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void main(String[] args) throws Exception {
        PrintStream console = System.out;
        try (PrintStream quiet = new PrintStream(OutputStream.nullOutputStream())) {
            System.setOut(quiet);
            for (int order = 4; order <= 9; order++) {
                for (int seed = 0; seed < 12; seed++) {
                    BPlusTree tree = new BPlusTree(order);
                    TreeSet<String> expected = new TreeSet<>();
                    Random random = new Random(seed);
                    for (int step = 0; step < 1600; step++) {
                        String name = String.format("Nombre%03d", random.nextInt(250));
                        switch (random.nextInt(3)) {
                            case 0:
                                require(tree.insert(name).inserted == expected.add(name), "Resultado de insertar");
                                break;
                            case 1:
                                require(tree.delete(name) == expected.remove(name), "Resultado de eliminar");
                                break;
                            default:
                                require(tree.search(name) == expected.contains(name), "Resultado de buscar");
                        }
                        validate(tree, order, expected);
                    }
                    List<String> remaining = new ArrayList<>(expected);
                    Collections.shuffle(remaining, random);
                    for (String name : remaining) {
                        require(tree.delete(name), "Vaciar árbol");
                        expected.remove(name);
                        validate(tree, order, expected);
                    }
                    require(tree.isEmpty(), "Árbol vacío final");
                }
                // Inserciones ordenadas y eliminaciones en ambos sentidos fuerzan cascadas.
                for (boolean reverse : new boolean[]{false, true}) {
                    BPlusTree tree = new BPlusTree(order);
                    TreeSet<String> expected = new TreeSet<>();
                    for (int i = 0; i < 400; i++) {
                        String name = String.format("Nombre%03d", i);
                        tree.insert(name);
                        expected.add(name);
                        validate(tree, order, expected);
                    }
                    List<String> names = new ArrayList<>(expected);
                    if (reverse) Collections.reverse(names);
                    for (String name : names) {
                        tree.delete(name);
                        expected.remove(name);
                        validate(tree, order, expected);
                    }
                }
            }
            BPlusTree tree = new BPlusTree(4);
            for (String name : new String[]{"Ana", "ana", "Ángela", "José", "Juan Pérez", " Zoe "}) {
                require(tree.insert(name).inserted, "Nombres Unicode y espacios");
                require(tree.search(name.strip()), "Búsqueda de nombre normalizado");
            }
            require(!tree.insert(" Ana ").inserted, "Duplicado con espacios externos");
            for (String invalid : new String[]{null, "", "   "}) {
                try { tree.insert(invalid); throw new AssertionError("Nombre inválido aceptado"); }
                catch (IllegalArgumentException expected) { checks++; }
            }
            for (int invalid : new int[]{-1, 0, 3, 10}) {
                try { new BPlusTree(invalid); throw new AssertionError("Orden inválido aceptado"); }
                catch (IllegalArgumentException expected) { checks++; }
            }
        } finally {
            System.setOut(console);
        }
        console.println("OK: " + checks + " comprobaciones. Órdenes 4 a 9, operaciones aleatorias, cascadas y entradas especiales.");
    }

    private static void validate(BPlusTree tree, int order, TreeSet<String> expected) throws Exception {
        Node root = (Node) ROOT.get(tree);
        require((root == null) == expected.isEmpty(), "Raíz vacía");
        if (root == null) return;
        List<Node> leaves = new ArrayList<>();
        List<String> actual = new ArrayList<>();
        walk(root, root, order, 0, new int[]{-1}, leaves, actual);
        require(actual.equals(new ArrayList<>(expected)), "Contenido ordenado de hojas");
        for (int i = 0; i < leaves.size(); i++) {
            require(leaves.get(i).next == (i + 1 < leaves.size() ? leaves.get(i + 1) : null), "Enlace entre hojas");
        }
    }

    private static void walk(Node node, Node root, int order, int depth, int[] leafDepth,
                             List<Node> leaves, List<String> values) {
        require(node.keys.size() <= order - 1, "Capacidad máxima");
        if (node != root) {
            require(node.keys.size() >= (node.isLeaf ? order / 2 : (order + 1) / 2 - 1), "Ocupación mínima");
        } else {
            require(!node.keys.isEmpty(), "Raíz no vacía");
        }
        for (int i = 1; i < node.keys.size(); i++) {
            require(node.keys.get(i - 1).compareTo(node.keys.get(i)) < 0, "Claves estrictamente ordenadas");
        }
        if (node.isLeaf) {
            require(node.children.isEmpty(), "Hoja sin hijos");
            if (leafDepth[0] < 0) leafDepth[0] = depth;
            require(depth == leafDepth[0], "Todas las hojas a igual profundidad");
            leaves.add(node);
            values.addAll(node.keys);
        } else {
            require(node.children.size() == node.keys.size() + 1, "Cantidad de hijos");
            for (int i = 1; i < node.children.size(); i++) {
                Node first = node.children.get(i);
                while (!first.isLeaf) first = first.children.get(0);
                require(node.keys.get(i - 1).equals(first.keys.get(0)), "Separador = mínimo derecho");
            }
            for (Node child : node.children) walk(child, root, order, depth + 1, leafDepth, leaves, values);
        }
    }

    private static void require(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
