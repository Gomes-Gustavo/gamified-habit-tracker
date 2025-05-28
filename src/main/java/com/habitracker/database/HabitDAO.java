package com.habitracker.database;

import com.habitracker.model.Habit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet; // Certifique-se que este import está presente
import java.sql.SQLException;
import java.sql.Statement; // Certifique-se que este import está presente
import java.time.LocalDate;
import java.sql.Date;
import java.util.List;
import java.util.ArrayList;

public class HabitDAO {

    /**
     * Adiciona um novo hábito ao banco de dados.
     * MODIFICADO: Agora retorna o objeto Habit com o ID preenchido pelo banco.
     * @param habit O objeto Habit a ser adicionado (sem ID ou com ID 0).
     * @return O objeto Habit com o ID atribuído pelo banco, ou null se a inserção falhar ou o ID não puder ser recuperado.
     */
    public Habit addHabit(Habit habit) { // <<< RETORNO ALTERADO PARA Habit
        String sql = "INSERT INTO habits (name, description, creationDate) VALUES (?, ?, ?)";
        // Usar try-with-resources para garantir que a conexão e o statement sejam fechados
        try (Connection conn = ConnectionFactory.getConnection();
             // Solicita as chaves geradas (ID) ao preparar o statement
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, habit.getName());
            pstmt.setString(2, habit.getDescription());
            pstmt.setDate(3, Date.valueOf(habit.getCreationDate()));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Se a inserção foi bem-sucedida, tenta obter o ID gerado
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        habit.setId(generatedKeys.getInt(1)); // Define o ID no objeto habit original
                        return habit; // Retorna o objeto habit agora com o ID
                    } else {
                        // Isso seria um erro inesperado se affectedRows > 0
                        System.err.println("Falha ao obter o ID gerado para o hábito, embora a linha tenha sido inserida.");
                        return null; // Indica que não foi possível obter o ID
                    }
                }
            } else {
                // Nenhuma linha foi afetada, a inserção falhou por algum motivo não SQLException
                System.err.println("Nenhuma linha afetada ao tentar adicionar hábito: " + habit.getName());
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Erro de SQL ao adicionar hábito '" + habit.getName() + "': " + e.getMessage());
            e.printStackTrace();
            // A camada de serviço tratará a SQLException (ou RuntimeException do ConnectionFactory)
            // e a converterá em uma PersistenceException apropriada.
            // Retornar null aqui sinaliza a falha para a camada de serviço.
            return null;
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
            System.err.println("Erro de SQL ao buscar todos os hábitos: " + e.getMessage());
            e.printStackTrace();
            // A camada de serviço pode decidir o que fazer com uma lista vazia ou
            // se uma exceção deveria ser propagada (encapsulada em PersistenceException).
            // Por ora, retorna lista vazia em caso de erro aqui.
        }
        return habits;
    }

    public Habit getHabitById(int habitId) {
        String sql = "SELECT id, name, description, creationDate FROM habits WHERE id = ?";
        Habit habit = null;
        if (habitId <= 0) {
            System.err.println("ID de hábito inválido fornecido para getHabitById: " + habitId);
            return null; // ID inválido não deve ir ao banco
        }
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // int id = rs.getInt("id"); // Não precisa pegar o id de novo, já temos em habitId
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    LocalDate creationDate = rs.getDate("creationDate").toLocalDate();
                    habit = new Habit(habitId, name, description, creationDate);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro de SQL ao buscar hábito por ID (" + habitId + "): " + e.getMessage());
            e.printStackTrace();
        }
        return habit; // Retorna null se não encontrado ou em caso de erro
    }

    public boolean updateHabit(Habit habit) {
        String sql = "UPDATE habits SET name = ?, description = ?, creationDate = ? WHERE id = ?";
        if (habit == null || habit.getId() <= 0) {
            System.err.println("Erro ao atualizar hábito: Objeto hábito nulo ou ID inválido.");
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
            System.err.println("Erro de SQL ao atualizar hábito com ID (" + habit.getId() + "): " + e.getMessage());
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
            System.err.println("Erro de SQL ao excluir hábito com ID (" + habitId + "): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Seu método main de teste, adaptado para o novo addHabit
    public static void main(String[] args) {
        HabitDAO habitDAO = new HabitDAO();

        System.out.println("--- Testando addHabit (DAO MODIFICADO) ---");
        Habit novoHabitoParaTeste = new Habit("Leitura Diária DAO", "Ler 20 páginas de um livro técnico", LocalDate.now());
        Habit habitoCriado = habitDAO.addHabit(novoHabitoParaTeste); // Chama o addHabit modificado

        if (habitoCriado != null && habitoCriado.getId() > 0) {
            System.out.println("Hábito '" + habitoCriado.getName() + "' adicionado com sucesso! ID: " + habitoCriado.getId());
            System.out.println("Objeto retornado: " + habitoCriado);

            // Testar getHabitById com o ID recém-criado
            System.out.println("\n--- Testando getHabitById (com ID recém-criado) ---");
            Habit habitoBuscado = habitDAO.getHabitById(habitoCriado.getId());
            if (habitoBuscado != null) {
                System.out.println("Hábito encontrado: " + habitoBuscado);
            } else {
                System.out.println("ERRO: Hábito com ID " + habitoCriado.getId() + " NÃO encontrado após adição.");
            }
        } else {
            System.out.println("Falha ao adicionar o hábito '" + novoHabitoParaTeste.getName() + "'.");
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

        // ... (você pode adicionar mais testes para update e delete aqui se desejar)
    }
}