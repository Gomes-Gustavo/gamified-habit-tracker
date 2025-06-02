package com.habitracker.database;

import com.habitracker.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario addUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nome, pontos) VALUES (?, ?)";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, usuario.getNome());
            pstmt.setInt(2, usuario.getPontos());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        usuario.setId(generatedKeys.getInt(1));
                        return usuario; 
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar usuário: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry")) {
                 System.err.println("Parece que o nome de usuário '" + usuario.getNome() + "' já existe.");
            }
            e.printStackTrace();
        }
        return null; 
    }

    public Usuario getUsuarioByNome(String nomeUsuario) {
        String sql = "SELECT id, nome, pontos FROM usuarios WHERE nome = ?";
        Usuario usuario = null;
        if (nomeUsuario == null || nomeUsuario.trim().isEmpty()) {
            System.err.println("Nome de usuário para busca não pode ser nulo ou vazio.");
            return null;
        }
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomeUsuario);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String nome = rs.getString("nome");
                    int pontos = rs.getInt("pontos");
                    usuario = new Usuario(id, nome, pontos);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por nome (" + nomeUsuario + "): " + e.getMessage());
            e.printStackTrace();
        }
        return usuario;
    }

    public List<Usuario> getAllUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id, nome, pontos FROM usuarios";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                int pontos = rs.getInt("pontos");
                usuarios.add(new Usuario(id, nome, pontos));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todos os usuários: " + e.getMessage());
            e.printStackTrace();
        }
        return usuarios;
    }

    public Usuario getUsuarioById(int usuarioId) {
        String sql = "SELECT id, nome, pontos FROM usuarios WHERE id = ?";
        Usuario usuario = null;
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuarioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String nome = rs.getString("nome");
                    int pontos = rs.getInt("pontos");
                    usuario = new Usuario(id, nome, pontos);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por ID (" + usuarioId + "): " + e.getMessage());
            e.printStackTrace();
        }
        return usuario;
    }

    public boolean updatePontosUsuario(int usuarioId, int novosPontos) {
        String sql = "UPDATE usuarios SET pontos = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, novosPontos);
            pstmt.setInt(2, usuarioId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar pontos do usuário ID (" + usuarioId + "): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteUsuario(int usuarioId) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        if (usuarioId <= 0) {
            System.err.println("Erro ao excluir usuário: ID inválido.");
            return false;
        }
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuarioId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir usuário com ID (" + usuarioId + "): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    

    public static void main(String[] args) {
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        System.out.println("--- Testando addUsuario ---");
        String nomeTeste1 = "JogadorParaDeletar_" + System.currentTimeMillis(); 
        Usuario usuarioParaDeletar = new Usuario(nomeTeste1);
        Usuario usuarioAdicionado = usuarioDAO.addUsuario(usuarioParaDeletar);

        int idUsuarioParaDeletar = -1;
        if (usuarioAdicionado != null && usuarioAdicionado.getId() > 0) {
            System.out.println("Usuário '" + nomeTeste1 + "' adicionado com sucesso para teste de delete: " + usuarioAdicionado);
            idUsuarioParaDeletar = usuarioAdicionado.getId();
        } else {
            System.out.println("Falha ao adicionar usuário '" + nomeTeste1 + "' para o teste de delete.");
        }

        String nomeTeste2 = "OutroJogador_" + System.currentTimeMillis(); 
        Usuario novoUsuario2 = new Usuario(nomeTeste2);
        usuarioDAO.addUsuario(novoUsuario2);


        System.out.println("\n--- Testando getAllUsuarios (antes do delete) ---");
        List<Usuario> todosOsUsuariosAntes = usuarioDAO.getAllUsuarios();
        System.out.println("Total de usuários antes do delete: " + todosOsUsuariosAntes.size());
        for (Usuario u : todosOsUsuariosAntes) {
            System.out.println(u);
        }

        
        System.out.println("\n--- Testando deleteUsuario ---");
        if (idUsuarioParaDeletar != -1) {
            System.out.println("Tentando deletar usuário com ID: " + idUsuarioParaDeletar + " (Nome: " + nomeTeste1 + ")");
            boolean deletadoComSucesso = usuarioDAO.deleteUsuario(idUsuarioParaDeletar);
            if (deletadoComSucesso) {
                System.out.println("Usuário com ID " + idUsuarioParaDeletar + " deletado com sucesso.");
                Usuario usuarioAposDelete = usuarioDAO.getUsuarioById(idUsuarioParaDeletar);
                if (usuarioAposDelete == null) {
                    System.out.println("VERIFICAÇÃO: Usuário com ID " + idUsuarioParaDeletar + " não encontrado após delete (CORRETO).");
                } else {
                    System.out.println("ERRO NA VERIFICAÇÃO: Usuário com ID " + idUsuarioParaDeletar + " ainda encontrado após delete: " + usuarioAposDelete);
                }
            } else {
                System.out.println("Falha ao deletar usuário com ID: " + idUsuarioParaDeletar);
            }
        } else {
            System.out.println("Skipping teste de delete pois o usuário para deletar não foi adicionado corretamente.");
        }
        
        
        int idNaoExistenteParaDelete = 99998;
        System.out.println("\nTentando deletar usuário com ID não existente: " + idNaoExistenteParaDelete);
        boolean deleteNaoExistente = usuarioDAO.deleteUsuario(idNaoExistenteParaDelete);
        if (!deleteNaoExistente) {
            System.out.println("Tentativa de deletar ID não existente retornou false (CORRETO).");
        } else {
            System.out.println("ERRO: Tentativa de deletar ID não existente retornou true.");
        }


        System.out.println("\n--- Testando getAllUsuarios (depois do delete) ---");
        List<Usuario> todosOsUsuariosDepois = usuarioDAO.getAllUsuarios();
        System.out.println("Total de usuários depois do delete: " + todosOsUsuariosDepois.size());
        for (Usuario u : todosOsUsuariosDepois) {
            System.out.println(u);
        }

    }
}