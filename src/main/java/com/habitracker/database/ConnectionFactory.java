package com.habitracker.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    private static final String URL = "jdbc:mysql://localhost:3307/habitracker_db"; 
    private static final String USER = "root"; 
    private static final String PASSWORD = "DeepVision";

    public static Connection getConnection() {
        try {
            
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            
            
            System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro na conexão com o banco de dados.", e);
        }
        
        
        
        
        
    }

    public static void main(String[] args) {
        
        try (Connection connection = getConnection()) {
            if (connection != null) {
                System.out.println("Conexão com o banco de dados estabelecida com sucesso!");
            } else {
                System.out.println("Falha ao conectar ao banco de dados.");
            }
        } catch (SQLException e) {
            System.err.println("Erro durante o teste de conexão: " + e.getMessage());
            e.printStackTrace();
        }
    }
}