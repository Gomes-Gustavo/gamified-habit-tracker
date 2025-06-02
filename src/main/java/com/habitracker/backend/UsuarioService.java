package com.habitracker.backend; 

import com.habitracker.database.UsuarioDAO;
import com.habitracker.model.Usuario;
import java.util.List;
import java.util.ArrayList; 

public class UsuarioService {

    private UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public Usuario addUsuario(Usuario usuario) {
        if (usuario == null || usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            System.err.println("UsuarioService: Tentativa de adicionar usuário inválido (nulo ou nome vazio).");
            return null;
        }
        System.out.println("UsuarioService: Adicionando usuário: " + usuario.getNome());
        return usuarioDAO.addUsuario(usuario);
    }

    public Usuario getUsuarioById(int usuarioId) {
        System.out.println("UsuarioService: Buscando usuário por ID: " + usuarioId);
        return usuarioDAO.getUsuarioById(usuarioId);
    }

    public Usuario getUsuarioByNome(String nomeUsuario) {
        System.out.println("UsuarioService: Buscando usuário por nome: " + nomeUsuario);
        return usuarioDAO.getUsuarioByNome(nomeUsuario);
    }

    public List<Usuario> getAllUsuarios() {
        System.out.println("UsuarioService: Buscando todos os usuários.");
        List<Usuario> usuarios = usuarioDAO.getAllUsuarios();
        return usuarios != null ? usuarios : new ArrayList<>(); 
    }

    public boolean updatePontosUsuario(int usuarioId, int novosPontos) {
        System.out.println("UsuarioService: Atualizando pontos para usuário ID " + usuarioId + " para " + novosPontos + " pontos.");
        if (usuarioId <= 0) {
            System.err.println("UsuarioService: ID de usuário inválido para atualização de pontos.");
            return false;
        }
        return usuarioDAO.updatePontosUsuario(usuarioId, novosPontos);
    }

    public boolean deleteUsuario(int usuarioId) {
        System.out.println("UsuarioService: Tentando excluir usuário com ID: " + usuarioId);
        if (usuarioId <= 0) {
            System.err.println("UsuarioService: ID de usuário inválido para exclusão.");
            return false;
        }
        return usuarioDAO.deleteUsuario(usuarioId);
    }

    
    public static void main(String[] args) {
        UsuarioService usuarioService = new UsuarioService();

        System.out.println("--- INICIANDO TESTES COMPLETOS DO CRUD DE USUÁRIOS VIA SERVICE ---");

        
        System.out.println("\n--- Testando addUsuario ---");
        String nomeUsuarioTeste = "ServiceUser_" + System.currentTimeMillis();
        Usuario novoUsuario = new Usuario(nomeUsuarioTeste);
        Usuario usuarioAdicionado = usuarioService.addUsuario(novoUsuario);
        int idUsuarioTeste = -1;

        if (usuarioAdicionado != null && usuarioAdicionado.getId() > 0) {
            System.out.println("SUCESSO (addUsuario): " + usuarioAdicionado);
            idUsuarioTeste = usuarioAdicionado.getId();
        } else {
            System.out.println("FALHA (addUsuario) para nome: " + nomeUsuarioTeste);
            
            
        }

        
        if (idUsuarioTeste != -1) { 
            System.out.println("\n--- Testando addUsuario (nome duplicado) ---");
            Usuario usuarioDuplicado = new Usuario(nomeUsuarioTeste);
            Usuario resultadoDuplicado = usuarioService.addUsuario(usuarioDuplicado);
            if (resultadoDuplicado == null) {
                System.out.println("SUCESSO (addUsuario duplicado): Falhou como esperado para nome '" + nomeUsuarioTeste + "'.");
            } else {
                System.out.println("FALHA (addUsuario duplicado): Usuário duplicado foi adicionado: " + resultadoDuplicado);
            }
        }

        
        if (idUsuarioTeste != -1) {
            System.out.println("\n--- Testando getUsuarioById (ID existente) ---");
            Usuario buscadoPorId = usuarioService.getUsuarioById(idUsuarioTeste);
            if (buscadoPorId != null && buscadoPorId.getId() == idUsuarioTeste) {
                System.out.println("SUCESSO (getUsuarioById existente): " + buscadoPorId);
            } else {
                System.out.println("FALHA (getUsuarioById existente): Não encontrou ID " + idUsuarioTeste + " ou ID não confere. Encontrado: " + buscadoPorId);
            }
        }

        System.out.println("\n--- Testando getUsuarioById (ID não existente) ---");
        int idNaoExistente = 99999;
        Usuario naoEncontradoPorId = usuarioService.getUsuarioById(idNaoExistente);
        if (naoEncontradoPorId == null) {
            System.out.println("SUCESSO (getUsuarioById não existente): Nenhum usuário encontrado com ID " + idNaoExistente + " (CORRETO).");
        } else {
            System.out.println("FALHA (getUsuarioById não existente): Encontrou usuário " + naoEncontradoPorId);
        }

        
        if (idUsuarioTeste != -1) {
            System.out.println("\n--- Testando getUsuarioByNome (Nome existente) ---");
            Usuario buscadoPorNome = usuarioService.getUsuarioByNome(nomeUsuarioTeste);
            if (buscadoPorNome != null && buscadoPorNome.getNome().equals(nomeUsuarioTeste)) {
                System.out.println("SUCESSO (getUsuarioByNome existente): " + buscadoPorNome);
            } else {
                System.out.println("FALHA (getUsuarioByNome existente): Não encontrou nome '" + nomeUsuarioTeste + "' ou nome não confere. Encontrado: " + buscadoPorNome);
            }
        }
        
        System.out.println("\n--- Testando getUsuarioByNome (Nome não existente) ---");
        String nomeNaoExistente = "UsuarioQueNaoExiste_" + System.currentTimeMillis();
        Usuario naoEncontradoPorNome = usuarioService.getUsuarioByNome(nomeNaoExistente);
        if (naoEncontradoPorNome == null) {
            System.out.println("SUCESSO (getUsuarioByNome não existente): Nenhum usuário encontrado com nome '" + nomeNaoExistente + "' (CORRETO).");
        } else {
            System.out.println("FALHA (getUsuarioByNome não existente): Encontrou usuário " + naoEncontradoPorNome);
        }

        
        if (idUsuarioTeste != -1) {
            System.out.println("\n--- Testando updatePontosUsuario ---");
            int novosPontos = 250;
            boolean pontosAtualizados = usuarioService.updatePontosUsuario(idUsuarioTeste, novosPontos);
            if (pontosAtualizados) {
                Usuario usuarioAtualizado = usuarioService.getUsuarioById(idUsuarioTeste);
                if (usuarioAtualizado != null && usuarioAtualizado.getPontos() == novosPontos) {
                    System.out.println("SUCESSO (updatePontosUsuario): Pontos atualizados para " + novosPontos + ". Usuário: " + usuarioAtualizado);
                } else {
                    System.out.println("FALHA (updatePontosUsuario): Verificação pós-update falhou. Usuário: " + usuarioAtualizado);
                }
            } else {
                System.out.println("FALHA (updatePontosUsuario): Método retornou false para ID " + idUsuarioTeste);
            }
        }

        
        System.out.println("\n--- Adicionando segundo usuário para testes de getAll e delete ---");
        String nomeUsuario2 = "UsuarioTemporario_" + System.currentTimeMillis();
        Usuario usuario2 = new Usuario(nomeUsuario2);
        Usuario usuario2Adicionado = usuarioService.addUsuario(usuario2);
        int idUsuario2 = -1;
        if (usuario2Adicionado != null) idUsuario2 = usuario2Adicionado.getId();


        
        System.out.println("\n--- Testando getAllUsuarios ---");
        List<Usuario> todosUsuarios = usuarioService.getAllUsuarios();
        System.out.println("Total de usuários encontrados: " + todosUsuarios.size());
        boolean encontrouUsuario1 = false;
        boolean encontrouUsuario2 = false;
        for (Usuario u : todosUsuarios) {
            System.out.println(u);
            if (u.getId() == idUsuarioTeste) encontrouUsuario1 = true;
            if (u.getId() == idUsuario2) encontrouUsuario2 = true;
        }
        if (idUsuarioTeste != -1 && !encontrouUsuario1) System.out.println("ALERTA: Usuário de teste principal (ID: "+idUsuarioTeste+") não encontrado no getAllUsuarios.");
        if (idUsuario2 != -1 && !encontrouUsuario2) System.out.println("ALERTA: Segundo usuário de teste (ID: "+idUsuario2+") não encontrado no getAllUsuarios.");


        
        if (idUsuario2 != -1) { 
            System.out.println("\n--- Testando deleteUsuario (ID existente) ---");
            boolean deletado = usuarioService.deleteUsuario(idUsuario2);
            if (deletado) {
                Usuario usuarioAposDelete = usuarioService.getUsuarioById(idUsuario2);
                if (usuarioAposDelete == null) {
                    System.out.println("SUCESSO (deleteUsuario): Usuário com ID " + idUsuario2 + " não encontrado após delete (CORRETO).");
                } else {
                    System.out.println("FALHA (deleteUsuario): Usuário com ID " + idUsuario2 + " ainda encontrado: " + usuarioAposDelete);
                }
            } else {
                System.out.println("FALHA (deleteUsuario): Método retornou false para ID " + idUsuario2);
            }
        }

        System.out.println("\n--- Testando deleteUsuario (ID não existente) ---");
        boolean deleteNaoExistente = usuarioService.deleteUsuario(idNaoExistente);
        if (!deleteNaoExistente) {
            System.out.println("SUCESSO (deleteUsuario não existente): Tentativa de deletar ID " + idNaoExistente + " retornou false (CORRETO).");
        } else {
            System.out.println("FALHA (deleteUsuario não existente): Retornou true.");
        }
        
        System.out.println("\n--- LISTANDO TODOS OS USUÁRIOS APÓS TODOS OS TESTES ---");
        todosUsuarios = usuarioService.getAllUsuarios();
        System.out.println("Total de usuários encontrados agora: " + todosUsuarios.size());
        for (Usuario u : todosUsuarios) {
            System.out.println(u);
        }

        System.out.println("\n--- FIM DOS TESTES COMPLETOS DO CRUD DE USUÁRIOS VIA SERVICE ---");
    }
}