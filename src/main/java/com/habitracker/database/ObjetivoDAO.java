package com.habitracker.database;

import com.habitracker.model.Objetivo;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ObjetivoDAO {

    public Objetivo addObjetivo(Objetivo objetivo) {
        
        String sql = "INSERT INTO objetivos (usuario_id, nome, descricao, concluido, data_criacao, data_conclusao, data_meta) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, objetivo.getUsuarioId());
            pstmt.setString(2, objetivo.getNome());
            pstmt.setString(3, objetivo.getDescricao());
            pstmt.setBoolean(4, objetivo.isConcluido());
            pstmt.setDate(5, Date.valueOf(objetivo.getDataCriacao()));
            
            if (objetivo.getDataConclusao() != null) {
                pstmt.setDate(6, Date.valueOf(objetivo.getDataConclusao()));
            } else {
                pstmt.setNull(6, Types.DATE);
            }
            
            if (objetivo.getDataMeta() != null) {
                pstmt.setDate(7, Date.valueOf(objetivo.getDataMeta()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        objetivo.setId(generatedKeys.getInt(1));
                        return objetivo;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar objetivo: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    
    private Objetivo mapResultSetToObjetivo(ResultSet rs) throws SQLException {
        LocalDate dataConclusao = null;
        Date sqlDataConclusao = rs.getDate("data_conclusao");
        if (sqlDataConclusao != null) {
            dataConclusao = sqlDataConclusao.toLocalDate();
        }

        LocalDate dataMeta = null;
        Date sqlDataMeta = rs.getDate("data_meta"); 
        if (sqlDataMeta != null) {
            dataMeta = sqlDataMeta.toLocalDate();
        }

        return new Objetivo(
            rs.getInt("id"),
            rs.getInt("usuario_id"),
            rs.getString("nome"),
            rs.getString("descricao"),
            rs.getBoolean("concluido"),
            rs.getDate("data_criacao").toLocalDate(),
            dataConclusao,
            dataMeta 
        );
    }

    public Objetivo getObjetivoById(int objetivoId) {
        
        String sql = "SELECT id, usuario_id, nome, descricao, concluido, data_criacao, data_conclusao, data_meta FROM objetivos WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, objetivoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToObjetivo(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar objetivo por ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Objetivo> getObjetivosByUserId(int usuarioId) {
        List<Objetivo> objetivos = new ArrayList<>();
        
        String sql = "SELECT id, usuario_id, nome, descricao, concluido, data_criacao, data_conclusao, data_meta FROM objetivos WHERE usuario_id = ? ORDER BY data_criacao DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuarioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    objetivos.add(mapResultSetToObjetivo(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar objetivos do usuário: " + e.getMessage());
            e.printStackTrace();
        }
        return objetivos;
    }

    public boolean updateObjetivo(Objetivo objetivo) {
        
        String sql = "UPDATE objetivos SET nome = ?, descricao = ?, concluido = ?, data_conclusao = ?, data_meta = ? WHERE id = ? AND usuario_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, objetivo.getNome());
            pstmt.setString(2, objetivo.getDescricao());
            pstmt.setBoolean(3, objetivo.isConcluido());
            
            if (objetivo.getDataConclusao() != null) {
                pstmt.setDate(4, Date.valueOf(objetivo.getDataConclusao()));
            } else {
                pstmt.setNull(4, Types.DATE);
            }
            
            if (objetivo.getDataMeta() != null) {
                pstmt.setDate(5, Date.valueOf(objetivo.getDataMeta()));
            } else {
                pstmt.setNull(5, Types.DATE);
            }
            
            pstmt.setInt(6, objetivo.getId());
            pstmt.setInt(7, objetivo.getUsuarioId()); 
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar objetivo: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    
    
    
    public boolean deleteObjetivo(int objetivoId, int usuarioId) {
        removeAllHabitoLinksForObjetivo(objetivoId); 
        String sql = "DELETE FROM objetivos WHERE id = ? AND usuario_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, objetivoId);
            pstmt.setInt(2, usuarioId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao deletar objetivo: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean addHabitoObjetivoLink(int habitoId, int objetivoId) {
        String sql = "INSERT IGNORE INTO habito_objetivo_links (habito_id, objetivo_id) VALUES (?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, habitoId);
            pstmt.setInt(2, objetivoId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar link hábito-objetivo: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean removeHabitoObjetivoLink(int habitoId, int objetivoId) {
        String sql = "DELETE FROM habito_objetivo_links WHERE habito_id = ? AND objetivo_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, habitoId);
            pstmt.setInt(2, objetivoId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao remover link hábito-objetivo: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeAllHabitoLinksForObjetivo(int objetivoId) {
        String sql = "DELETE FROM habito_objetivo_links WHERE objetivo_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, objetivoId);
            pstmt.executeUpdate(); 
            return true; 
        } catch (SQLException e) {
            System.err.println("Erro ao remover todos os links para o objetivo ID " + objetivoId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean removeAllObjetivoLinksForHabito(int habitoId) {
        String sql = "DELETE FROM habito_objetivo_links WHERE habito_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, habitoId);
            pstmt.executeUpdate();
            return true; 
        } catch (SQLException e) {
            System.err.println("Erro ao remover todos os links para o hábito ID " + habitoId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Integer> getHabitoIdsForObjetivo(int objetivoId) {
        List<Integer> habitIds = new ArrayList<>();
        String sql = "SELECT habito_id FROM habito_objetivo_links WHERE objetivo_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, objetivoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    habitIds.add(rs.getInt("habito_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar IDs de hábitos para o objetivo: " + e.getMessage());
            e.printStackTrace();
        }
        return habitIds;
    }
}