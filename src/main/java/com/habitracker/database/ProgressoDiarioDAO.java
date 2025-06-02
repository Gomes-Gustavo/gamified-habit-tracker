package com.habitracker.database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime; 
import java.util.ArrayList;
import java.util.List;

import com.habitracker.model.Habit; 
import com.habitracker.model.ProgressoDiario;
import com.habitracker.model.Usuario; 

public class ProgressoDiarioDAO {

    public ProgressoDiario addProgresso(ProgressoDiario progresso) {
        String sql = "INSERT INTO registros_progresso (usuario_id, habito_id, data_registro, status_cumprido) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, progresso.getUsuarioId());
            pstmt.setInt(2, progresso.getHabitoId());
            pstmt.setDate(3, Date.valueOf(progresso.getDataRegistro()));
            pstmt.setBoolean(4, progresso.isStatusCumprido());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        progresso.setId(generatedKeys.getInt(1));
                        return progresso;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar progresso diário: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                System.err.println("Progresso para este usuário, hábito e data já existe.");
            }
            e.printStackTrace();
        }
        return null;
    }

    public int getCountProgressoCumprido(int usuarioId) {
        String sql = "SELECT COUNT(*) FROM registros_progresso WHERE usuario_id = ? AND status_cumprido = TRUE";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuarioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao contar progresso cumprido para usuário ID " + usuarioId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public ProgressoDiario getProgresso(int usuarioId, int habitoId, LocalDate data) {
        String sql = "SELECT id, usuario_id, habito_id, data_registro, status_cumprido FROM registros_progresso " +
                     "WHERE usuario_id = ? AND habito_id = ? AND data_registro = ?";
        ProgressoDiario progresso = null;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuarioId);
            pstmt.setInt(2, habitoId);
            pstmt.setDate(3, Date.valueOf(data));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    progresso = new ProgressoDiario(
                            rs.getInt("id"),
                            rs.getInt("usuario_id"),
                            rs.getInt("habito_id"),
                            rs.getDate("data_registro").toLocalDate(),
                            rs.getBoolean("status_cumprido")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar progresso diário: " + e.getMessage());
            e.printStackTrace();
        }
        return progresso;
    }

    
    public List<ProgressoDiario> getProgressosDoMes(int usuarioId, int ano, int mes) {
        List<ProgressoDiario> progressosDoMes = new ArrayList<>();
        
        
        String sql = "SELECT id, usuario_id, habito_id, data_registro, status_cumprido " +
                     "FROM registros_progresso " +
                     "WHERE usuario_id = ? AND YEAR(data_registro) = ? AND MONTH(data_registro) = ?";
        
        
        


        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);
            pstmt.setInt(2, ano);       
            pstmt.setInt(3, mes);       
            
            
            


            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    progressosDoMes.add(new ProgressoDiario(
                            rs.getInt("id"),
                            rs.getInt("usuario_id"),
                            rs.getInt("habito_id"),
                            rs.getDate("data_registro").toLocalDate(),
                            rs.getBoolean("status_cumprido")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro de SQL ao buscar progressos do mês para usuário ID " + usuarioId + " (" + ano + "/" + mes + "): " + e.getMessage());
            e.printStackTrace();
        }
        return progressosDoMes;
    }


    public static void main(String[] args) {
        
        
        ProgressoDiarioDAO progressoDAO = new ProgressoDiarioDAO();
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        HabitDAO habitDAO = new HabitDAO();

        System.out.println("--- Testando ProgressoDiarioDAO ---");
        Usuario uTeste = null;
        try {
            Usuario tempUser = new Usuario("UsuarioProgresso_" + System.currentTimeMillis() % 1000);
            uTeste = usuarioDAO.addUsuario(tempUser);
            if (uTeste == null || uTeste.getId() <= 0) {
                System.out.println("Falha ao criar usuário de teste via DAO, usando mock simples para ID.");
                if (uTeste == null) uTeste = tempUser;
                uTeste.setId(999); 
            }
        } catch (Exception e) {
            System.out.println("Exceção ao criar usuário de teste, usando mock simples: " + e.getMessage());
            uTeste = new Usuario("MockUserEx");
            uTeste.setId(998);
        }
        if (uTeste == null) {
             System.out.println("ERRO CRÍTICO: Usuário de teste não pôde ser inicializado. Abortando.");
             return;
        }

        String nomeHabitoTesteProg = "HabitoParaProgresso_" + System.currentTimeMillis();
        LocalTime horarioTesteOpcional = LocalTime.of(10, 0);

        Habit novoHabitoObj = new Habit(
                nomeHabitoTesteProg,
                "Teste de progresso no main do ProgressoDiarioDAO",
                LocalDate.now(),
                uTeste.getId(),
                horarioTesteOpcional
        );
        Habit habitoAdicionadoNoTeste = habitDAO.addHabit(novoHabitoObj);

        if (habitoAdicionadoNoTeste == null || habitoAdicionadoNoTeste.getId() <= 0) {
            System.out.println("ERRO: Falha ao adicionar o hábito de teste '" + nomeHabitoTesteProg + "'. Abortando.");
            return;
        }
        System.out.println("Hábito de teste '" + nomeHabitoTesteProg + "' adicionado com ID: " + habitoAdicionadoNoTeste.getId());
        Habit hTeste = habitoAdicionadoNoTeste;

        System.out.println("Usando Usuario ID: " + uTeste.getId() + " e Habito ID: " + hTeste.getId() + " para os testes.");

        System.out.println("\n--- Testando addProgresso (novo) ---");
        ProgressoDiario novoProgresso = new ProgressoDiario(uTeste.getId(), hTeste.getId(), LocalDate.now(), true);
        ProgressoDiario progressoAdicionado = progressoDAO.addProgresso(novoProgresso);

        if (progressoAdicionado != null && progressoAdicionado.getId() > 0) {
            System.out.println("SUCESSO (addProgresso novo): " + progressoAdicionado);
        } else {
            System.out.println("FALHA (addProgresso novo).");
        }
        
    }
}