package core.basesyntax.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FruitStorage {
    // Инициализируем карту прямо при объявлении
    private final Map<String, Integer> fruits = new HashMap<>();

    /** Устанавливает начальный баланс или пополняет склад */
    public void addFruit(String fruitName, int quantity) {
        fruits.put(fruitName,
                fruits.getOrDefault(fruitName, 0) + quantity
        );
    }

    /** Снимает с склада указанное количество, бросает исключение, если не хватает */
    public void removeFruit(String fruitName, int quantity) {
        Integer current = fruits.get(fruitName);
        if (current == null) {
            throw new IllegalArgumentException("Fruit not found: " + fruitName);
        }
        if (current < quantity) {
            throw new IllegalArgumentException(
                    "Not enough " + fruitName + " in stock: requested="
                            + quantity + ", available=" + current
            );
        }
        fruits.put(fruitName, current - quantity);
    }

    /** Возвращает текущее состояние склада (неизменяемый вид) */
    public Map<String, Integer> getFruits() {
        return Collections.unmodifiableMap(fruits);
    }
}