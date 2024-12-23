package com.example.groupprojectapp2;

public class Model {
    private static Model instance;
    private String currentUser;
    private String doc;
    private String stock;

    // Private constructor to prevent instantiation
    private Model() {}

    // Public method to get the single instance of Model
    public static Model getInstance() {
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }

    public String getCurrentUser() {
        
        return currentUser;
    }
    public String getCurrentUserDoc() {

        return doc;
    }
//
    public void setCurrentUser(String currentUser,String doc) {
        this.currentUser = currentUser;
        this.doc = doc;
    }
    public void setCurrentstock(String stock) {
        this.stock = stock;

    }
    public String getCurrentstock() {

        return stock;
    }
}