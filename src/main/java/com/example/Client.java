package com.example;

public class Client {

    int clientId;
    String name;
    String address;
    String category;

    public Client(int clientID, String name, String address, String category) {
        this.clientId = clientID;
        this.name = name;
        this.address = address;
        this.category = category;
    }

    public int getClientId() {
        return clientId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCategory() {
        return category;
    }
    
}
