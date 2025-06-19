package core.basesyntax.service;

import core.basesyntax.model.FruitTransaction;
import java.util.List;

public interface ShopService {
    /** Применить список транзакций к складу */
    void process(List<FruitTransaction> transactions);

    /** Прочитать CSV-файл, сконвертировать в транзакции и применить их */
    List<FruitTransaction> loadFrom(String filename);

    /** Вернуть все транзакции, которые уже были обработаны */
    List<FruitTransaction> getAllTransactions();

    /** Сгенерировать текст отчёта по текущему состоянию склада */
    String getReportText();

    /** Записать сгенерированный отчёт в файл */
    void saveReportTo(String filename);

    void applyOperation(FruitTransaction fruitTransaction);
}