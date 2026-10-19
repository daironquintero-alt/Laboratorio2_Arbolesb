package com.udea.co.demo;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            Integer order = askOrder(sc);
            if (order == null) return;
            BPlusTree tree = new BPlusTree(order);
            int option;
            do {
                showMenu();
                Integer input = readInt(sc, "Seleccione una opción: ");
                if (input == null) return;
                option = input;
                switch (option) {
                    case 1:
                        tree.printByLevels();
                        break;
                    case 2:
                    case 3:
                    case 4:
                        String name = readName(sc);
                        if (name == null) return;
                        if (option == 2) {
                            InsertResult result = tree.insert(name);
                            System.out.println(!result.inserted ? "El nombre ya existe." :
                                    result.hadOverflow ? "Inserción CON OVERFLOW/SPLIT." : "Inserción NORMAL.");
                        } else if (option == 3) {
                            System.out.println(tree.search(name) ? "El nombre existe." : "El nombre no existe.");
                        } else {
                            System.out.println(tree.delete(name) ? "Nombre eliminado." : "El nombre no existe.");
                        }
                        break;
                    case 5:
                        System.out.println("Saliendo...");
                        break;
                    default:
                        System.out.println("Opción inválida.");
                }
                System.out.println();
            } while (option != 5);
        }
    }

    private static Integer askOrder(Scanner sc) {
        while (true) {
            Integer order = readInt(sc, "Ingrese el orden del árbol B+ (4 a 9): ");
            if (order == null || (order >= 4 && order <= 9)) return order;
            System.out.println("El orden debe estar entre 4 y 9.");
        }
    }

    private static void showMenu() {
        System.out.println("=================================");
        System.out.println("         MENÚ ÁRBOL B+");
        System.out.println("=================================");
        System.out.println("1. Visualizar árbol");
        System.out.println("2. Agregar nombre");
        System.out.println("3. Buscar nombre");
        System.out.println("4. Eliminar nombre");
        System.out.println("5. Salir");
    }

    private static Integer readInt(Scanner sc, String message) {
        while (true) {
            System.out.print(message);
            if (!sc.hasNextLine()) return null;
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Ingrese un número entero.");
            }
        }
    }

    private static String readName(Scanner sc) {
        while (true) {
            System.out.print("Ingrese el nombre: ");
            if (!sc.hasNextLine()) return null;
            String name = sc.nextLine().strip();
            if (!name.isEmpty()) return name;
            System.out.println("El nombre no puede estar vacío.");
        }
    }
}
