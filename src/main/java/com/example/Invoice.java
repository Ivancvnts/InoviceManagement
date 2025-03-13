package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Invoice {

    private int invoiceId;
    private int clientId;
    private String clientName;
    

    private java.sql.Date date;
    private double total;
    
    @SuppressWarnings("exports")
    public Invoice(int invoiceId, int clientId, java.sql.Date date, double total) {
        this.invoiceId = invoiceId;
        this.clientId = clientId;
        this.clientName = getClientNameFromDb(clientId);
        this.date = date;
        this.total = total;
    }

    private String getClientNameFromDb(int clientId)
    {
        String name = "";
        String query = "SELECT name FROM clients WHERE clientId = ?";
        try(Connection conn = DataBaseHandler.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query))
        {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next())
            {
                name = rs.getString("name");
            }
            
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
        return name;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public int getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    @SuppressWarnings("exports")
    public java.sql.Date getDate() {
        return date;
    }

    public double getTotal() {
        return total;
    }

    
    
}
