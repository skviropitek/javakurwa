package core.basesyntax.dao.impl;

import core.basesyntax.dao.DataConverter;
import core.basesyntax.model.FruitTransaction;
import core.basesyntax.model.Operation;

import java.util.ArrayList;
import java.util.List;

/**
 * Умеет читать и «обычный» транзакционный CSV (3 поля),
 * и наш отчёт (2 поля: fruit,quantity), превращая его в BALANCE-операции.
 */
public class DataConverterImpl implements DataConverter {
    private static final String COMMA_SEPARATOR      = ",";
    private static final int    TRANSACTION_FIELDS   = 3;
    private static final int    REPORT_FIELDS        = 2;
    private static final int    MINIMAL_QUANTITY     = 0;

    @Override
    public List<FruitTransaction> convertToTransaction(List<String> report) {
        List<FruitTransaction> txs = new ArrayList<>();
        if (report == null || report.isEmpty()) {
            return txs;
        }
        // Определяем формат по первой (заголовочной) строке
        String header = report.get(0).trim();
        String[] headerParts = header.split(COMMA_SEPARATOR);
        boolean isReportFormat = headerParts.length == REPORT_FIELDS
                && headerParts[0].equalsIgnoreCase("fruit")
                && headerParts[1].equalsIgnoreCase("quantity");

        // Считываем со второй строки, пропуская заголовок
        for (int i = 1; i < report.size(); i++) {
            String line = report.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split(COMMA_SEPARATOR);
            try {
                if (isReportFormat) {
                    // формат отчёта: fruit,quantity
                    if (parts.length != REPORT_FIELDS) {
                        continue;
                    }
                    String fruit = parts[0].trim();
                    int    qty   = Integer.parseInt(parts[1].trim());
                    if (qty < MINIMAL_QUANTITY) {
                        continue;
                    }
                    // превращаем в BALANCE-операцию
                    txs.add(new FruitTransaction(Operation.BALANCE, fruit, qty));
                } else {
                    // обычный транзакционный формат: op,fruit,quantity
                    if (parts.length != TRANSACTION_FIELDS) {
                        continue;
                    }
                    Operation op  = Operation.getOperation(parts[0].trim());
                    String    fruit = parts[1].trim();
                    int       qty   = Integer.parseInt(parts[2].trim());
                    if (qty < MINIMAL_QUANTITY) {
                        continue;
                    }
                    txs.add(new FruitTransaction(op, fruit, qty));
                }
            } catch (IllegalArgumentException ex) {
                // пропускаем невалидную строку
            }
        }
        return txs;
    }
}
