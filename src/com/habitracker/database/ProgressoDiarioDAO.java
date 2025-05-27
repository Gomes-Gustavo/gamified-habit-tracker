package com.habitracker.database;

import com.habitracker.model.ProgressoDiario;
import com.habitracker.model.Usuario; // Para o main de teste
import com.habitracker.model.Habit;   // Para o main de teste
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;       // Para um futuro método getHistorico
import java.util.ArrayList;  // Para um futuro método getHistorico

public class ProgressoDiarioDAO {

    public ProgressoDiario addProgresso(ProgressoDiario progresso) {
        // A tabela tem uma UNIQUE KEY em (usuario_id, habito_id, data_registro)
        // Então, uma tentativa de inserir um duplicado vai falhar, o que é bom.
        // Poderíamos verificar antes com getProgresso se quisermos um comportamento diferente.
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
                        return progresso; // Retorna o progresso com o ID preenchido
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar progresso diário: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry")) {
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
    return 0; // Retorna 0 se houver erro ou nenhum registro
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

    // TODO: Implementar getHistoricoHabitoUsuario(int usuarioId, int habitoId)
    // public List<ProgressoDiario> getHistoricoHabitoUsuario(int usuarioId, int habitoId) { ... }

    // TODO: Implementar getProgressosPorData(int usuarioId, LocalDate data)
    // public List<ProgressoDiario> getProgressosPorData(int usuarioId, LocalDate data) { ... }


    public static void main(String[] args) {
        ProgressoDiarioDAO progressoDAO = new ProgressoDiarioDAO();
        UsuarioDAO usuarioDAO = new UsuarioDAO(); // Para pegar/criar IDs de usuário para teste
        HabitDAO habitDAO = new HabitDAO();       // Para pegar/criar IDs de hábito para teste

        System.out.println("--- Testando ProgressoDiarioDAO ---");

        // Etapa 1: Garantir que temos um usuário e um hábito para testar
        // Você pode criar novos ou buscar existentes. Para este teste, vamos criar.
        Usuario uTeste = usuarioDAO.addUsuario(new Usuario("UsuarioProgresso_" + System.currentTimeMillis()));
        Habit hTeste = null; 
        // Adicionar um hábito através do DAO (addHabit no HabitDAO retorna boolean, não o hábito com ID)
        // Então, vamos criar e depois buscar pelo nome se necessário, ou assumir IDs.
        // Para simplificar, vamos assumir que existem hábitos ou adicionar um e tentar pegar o ID.
        
        String nomeHabitoTesteProg = "HabitoParaProgresso_" + System.currentTimeMillis();
        boolean habitoAdicionado = habitDAO.addHabit(new Habit(nomeHabitoTesteProg, "Teste de progresso", LocalDate.now()));
        if(habitoAdicionado){
            List<Habit> habitos = habitDAO.getAllHabits(); // Pega todos para achar o ID
            for(Habit h : habitos){
                if(h.getName().equals(nomeHabitoTesteProg)){
                    hTeste = h;
                    break;
                }
            }
        }

        if (uTeste == null || uTeste.getId() <= 0 || hTeste == null || hTeste.getId() <= 0) {
            System.out.println("ERRO: Não foi possível criar/obter usuário ou hábito de teste. Abortando testes do ProgressoDiarioDAO.");
            return;
        }
        System.out.println("Usando Usuario ID: " + uTeste.getId() + " e Habito ID: " + hTeste.getId() + " para os testes.");


        // Teste 1: Adicionar um novo progresso
        System.out.println("\n--- Testando addProgresso (novo) ---");
        ProgressoDiario novoProgresso = new ProgressoDiario(uTeste.getId(), hTeste.getId(), LocalDate.now(), true);
        ProgressoDiario progressoAdicionado = progressoDAO.addProgresso(novoProgresso);

        if (progressoAdicionado != null && progressoAdicionado.getId() > 0) {
            System.out.println("SUCESSO (addProgresso novo): " + progressoAdicionado);
        } else {
            System.out.println("FALHA (addProgresso novo).");
        }

        // Teste 2: Tentar adicionar o mesmo progresso novamente (deve falhar devido à UNIQUE KEY)
        System.out.println("\n--- Testando addProgresso (duplicado) ---");
        ProgressoDiario progressoDuplicado = new ProgressoDiario(uTeste.getId(), hTeste.getId(), LocalDate.now(), false); // status diferente, mas chave é a mesma
        ProgressoDiario resultadoDuplicado = progressoDAO.addProgresso(progressoDuplicado);
        if (resultadoDuplicado == null) {
            System.out.println("SUCESSO (addProgresso duplicado): Falhou como esperado.");
        } else {
            System.out.println("FALHA (addProgresso duplicado): Conseguiu adicionar progresso duplicado: " + resultadoDuplicado);
        }

        // Teste 3: Buscar o progresso que foi adicionado
        System.out.println("\n--- Testando getProgresso (existente) ---");
        ProgressoDiario progressoBuscado = progressoDAO.getProgresso(uTeste.getId(), hTeste.getId(), LocalDate.now());
        if (progressoBuscado != null && progressoBuscado.getId() == progressoAdicionado.getId()) {
            System.out.println("SUCESSO (getProgresso existente): " + progressoBuscado);
        } else {
            System.out.println("FALHA (getProgresso existente). Buscado: " + progressoBuscado);
        }

        // Teste 4: Buscar um progresso que não existe (data diferente)
        System.out.println("\n--- Testando getProgresso (data não existente) ---");
        ProgressoDiario progressoNaoExistente = progressoDAO.getProgresso(uTeste.getId(), hTeste.getId(), LocalDate.now().minusDays(1));
        if (progressoNaoExistente == null) {
            System.out.println("SUCESSO (getProgresso data não existente): Nenhum progresso encontrado (CORRETO).");
        } else {
            System.out.println("FALHA (getProgresso data não existente): Encontrou progresso: " + progressoNaoExistente);
        }
        System.out.println("\n--- FIM DOS TESTES ProgressoDiarioDAO ---");
    }
}