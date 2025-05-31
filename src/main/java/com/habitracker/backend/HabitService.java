package com.habitracker.backend;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections; // Mantido para o DTO, se ele ainda usar para listas vazias
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.habitracker.database.HabitDAO;
import com.habitracker.database.ProgressoDiarioDAO;
import com.habitracker.database.UsuarioDAO;
// import com.habitracker.model.Conquista; // REMOVIDO
import com.habitracker.model.Habit;
import com.habitracker.model.ProgressoDiario;
import com.habitracker.model.Usuario;
import com.habitracker.serviceapi.HabitTrackerServiceAPI;
import com.habitracker.serviceapi.dto.FeedbackMarcacaoDTO; // Este DTO precisa ser ajustado
import com.habitracker.serviceapi.exceptions.HabitNotFoundException;
import com.habitracker.serviceapi.exceptions.PersistenceException;
import com.habitracker.serviceapi.exceptions.UserNotFoundException;
import com.habitracker.serviceapi.exceptions.ValidationException;
// import com.habitracker.backend.ConquistaService; // REMOVIDO

public class HabitService implements HabitTrackerServiceAPI {

    private final HabitDAO habitDAO;
    private final UsuarioDAO usuarioDAO;
    private final ProgressoDiarioDAO progressoDiarioDAO;
    // private final ConquistaService conquistaService; // REMOVIDO

    public HabitService(HabitDAO habitDAO, UsuarioDAO usuarioDAO,
                        ProgressoDiarioDAO progressoDiarioDAO /*, ConquistaService conquistaService REMOVIDO */) {
        this.habitDAO = habitDAO;
        this.usuarioDAO = usuarioDAO;
        this.progressoDiarioDAO = progressoDiarioDAO;
        // this.conquistaService = conquistaService; // REMOVIDO
    }

    @Override
    public List<Habit> getAllHabits() throws PersistenceException {
        try {
            List<Habit> habits = habitDAO.getAllHabits();
            return habits != null ? habits : new ArrayList<>();
        } catch (Exception e) {
            throw new PersistenceException("Erro ao buscar todos os hábitos.", e);
        }
    }
    
    @Override
    public List<Habit> getHabitsByUserId(int userId) throws PersistenceException, UserNotFoundException {
        getUsuarioById(userId); // Valida usuário
        try {
            List<Habit> userHabits = habitDAO.getHabitsByUserId(userId);
            return userHabits != null ? userHabits : new ArrayList<>();
        } catch (Exception e) {
            throw new PersistenceException("Erro ao buscar hábitos para o usuário ID: " + userId, e);
        }
    }

    @Override
    public Habit addHabit(Habit habit) throws PersistenceException, ValidationException {
        if (habit == null || habit.getName() == null || habit.getName().trim().isEmpty()) {
            throw new ValidationException("Dados do hábito inválidos: nome não pode ser vazio.");
        }
        if (habit.getUsuarioId() <= 0) {
            throw new ValidationException("ID de usuário inválido para o hábito.");
        }
        try {
            Habit habitAdicionado = habitDAO.addHabit(habit);
            if (habitAdicionado == null || habitAdicionado.getId() <= 0) {
                throw new PersistenceException("Falha ao salvar o hábito ou obter ID gerado.");
            }
            return habitAdicionado;
        } catch (Exception e) {
            throw new PersistenceException("Erro de persistência ao adicionar hábito: " + e.getMessage(), e);
        }
    }

    @Override
    public Habit getHabitById(int habitId) throws HabitNotFoundException, PersistenceException {
        if (habitId <= 0) {
            throw new HabitNotFoundException("ID do hábito inválido: " + habitId + ". IDs devem ser positivos.");
        }
        try {
            Habit habit = habitDAO.getHabitById(habitId);
            if (habit == null) {
                throw new HabitNotFoundException("Hábito com ID " + habitId + " não encontrado.");
            }
            return habit;
        } catch (HabitNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenceException("Erro de persistência ao buscar hábito por ID: " + habitId, e);
        }
    }

    @Override
    public Habit updateHabit(Habit habitToUpdate) throws HabitNotFoundException, PersistenceException, ValidationException {
        if (habitToUpdate == null || habitToUpdate.getId() <= 0) {
            throw new ValidationException("Dados do hábito para atualização inválidos: ID nulo ou inválido.");
        }
        if (habitToUpdate.getName() == null || habitToUpdate.getName().trim().isEmpty()) {
            throw new ValidationException("Nome do hábito não pode ser vazio na atualização.");
        }
        if (habitToUpdate.getUsuarioId() <= 0) {
            throw new ValidationException("ID de usuário inválido no hábito a ser atualizado.");
        }
        Habit existente = getHabitById(habitToUpdate.getId());
        if (existente.getUsuarioId() != habitToUpdate.getUsuarioId()) {
            throw new ValidationException("Não é permitido alterar o proprietário do hábito.");
        }
        try {
            boolean sucessoNoDAO = habitDAO.updateHabit(habitToUpdate);
            if (sucessoNoDAO) {
                return getHabitById(habitToUpdate.getId());
            } else {
                throw new PersistenceException("Falha ao atualizar o hábito no banco (DAO retornou false), ID: " + habitToUpdate.getId());
            }
        } catch (Exception e) {
            if (e instanceof HabitNotFoundException || e instanceof ValidationException || e instanceof PersistenceException) {
                throw e;
            }
            throw new PersistenceException("Erro de persistência ao atualizar hábito: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteHabit(int habitId) throws HabitNotFoundException, PersistenceException {
        getHabitById(habitId); // Valida se existe
        try {
            // Antes de deletar o hábito, você pode querer deletar o progresso associado
            // progressoDiarioDAO.deleteProgressosByHabitoId(habitId); // Se você tiver esse método no DAO
            
            boolean sucessoNoDAO = habitDAO.deleteHabit(habitId);
            if (!sucessoNoDAO) {
                throw new PersistenceException("Falha ao excluir o hábito com ID " + habitId + " (DAO retornou false).");
            }
            return true;
        } catch (Exception e) {
            if (e instanceof HabitNotFoundException || e instanceof PersistenceException) {
                throw e;
            }
            throw new PersistenceException("Erro de persistência ao excluir hábito: " + e.getMessage(), e);
        }
    }

    @Override
    public Usuario getUsuarioById(int usuarioId) throws UserNotFoundException, PersistenceException {
        if (usuarioId <= 0) {
            throw new UserNotFoundException("ID de usuário inválido: " + usuarioId + ". IDs devem ser positivos.");
        }
        try {
            Usuario usuario = usuarioDAO.getUsuarioById(usuarioId);
            if (usuario == null) {
                throw new UserNotFoundException("Usuário com ID " + usuarioId + " não encontrado.");
            }
            return usuario;
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenceException("Erro de persistência ao buscar usuário com ID: " + usuarioId, e);
        }
    }

    @Override
    public Usuario addUsuario(String nome) throws PersistenceException, ValidationException {
        if (nome == null || nome.trim().isEmpty()) {
            throw new ValidationException("O nome do usuário não pode ser vazio.");
        }
        Usuario novoUsuario = new Usuario(nome.trim());
        try {
            Usuario usuarioCriado = usuarioDAO.addUsuario(novoUsuario);
            if (usuarioCriado == null || usuarioCriado.getId() <= 0) {
                throw new PersistenceException("Falha ao criar o usuário no banco de dados ou ID não retornado.");
            }
            return usuarioCriado;
        } catch (Exception e) {
            throw new PersistenceException("Erro de persistência ao tentar adicionar novo usuário: " + e.getMessage(), e);
        }
    }

    @Override
    public FeedbackMarcacaoDTO marcarHabitoComoFeito(int usuarioId, int habitoId, LocalDate data)
            throws UserNotFoundException, HabitNotFoundException, PersistenceException, ValidationException {
        
        Usuario usuarioParaMarcar = getUsuarioById(usuarioId);
        Habit habitParaMarcar = getHabitById(habitoId);
        
        if (habitParaMarcar.getUsuarioId() != usuarioId) {
            throw new ValidationException("Este hábito não pertence ao usuário especificado.");
        }
        if (data == null) {
            throw new ValidationException("Data para marcar hábito não pode ser nula.");
        }

        final boolean STATUS_A_MARCAR_COMO_FEITO = true;
        ProgressoDiario progressoExistente;
        try {
            progressoExistente = progressoDiarioDAO.getProgresso(usuarioId, habitoId, data);
        } catch (Exception e) {
            throw new PersistenceException("Erro ao verificar progresso existente.", e);
        }

        boolean foiUmaNovaConclusaoReal = false;
        int pontosGanhosPeloHabito = 0; // Será calculado com a sequência
        final int PONTOS_BASE_POR_HABITO = 10; // Defina o valor base aqui ou no Hábito

        if (progressoExistente != null) {
            if (progressoExistente.isStatusCumprido()) {
                // Assumindo que FeedbackMarcacaoDTO foi ajustado
                return new FeedbackMarcacaoDTO(true, "Hábito já estava marcado como feito para esta data.", 0, usuarioParaMarcar.getPontos());
            } else {
                // Se existe um registro "não cumprido", não permitir remarcar como "cumprido" diretamente.
                // O usuário precisaria de uma lógica para "desmarcar" ou "alterar status".
                // Por agora, lançamos uma exceção.
                throw new ValidationException("Hábito já registrado como 'não cumprido' para esta data. Alteração não suportada por esta ação.");
            }
        } else {
            // Novo registro de progresso
            ProgressoDiario novoProgresso = new ProgressoDiario(usuarioId, habitoId, data, STATUS_A_MARCAR_COMO_FEITO);
            ProgressoDiario progressoProcessado;
            try {
                progressoProcessado = progressoDiarioDAO.addProgresso(novoProgresso);
            } catch (Exception e) {
                throw new PersistenceException("Falha ao adicionar novo progresso.", e);
            }
            if (progressoProcessado == null || progressoProcessado.getId() <= 0) {
                throw new PersistenceException("Falha ao salvar novo progresso ou obter ID gerado.");
            }
            foiUmaNovaConclusaoReal = true;
        }

        if (foiUmaNovaConclusaoReal) {
            // Calcular sequência
            int sequenciaAtual = 1;
            LocalDate diaVerificacao = data.minusDays(1);
            while (true) {
                try {
                    ProgressoDiario progressoDiaAnterior = progressoDiarioDAO.getProgresso(usuarioId, habitoId, diaVerificacao);
                    if (progressoDiaAnterior != null && progressoDiaAnterior.isStatusCumprido()) {
                        sequenciaAtual++;
                        diaVerificacao = diaVerificacao.minusDays(1);
                    } else {
                        break; // Sequência quebrada ou início da contagem
                    }
                } catch (Exception e) {
                    // Tratar erro na busca do progresso anterior, pode ser logar e quebrar a sequência
                    System.err.println("Erro ao verificar sequência para hábito ID " + habitoId + ": " + e.getMessage());
                    break;
                }
            }
            
            pontosGanhosPeloHabito = PONTOS_BASE_POR_HABITO * sequenciaAtual;
            // Você pode adicionar um limite máximo ao multiplicador, ex: Math.min(sequenciaAtual, 7) se x7 for o máximo.
            // Ou usar uma lógica de faixas: se sequencia > 7, multiplicador = 2, etc.
            // Para "uma semana -> x7", o multiplicador é a própria sequência.

            int pontosAtuaisDoUsuario = usuarioParaMarcar.getPontos();
            int pontosUsuarioAposHabito = pontosAtuaisDoUsuario + pontosGanhosPeloHabito;
            try {
                if (!usuarioDAO.updatePontosUsuario(usuarioId, pontosUsuarioAposHabito)) {
                    throw new PersistenceException("Falha ao atualizar pontos do usuário (" + usuarioId + ").");
                }
            } catch (Exception e) {
                throw new PersistenceException("Erro de persistência ao atualizar pontos do usuário.", e);
            }
            usuarioParaMarcar.setPontos(pontosUsuarioAposHabito);
            // Lógica de conquistas removida
        }

        // Assumindo que FeedbackMarcacaoDTO foi ajustado para não ter a lista de conquistas
        return new FeedbackMarcacaoDTO(true, 
                                       foiUmaNovaConclusaoReal ? "Hábito marcado como feito! Sequência: " + (pontosGanhosPeloHabito / PONTOS_BASE_POR_HABITO) + "x" 
                                                              : "Hábito já estava marcado como feito.", 
                                       pontosGanhosPeloHabito, 
                                       usuarioParaMarcar.getPontos());
    }

    @Override
    public int getPontuacaoUsuario(int usuarioId) throws UserNotFoundException, PersistenceException {
        Usuario usuario = getUsuarioById(usuarioId);
        return usuario.getPontos();
    }

    // --- Métodos de Conquista Removidos ---
    // public List<Conquista> getConquistasDesbloqueadasUsuario(...) foi removido
    // public List<Conquista> getAllConquistasPossiveis() foi removido
    // private List<Conquista> verificarEConcederConquistasInterno(...) foi removido
    public int getSequenciaEfetivaTerminadaEm(int usuarioId, int habitoId, LocalDate dataLimite) throws PersistenceException {
        int sequencia = 0;
        LocalDate diaVerificacao = dataLimite;

        // Validação básica (opcional, dependendo de onde é chamado)
        // getUsuarioById(usuarioId); 
        // getHabitById(habitoId);

        while (true) {
            ProgressoDiario progresso;
            try {
                progresso = progressoDiarioDAO.getProgresso(usuarioId, habitoId, diaVerificacao);
            } catch (Exception e) {
                // Logar o erro ou decidir se deve ser lançado como PersistenceException
                System.err.println("Erro ao buscar progresso para cálculo de sequência (hábito ID " + habitoId + 
                                   ", data " + diaVerificacao + "): " + e.getMessage());
                // Se não puder buscar o progresso, a sequência é interrompida.
                // Pode-se lançar a PersistenceException ou apenas quebrar o loop.
                // Lançar a exceção é mais robusto para o chamador lidar.
                // throw new PersistenceException("Erro ao buscar progresso para cálculo de sequência.", e);
                break; // Para este exemplo, vamos interromper e retornar o que foi contado.
            }

            if (progresso != null && progresso.isStatusCumprido()) {
                sequencia++;
                diaVerificacao = diaVerificacao.minusDays(1);
            } else {
                // Sequência quebrada ou hábito não foi cumprido no dia de início da verificação (dataLimite)
                break;
            }
        }
        return sequencia;
    }

    @Override
    public Map<Integer, Boolean> getStatusHabitosPorDia(int usuarioId, List<Integer> habitIds, LocalDate data)
            throws PersistenceException, UserNotFoundException {
        getUsuarioById(usuarioId); // Valida usuário
        Map<Integer, Boolean> statusMap = new HashMap<>();
        if (habitIds == null || habitIds.isEmpty()) {
            return statusMap;
        }
        for (Integer habitId : habitIds) {
            try {
                ProgressoDiario progresso = progressoDiarioDAO.getProgresso(usuarioId, habitId, data);
                statusMap.put(habitId, progresso != null && progresso.isStatusCumprido());
            } catch (Exception e) {
                System.err.println("Erro ao buscar progresso para hábito ID " + habitId + " em " + data + ": " + e.getMessage());
                statusMap.put(habitId, false); 
            }
        }
        return statusMap;
    }

    @Override
    public List<ProgressoDiario> getProgressoDiarioDoMes(int usuarioId, int ano, int mes)
            throws UserNotFoundException, PersistenceException {
        getUsuarioById(usuarioId);
        try {
            return progressoDiarioDAO.getProgressosDoMes(usuarioId, ano, mes);
        } catch (Exception e) {
            throw new PersistenceException("Falha ao buscar dados de progresso do calendário.", e);
        }
    }
    
    // --- MÉTODOS PARA OBJETIVOS (ESQUELETO - VOCÊ PRECISA IMPLEMENTAR O DAO E MODEL) ---
    // Estes métodos seriam idealmente em um ObjetivoService.java, mas para simplificar
    // podem ser adicionados aqui temporariamente ou você pode chamar o ObjetivoDAO diretamente do MainFrame.

    /*
    public List<Objetivo> getObjetivosDoUsuario(int usuarioId) throws UserNotFoundException, PersistenceException {
        getUsuarioById(usuarioId); // Valida usuário
        // return objetivoDAO.getObjetivosByUserId(usuarioId);
        throw new UnsupportedOperationException("Funcionalidade de buscar objetivos não implementada.");
    }

    public Objetivo addObjetivo(Objetivo objetivo, List<Integer> idsHabitosVinculados) throws PersistenceException, ValidationException {
        // Validar objetivo
        // Objetivo objSalvo = objetivoDAO.addObjetivo(objetivo);
        // if (objSalvo != null && idsHabitosVinculados != null) {
        //     for (Integer habitoId : idsHabitosVinculados) {
        //         objetivoDAO.addHabitoObjetivoLink(habitoId, objSalvo.getId());
        //     }
        // }
        // return objSalvo;
        throw new UnsupportedOperationException("Funcionalidade de adicionar objetivo não implementada.");
    }

    public Objetivo updateObjetivo(Objetivo objetivo, List<Integer> idsHabitosVinculados) throws PersistenceException, ValidationException {
        // Validar objetivo
        // boolean sucesso = objetivoDAO.updateObjetivo(objetivo);
        // if (sucesso) {
        //     objetivoDAO.removeAllHabitoLinksForObjetivo(objetivo.getId()); // Limpa links antigos
        //     if (idsHabitosVinculados != null) {
        //         for (Integer habitoId : idsHabitosVinculados) {
        //             objetivoDAO.addHabitoObjetivoLink(habitoId, objetivo.getId());
        //         }
        //     }
        //     return objetivoDAO.getObjetivoById(objetivo.getId());
        // }
        // return null;
        throw new UnsupportedOperationException("Funcionalidade de atualizar objetivo não implementada.");
    }

    public boolean deleteObjetivo(int objetivoId) throws PersistenceException {
        // objetivoDAO.removeAllHabitoLinksForObjetivo(objetivoId);
        // return objetivoDAO.deleteObjetivo(objetivoId);
        throw new UnsupportedOperationException("Funcionalidade de deletar objetivo não implementada.");
    }

    public boolean toggleConclusaoObjetivo(int objetivoId) throws PersistenceException {
        // Objetivo obj = objetivoDAO.getObjetivoById(objetivoId);
        // if (obj != null) {
        //     obj.setConcluido(!obj.isConcluido());
        //     obj.setDataConclusao(obj.isConcluido() ? LocalDate.now() : null);
        //     return objetivoDAO.updateObjetivo(obj);
        // }
        // return false;
        throw new UnsupportedOperationException("Funcionalidade de marcar objetivo como concluído não implementada.");
    }
    */
}