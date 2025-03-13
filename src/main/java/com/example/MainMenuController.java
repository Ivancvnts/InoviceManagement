package com.example;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MainMenuController {
    @FXML
    private Button clientsBtn;

    @FXML
    private Button invoicesBtn;

    @FXML
    void openClientsMenu(ActionEvent event) throws IOException {
        App.setRoot("clientscene");
    }

    @FXML
    void openInvoiceMenu(ActionEvent event) throws IOException {
        App.setRoot("invoicescene");
    }

}
