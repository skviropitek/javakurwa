package core.basesyntax.service.impl;

import core.basesyntax.dao.DataConverter;
import core.basesyntax.dao.FileReader;
import core.basesyntax.dao.FileWriter;
import core.basesyntax.dao.ReportGenerator;
import core.basesyntax.model.FruitTransaction;
import core.basesyntax.service.ShopService;
import core.basesyntax.strategy.OperationHandler;
import core.basesyntax.strategy.OperationStrategy;

import java.util.ArrayList;
import java.util.List;

public class ShopServiceImpl implements ShopService {
    private final OperationStrategy operationStrategy;
    private final DataConverter      dataConverter;
    private final FileReader         fileReader;
    private final FileWriter         fileWriter;
    private final ReportGenerator    reportGenerator;

    /** Внутренний список всех выполненных (и загруженных) транзакций */
    private final List<FruitTransaction> transactions = new ArrayList<>();

    public ShopServiceImpl(OperationStrategy operationStrategy,
                           DataConverter dataConverter,
                           FileReader fileReader,
                           FileWriter fileWriter,
                           ReportGenerator reportGenerator) {
        this.operationStrategy = operationStrategy;
        this.dataConverter     = dataConverter;
        this.fileReader        = fileReader;
        this.fileWriter        = fileWriter;
        this.reportGenerator   = reportGenerator;
    }

    @Override
    public void process(List<FruitTransaction> txs) {
        // очищаем старые и добавляем новые
        transactions.clear();
        transactions.addAll(txs);
        // применяем каждую
        for (FruitTransaction tx : txs) {
            OperationHandler handler = operationStrategy.getHandler(tx.getOperation());
            handler.apply(tx);
        }
    }

    @Override
    public List<FruitTransaction> loadFrom(String filename) {
        List<String> lines = fileReader.read(filename);
        List<FruitTransaction> txs = dataConverter.convertToTransaction(lines);
        process(txs);
        return txs;
    }

    @Override
    public List<FruitTransaction> getAllTransactions() {
        // возвращаем копию, чтобы внешний код не мог сломать наш внутренний список
        return new ArrayList<>(transactions);
    }

    @Override
    public String getReportText() {
        return reportGenerator.getReport();
    }

    @Override
    public void saveReportTo(String filename) {
        String report = getReportText();
        fileWriter.write(report, filename);
    }

    @Override
    public void applyOperation(FruitTransaction tx) {
        // добавляем к уже существующим
        transactions.add(tx);
        // применяем логику к складу
        OperationHandler handler = operationStrategy.getHandler(tx.getOperation());
        handler.apply(tx);
    }
}
