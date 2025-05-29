package com.habitracker.database;

import com.habitracker.model.Habit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.sql.Date;
import java.util.List;
import java.util.ArrayList;

public class HabitDAO {

    /**
     * Adiciona um novo hábito ao banco de dados.
     * @param habit O objeto Habit a ser adicionado (com usuarioId preenchido).
     * @return O objeto Habit com o ID atribuído pelo banco, ou null se a inserção falhar.
     */
    public Habit addHabit(Habit habit) {
        // AJUSTE SQL: Adicionar a coluna usuario_id
        String sql = "INSERT INTO habits (name, description, creationDate, usuario_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, habit.getName());
            pstmt.setString(2, habit.getDescription());
            pstmt.setDate(3, Date.valueOf(habit.getCreationDate()));
            pstmt.setInt(4, habit.getUsuarioId()); // AJUSTE: Definir o usuario_id

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        habit.setId(generatedKeys.getInt(1));
                        return habit;
                    } else {
                        System.err.println("Falha ao obter o ID gerado para o hábito, embora a linha tenha sido inserida.");
                        return null;
                    }
                }
            } else {
                System.err.println("Nenhuma linha afetada ao tentar adicionar hábito: " + habit.getName());
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Erro de SQL ao adicionar hábito '" + habit.getName() + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<Habit> getAllHabits() { // Considere getAllHabitsByUserId(int userId) no futuro
        List<Habit> habits = new ArrayList<>();
        // AJUSTE SQL: Selecionar a coluna usuario_id
        String sql = "SELECT id, name, description, creationDate, usuario_id FROM habits";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                LocalDate creationDate = rs.getDate("creationDate").toLocalDate();
                int usuarioId = rs.getInt("usuario_id"); // AJUSTE: Obter o usuario_id
                // AJUSTE CONSTRUTOR: Usar o construtor que inclui usuario_id
                habits.add(new Habit(id, name, description, creationDate, usuarioId));
            }
        } catch (SQLException e) {
            System.err.println("Erro de SQL ao buscar todos os hábitos: " + e.getMessage());
            e.printStackTrace();
        }
        return habits;
    }

    public Habit getHabitById(int habitIdParam) {
        // AJUSTE SQL: Selecionar a coluna usuario_id
        String sql = "SELECT id, name, description, creationDate, usuario_id FROM habits WHERE id = ?";
        Habit habit = null;
        if (habitIdParam <= 0) {
            System.err.println("ID de hábito inválido fornecido para getHabitById: " + habitIdParam);
            return null;
        }
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, habitIdParam);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    LocalDate creationDate = rs.getDate("creationDate").toLocalDate();
                    int usuarioId = rs.getInt("usuario_id"); // AJUSTE: Obter o usuario_id
                    // AJUSTE CONSTRUTOR: Usar o construtor que inclui usuario_id (passando habitIdParam para o id)
                    habit = new Habit(habitIdParam, name, description, creationDate, usuarioId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro de SQL ao buscar hábito por ID (" + habitIdParam + "): " + e.getMessage());
            e.printStackTrace();
        }
        return habit;
    }

    public List<Habit> getHabitsByUserId(int userId) {
        List<Habit> userHabits = new ArrayList<>();
        String sql = "SELECT id, name, description, creationDate, usuario_id FROM habits WHERE usuario_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    LocalDate creationDate = rs.getDate("creationDate").toLocalDate();
                    int habitUsuarioId = rs.getInt("usuario_id"); // Confirmando o usuario_id do hábito

                    // Usando o construtor que inclui usuario_id
                    userHabits.add(new Habit(id, name, description, creationDate, habitUsuarioId));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro de SQL ao buscar hábitos para o usuário ID (" + userId + "): " + e.getMessage());
            e.printStackTrace();
            // Em caso de erro, retorna uma lista vazia para não quebrar a aplicação,
            // mas o erro é logado. A camada de serviço pode tratar isso de forma mais robusta.
        }
        return userHabits;
    }

    public boolean updateHabit(Habit habit) {
        // AJUSTE SQL: Pode incluir usuario_id se for permitido alterá-lo,
        // caso contrário, certifique-se de que o WHERE id = ? é suficiente e o objeto habit
        // tem o usuarioId correto internamente se for usado em alguma lógica de validação posterior.
        // Por ora, vamos assumir que usuario_id não é alterado no UPDATE.
        String sql = "UPDATE habits SET name = ?, description = ?, creationDate = ? WHERE id = ?";
        // Se você quiser permitir a alteração do usuario_id (geralmente não é uma boa prática para FKs):
        // String sql = "UPDATE habits SET name = ?, description = ?, creationDate = ?, usuario_id = ? WHERE id = ?";
        if (habit == null || habit.getId() <= 0) {
            System.err.println("Erro ao atualizar hábito: Objeto hábito nulo ou ID inválido.");
            return false;
        }
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, habit.getName());
            pstmt.setString(2, habit.getDescription());
            pstmt.setDate(3, Date.valueOf(habit.getCreationDate()));
            // Se for atualizar usuario_id: pstmt.setInt(4, habit.getUsuarioId());
            // E o ID no WHERE passaria a ser o próximo parâmetro: pstmt.setInt(5, habit.getId());
            pstmt.setInt(4, habit.getId()); // Se não atualiza usuario_id, este é o 4º parâmetro
            
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

    public static void main(String[] args) {
        HabitDAO habitDAO = new HabitDAO();
        int testeUsuarioId = 1; // Use um ID de usuário existente para teste, ou crie um

        System.out.println("--- Testando addHabit (DAO MODIFICADO) ---");
        // AJUSTE CONSTRUTOR: Adicionar um usuarioId de teste
        Habit novoHabitoParaTeste = new Habit("Leitura Diária DAO Teste", "Ler 20 páginas", LocalDate.now(), testeUsuarioId);
        Habit habitoCriado = habitDAO.addHabit(novoHabitoParaTeste);

        if (habitoCriado != null && habitoCriado.getId() > 0) {
            System.out.println("Hábito '" + habitoCriado.getName() + "' adicionado! ID: " + habitoCriado.getId() + ", UsuarioID: " + habitoCriado.getUsuarioId());
            System.out.println("Objeto retornado: " + habitoCriado);

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
        // Adicione mais testes se desejar
    }
}