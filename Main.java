import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {
    private static final String FILE_PATH = "data.json";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== JSON CRUD Menu ===");
            System.out.println("1. Read all items");
            System.out.println("2. Add item");
            System.out.println("3. Update item");
            System.out.println("4. Delete item");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");
            String line = sc.nextLine().trim();
            int ch;
            try { ch = Integer.parseInt(line); } catch (NumberFormatException e) { ch = -1; }

            switch (ch) {
                case 1 -> readItems();
                case 2 -> addItem(sc);
                case 3 -> updateItem(sc);
                case 4 -> deleteItem(sc);
                case 5 -> {
                    System.out.println("Exiting...");
                    sc.close();
                    return;
                }
                default -> System.out.println("Invalid choice! Enter a number 1-5.");
            }
        }
    }

    private static JSONArray readFile() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(FILE_PATH)) {
            Object obj = parser.parse(reader);
            return (JSONArray) obj;
        } catch (IOException | ParseException e) {
            // If file doesn't exist or is empty/invalid, return empty array
            return new JSONArray();
        }
    }

    private static void writeFile(JSONArray array) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            writer.write(array.toJSONString());
            writer.flush();
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }

    private static long nextId(JSONArray arr) {
        long max = 0;
        for (Object o : arr) {
            JSONObject item = (JSONObject) o;
            Number idNum = (Number) item.get("id");
            if (idNum != null && idNum.longValue() > max) max = idNum.longValue();
        }
        return max + 1;
    }

    private static void readItems() {
        JSONArray arr = readFile();
        if (arr.isEmpty()) {
            System.out.println("No items found.");
            return;
        }
        System.out.println("\nItems:");
        for (Object o : arr) {
            JSONObject item = (JSONObject) o;
            System.out.printf("id: %s | name: %s | price: %s%n",
                    item.get("id"), item.get("name"), item.get("price"));
        }
    }

    private static void addItem(Scanner sc) {
        JSONArray arr = readFile();
        System.out.print("Enter name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        System.out.print("Enter price: ");
        String priceLine = sc.nextLine().trim();
        double price;
        try { price = Double.parseDouble(priceLine); } catch (NumberFormatException e) {
            System.out.println("Invalid price.");
            return;
        }

        JSONObject newItem = new JSONObject();
        newItem.put("id", nextId(arr));
        newItem.put("name", name);
        newItem.put("price", price);
        arr.add(newItem);
        writeFile(arr);
        System.out.println("Item added: " + newItem.toJSONString());
    }

    private static void updateItem(Scanner sc) {
        JSONArray arr = readFile();
        System.out.print("Enter ID to update: ");
        String idLine = sc.nextLine().trim();
        long id;
        try { id = Long.parseLong(idLine); } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }
        boolean found = false;
        for (Object o : arr) {
            JSONObject item = (JSONObject) o;
            Number idNum = (Number) item.get("id");
            if (idNum != null && idNum.longValue() == id) {
                System.out.print("Enter new name (leave blank to keep): ");
                String name = sc.nextLine().trim();
                System.out.print("Enter new price (leave blank to keep): ");
                String priceLine = sc.nextLine().trim();

                if (!name.isEmpty()) item.put("name", name);
                if (!priceLine.isEmpty()) {
                    try {
                        double newPrice = Double.parseDouble(priceLine);
                        item.put("price", newPrice);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid price. Keeping old price.");
                    }
                }
                found = true;
                break;
            }
        }
        if (found) {
            writeFile(arr);
            System.out.println("Item updated.");
        } else {
            System.out.println("Item not found.");
        }
    }

    private static void deleteItem(Scanner sc) {
        JSONArray arr = readFile();
        System.out.print("Enter ID to delete: ");
        String idLine = sc.nextLine().trim();
        long id;
        try { id = Long.parseLong(idLine); } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }
        boolean removed = arr.removeIf(o -> {
            JSONObject item = (JSONObject) o;
            Number idNum = (Number) item.get("id");
            return idNum != null && idNum.longValue() == id;
        });
        if (removed) {
            writeFile(arr);
            System.out.println("Item deleted.");
        } else {
            System.out.println("Item not found.");
        }
    }
}
