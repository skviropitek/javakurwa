// src/main/java/core/basesyntax/ui/controllers/MainController.java
package core.basesyntax.ui.controllers;

import core.basesyntax.dao.DataConverter;
import core.basesyntax.dao.FileReader;
import core.basesyntax.dao.FileWriter;
import core.basesyntax.dao.ReportGenerator;
import core.basesyntax.dao.impl.DataConverterImpl;
import core.basesyntax.dao.impl.FileReaderImpl;
import core.basesyntax.dao.impl.FileWriterImpl;
import core.basesyntax.dao.impl.ReportGeneratorImpl;
import core.basesyntax.db.FruitStorage;
import core.basesyntax.model.FruitTransaction;
import core.basesyntax.model.Operation;
import core.basesyntax.service.ShopService;
import core.basesyntax.service.impl.ShopServiceImpl;
import core.basesyntax.strategy.OperationHandler;
import core.basesyntax.strategy.OperationStrategy;
import core.basesyntax.strategy.impl.BalanceOperationHandler;
import core.basesyntax.strategy.impl.OperationStrategyImpl;
import core.basesyntax.strategy.impl.PurchaseOperationHandler;
import core.basesyntax.strategy.impl.ReturnOperationHandler;
import core.basesyntax.strategy.impl.SupplyOperationHandler;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {
    @FXML private TableView<FruitTransaction> transactionTable;
    @FXML private TableColumn<FruitTransaction,String> colOperation;
    @FXML private TableColumn<FruitTransaction,String> colFruit;
    @FXML private TableColumn<FruitTransaction,Integer> colQty;
    @FXML private TableColumn<FruitTransaction,String> colDate;
    @FXML private ComboBox<Operation> cmbOperation;
    @FXML private TextField txtFruit;
    @FXML private Spinner<Integer> spnQuantity;
    @FXML private Label statusBar;

    private ShopService shopService;
    private Window primaryWindow;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        // Захватим окно для FileChooser
        primaryWindow = statusBar.getScene().getWindow();
        FruitStorage storage = new FruitStorage();

        // 1) Собираем бэкенд
        Map<Operation, OperationHandler> handlers = new HashMap<>();
        handlers.put(Operation.BALANCE,  new BalanceOperationHandler(storage));
        handlers.put(Operation.SUPPLY,   new SupplyOperationHandler(storage));
        handlers.put(Operation.PURCHASE, new PurchaseOperationHandler(storage));
        handlers.put(Operation.RETURN,   new ReturnOperationHandler(storage));
        OperationStrategy strategy = new OperationStrategyImpl(handlers);

        // 3) Остальные зависимости
        DataConverter   dc = new DataConverterImpl();
        FileReader      fr = new FileReaderImpl();
        FileWriter      fw = new FileWriterImpl();
        ReportGenerator rg = new ReportGeneratorImpl(storage);

        shopService = new ShopServiceImpl(strategy, dc, fr, fw, rg);

        // 2) Настраиваем таблицу
        colOperation.setCellValueFactory(new PropertyValueFactory<>("operation"));
        colFruit.setCellValueFactory(new PropertyValueFactory<>("fruit"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colDate.setCellValueFactory(c ->
                new ReadOnlyStringWrapper(c.getValue().getDate().format(dtf))
        );
        transactionTable.getItems().clear();

        // 3) Настраиваем форму операций
        cmbOperation.getItems().setAll(Operation.values());
        cmbOperation.getSelectionModel().select(Operation.SUPPLY);
        txtFruit.clear();
        spnQuantity.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0)
        );
    }

    @FXML
    private void onLoadCsv() {
        File file = new FileChooser().showOpenDialog(primaryWindow);
        if (file == null) return;
        try {
            List<FruitTransaction> txs = shopService.loadFrom(file.getAbsolutePath());
            refreshTable();
            statusBar.setText("Загружено " + txs.size() + " записей");
        } catch (Exception e) {
            showError("Ошибка загрузки CSV", e);
        }
    }

    @FXML
    private void onSaveReport() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("report.csv");
        File file = chooser.showSaveDialog(primaryWindow);
        if (file == null) return;
        try {
            shopService.saveReportTo(file.getAbsolutePath());
            statusBar.setText("Отчёт сохранён в " + file.getName());
        } catch (Exception e) {
            showError("Ошибка сохранения отчёта", e);
        }
    }

    @FXML
    private void onApplyOperation() {
        Operation op = cmbOperation.getValue();
        String fruit = txtFruit.getText().trim();
        int qty = spnQuantity.getValue();
        if (fruit.isEmpty()) {
            showError("Входные данные", new IllegalArgumentException("Название фрукта пусто"));
            return;
        }
        FruitTransaction tx = new FruitTransaction(op, fruit, qty);
        try {
            shopService.applyOperation(tx);
            refreshTable();
            statusBar.setText("Операция " + op + " выполнена");
        } catch (Exception e) {
            showError("Ошибка операции", e);
        }
    }

    @FXML
    private void onCheckBalance() {
        try {
            String report = shopService.getReportText();
            TextArea ta = new TextArea(report);
            ta.setEditable(false);
            ta.setWrapText(true);
            ta.setPrefSize(400, 300);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Текущий баланс");
            alert.getDialogPane().setContent(ta);
            alert.showAndWait();
            statusBar.setText("Баланс показан");
        } catch (Exception e) {
            showError("Ошибка при проверке баланса", e);
        }
    }

    private void refreshTable() {
        transactionTable.getItems().setAll(shopService.getAllTransactions());
    }

    private void showError(String header, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(ex.getMessage());
        alert.showAndWait();
        statusBar.setText("Ошибка: " + header);
    }
}
