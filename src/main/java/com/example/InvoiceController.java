package com.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class InvoiceController {

    private boolean isEditing = false;
    private boolean isSearching = false;

    @FXML
    private Button addBtn;

    @FXML
    private Button EditBtn;

    @FXML
    private Button SearchBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button backToMenuBtn;

    @FXML
    private ComboBox<String> clientNameField;

    @FXML
    private DatePicker dateField;

    @FXML
    private TextField totalField;

    @FXML
    private TableView<Invoice> tableView;

    @FXML
    private TableColumn<Invoice, Integer> invoiceIdColumn;

    @FXML
    private TableColumn<Invoice, String> nameColumn;

    @FXML
    private TableColumn<Invoice, java.sql.Date> dateColumn;

    @FXML
    private TableColumn<Invoice, Double> totalColumn;
    
    @FXML
    public void initialize() {
        invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        loadClients();

        refreshTable();
    }

    private void loadClients()
    {
        String query = "SELECT DISTINCT name FROM clients";

        try(Connection conn = DataBaseHandler.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query))
            {

                ObservableList<String> clientNames = FXCollections.observableArrayList();

                while (rs.next()) {
                    clientNames.add(rs.getString("name"));
                }

                clientNameField.setItems(clientNames);

            }catch(SQLException e){
                e.printStackTrace();
            }
    }

    private ObservableList<Invoice> getAllInovices()
    {
        ObservableList<Invoice> invoices = FXCollections.observableArrayList();
        String query = "SELECT * FROM invoices";
        
        try(Connection conn = DataBaseHandler.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query))
        {
            while(rs.next())
            {
                int invoiceId = rs.getInt("invoice_id");
                int clientId = rs.getInt("clientId");
                java.sql.Date date = rs.getDate("date");
                double total = rs.getDouble("total");
                Invoice invoice = new Invoice(invoiceId, clientId, date, total);               
                invoices.add(invoice);
            }
        }catch(SQLException e)
        {
            e.printStackTrace();
        }

        return invoices;
    }

    private void refreshTable()
    {
        tableView.setItems(getAllInovices());
    }

    @FXML
    void addInvoice(ActionEvent event) {

        if(isEditing)
        {
            String query = "UPDATE invoices SET clientId = ?, date = ?, total = ? WHERE invoice_id = ?";

            try(Connection conn = DataBaseHandler.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query))
            {
                Invoice invoice = tableView.getSelectionModel().getSelectedItem();
                int invoiceId = invoice.getInvoiceId();
                int clientId = getClientIdFromDb(clientNameField.getValue());
                java.sql.Date date = java.sql.Date.valueOf(dateField.getValue());
                double total = Double.parseDouble(totalField.getText());

                pstmt.setInt(1, clientId);
                pstmt.setDate(2, date);
                pstmt.setDouble(3, total);
                pstmt.setInt(4, invoiceId);
                pstmt.executeUpdate();
                refreshTable();
            }catch(SQLException e)
            {
                e.printStackTrace();
            }

            clientNameField.setValue(null);
            dateField.setValue(null);
            totalField.clear();
            tableView.setDisable(false);
            updateAddButton(false);
        }
        else
        {
            int clientId = getClientIdFromDb(clientNameField.getValue());
            java.sql.Date date = java.sql.Date.valueOf(dateField.getValue());
            double total = Double.parseDouble(totalField.getText());
            
            String query = "INSERT INTO invoices(clientId, date, total) VALUES(?,?,?)";
            try(Connection conn = DataBaseHandler.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query))
            {
                pstmt.setInt(1, clientId);
                pstmt.setDate(2, (java.sql.Date) date);
                pstmt.setDouble(3, total);
                pstmt.executeUpdate();
                refreshTable();
            }catch(SQLException e)
            {
                e.printStackTrace();
            }catch(NullPointerException e)
            {
                e.printStackTrace();
            }

            clientNameField.setValue(null);
            dateField.setValue(null);
            totalField.clear();
            tableView.setDisable(false);
            updateAddButton(false);
        }        
    }

    @FXML
    void editInvoice(ActionEvent event) {
        Invoice invoice = tableView.getSelectionModel().getSelectedItem();
        clientNameField.setValue(invoice.getClientName());
        dateField.setValue(invoice.getDate().toLocalDate());
        totalField.setText(String.valueOf(invoice.getTotal()));
        tableView.setDisable(true);
        updateAddButton(true);
    }

    @FXML
    void deleteInvoice(ActionEvent event) {
        Invoice invoice = tableView.getSelectionModel().getSelectedItem();
        int invoiceId = invoice.getInvoiceId();
        String query = "DELETE FROM invoices WHERE invoice_id = ?";
        try(Connection conn = DataBaseHandler.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setInt(1, invoiceId);
            pstmt.executeUpdate();
            refreshTable();
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    @FXML
    void searchInvoiceByName(ActionEvent event) {

        if(isSearching)
        {
            clientNameField.setValue(null);
            SearchBtn.setText("Search by Client Name");
            refreshTable();
            isSearching = false;
            return;
        }
        else
        {
            String query = "SELECT * FROM invoices WHERE clientId = ?";

            try(Connection conn = DataBaseHandler.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query))
            {
                pstmt.setInt(1, getClientIdFromDb(clientNameField.getValue()));
                ResultSet rs = pstmt.executeQuery();
                ObservableList<Invoice> invoices = FXCollections.observableArrayList();
                while(rs.next())
                {
                    int invoiceId = rs.getInt("invoice_id");
                    int clientId = rs.getInt("clientId");
                    java.sql.Date date = rs.getDate("date");
                    double total = rs.getDouble("total");
                    Invoice invoice = new Invoice(invoiceId, clientId, date, total);
                    invoices.add(invoice);
                }
                tableView.setItems(invoices);
            }catch(SQLException e)
            {
                e.printStackTrace();
            }
            
            SearchBtn.setText("Clear Search");
            isSearching = true;
        }
    }

    private int getClientIdFromDb(String clientName)
    {
        String query = "SELECT clientId FROM clients WHERE name = ?";
        try(Connection conn = DataBaseHandler.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setString(1, clientName);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
            {
                return rs.getInt("clientId");
            }
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    private void updateAddButton(boolean isEditing)
    {
        this.isEditing = isEditing;
        addBtn.setText(isEditing ? "Save" : "Add");
    }

    @FXML
    void backToMenuScene(ActionEvent event) throws IOException {
        App.setRoot("mainscene");
    }

}
