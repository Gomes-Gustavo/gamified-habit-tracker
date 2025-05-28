package com.habitracker.database;

import java.sql.Connection;
import java.sql.Date; // Para o main de teste
import java.sql.PreparedStatement;   // Para o main de teste
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

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

    // TODO: Implementar getHistoricoHabitoUsuario(int usuarioId, int habitoId)
    // public List<ProgressoDiario> getHistoricoHabitoUsuario(int usuarioId, int habitoId) { ... }

    // TODO: Implementar getProgressosPorData(int usuarioId, LocalDate data)
    // public List<ProgressoDiario> getProgressosPorData(int usuarioId, LocalDate data) { ... }


    public static void main(String[] args) {
        ProgressoDiarioDAO progressoDAO = new ProgressoDiarioDAO();
        UsuarioDAO usuarioDAO = new UsuarioDAO(); 
        HabitDAO habitDAO = new HabitDAO();     // Instância do HabitDAO para o teste

        System.out.println("--- Testando ProgressoDiarioDAO ---");

        Usuario uTeste = usuarioDAO.addUsuario(new Usuario("UsuarioProgresso_" + System.currentTimeMillis()));
        Habit hTeste = null; 
        
        String nomeHabitoTesteProg = "HabitoParaProgresso_" + System.currentTimeMillis();
        
        // --- PONTO DA CORREÇÃO ---
        // Antes, você poderia ter algo que esperava um boolean.
        // Agora, habitDAO.addHabit retorna um objeto Habit (ou null se falhar)
        Habit novoHabitoObj = new Habit(nomeHabitoTesteProg, "Teste de progresso no main do ProgressoDiarioDAO", LocalDate.now());
        Habit habitoAdicionadoNoTeste = habitDAO.addHabit(novoHabitoObj); // Chama o addHabit que retorna Habit

        // A verificação de sucesso agora é se o objeto não é nulo E se tem um ID válido
        if (habitoAdicionadoNoTeste != null && habitoAdicionadoNoTeste.getId() > 0) {
            System.out.println("Hábito de teste '" + nomeHabitoTesteProg + "' adicionado com sucesso com ID: " + habitoAdicionadoNoTeste.getId());
            // Se você precisa do objeto hTeste para os testes de progresso, atribua o objeto retornado:
            hTeste = habitoAdicionadoNoTeste;
        } else {
            System.out.println("ERRO: Falha ao adicionar o hábito de teste '" + nomeHabitoTesteProg + "' no main do ProgressoDiarioDAO.");
            // Se o hábito não pôde ser criado, talvez você não queira continuar com os testes que dependem dele.
        }
        // --- FIM DO PONTO DA CORREÇÃO ---

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

        // Teste 2: Tentar adicionar o mesmo progresso novamente
        System.out.println("\n--- Testando addProgresso (duplicado) ---");
        ProgressoDiario progressoDuplicado = new ProgressoDiario(uTeste.getId(), hTeste.getId(), LocalDate.now(), false);
        ProgressoDiario resultadoDuplicado = progressoDAO.addProgresso(progressoDuplicado);
        if (resultadoDuplicado == null) {
            System.out.println("SUCESSO (addProgresso duplicado): Falhou como esperado.");
        } else {
            System.out.println("FALHA (addProgresso duplicado): Conseguiu adicionar progresso duplicado: " + resultadoDuplicado);
        }

        // Teste 3: Buscar o progresso que foi adicionado
        System.out.println("\n--- Testando getProgresso (existente) ---");
        // Verifica se progressoAdicionado não é nulo antes de tentar usar seu ID
        if (progressoAdicionado != null) {
            ProgressoDiario progressoBuscado = progressoDAO.getProgresso(uTeste.getId(), hTeste.getId(), LocalDate.now());
            if (progressoBuscado != null && progressoBuscado.getId() == progressoAdicionado.getId()) {
                System.out.println("SUCESSO (getProgresso existente): " + progressoBuscado);
            } else {
                System.out.println("FALHA (getProgresso existente). Buscado: " + progressoBuscado);
            }
        } else {
            System.out.println("SKIP (getProgresso existente): Não foi possível adicionar o progresso inicial.");
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