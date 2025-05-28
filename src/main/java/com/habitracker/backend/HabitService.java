package com.habitracker.backend;

// Imports dos seus pacotes de model, database
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.habitracker.database.HabitDAO;
import com.habitracker.database.ProgressoDiarioDAO;
import com.habitracker.database.UsuarioDAO;
import com.habitracker.model.Conquista;
import com.habitracker.model.Habit;
import com.habitracker.model.ProgressoDiario;
import com.habitracker.model.Usuario;
import com.habitracker.serviceapi.HabitTrackerServiceAPI;
import com.habitracker.serviceapi.dto.FeedbackMarcacaoDTO;
import com.habitracker.serviceapi.exceptions.HabitNotFoundException;
import com.habitracker.serviceapi.exceptions.PersistenceException;
import com.habitracker.serviceapi.exceptions.UserNotFoundException; // Para listas vazias no DTO
import com.habitracker.serviceapi.exceptions.ValidationException;

public class HabitService implements HabitTrackerServiceAPI { // Implementa a nova interface

    private final HabitDAO habitDAO; // Boa prática torná-las 'final' se injetadas pelo construtor
    private final UsuarioDAO usuarioDAO;
    private final ProgressoDiarioDAO progressoDiarioDAO;
    private final ConquistaService conquistaService;

    public HabitService(HabitDAO habitDAO, UsuarioDAO usuarioDAO, 
                        ProgressoDiarioDAO progressoDiarioDAO, 
                        ConquistaService conquistaService) {
        this.habitDAO = habitDAO;
        this.usuarioDAO = usuarioDAO;
        this.progressoDiarioDAO = progressoDiarioDAO;
        this.conquistaService = conquistaService;
    }

    @Override
    public List<Habit> getAllHabits() {
        List<Habit> habits = habitDAO.getAllHabits();
        // É uma boa prática garantir que nunca se retorne null para uma coleção
        return habits != null ? habits : new ArrayList<>();
    }

    @Override
    public Habit addHabit(Habit habit) throws PersistenceException, ValidationException {
        if (habit == null || habit.getName() == null || habit.getName().trim().isEmpty()) {
            throw new ValidationException("Dados do hábito inválidos: nome não pode ser vazio.");
        }
        // ASSUMINDO que habitDAO.addHabit foi modificado para retornar Habit com ID
        Habit habitAdicionado = habitDAO.addHabit(habit);
        if (habitAdicionado == null) {
            // Isso aconteceria se o DAO falhasse em inserir ou em obter o ID gerado
            throw new PersistenceException("Falha ao salvar o hábito no banco de dados.");
        }
        System.out.println("HabitService: Hábito adicionado com ID: " + habitAdicionado.getId());
        return habitAdicionado;
    }

    @Override
    public Habit getHabitById(int habitId) throws HabitNotFoundException {
        if (habitId <= 0) {
             throw new HabitNotFoundException("ID do hábito inválido: " + habitId + ". IDs devem ser positivos.");
        }
        Habit habit = habitDAO.getHabitById(habitId);
        if (habit == null) {
            throw new HabitNotFoundException("Hábito com ID " + habitId + " não encontrado.");
        }
        System.out.println("HabitService: Hábito encontrado: " + habit.getName());
        return habit;
    }

    @Override
    public Habit updateHabit(Habit habitToUpdate) throws HabitNotFoundException, PersistenceException, ValidationException {
        if (habitToUpdate == null || habitToUpdate.getId() <= 0) {
            throw new ValidationException("Dados do hábito para atualização inválidos: ID nulo ou inválido.");
        }
        if (habitToUpdate.getName() == null || habitToUpdate.getName().trim().isEmpty()) {
            throw new ValidationException("Nome do hábito não pode ser vazio na atualização.");
        }

        // Verifica se o hábito existe antes de tentar atualizar
        // O método getHabitById já lança HabitNotFoundException se não encontrar
        getHabitById(habitToUpdate.getId()); // Se não existir, lança exceção aqui

        boolean sucessoNoDAO = habitDAO.updateHabit(habitToUpdate); 
        if (sucessoNoDAO) {
            System.out.println("HabitService: Hábito com ID " + habitToUpdate.getId() + " atualizado com sucesso no DAO.");
            // Re-busca do DAO para garantir que temos o objeto mais atualizado e para consistência de retorno
            Habit habitAtualizado = habitDAO.getHabitById(habitToUpdate.getId());
            if (habitAtualizado == null) { // Inesperado se o update foi sucesso
                 throw new PersistenceException("Falha crítica ao re-buscar o hábito após atualização bem-sucedida, ID: " + habitToUpdate.getId());
            }
            return habitAtualizado;
        } else {
            // Se o DAO retornou false, pode ser que o ID não existia (já checado) ou outro erro de DB
            System.err.println("HabitService: Falha ao atualizar hábito com ID " + habitToUpdate.getId() + " no DAO (nenhuma linha afetada).");
            throw new PersistenceException("Falha ao atualizar o hábito no banco de dados (nenhuma linha afetada ou erro), ID: " + habitToUpdate.getId());
        }
    }

    @Override
    public boolean deleteHabit(int habitId) throws HabitNotFoundException, PersistenceException {
         if (habitId <= 0) {
            throw new HabitNotFoundException("ID do hábito inválido para exclusão: " + habitId + ". IDs devem ser positivos.");
        }
        // Verifica se o hábito existe antes de tentar deletar
        getHabitById(habitId); // Lança HabitNotFoundException se não existir

        boolean sucessoNoDAO = habitDAO.deleteHabit(habitId);
        if (!sucessoNoDAO) {
            // Se chegou aqui, e o hábito existia, a falha no delete é um erro de persistência
            throw new PersistenceException("Falha ao excluir o hábito com ID " + habitId + " do banco de dados (operação DAO retornou false).");
        }
        System.out.println("HabitService: Hábito com ID " + habitId + " excluído com sucesso.");
        return true;
    }

    @Override
    public FeedbackMarcacaoDTO marcarHabitoComoFeito(int usuarioId, int habitoId, LocalDate data)
            throws UserNotFoundException, HabitNotFoundException, PersistenceException, ValidationException {
        
        Usuario usuarioParaMarcar = usuarioDAO.getUsuarioById(usuarioId);
        if (usuarioParaMarcar == null) {
            throw new UserNotFoundException("Usuário com ID " + usuarioId + " não encontrado para marcar hábito.");
        }
        // getHabitById já lança HabitNotFoundException se não encontrar o hábito
        Habit habitParaMarcar = this.getHabitById(habitoId); // Usa o método da própria classe que já lança exceção
        
        if (data == null) {
            throw new ValidationException("Data para marcar hábito não pode ser nula."); // Usando ValidationException
        }

        final boolean STATUS_A_MARCAR_COMO_FEITO = true; // Esta chamada da API sempre marca como feito
        ProgressoDiario progressoExistente = progressoDiarioDAO.getProgresso(usuarioId, habitoId, data);
        ProgressoDiario progressoProcessado = null;
        boolean foiUmaNovaConclusaoReal = false;
        int pontosGanhosPeloHabito = 0;

        if (progressoExistente != null) {
            if (progressoExistente.isStatusCumprido() == STATUS_A_MARCAR_COMO_FEITO) {
                progressoProcessado = progressoExistente;
                System.out.println("HabitService: Progresso (ID: " + progressoExistente.getId() + ") já existe com status 'cumprido'.");
            } else {
                // Progresso existe mas com status 'não cumprido'.
                // Seu DAO ProgressoDiarioDAO não tem um método 'update'.
                // O método 'addProgresso' falharia por constraint de chave única.
                // Idealmente, você adicionaria um método updateStatus no ProgressoDiarioDAO.
                // Por agora, vamos indicar que não podemos alterar e retornar feedback disso.
                System.err.println("HabitService: Progresso (ID: " + progressoExistente.getId() + ") já existe com status 'não cumprido'. Atualização de status não implementada no DAO.");
                return new FeedbackMarcacaoDTO(false, "Hábito já registrado como 'não cumprido'. Alteração de status não suportada no momento.");
            }
        } else {
            // Progresso não existe, criar novo.
            ProgressoDiario novoProgresso = new ProgressoDiario(usuarioId, habitoId, data, STATUS_A_MARCAR_COMO_FEITO);
            progressoProcessado = progressoDiarioDAO.addProgresso(novoProgresso);
            if (progressoProcessado == null) {
                throw new PersistenceException("Falha ao adicionar novo progresso para hábito ID: " + habitoId + " e usuário ID: " + usuarioId);
            }
            System.out.println("HabitService: Novo progresso adicionado (ID: " + progressoProcessado.getId() + ").");
            if (STATUS_A_MARCAR_COMO_FEITO) {
                foiUmaNovaConclusaoReal = true;
            }
        }

        List<Conquista> novasConquistasNestaRodada = Collections.emptyList();
        // O objeto usuarioParaMarcar tem os pontos ANTES desta operação.
        // Vamos pegar uma cópia ou usar os valores e atualizar o objeto se necessário.
        int pontosAtuaisDoUsuario = usuarioParaMarcar.getPontos();

        if (foiUmaNovaConclusaoReal) {
            // Se o seu modelo Habit tiver um campo para pontos, use-o. Ex: habitParaMarcar.getPontosRecompensa()
            pontosGanhosPeloHabito = 10; // Valor fixo de exemplo
            int pontosUsuarioAposHabito = pontosAtuaisDoUsuario + pontosGanhosPeloHabito;
            
            if (!usuarioDAO.updatePontosUsuario(usuarioId, pontosUsuarioAposHabito)) {
                throw new PersistenceException("Falha ao atualizar pontos do usuário (" + usuarioId + ") após marcar hábito.");
            }
            usuarioParaMarcar.setPontos(pontosUsuarioAposHabito); // Atualiza o objeto em memória
            System.out.println("HabitService: Usuário ID " + usuarioId + " ganhou " + pontosGanhosPeloHabito + " pontos pelo hábito. Total parcial: " + pontosUsuarioAposHabito);
            
            // Passa o objeto 'usuarioParaMarcar' que agora tem os pontos atualizados (pelo hábito)
            novasConquistasNestaRodada = verificarEConcederConquistasInterno(usuarioId, usuarioParaMarcar);
        }

        // usuarioParaMarcar.getPontos() agora reflete o total após hábito e possíveis bônus de conquista.
        return new FeedbackMarcacaoDTO(
            true, 
            foiUmaNovaConclusaoReal ? "Hábito marcado como feito com sucesso!" : "Hábito já estava marcado como feito.",
            pontosGanhosPeloHabito,
            usuarioParaMarcar.getPontos(), 
            novasConquistasNestaRodada
        );
    }

    // Renomeado para evitar conflito com um possível método público futuro na API se necessário.
    // Este método modifica o objeto Usuario passado (usuarioParaAtualizarPontos)
    private List<Conquista> verificarEConcederConquistasInterno(int usuarioId, Usuario usuarioParaAtualizarPontos) throws UserNotFoundException, PersistenceException {
        System.out.println("HabitService: Verificando conquistas para usuário ID: " + usuarioId + " com " + usuarioParaAtualizarPontos.getPontos() + " pontos.");

        List<Conquista> todasAsDefinicoes = conquistaService.getAllConquistaDefinicoes();
        List<Conquista> conquistasAtuaisDoUsuario = conquistaService.getConquistasDoUsuario(usuarioId);
        List<Conquista> novasConquistasDesbloqueadasNestaRodada = new ArrayList<>();

        for (Conquista definicaoConquista : todasAsDefinicoes) {
            boolean usuarioJaPossui = false;
            for (Conquista conquistaUsuario : conquistasAtuaisDoUsuario) {
                if (conquistaUsuario.getId() == definicaoConquista.getId()) {
                    usuarioJaPossui = true;
                    break;
                }
            }

            if (!usuarioJaPossui) {
                boolean criterioAtendido = false;
                // EXEMPLOS DE CRITÉRIOS (você precisará expandir isso):
                if ("Primeiros Passos".equalsIgnoreCase(definicaoConquista.getNome())) {
                    int totalCumpridos = progressoDiarioDAO.getCountProgressoCumprido(usuarioId);
                    if (totalCumpridos >= 1) criterioAtendido = true;
                } else if ("Pontuador Nato".equalsIgnoreCase(definicaoConquista.getNome())) {
                    // Usa os pontos do objeto Usuario que foi passado, que pode já incluir pontos do hábito.
                    if (usuarioParaAtualizarPontos.getPontos() >= 50) criterioAtendido = true; 
                }
                // Adicione mais 'else if' para outras conquistas

                if (criterioAtendido) {
                    System.out.println("HabitService: Critério para conquista '" + definicaoConquista.getNome() + "' ATENDIDO.");
                    if (conquistaService.darConquistaParaUsuario(usuarioId, definicaoConquista.getId())) {
                        System.out.println("HabitService: Conquista '" + definicaoConquista.getNome() + "' concedida.");
                        novasConquistasDesbloqueadasNestaRodada.add(definicaoConquista);

                        if (definicaoConquista.getPontosBonus() > 0) {
                            int pontosAntesBonusConquista = usuarioParaAtualizarPontos.getPontos();
                            int pontosAposBonusConquista = pontosAntesBonusConquista + definicaoConquista.getPontosBonus();
                            if (!usuarioDAO.updatePontosUsuario(usuarioId, pontosAposBonusConquista)) {
                                throw new PersistenceException("Falha ao aplicar bônus de " + definicaoConquista.getPontosBonus() + " pontos pela conquista '" + definicaoConquista.getNome() + "'.");
                            }
                            usuarioParaAtualizarPontos.setPontos(pontosAposBonusConquista); // Atualiza objeto em memória
                            System.out.println("HabitService: Bônus de " + definicaoConquista.getPontosBonus() + " pontos concedido. Novo total: " + pontosAposBonusConquista);
                        }
                    } else {
                        System.err.println("HabitService: Falha ao registrar a conquista '" + definicaoConquista.getNome() + "' para o usuário no DAO, mas critério foi atendido.");
                        // Decida se isso deve lançar uma PersistenceException ou ser apenas um log.
                    }
                }
            }
        }
        return novasConquistasDesbloqueadasNestaRodada;
    }


    @Override
    public int getPontuacaoUsuario(int usuarioId) throws UserNotFoundException {
        Usuario usuario = usuarioDAO.getUsuarioById(usuarioId);
        if (usuario == null) {
            throw new UserNotFoundException("Usuário com ID " + usuarioId + " não encontrado ao buscar pontuação.");
        }
        return usuario.getPontos();
    }

    @Override
    public List<Conquista> getConquistasDesbloqueadasUsuario(int usuarioId) throws UserNotFoundException {
        // Primeiro, verifica se o usuário existe para lançar UserNotFoundException se aplicável.
        if (usuarioDAO.getUsuarioById(usuarioId) == null) {
            throw new UserNotFoundException("Usuário com ID " + usuarioId + " não encontrado ao buscar conquistas desbloqueadas.");
        }
        List<Conquista> conquistas = conquistaService.getConquistasDoUsuario(usuarioId);
        return conquistas != null ? conquistas : new ArrayList<>();
    }

    @Override
    public List<Conquista> getAllConquistasPossiveis() {
        List<Conquista> definicoes = conquistaService.getAllConquistaDefinicoes();
        return definicoes != null ? definicoes : new ArrayList<>();
    }

    // O método main() de teste que você tinha aqui foi removido.
    // Recomenda-se criar testes unitários formais (ex: com JUnit) ou adaptar
    // o main em uma classe separada, agora lidando com try-catch para as exceções
    // e os novos tipos de retorno.
}