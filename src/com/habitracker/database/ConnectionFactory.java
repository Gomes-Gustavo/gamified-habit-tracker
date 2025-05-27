package com.habitracker.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    // Configure com suas credenciais do MySQL
    private static final String URL = "jdbc:mysql://localhost:3307/habitracker_db"; // Altere nome do banco se necessário
    private static final String USER = "root"; // Ex: root
    private static final String PASSWORD = "DeepVision";

    public static Connection getConnection() {
        try {
            // Opcional para versões mais antigas do JDBC, mas bom ter:
            // Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            // Em uma aplicação real, trate essa exceção de forma mais robusta
            // (ex: logar o erro, lançar uma exceção customizada)
            System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro na conexão com o banco de dados.", e);
        }
        // catch (ClassNotFoundException e) {
        //     System.err.println("Driver JDBC do MySQL não encontrado: " + e.getMessage());
        //     e.printStackTrace();
        //     throw new RuntimeException("Driver JDBC não encontrado.", e);
        // }
    }

    public static void main(String[] args) {
        // Teste rápido da conexão (opcional)
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