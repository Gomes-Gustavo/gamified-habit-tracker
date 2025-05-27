package com.habitracker.database;

import com.habitracker.model.Conquista;
import com.habitracker.model.Usuario; // Para o main de teste
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp; // Para data_desbloqueio
import java.time.LocalDateTime; // Para data_desbloqueio
import java.util.ArrayList;
import java.util.List;

public class ConquistaDAO {

    // Métodos para gerenciar as DEFINIÇÕES de Conquistas (tabela 'conquistas')

    public Conquista addConquistaDefinicao(Conquista conquista) {
        String sql = "INSERT INTO conquistas (nome, descricao, criterio_desbloqueio, pontos_bonus) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, conquista.getNome());
            pstmt.setString(2, conquista.getDescricao());
            pstmt.setString(3, conquista.getCriterioDesbloqueio());
            pstmt.setInt(4, conquista.getPontosBonus());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        conquista.setId(generatedKeys.getInt(1));
                        return conquista;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar definição de conquista: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Conquista getConquistaDefinicaoById(int conquistaId) {
        String sql = "SELECT id, nome, descricao, criterio_desbloqueio, pontos_bonus FROM conquistas WHERE id = ?";
        Conquista conquista = null;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, conquistaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    conquista = new Conquista(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("descricao"),
                        rs.getString("criterio_desbloqueio"),
                        rs.getInt("pontos_bonus")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar definição de conquista por ID: " + e.getMessage());
            e.printStackTrace();
        }
        return conquista;
    }
    
    public List<Conquista> getAllConquistaDefinicoes() {
        List<Conquista> conquistas = new ArrayList<>();
        String sql = "SELECT id, nome, descricao, criterio_desbloqueio, pontos_bonus FROM conquistas";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                conquistas.add(new Conquista(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("descricao"),
                    rs.getString("criterio_desbloqueio"),
                    rs.getInt("pontos_bonus")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todas as definições de conquistas: " + e.getMessage());
            e.printStackTrace();
        }
        return conquistas;
    }

    // Métodos para gerenciar as CONQUISTAS DESBLOQUEADAS pelos usuários (tabela 'usuario_conquistas')

    public boolean darConquistaParaUsuario(int usuarioId, int conquistaId) {
        // Primeiro, verificar se o usuário já tem essa conquista para evitar duplicatas
        if (usuarioJaTemConquista(usuarioId, conquistaId)) {
            System.out.println("Usuário ID " + usuarioId + " já possui a conquista ID " + conquistaId + ".");
            return true; // Considera sucesso pois o estado desejado (usuário ter a conquista) já é verdade
        }

        String sql = "INSERT INTO usuario_conquistas (id_usuario, id_conquista, data_desbloqueio) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuarioId);
            pstmt.setInt(2, conquistaId);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now())); // Data e hora atuais
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao dar conquista ID " + conquistaId + " para usuário ID " + usuarioId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Conquista> getConquistasDoUsuario(int usuarioId) {
        List<Conquista> conquistasDoUsuario = new ArrayList<>();
        // SQL que junta as tabelas: usuario_conquistas e conquistas
        String sql = "SELECT c.id, c.nome, c.descricao, c.criterio_desbloqueio, c.pontos_bonus " +
                     "FROM conquistas c " +
                     "JOIN usuario_conquistas uc ON c.id = uc.id_conquista " +
                     "WHERE uc.id_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuarioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    conquistasDoUsuario.add(new Conquista(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("descricao"),
                        rs.getString("criterio_desbloqueio"),
                        rs.getInt("pontos_bonus")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar conquistas do usuário ID " + usuarioId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return conquistasDoUsuario;
    }

    public boolean usuarioJaTemConquista(int usuarioId, int conquistaId) {
        String sql = "SELECT COUNT(*) FROM usuario_conquistas WHERE id_usuario = ? AND id_conquista = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuarioId);
            pstmt.setInt(2, conquistaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar se usuário já tem conquista: " + e.getMessage());
            e.printStackTrace();
        }
        return false; // Em caso de erro, assume que não tem para evitar dar duplicado se a lógica depender disso.
    }


    public static void main(String[] args) {
        ConquistaDAO conquistaDAO = new ConquistaDAO();
        UsuarioDAO usuarioDAO = new UsuarioDAO(); // Para criar/buscar usuário de teste

        System.out.println("--- Testando ConquistaDAO ---");

        // Adicionar algumas definições de conquistas
        System.out.println("\n--- Adicionando Definições de Conquistas ---");
        Conquista c1Def = new Conquista("Primeiros Passos", "Cadastrou seu primeiro hábito.", "Cadastrar 1 hábito", 10);
        Conquista c2Def = new Conquista("Persistente", "Cumpriu um hábito por 3 dias seguidos.", "Cumprir hábito por 3 dias", 50);
        Conquista c3Def = new Conquista("Colecionador", "Cadastrou 5 hábitos.", "Cadastrar 5 hábitos", 30);

        Conquista c1Salva = conquistaDAO.addConquistaDefinicao(c1Def);
        Conquista c2Salva = conquistaDAO.addConquistaDefinicao(c2Def);
        Conquista c3Salva = conquistaDAO.addConquistaDefinicao(c3Def);

        if(c1Salva != null) System.out.println("Definição salva: " + c1Salva);
        if(c2Salva != null) System.out.println("Definição salva: " + c2Salva);
        if(c3Salva != null) System.out.println("Definição salva: " + c3Salva);


        System.out.println("\n--- Testando getAllConquistaDefinicoes ---");
        List<Conquista> todasDefinicoes = conquistaDAO.getAllConquistaDefinicoes();
        System.out.println("Total de definições de conquistas: " + todasDefinicoes.size());
        for (Conquista c : todasDefinicoes) {
            System.out.println(c);
        }

        // Criar um usuário de teste
        Usuario uTesteConquista = usuarioDAO.addUsuario(new Usuario("UsuarioDasConquistas_" + System.currentTimeMillis()));
        if (uTesteConquista == null || uTesteConquista.getId() <= 0) {
            System.out.println("ERRO: Não foi possível criar usuário para teste de conquistas. Abortando.");
            return;
        }
        System.out.println("\nUsuário de teste para conquistas: " + uTesteConquista);


        // Dar algumas conquistas ao usuário (usar IDs das conquistas salvas)
        if (c1Salva != null && c3Salva != null) {
            System.out.println("\n--- Testando darConquistaParaUsuario ---");
            boolean deuC1 = conquistaDAO.darConquistaParaUsuario(uTesteConquista.getId(), c1Salva.getId());
            System.out.println("Resultado ao dar Conquista ID " + c1Salva.getId() + " para Usuário ID " + uTesteConquista.getId() + ": " + deuC1);
            
            boolean deuC3 = conquistaDAO.darConquistaParaUsuario(uTesteConquista.getId(), c3Salva.getId());
            System.out.println("Resultado ao dar Conquista ID " + c3Salva.getId() + " para Usuário ID " + uTesteConquista.getId() + ": " + deuC3);

            // Tentar dar a mesma conquista novamente
            System.out.println("\nTentando dar Conquista ID " + c1Salva.getId() + " novamente (esperado não duplicar):");
            boolean deuC1Novamente = conquistaDAO.darConquistaParaUsuario(uTesteConquista.getId(), c1Salva.getId());
            System.out.println("Resultado: " + deuC1Novamente + " (deve ser true se a lógica de 'já possui' está ativa ou se a inserção falha silenciosamente por constraint)");


            System.out.println("\n--- Testando getConquistasDoUsuario ---");
            List<Conquista> conquistasDoUser = conquistaDAO.getConquistasDoUsuario(uTesteConquista.getId());
            System.out.println("Usuário ID " + uTesteConquista.getId() + " possui " + conquistasDoUser.size() + " conquistas:");
            for (Conquista cUser : conquistasDoUser) {
                System.out.println(cUser);
            }

             System.out.println("\n--- Testando usuarioJaTemConquista ---");
             System.out.println("Usuário tem conquista ID " + c1Salva.getId() + "? " + conquistaDAO.usuarioJaTemConquista(uTesteConquista.getId(), c1Salva.getId()) + " (Esperado: true)");
             if(c2Salva != null){
                System.out.println("Usuário tem conquista ID " + c2Salva.getId() + "? " + conquistaDAO.usuarioJaTemConquista(uTesteConquista.getId(), c2Salva.getId()) + " (Esperado: false)");
             }

        } else {
            System.out.println("ERRO: Alguma definição de conquista não foi salva, teste de dar conquista abortado.");
        }
        System.out.println("\n--- FIM DOS TESTES ConquistaDAO ---");
    }
}