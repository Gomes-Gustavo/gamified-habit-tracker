package com.habitracker.database;

import com.habitracker.model.Habit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.Date;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;

public class HabitDAO {

    public boolean addHabit(Habit habit) {
        String sql = "INSERT INTO habits (name, description, creationDate) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, habit.getName());
            pstmt.setString(2, habit.getDescription());
            pstmt.setDate(3, Date.valueOf(habit.getCreationDate()));
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar hábito: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Habit> getAllHabits() {
        List<Habit> habits = new ArrayList<>();
        String sql = "SELECT id, name, description, creationDate FROM habits";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                LocalDate creationDate = rs.getDate("creationDate").toLocalDate();
                habits.add(new Habit(id, name, description, creationDate));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar hábitos: " + e.getMessage());
            e.printStackTrace();
        }
        return habits;
    }

    public Habit getHabitById(int habitId) {
        String sql = "SELECT id, name, description, creationDate FROM habits WHERE id = ?";
        Habit habit = null;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    LocalDate creationDate = rs.getDate("creationDate").toLocalDate();
                    habit = new Habit(id, name, description, creationDate);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar hábito por ID (" + habitId + "): " + e.getMessage());
            e.printStackTrace();
        }
        return habit;
    }

    public boolean updateHabit(Habit habit) {
        String sql = "UPDATE habits SET name = ?, description = ?, creationDate = ? WHERE id = ?";
        if (habit == null || habit.getId() <= 0) {
            System.err.println("Erro ao atualizar hábito: Hábito nulo ou ID inválido.");
            return false;
        }
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, habit.getName());
            pstmt.setString(2, habit.getDescription());
            pstmt.setDate(3, Date.valueOf(habit.getCreationDate()));
            pstmt.setInt(4, habit.getId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar hábito com ID (" + habit.getId() + "): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteHabit(int habitId) {
        String sql = "DELETE FROM habits WHERE id = ?";
        if (habitId <= 0) {
            System.err.println("Erro ao excluir hábito: ID inválido.");
            return false;
        }
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir hábito com ID (" + habitId + "): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        HabitDAO habitDAO = new HabitDAO();

        System.out.println("--- Testando addHabit ---");
        Habit novoHabito1 = new Habit("Correr no Parque DAO", "Correr por pelo menos 30 minutos", LocalDate.now());
        if (habitDAO.addHabit(novoHabito1)) {
            System.out.println("Hábito '" + novoHabito1.getName() + "' adicionado com sucesso!");
        } else {
            System.out.println("Falha ao adicionar o hábito '" + novoHabito1.getName() + "'.");
        }

        Habit novoHabito2 = new Habit("Estudar Java DAO", "Estudar por 1 hora", LocalDate.now().minusDays(1));
        if (habitDAO.addHabit(novoHabito2)) {
            System.out.println("Hábito '" + novoHabito2.getName() + "' adicionado com sucesso!");
        } else {
            System.out.println("Falha ao adicionar o hábito '" + novoHabito2.getName() + "'.");
        }

        System.out.println("\n--- Testando getAllHabits ---");
        List<Habit> todosOsHabitos = habitDAO.getAllHabits();
        if (todosOsHabitos.isEmpty()) {
            System.out.println("Nenhum hábito encontrado no banco de dados.");
        } else {
            System.out.println("Hábitos encontrados (" + todosOsHabitos.size() + "):");
            for (Habit habit : todosOsHabitos) {
                System.out.println(habit);
            }
        }

        System.out.println("\n--- Testando getHabitById ---");
        int idParaTestarExistente = -1;
        if (!todosOsHabitos.isEmpty()) {
            idParaTestarExistente = todosOsHabitos.get(0).getId();
        } else {
            idParaTestarExistente = 1;
            System.out.println("A lista de hábitos está vazia, tentando buscar ID 1 como teste.");
        }

        if (idParaTestarExistente != -1 || todosOsHabitos.isEmpty()) {
            System.out.println("Buscando hábito com ID: " + idParaTestarExistente);
            Habit habitoEncontrado = habitDAO.getHabitById(idParaTestarExistente);
            if (habitoEncontrado != null) {
                System.out.println("Hábito encontrado: " + habitoEncontrado);
            } else {
                System.out.println("Hábito com ID " + idParaTestarExistente + " NÃO encontrado.");
            }
        }

        int idNaoExistente = 9999;
        System.out.println("\nBuscando hábito com ID: " + idNaoExistente);
        Habit habitoNaoEncontrado = habitDAO.getHabitById(idNaoExistente);
        if (habitoNaoEncontrado != null) {
            System.out.println("ERRO: Hábito encontrado, mas não deveria existir! " + habitoNaoEncontrado);
        } else {
            System.out.println("Hábito com ID " + idNaoExistente + " NÃO encontrado (CORRETO).");
        }
    }
}