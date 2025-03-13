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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ClientsController {
    
    private boolean isEditing = false;
    private boolean isSearching = false;

    @FXML
    private Button addBtn;

    @FXML
    private Button EditBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button SearchBtn;

    @FXML
    private Button backToMenuBtn;

    @FXML
    private TextField nameField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField categoryField;

    @FXML
    private TableView<Client> tableView;
    
    @FXML
    private TableColumn<Client, Integer> clientIdColumn;

    @FXML
    private TableColumn<Client, String> nameColumn;

    @FXML
    private TableColumn<Client, String> addressColumn;    

    @FXML
    private TableColumn<Client, String> categoryColumn;

    @FXML
    public void initialize() {
        clientIdColumn.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        refreshTable();
    }


    private ObservableList<Client> getAllClients()
    {
        ObservableList<Client> clients = FXCollections.observableArrayList();
        String query = "SELECT * FROM clients";
        
        try(Connection conn = DataBaseHandler.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query))
        {
            while(rs.next())
            {
                int clientId = rs.getInt("clientId");
                String name = rs.getString("name");
                String address = rs.getString("address");
                String category = rs.getString("category");
                
                Client client = new Client(clientId, name, address, category);
                clients.add(client);
            }
        }catch(SQLException e)
        {
            e.printStackTrace();
        }

        return clients;
    }

    private void refreshTable()
    {
        tableView.setItems(getAllClients());
    }

    @FXML
    void addClient(ActionEvent event) {
        if(isEditing)
        {
            String query = "UPDATE clients SET name = ?, address = ?, category = ? WHERE clientId = ?";

            try(Connection conn = DataBaseHandler.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query))
            {
                Client client = tableView.getSelectionModel().getSelectedItem();
                int clientId = client.getClientId();
                String name = nameField.getText();
                String adress = addressField.getText();
                String category = categoryField.getText();

                pstmt.setString(1, name);
                pstmt.setString(2, adress);
                pstmt.setString(3, category);
                pstmt.setInt(4, clientId);
                pstmt.executeUpdate();
                refreshTable();
            }catch(SQLException e)
            {
                e.printStackTrace();
            }

            nameField.clear();
            addressField.clear();
            categoryField.clear();
            tableView.setDisable(false);
            updateAddButton(false);
        }
        else
        {
            String name = nameField.getText();
            String address = addressField.getText();
            String category = categoryField.getText();
            
            String query = "INSERT INTO clients(name, address, category) VALUES(?,?,?)";
            try(Connection conn = DataBaseHandler.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query))
            {
                pstmt.setString(1, name);
                pstmt.setString(2, address);
                pstmt.setString(3, category);
                pstmt.executeUpdate();
                refreshTable();
            }catch(SQLException e)
            {
                e.printStackTrace();
            }catch(NullPointerException e)
            {
                e.printStackTrace();
            }
        }        
    }

    @FXML
    void deleteClient(ActionEvent event) {
        Client client = tableView.getSelectionModel().getSelectedItem();
        int clientId = client.getClientId();
        String query = "DELETE FROM clients WHERE clientId = ?";
        try(Connection conn = DataBaseHandler.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query))
        {
            pstmt.setInt(1, clientId);
            pstmt.executeUpdate();
            refreshTable();
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    @FXML
    void editClient(ActionEvent event) {
        Client client = tableView.getSelectionModel().getSelectedItem();
        nameField.setText(client.getName());
        addressField.setText(client.getAddress());
        categoryField.setText(client.getCategory());
        tableView.setDisable(true);
        updateAddButton(true);
    }

    @FXML
    void searchByName(ActionEvent event) {
        if(isSearching)
        {
            SearchBtn.setText("Search by Client Name");
            refreshTable();
            isSearching = false;
            return;
        }
        else
        {
            String query = "SELECT * FROM clients WHERE name = ?";

            try(Connection conn = DataBaseHandler.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query))
            {
                pstmt.setString(1, nameField.getText());
                ResultSet rs = pstmt.executeQuery();
                ObservableList<Client> clients = FXCollections.observableArrayList();
                while(rs.next())
                {
                    int clientId = rs.getInt("clientId");
                    String name = rs.getString("name");
                    String adress = rs.getString("address");
                    String category = rs.getString("category");
                    Client client = new Client(clientId, name, adress, category);
                    clients.add(client);
                }
                tableView.setItems(clients);
            }catch(SQLException e)
            {
                e.printStackTrace();
            }
            SearchBtn.setText("Clear Search");
            isSearching = true;
        }
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
