package com.habitracker.backend; // Ou com.habitracker.backend

import com.habitracker.database.ConquistaDAO;
import com.habitracker.database.UsuarioDAO; // Para o main de teste
import com.habitracker.model.Conquista;
import com.habitracker.model.Usuario;   // Para o main de teste
import java.util.List;
import java.util.ArrayList;

public class ConquistaService {

    private ConquistaDAO conquistaDAO;

    public ConquistaService() {
        this.conquistaDAO = new ConquistaDAO();
    }

    public Conquista addConquistaDefinicao(Conquista conquista) {
        if (conquista == null || conquista.getNome() == null || conquista.getNome().trim().isEmpty()) {
            System.err.println("ConquistaService: Tentativa de adicionar definição de conquista inválida.");
            return null;
        }
        System.out.println("ConquistaService: Adicionando definição de conquista: " + conquista.getNome());
        return conquistaDAO.addConquistaDefinicao(conquista);
    }

    public Conquista getConquistaDefinicaoById(int conquistaId) {
        System.out.println("ConquistaService: Buscando definição de conquista por ID: " + conquistaId);
        return conquistaDAO.getConquistaDefinicaoById(conquistaId);
    }

    public List<Conquista> getAllConquistaDefinicoes() {
        System.out.println("ConquistaService: Buscando todas as definições de conquistas.");
        List<Conquista> definicoes = conquistaDAO.getAllConquistaDefinicoes();
        return definicoes != null ? definicoes : new ArrayList<>();
    }

    public boolean darConquistaParaUsuario(int usuarioId, int conquistaId) {
        System.out.println("ConquistaService: Tentando dar conquista ID " + conquistaId + " para usuário ID " + usuarioId);
        if (usuarioId <= 0 || conquistaId <= 0) {
            System.err.println("ConquistaService: IDs inválidos para dar conquista.");
            return false;
        }
        // A lógica de verificar se já possui está no DAO, mas poderia estar aqui também.
        return conquistaDAO.darConquistaParaUsuario(usuarioId, conquistaId);
    }

    public List<Conquista> getConquistasDoUsuario(int usuarioId) {
        System.out.println("ConquistaService: Buscando conquistas do usuário ID: " + usuarioId);
        if (usuarioId <= 0) {
            System.err.println("ConquistaService: ID de usuário inválido para buscar conquistas.");
            return new ArrayList<>();
        }
        List<Conquista> conquistas = conquistaDAO.getConquistasDoUsuario(usuarioId);
        return conquistas != null ? conquistas : new ArrayList<>();
    }

    public static void main(String[] args) {
        ConquistaService conquistaService = new ConquistaService();
        UsuarioService usuarioServiceParaTeste = new UsuarioService(); // Para criar/buscar usuário

        System.out.println("--- Testando ConquistaService ---");

        // 1. Adicionar definições de conquistas via serviço
        System.out.println("\n--- Adicionando Definições de Conquistas (via Service) ---");
        Conquista cDef1 = new Conquista("Iniciante Mestre", "Completou o tutorial.", "Completar tutorial", 5);
        Conquista cDef2 = new Conquista("Foco Total", "Manteve um hábito por 7 dias.", "7 dias de um hábito", 75);
        
        Conquista cSalva1 = conquistaService.addConquistaDefinicao(cDef1);
        Conquista cSalva2 = conquistaService.addConquistaDefinicao(cDef2);

        if(cSalva1 != null) System.out.println("Definição (via Service) salva: " + cSalva1); else System.out.println("Falha ao salvar cDef1");
        if(cSalva2 != null) System.out.println("Definição (via Service) salva: " + cSalva2); else System.out.println("Falha ao salvar cDef2");

        // 2. Listar todas as definições de conquistas via serviço
        System.out.println("\n--- Listando Todas as Definições de Conquistas (via Service) ---");
        List<Conquista> todasDefinicoes = conquistaService.getAllConquistaDefinicoes();
        System.out.println("Total de definições encontradas: " + todasDefinicoes.size());
        for(Conquista c : todasDefinicoes) {
            System.out.println(c);
        }
        
        // 3. Criar um usuário de teste
        String nomeUsuarioConqServ = "UserConqService_" + System.currentTimeMillis();
        Usuario usuarioTeste = usuarioServiceParaTeste.addUsuario(new Usuario(nomeUsuarioConqServ));
        int idUsuarioTeste = -1;

        if (usuarioTeste == null || usuarioTeste.getId() <= 0) {
            System.out.println("ERRO: Não foi possível criar usuário para teste de ConquistaService. Abortando parte dos testes.");
        } else {
            idUsuarioTeste = usuarioTeste.getId();
            System.out.println("\nUsuário de teste para ConquistaService: " + usuarioTeste);

            // 4. Dar uma conquista ao usuário via serviço (usar ID de conquista salva)
            if (cSalva1 != null) {
                System.out.println("\n--- Dando Conquista '" + cSalva1.getNome() + "' ao Usuário ID " + idUsuarioTeste + " (via Service) ---");
                boolean conquistaDada = conquistaService.darConquistaParaUsuario(idUsuarioTeste, cSalva1.getId());
                System.out.println("Resultado de dar conquista: " + conquistaDada);

                // Tentar dar a mesma conquista novamente
                System.out.println("Tentando dar a MESMA conquista '" + cSalva1.getNome() + "' novamente...");
                conquistaDada = conquistaService.darConquistaParaUsuario(idUsuarioTeste, cSalva1.getId());
                System.out.println("Resultado de dar conquista novamente: " + conquistaDada + " (Esperado true, pois DAO lida com duplicidade)");
            } else {
                System.out.println("Skipping teste 'darConquistaParaUsuario' pois cSalva1 é null.");
            }

            // 5. Listar conquistas do usuário via serviço
            System.out.println("\n--- Listando Conquistas do Usuário ID " + idUsuarioTeste + " (via Service) ---");
            List<Conquista> conquistasDoUsuario = conquistaService.getConquistasDoUsuario(idUsuarioTeste);
            System.out.println("Usuário possui " + conquistasDoUsuario.size() + " conquistas:");
            for(Conquista cUser : conquistasDoUsuario) {
                System.out.println(cUser);
            }
        }
        System.out.println("\n--- FIM DOS TESTES ConquistaService ---");
    }
}