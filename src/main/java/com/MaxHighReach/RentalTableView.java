package com.MaxHighReach;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public abstract class RentalTableView extends TableView<CustomerRental>{
    private TableView<CustomerRental> TableView;

    public RentalTableView(){
        TableView = new TableView<CustomerRental>();
        TableColumn<CustomerRental, String> fillerColumn = new TableColumn<>("Filler");
        fillerColumn.setCellValueFactory(new PropertyValueFactory<>("filler"));
        fillerColumn.setPrefWidth(100);
        TableView.getColumns().add(fillerColumn);

    }

    public TableView<CustomerRental> getTableView(){
        return TableView;
    }
}
