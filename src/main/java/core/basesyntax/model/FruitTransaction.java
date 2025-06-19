package core.basesyntax.model;

import java.time.LocalDateTime;

public class FruitTransaction {
    private Operation operation;
    private String fruit;
    private int quantity;
    private LocalDateTime date;

    public FruitTransaction(Operation operation, String fruit, int quantity) {
        this.operation = operation;
        this.fruit = fruit;
        this.quantity = quantity;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getFruit() {
        return fruit;
    }

    public void setFruit(String fruit) {
        this.fruit = fruit;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getDate() {
        return date;
    }

    /** И, при желании, сеттер для даты */
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
