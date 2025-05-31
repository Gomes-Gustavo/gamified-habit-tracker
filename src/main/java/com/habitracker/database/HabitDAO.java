package com.habitracker.database;

import com.habitracker.model.Habit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types; // Para Types.TIME
import java.time.DayOfWeek; // IMPORTADO
import java.time.LocalDate;
import java.time.LocalTime;
import java.sql.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;   // IMPORTADO
import java.util.Set;       // IMPORTADO

public class HabitDAO {

    public Habit addHabit(Habit habit) {
        String sqlHabit = "INSERT INTO habits (name, description, creationDate, usuario_id, horario_opcional) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmtHabit = null;
        PreparedStatement pstmtDias = null;
        ResultSet generatedKeys = null;

        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false); // Iniciar transação

            pstmtHabit = conn.prepareStatement(sqlHabit, Statement.RETURN_GENERATED_KEYS);
            pstmtHabit.setString(1, habit.getName());
            pstmtHabit.setString(2, habit.getDescription());
            pstmtHabit.setDate(3, Date.valueOf(habit.getCreationDate()));
            pstmtHabit.setInt(4, habit.getUsuarioId());
            if (habit.getHorarioOpcional() != null) {
                pstmtHabit.setTime(5, Time.valueOf(habit.getHorarioOpcional()));
            } else {
                pstmtHabit.setNull(5, Types.TIME);
            }

            int affectedRows = pstmtHabit.executeUpdate();

            if (affectedRows > 0) {
                generatedKeys = pstmtHabit.getGeneratedKeys();
                if (generatedKeys.next()) {
                    habit.setId(generatedKeys.getInt(1));

                    // Salvar os dias da semana na tabela de junção
                    if (habit.getDiasDaSemana() != null && !habit.getDiasDaSemana().isEmpty()) {
                        String sqlDias = "INSERT INTO habito_dias_semana (habito_id, dia_semana) VALUES (?, ?)";
                        pstmtDias = conn.prepareStatement(sqlDias);
                        for (DayOfWeek dia : habit.getDiasDaSemana()) {
                            pstmtDias.setInt(1, habit.getId());
                            pstmtDias.setString(2, dia.name()); // Armazena como "MONDAY", "TUESDAY", etc.
                            pstmtDias.addBatch();
                        }
                        pstmtDias.executeBatch();
                    }
                    conn.commit(); // Finalizar transação com sucesso
                    return habit;
                }
            }
            conn.rollback(); // Se não conseguiu obter o ID gerado ou inserir o hábito principal
        } catch (SQLException e) {
            System.err.println("Erro de SQL ao adicionar hábito: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Tenta reverter em caso de erro
                } catch (SQLException ex) {
                    System.err.println("Erro ao reverter transação: " + ex.getMessage());
                }
            }
        } finally {
            // Bloco finally para fechar recursos
            try { if (generatedKeys != null) generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmtDias != null) pstmtDias.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmtHabit != null) pstmtHabit.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { 
                if (conn != null) {
                    conn.setAutoCommit(true); // Restaurar autoCommit
                    conn.close(); 
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }
        return null; // Retorna null se a adição falhou
    }
    
    private Set<DayOfWeek> getDiasDaSemanaParaHabito(int habitoId, Connection conn) throws SQLException {
        Set<DayOfWeek> dias = new HashSet<>();
        String sql = "SELECT dia_semana FROM habito_dias_semana WHERE habito_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, habitoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        dias.add(DayOfWeek.valueOf(rs.getString("dia_semana").toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Valor de dia_semana inválido no banco para habito_id " + habitoId + ": " + rs.getString("dia_semana"));
                    }
                }
            }
        }
        return dias;
    }

    private Habit mapResultSetToHabit(ResultSet rs, Connection conn) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate creationDate = rs.getDate("creationDate").toLocalDate();
        int usuarioId = rs.getInt("usuario_id");
        Time timeSQL = rs.getTime("horario_opcional");
        LocalTime horarioOpcional = (timeSQL != null) ? timeSQL.toLocalTime() : null;

        Habit habit = new Habit(id, name, description, creationDate, usuarioId, horarioOpcional);
        habit.setDiasDaSemana(getDiasDaSemanaParaHabito(id, conn));
        return habit;
    }

    public List<Habit> getAllHabits() { // Usado principalmente para testes ou admin, agora carrega dias
        List<Habit> habits = new ArrayList<>();
        String sql = "SELECT id, name, description, creationDate, usuario_id, horario_opcional FROM habits";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                habits.add(mapResultSetToHabit(rs, conn));
            }
        } catch (SQLException e) {
            System.err.println("Erro de SQL ao buscar todos os hábitos: " + e.getMessage());
            e.printStackTrace();
        }
        return habits;
    }

    public Habit getHabitById(int habitIdParam) {
        String sql = "SELECT id, name, description, creationDate, usuario_id, horario_opcional FROM habits WHERE id = ?";
        if (habitIdParam <= 0) return null;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, habitIdParam);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToHabit(rs, conn);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro de SQL ao buscar hábito por ID (" + habitIdParam + "): " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Habit> getHabitsByUserId(int userId) {
        List<Habit> userHabits = new ArrayList<>();
        String sql = "SELECT id, name, description, creationDate, usuario_id, horario_opcional FROM habits WHERE usuario_id = ? ORDER BY creationDate DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    userHabits.add(mapResultSetToHabit(rs, conn));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro de SQL ao buscar hábitos para o usuário ID (" + userId + "): " + e.getMessage());
            e.printStackTrace();
        }
        return userHabits;
    }

    public boolean updateHabit(Habit habit) {
        String sqlHabit = "UPDATE habits SET name = ?, description = ?, creationDate = ?, horario_opcional = ? WHERE id = ? AND usuario_id = ?";
        String sqlDeleteDias = "DELETE FROM habito_dias_semana WHERE habito_id = ?";
        String sqlInsertDias = "INSERT INTO habito_dias_semana (habito_id, dia_semana) VALUES (?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmtHabit = null;
        PreparedStatement pstmtDeleteDias = null;
        PreparedStatement pstmtInsertDias = null;

        if (habit == null || habit.getId() <= 0) return false;

        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            pstmtHabit = conn.prepareStatement(sqlHabit);
            pstmtHabit.setString(1, habit.getName());
            pstmtHabit.setString(2, habit.getDescription());
            pstmtHabit.setDate(3, Date.valueOf(habit.getCreationDate()));
            if (habit.getHorarioOpcional() != null) {
                pstmtHabit.setTime(4, Time.valueOf(habit.getHorarioOpcional()));
            } else {
                pstmtHabit.setNull(4, Types.TIME);
            }
            pstmtHabit.setInt(5, habit.getId());
            pstmtHabit.setInt(6, habit.getUsuarioId());
            int affectedRows = pstmtHabit.executeUpdate();

            if (affectedRows > 0) {
                pstmtDeleteDias = conn.prepareStatement(sqlDeleteDias);
                pstmtDeleteDias.setInt(1, habit.getId());
                pstmtDeleteDias.executeUpdate(); // Remove todos os dias antigos

                // Inserir os novos dias selecionados
                if (habit.getDiasDaSemana() != null && !habit.getDiasDaSemana().isEmpty()) {
                    pstmtInsertDias = conn.prepareStatement(sqlInsertDias);
                    for (DayOfWeek dia : habit.getDiasDaSemana()) {
                        pstmtInsertDias.setInt(1, habit.getId());
                        pstmtInsertDias.setString(2, dia.name());
                        pstmtInsertDias.addBatch();
                    }
                    pstmtInsertDias.executeBatch();
                }
                conn.commit();
                return true;
            } else {
                conn.rollback(); // Hábito principal não foi encontrado/atualizado
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Erro de SQL ao atualizar hábito com ID (" + habit.getId() + "): " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        } finally {
            try { if (pstmtHabit != null) pstmtHabit.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmtDeleteDias != null) pstmtDeleteDias.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmtInsertDias != null) pstmtInsertDias.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) { e.printStackTrace(); }
        }
        return false;
    }

    public boolean deleteHabit(int habitId) {
        // ON DELETE CASCADE na FK da tabela habito_dias_semana deve remover os links.
        // Se não houver CASCADE, você precisaria deletar de habito_dias_semana primeiro:
        // String sqlDeleteLinks = "DELETE FROM habito_dias_semana WHERE habito_id = ?";
        String sqlDeleteHabit = "DELETE FROM habits WHERE id = ?";
        if (habitId <= 0) return false;
        
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            // Se não usar ON DELETE CASCADE:
            // try (PreparedStatement pstmtLinks = conn.prepareStatement(sqlDeleteLinks)) {
            //     pstmtLinks.setInt(1, habitId);
            //     pstmtLinks.executeUpdate();
            // }
            try (PreparedStatement pstmtHabit = conn.prepareStatement(sqlDeleteHabit)) {
                pstmtHabit.setInt(1, habitId);
                return pstmtHabit.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro de SQL ao excluir hábito com ID (" + habitId + "): " + e.getMessage());
            e.printStackTrace();
        } finally {
             try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return false;
    }

    // main de teste (pode ser ajustado para testar dias da semana)
    public static void main(String[] args) {
        HabitDAO habitDAO = new HabitDAO();
        int testeUsuarioId = 1; // Crie este usuário se não existir

        System.out.println("--- Testando addHabit com dias da semana ---");
        Habit novoHabitoComDias = new Habit("Yoga Matinal", "30 min de yoga", LocalDate.now(), testeUsuarioId, LocalTime.of(7, 0));
        Set<DayOfWeek> diasYoga = new HashSet<>();
        diasYoga.add(DayOfWeek.MONDAY);
        diasYoga.add(DayOfWeek.WEDNESDAY);
        diasYoga.add(DayOfWeek.FRIDAY);
        novoHabitoComDias.setDiasDaSemana(diasYoga);
        
        Habit habitoCriado = habitDAO.addHabit(novoHabitoComDias);
        if (habitoCriado != null) {
            System.out.println("Hábito com dias adicionado: " + habitoCriado);
            System.out.println("Dias recuperados: " + habitoCriado.getDiasDaSemana());

            System.out.println("\n--- Testando updateHabit ---");
            habitoCriado.setName("Yoga Vespertina");
            Set<DayOfWeek> novosDias = new HashSet<>();
            novosDias.add(DayOfWeek.TUESDAY);
            novosDias.add(DayOfWeek.THURSDAY);
            habitoCriado.setDiasDaSemana(novosDias);
            habitoCriado.setHorarioOpcional(LocalTime.of(18,0));

            boolean atualizou = habitDAO.updateHabit(habitoCriado);
            System.out.println("Hábito atualizado? " + atualizou);
            if (atualizou) {
                Habit habitoRebuscado = habitDAO.getHabitById(habitoCriado.getId());
                System.out.println("Hábito re-buscado: " + habitoRebuscado);
                System.out.println("Dias re-buscados: " + habitoRebuscado.getDiasDaSemana());
            }
        } else {
            System.out.println("Falha ao adicionar hábito com dias.");
        }

        System.out.println("\n--- Listando hábitos do usuário " + testeUsuarioId + " ---");
        List<Habit> habitsDoUsuario = habitDAO.getHabitsByUserId(testeUsuarioId);
        if (habitsDoUsuario.isEmpty()) {
            System.out.println("Nenhum hábito encontrado.");
        } else {
            for (Habit h : habitsDoUsuario) {
                System.out.println(h + " | Dias: " + h.getDiasDaSemana());
            }
        }
    }
}