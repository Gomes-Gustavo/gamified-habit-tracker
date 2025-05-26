// Dentro de src/com/gomesgustavo/gamifiedhabittracker/database/HabitDAO.java
package com.habitracker.database;

import com.habitracker.model.Habit; // Importe sua classe Habit
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.Date; // Para converter LocalDate para SQL Date
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;

public class HabitDAO {

    public boolean addHabit(Habit habit) {
        String sql = "INSERT INTO habits (name, description, creationDate) VALUES (?, ?, ?)";

        // Try-with-resources garante que a conexão e o PreparedStatement sejam fechados
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, habit.getName());
            pstmt.setString(2, habit.getDescription());
            pstmt.setDate(3, Date.valueOf(habit.getCreationDate())); // Converte LocalDate para SQL Date

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0; // Retorna true se uma linha foi inserida

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
                LocalDate creationDate = rs.getDate("creationDate").toLocalDate(); // Converte SQL Date para LocalDate

                habits.add(new Habit(id, name, description, creationDate));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar hábitos: " + e.getMessage());
            e.printStackTrace();
            // Em uma aplicação real, pode ser melhor lançar uma exceção ou retornar uma lista vazia controladamente.
        }
        return habits;
    }
}