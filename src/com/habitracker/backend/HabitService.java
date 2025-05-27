package com.habitracker.backend; // Ou com.habitracker.service

import com.habitracker.database.HabitDAO;
import com.habitracker.database.ProgressoDiarioDAO;        // Adicionado
import com.habitracker.database.UsuarioDAO; // Adicionado
import com.habitracker.model.Conquista;
import com.habitracker.model.Habit;
import com.habitracker.model.ProgressoDiario;        // Adicionado
import com.habitracker.model.Usuario; // Adicionado
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List; // Para garantir que getAllUsuarios/Habits nunca retorne null

public class HabitService {

    private HabitDAO habitDAO;
    private UsuarioDAO usuarioDAO;              // Adicionado
    private ProgressoDiarioDAO progressoDiarioDAO; // Adicionado
    private ConquistaService conquistaService;

    public HabitService() {
        this.habitDAO = new HabitDAO();
        this.usuarioDAO = new UsuarioDAO();              // Adicionado
        this.progressoDiarioDAO = new ProgressoDiarioDAO(); // Adicionado
        this.conquistaService = new ConquistaService();
    }

    public List<Habit> getAllHabits() {
        List<Habit> habits = habitDAO.getAllHabits();
        return habits != null ? habits : new ArrayList<>();
    }

    public boolean addHabit(Habit habit) {
        if (habit == null || habit.getName() == null || habit.getName().trim().isEmpty()) {
            System.err.println("HabitService: Tentativa de adicionar hábito inválido.");
            return false;
        }
        return habitDAO.addHabit(habit);
    }

    public Habit getHabitById(int habitId) {
        System.out.println("HabitService: Buscando hábito com ID: " + habitId);
        Habit habit = habitDAO.getHabitById(habitId);
        if (habit != null) {
            System.out.println("HabitService: Hábito encontrado: " + habit.getName());
        } else {
            System.out.println("HabitService: Hábito com ID " + habitId + " não encontrado.");
        }
        return habit;
    }

    private void verificarEConcederConquistas(int usuarioId) {
    System.out.println("HabitService: Verificando conquistas para usuário ID: " + usuarioId);
    Usuario usuario = usuarioDAO.getUsuarioById(usuarioId); // Precisamos dos pontos atuais do usuário
    if (usuario == null) {
        System.err.println("HabitService: Usuário não encontrado para verificação de conquistas.");
        return;
    }

    List<Conquista> todasAsDefinicoes = conquistaService.getAllConquistaDefinicoes();
    List<Conquista> conquistasDoUsuario = conquistaService.getConquistasDoUsuario(usuarioId);

    for (Conquista definicaoConquista : todasAsDefinicoes) {
        boolean usuarioJaPossui = false;
        for (Conquista conquistaUsuario : conquistasDoUsuario) {
            if (conquistaUsuario.getId() == definicaoConquista.getId()) {
                usuarioJaPossui = true;
                break;
            }
        }

        if (!usuarioJaPossui) {
            boolean criterioAtendido = false;
            // Implementar lógica de verificação de critério para cada conquista
            // EXEMPLOS DE CRITÉRIOS:
            if ("Primeiros Passos".equalsIgnoreCase(definicaoConquista.getNome())) {
                // Critério: Ter completado pelo menos 1 hábito
                int totalCumpridos = progressoDiarioDAO.getCountProgressoCumprido(usuarioId);
                if (totalCumpridos >= 1) {
                    criterioAtendido = true;
                }
            } else if ("Pontuador Nato".equalsIgnoreCase(definicaoConquista.getNome())) {
                // Critério: Atingir 50 pontos (Exemplo)
                // (Este valor "50" poderia vir da 'definicaoConquista.getCriterioDesbloqueio()' se bem formatado)
                if (usuario.getPontos() >= 50) {
                    criterioAtendido = true;
                }
            }
            // Adicione mais 'else if' para outras conquistas e seus critérios específicos
            // Ex: "Persistente" (requereria buscar histórico de ProgressoDiario para verificar sequências)
            // Ex: "Colecionador de Hábitos" (requereria contar quantos hábitos o usuário tem cadastrados - precisaria de um método no HabitDAO/Service)


            if (criterioAtendido) {
                System.out.println("HabitService: Critério para conquista '" + definicaoConquista.getNome() + "' ATENDIDO para usuário ID " + usuarioId);
                boolean conquistaDada = conquistaService.darConquistaParaUsuario(usuarioId, definicaoConquista.getId());
                if (conquistaDada) {
                    System.out.println("HabitService: Conquista '" + definicaoConquista.getNome() + "' concedida ao usuário ID " + usuarioId);
                    // Aplicar pontos bônus, se houver
                    if (definicaoConquista.getPontosBonus() > 0) {
                        int pontosAtuais = usuario.getPontos(); // Pega os pontos mais recentes do usuário
                        int novosPontos = pontosAtuais + definicaoConquista.getPontosBonus();
                        usuarioDAO.updatePontosUsuario(usuarioId, novosPontos);
                        usuario.setPontos(novosPontos); // Atualiza o objeto usuário em memória para próximas verificações
                        System.out.println("HabitService: Bônus de " + definicaoConquista.getPontosBonus() + 
                                           " pontos concedido pela conquista. Novo total: " + novosPontos);
                    }
                }
            }
        }
    }
}

    public boolean updateHabit(Habit habit) {
        System.out.println("HabitService: Tentando atualizar hábito com ID: " + (habit != null ? habit.getId() : "null"));
        if (habit == null || habit.getId() <= 0) {
            System.err.println("HabitService: Tentativa de atualizar hábito inválido (hábito nulo ou ID inválido).");
            return false;
        }
        boolean sucesso = habitDAO.updateHabit(habit);
        if (sucesso) {
            System.out.println("HabitService: Hábito com ID " + habit.getId() + " atualizado com sucesso.");
        } else {
            System.out.println("HabitService: Falha ao atualizar hábito com ID " + habit.getId() + ".");
        }
        return sucesso;
    }

    public boolean deleteHabit(int habitId) {
        System.out.println("HabitService: Tentando excluir hábito com ID: " + habitId);
        if (habitId <= 0) {
            System.err.println("HabitService: Tentativa de excluir hábito com ID inválido.");
            return false;
        }
        boolean sucesso = habitDAO.deleteHabit(habitId);
        if (sucesso) {
            System.out.println("HabitService: Hábito com ID " + habitId + " excluído com sucesso.");
        } else {
            System.out.println("HabitService: Falha ao excluir hábito com ID " + habitId + " (pode já não existir).");
        }
        return sucesso;
    }

    // --- NOVO MÉTODO PARA MARCAR PROGRESSO E DAR PONTOS ---
    public boolean marcarHabitoComoFeito(int usuarioId, int habitoId, LocalDate data, boolean novoStatusCumprido) {
    // 1. Validar entradas básicas
    if (usuarioId <= 0 || habitoId <= 0 || data == null) {
        System.err.println("HabitService: Parâmetros inválidos para marcar hábito como feito (IDs ou data).");
        return false;
    }

    // 2. Verificar se o hábito e usuário existem
    Habit habitParaMarcar = habitDAO.getHabitById(habitoId); // Renomeado para evitar conflito de nome
    Usuario usuarioParaMarcar = usuarioDAO.getUsuarioById(usuarioId); // Renomeado

    if (habitParaMarcar == null) {
        System.err.println("HabitService: Hábito com ID " + habitoId + " não encontrado.");
        return false;
    }
    if (usuarioParaMarcar == null) {
        System.err.println("HabitService: Usuário com ID " + usuarioId + " não encontrado.");
        return false;
    }

    // 3. Lógica para lidar com o progresso
    ProgressoDiario progressoExistente = progressoDiarioDAO.getProgresso(usuarioId, habitoId, data);
    ProgressoDiario progressoProcessado = null; // Esta variável substitui 'progressoSalvo'

    if (progressoExistente != null) {
        // Progresso já existe para este usuário, hábito e data
        if (progressoExistente.isStatusCumprido() == novoStatusCumprido) {
            // O status existente já é o que queremos marcar. Consideramos um sucesso.
            System.out.println("HabitService: Progresso já existe com o status desejado (ID: " + progressoExistente.getId() +
                               ", Status: " + progressoExistente.isStatusCumprido() + "). Nenhuma alteração necessária.");
            progressoProcessado = progressoExistente;
        } else {
            // Progresso existe, mas com status diferente.
            // O DAO atual não tem um método update. Tentar adicionar novamente falharia.
            System.err.println("HabitService: Progresso já existe (ID: " + progressoExistente.getId() +
                               ") com status (" + progressoExistente.isStatusCumprido() +
                               ") diferente do novo status solicitado (" + novoStatusCumprido +
                               "). O DAO atual não suporta atualização direta do status. " +
                               "A operação para alterar o status não será realizada.");
            // progressoProcessado permanece null aqui, pois não conseguimos alterar o status.
            // Se a intenção fosse apenas garantir que *algum* registro exista, poderíamos usar progressoExistente.
            // Mas como a intenção é "marcar com novoStatusCumprido", e não conseguimos, tratamos como falha na mudança.
        }
    } else {
        // Progresso não existe, então tentaremos adicionar um novo.
        System.out.println("HabitService: Nenhum progresso existente encontrado para Usuário ID: " + usuarioId +
                           ", Hábito ID: " + habitoId + ", Data: " + data + ". Tentando adicionar novo progresso.");
        // Assumindo que seu construtor de ProgressoDiario seja:
        // ProgressoDiario(int usuarioId, int habitoId, LocalDate dataRegistro, boolean statusCumprido)
        ProgressoDiario novoProgresso = new ProgressoDiario(usuarioId, habitoId, data, novoStatusCumprido);
        progressoProcessado = progressoDiarioDAO.addProgresso(novoProgresso);

        if (progressoProcessado != null) {
            System.out.println("HabitService: Novo progresso adicionado com sucesso (ID: " + progressoProcessado.getId() +
                               ", Status: " + progressoProcessado.isStatusCumprido() + ").");
        } else {
            System.err.println("HabitService: Falha ao adicionar novo progresso (verificar logs do ProgressoDiarioDAO, " +
                               "pode ser erro de constraint ou conexão).");
        }
    }

    // 4. Processar resultado e dar pontos/conquistas se aplicável
    if (progressoProcessado != null && progressoProcessado.getId() > 0) {
    boolean concederPontosEVerificarConquistas = false;
    if (novoStatusCumprido && progressoProcessado.isStatusCumprido()) {
        if (progressoExistente == null) {
            // Foi um novo registro de progresso adicionado como 'cumprido'.
            concederPontosEVerificarConquistas = true;
            System.out.println("HabitService: Hábito recém-marcado como CUMPRIDO. Concedendo pontos.");
        } else if (progressoExistente.isStatusCumprido()) {
            // Já existia e JÁ ESTAVA cumprido. Nenhuma nova pontuação.
            System.out.println("HabitService: Hábito já estava CUMPRIDO (ID Progresso: " + progressoProcessado.getId() + "). Nenhuma nova pontuação.");
        } else {
             System.out.println("HabitService: Hábito existia com status diferente. Status final: " + progressoProcessado.isStatusCumprido());
        }
    }


    if (concederPontosEVerificarConquistas) {
        Usuario usuarioParaPontos = usuarioDAO.getUsuarioById(usuarioId);
        if (usuarioParaPontos != null) {
            int pontosGanhos = 10;
            int pontosAtuais = usuarioParaPontos.getPontos();
            int novosPontos = pontosAtuais + pontosGanhos;

            boolean pontosAtualizados = usuarioDAO.updatePontosUsuario(usuarioId, novosPontos);
            if (pontosAtualizados) {
                System.out.println("HabitService: Usuário ID " + usuarioId + " ganhou " + pontosGanhos +
                                   " pontos. Total agora: " + novosPontos);
                usuarioParaPontos.setPontos(novosPontos);
                verificarEConcederConquistas(usuarioId);
            } else {
                System.err.println("HabitService: Falha ao atualizar os pontos do usuário ID " + usuarioId);
            }
        } else {
            System.err.println("HabitService: Usuário ID " + usuarioId + " não encontrado para atualização de pontos (inesperado).");
        }
    } else if (progressoProcessado.isStatusCumprido() && novoStatusCumprido) {
        // Se chegou aqui, significa que novoStatusCumprido = true, progressoProcessado.isStatusCumprido() = true,
        // mas não era um NOVO registro. Ex: já estava cumprido.
        System.out.println("HabitService: Hábito (ID Progresso: " + progressoProcessado.getId() +
                           ") confirmado como CUMPRIDO, mas não era uma nova marcação para pontuação.");
    } else if (!novoStatusCumprido && !progressoProcessado.isStatusCumprido()) {
        System.out.println("HabitService: Hábito (ID Progresso: " + progressoProcessado.getId() +
                           ") efetivamente marcado como NÃO CUMPRIDO.");
    }

    return true;
} else {
    System.err.println("HabitService: Falha ao registrar ou confirmar progresso diário. " +
                       "Nenhum progresso válido foi efetivamente processado ou criado.");
    return false;
}
}

    public static void main(String[] args) {
        HabitService habitService = new HabitService();
        UsuarioService usuarioServiceParaTeste = new UsuarioService(); // Usar UsuarioService para criar/gerenciar usuários nos testes

        System.out.println("--- INICIANDO TESTES COMPLETOS DO CRUD DE HÁBITOS VIA SERVICE ---");

        System.out.println("\n--- Testando addHabit ---");
        String nomeHabitoTesteCRUD = "Hábito CRUD Master - " + System.currentTimeMillis();
        Habit habitoParaCRUD = new Habit(nomeHabitoTesteCRUD, "Descrição para teste CRUD completo", LocalDate.now());
        boolean adicionado = habitService.addHabit(habitoParaCRUD);
        int idHabitoCRUD = -1;

        if (adicionado) {
            System.out.println("Hábito '" + nomeHabitoTesteCRUD + "' adicionado para os testes.");
            List<Habit> habitosPosAdicao = habitService.getAllHabits();
            for (Habit h : habitosPosAdicao) {
                if (h.getName().equals(nomeHabitoTesteCRUD)) {
                    idHabitoCRUD = h.getId();
                    System.out.println("ID do hábito de teste CRUD: " + idHabitoCRUD);
                    break;
                }
            }
        } else {
            System.out.println("ERRO: Falha ao adicionar hábito principal para os testes CRUD.");
            // Considerar não retornar se a falha for esperada em algum cenário de teste futuro
        }

        if (idHabitoCRUD == -1 && adicionado) { // Se foi adicionado mas não achou ID
            System.out.println("ERRO CRÍTICO: Hábito foi adicionado mas não foi possível obter o ID. Testes subsequentes podem falhar.");
        }
        
        System.out.println("\n--- Testando getAllHabits ---");
        List<Habit> todosOsHabitos = habitService.getAllHabits();
        System.out.println("Total de hábitos encontrados: " + todosOsHabitos.size());
        for (Habit h : todosOsHabitos) {
            System.out.println(h);
        }

        if(idHabitoCRUD != -1) { // Só testa get, update, delete se o hábito foi criado e ID obtido
            System.out.println("\n--- Testando getHabitById (com ID existente) ---");
            Habit hEncontrado = habitService.getHabitById(idHabitoCRUD);
            if (hEncontrado != null) {
                System.out.println("Encontrado: " + hEncontrado);
            } else {
                System.out.println("ERRO: Hábito com ID " + idHabitoCRUD + " não encontrado, mas deveria existir.");
            }

            System.out.println("\n--- Testando updateHabit ---");
            if (hEncontrado != null) {
                String novoNomeUpdate = "Hábito CRUD ATUALIZADO - " + System.currentTimeMillis();
                hEncontrado.setName(novoNomeUpdate);
                hEncontrado.setDescription("Descrição ATUALIZADA para teste CRUD.");
                
                boolean atualizado = habitService.updateHabit(hEncontrado);
                if (atualizado) {
                    System.out.println("Update reportado como sucesso.");
                    Habit hAposUpdate = habitService.getHabitById(idHabitoCRUD);
                    if (hAposUpdate != null && hAposUpdate.getName().equals(novoNomeUpdate)) {
                        System.out.println("VERIFICADO: Hábito atualizado corretamente para: " + hAposUpdate);
                    } else {
                        System.out.println("ERRO NA VERIFICAÇÃO do update. Hábito encontrado: " + hAposUpdate);
                    }
                } else {
                    System.out.println("ERRO: Update reportado como falha.");
                }
            } else {
                System.out.println("Skipping teste de update pois o hábito base não foi encontrado anteriormente.");
            }
        } else {
            System.out.println("\nSkipping testes de getById, update e delete para hábito CRUD pois o ID não foi obtido.");
        }

        System.out.println("\n--- Testando getHabitById (com ID não existente) ---");
        int idNaoExistente = 99999;
        Habit hNaoEncontrado = habitService.getHabitById(idNaoExistente);
        if (hNaoEncontrado == null) {
            System.out.println("Hábito com ID " + idNaoExistente + " não encontrado (CORRETO).");
        } else {
            System.out.println("ERRO: Hábito com ID " + idNaoExistente + " encontrado, mas não deveria.");
        }
        
        // --- INÍCIO DOS TESTES DE MARCAR HÁBITO COMO FEITO ---
        System.out.println("\n\n--- INICIANDO TESTES DE MARCAR HÁBITO COMO FEITO ---");

        // 1. Criar um usuário para o teste
        String nomeUsuarioProgresso = "UsuarioParaProgresso_" + System.currentTimeMillis();
        Usuario usuarioTesteProgresso = usuarioServiceParaTeste.addUsuario(new Usuario(nomeUsuarioProgresso));
        int idUsuarioParaProgresso = -1;

        if (usuarioTesteProgresso != null && usuarioTesteProgresso.getId() > 0) {
            idUsuarioParaProgresso = usuarioTesteProgresso.getId();
            System.out.println("Usuário de teste para progresso criado: " + usuarioTesteProgresso);
        } else {
            System.out.println("ERRO: Não foi possível criar usuário para teste de progresso. Abortando testes de progresso.");
            // Se não temos usuário, não podemos prosseguir com os testes de progresso
            System.out.println("\n--- FIM DOS TESTES (HABITSERVICE) ---");
            return; 
        }

        // 2. Criar um hábito para o teste (ou usar idHabitoCRUD se ele foi criado com sucesso)
        int idHabitoParaProgresso = idHabitoCRUD; // Reutiliza o hábito do teste CRUD se existir
        if (idHabitoParaProgresso == -1) { // Se o hábito do CRUD não foi criado, adiciona um novo
            String nomeHabitoUnicoProgresso = "HabitoProgressoUnico_" + System.currentTimeMillis();
            Habit habitoProg = new Habit(nomeHabitoUnicoProgresso, "Habito para testar progresso", LocalDate.now());
            if(habitService.addHabit(habitoProg)){
                 List<Habit> todosHabitosAgora = habitService.getAllHabits();
                 for(Habit h : todosHabitosAgora){
                     if(h.getName().equals(nomeHabitoUnicoProgresso)){
                         idHabitoParaProgresso = h.getId();
                         System.out.println("Novo hábito (ID: "+idHabitoParaProgresso+") criado para teste de progresso.");
                         break;
                     }
                 }
            }
        }
        
        if (idHabitoParaProgresso == -1) {
            System.out.println("ERRO: Não foi possível obter ID de hábito para teste de marcar como feito. Abortando testes de progresso.");
            System.out.println("\n--- FIM DOS TESTES (HABITSERVICE) ---");
            return;
        }
        
        // 3. Marcar como feito hoje
        LocalDate hoje = LocalDate.now();
        System.out.println("\nTentando marcar hábito ID " + idHabitoParaProgresso + " como CUMPRIDO para usuário ID " + idUsuarioParaProgresso + " em " + hoje);
        
        Usuario usuarioAntesPontos = usuarioServiceParaTeste.getUsuarioById(idUsuarioParaProgresso);
        int pontosAntes = (usuarioAntesPontos != null) ? usuarioAntesPontos.getPontos() : 0;
        System.out.println("Pontos do usuário ANTES de marcar: " + pontosAntes);

        boolean marcadoOK = habitService.marcarHabitoComoFeito(idUsuarioParaProgresso, idHabitoParaProgresso, hoje, true);

        if (marcadoOK) {
            System.out.println("SUCESSO: Hábito marcado como feito.");
            Usuario usuarioAposMarcar = usuarioServiceParaTeste.getUsuarioById(idUsuarioParaProgresso); // Usar UsuarioService para buscar usuário
            System.out.println("Status do usuário APÓS marcar hábito: " + usuarioAposMarcar);
            if (usuarioAposMarcar != null && usuarioAposMarcar.getPontos() == (pontosAntes + 10)) {
                System.out.println("VERIFICADO: Pontos do usuário atualizados corretamente!");
            } else {
                System.out.println("ERRO NA VERIFICAÇÃO: Pontos do usuário NÃO foram atualizados como esperado. Esperado: "+(pontosAntes+10)+", Atual: "+ (usuarioAposMarcar != null ? usuarioAposMarcar.getPontos() : "null"));
            }
            
            ProgressoDiarioDAO pDAO = new ProgressoDiarioDAO(); 
            ProgressoDiario progressoVerificado = pDAO.getProgresso(idUsuarioParaProgresso, idHabitoParaProgresso, hoje);
            if (progressoVerificado != null && progressoVerificado.isStatusCumprido()) {
                 System.out.println("VERIFICADO: Registro de progresso encontrado e correto: " + progressoVerificado);
            } else {
                System.out.println("ERRO NA VERIFICAÇÃO: Registro de progresso não encontrado ou status incorreto. Encontrado: " + progressoVerificado);
            }
        } else {
            System.out.println("FALHA ao marcar hábito como feito.");
        }

        // 4. Tentar marcar o mesmo novamente (deve informar que já existe com mesmo status)
        System.out.println("\nTentando marcar o MESMO hábito ID " + idHabitoParaProgresso + " como CUMPRIDO novamente para usuário ID " + idUsuarioParaProgresso + " em " + hoje);
        marcadoOK = habitService.marcarHabitoComoFeito(idUsuarioParaProgresso, idHabitoParaProgresso, hoje, true);
        if (marcadoOK) { 
            System.out.println("SUCESSO: Tentativa de marcar novamente (mesmo status) reportou sucesso (já estava como queríamos).");
        } else {
             System.out.println("FALHA ou lógica diferente na remarcação (status diferente?). Retorno: " + marcadoOK);
        }

        // 5. Teste de delete do hábito principal do CRUD (se foi criado)
        if (idHabitoCRUD != -1) {
             System.out.println("\n--- Testando deleteHabit (hábito do CRUD) ---");
            System.out.println("Tentando excluir hábito com ID: " + idHabitoCRUD);
            boolean deletado = habitService.deleteHabit(idHabitoCRUD);
            if (deletado) {
                System.out.println("Delete reportado como sucesso.");
                Habit hAposDelete = habitService.getHabitById(idHabitoCRUD);
                if (hAposDelete == null) {
                    System.out.println("VERIFICADO: Hábito com ID " + idHabitoCRUD + " não encontrado após delete (CORRETO).");
                } else {
                    System.out.println("ERRO NA VERIFICAÇÃO do delete. Hábito ainda encontrado: " + hAposDelete);
                }
            } else {
                System.out.println("ERRO: Delete reportado como falha para o ID: " + idHabitoCRUD);
            }
        }


        System.out.println("\n--- LISTANDO TODOS OS HÁBITOS NO FINAL DOS TESTES ---");
        todosOsHabitos = habitService.getAllHabits();
        System.out.println("Total de hábitos encontrados agora: " + todosOsHabitos.size());
        for (Habit h : todosOsHabitos) {
            System.out.println(h);
        }
        
        System.out.println("\n--- LISTANDO TODOS OS USUÁRIOS NO FINAL DOS TESTES ---");
        List<Usuario> todosUsuariosFinal = usuarioServiceParaTeste.getAllUsuarios();
        System.out.println("Total de usuários encontrados agora: " + todosUsuariosFinal.size());
        for (Usuario u : todosUsuariosFinal) {
            System.out.println(u);
        }


        System.out.println("\n--- FIM DOS TESTES (HABITSERVICE) ---");
    }
}