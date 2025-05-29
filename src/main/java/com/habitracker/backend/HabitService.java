package com.habitracker.backend;

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
import com.habitracker.model.Usuario; // Certifique-se que esta classe tem um construtor public Usuario() {}
import com.habitracker.serviceapi.HabitTrackerServiceAPI;
import com.habitracker.serviceapi.dto.FeedbackMarcacaoDTO;
import com.habitracker.serviceapi.exceptions.HabitNotFoundException;
import com.habitracker.serviceapi.exceptions.PersistenceException;
import com.habitracker.serviceapi.exceptions.UserNotFoundException;
import com.habitracker.serviceapi.exceptions.ValidationException;

public class HabitService implements HabitTrackerServiceAPI {

    private final HabitDAO habitDAO;
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
    public List<Habit> getAllHabits() throws PersistenceException {
        try {
            List<Habit> habits = habitDAO.getAllHabits();
            return habits != null ? habits : new ArrayList<>();
        } catch (Exception e) {
            // Logue o erro e/ou relance como PersistenceException
            // System.err.println("Erro de persistência em getAllHabits: " + e.getMessage());
            throw new PersistenceException("Erro ao buscar todos os hábitos.", e);
        }
    }

    @Override
    public List<Habit> getHabitsByUserId(int userId) throws PersistenceException, UserNotFoundException {
        // Validação opcional: verificar se o usuário existe antes de buscar os hábitos
        // Se getUsuarioById já lança UserNotFoundException, isso já cobre.
        // usuarioDAO.getUsuarioById(userId); // Se este método não existir ou não lançar, adicione a lógica.
        // Por enquanto, vamos confiar que o MainFrame passa um userId válido.

        try {
            // Primeiro, podemos verificar se o usuário existe para lançar uma UserNotFoundException clara se necessário.
            // (getUsuarioById já faz isso e lança a exceção se não encontrar)
            getUsuarioById(userId); // Isso garante que o usuário existe ou lança UserNotFoundException

            List<Habit> userHabits = habitDAO.getHabitsByUserId(userId);
            return userHabits != null ? userHabits : new ArrayList<>();
        } catch (UserNotFoundException e) {
            throw e; // Relança a exceção vinda do getUsuarioById
        } catch (Exception e) {
            // Log o erro e encapsula como PersistenceException
            // logger.error("Erro de persistência ao buscar hábitos para o usuário ID: " + userId, e);
            throw new PersistenceException("Erro ao buscar hábitos para o usuário ID: " + userId, e);
        }
    }
    
    @Override
    public Habit addHabit(Habit habit) throws PersistenceException, ValidationException {
        if (habit == null || habit.getName() == null || habit.getName().trim().isEmpty()) {
            throw new ValidationException("Dados do hábito inválidos: nome não pode ser vazio.");
        }
        try {
            Habit habitAdicionado = habitDAO.addHabit(habit);
            if (habitAdicionado == null || habitAdicionado.getId() <= 0) {
                throw new PersistenceException("Falha ao salvar o hábito ou obter ID gerado.");
            }
            System.out.println("HabitService: Hábito adicionado com ID: " + habitAdicionado.getId());
            return habitAdicionado;
        } catch (Exception e) {
            throw new PersistenceException("Erro de persistência ao adicionar hábito: " + e.getMessage(), e);
        }
    }

    @Override
    public Habit getHabitById(int habitId) throws HabitNotFoundException, PersistenceException {
        if (habitId <= 0) {
            // Esta exceção é mais apropriada aqui, pois o ID é inválido antes mesmo de ir ao DAO
            throw new HabitNotFoundException("ID do hábito inválido: " + habitId + ". IDs devem ser positivos.");
        }
        try {
            Habit habit = habitDAO.getHabitById(habitId);
            if (habit == null) {
                throw new HabitNotFoundException("Hábito com ID " + habitId + " não encontrado.");
            }
            System.out.println("HabitService: Hábito encontrado: " + habit.getName());
            return habit;
        } catch (HabitNotFoundException e) {
            throw e; // Relança a exceção específica
        } catch (Exception e) { // Captura outras exceções do DAO (ex: SQLException)
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

        getHabitById(habitToUpdate.getId()); // Valida se existe

        try {
            boolean sucessoNoDAO = habitDAO.updateHabit(habitToUpdate);
            if (sucessoNoDAO) {
                System.out.println("HabitService: Hábito com ID " + habitToUpdate.getId() + " atualizado.");
                Habit habitAtualizado = habitDAO.getHabitById(habitToUpdate.getId());
                if (habitAtualizado == null) {
                    throw new PersistenceException("Falha crítica: Hábito não encontrado após atualização, ID: " + habitToUpdate.getId());
                }
                return habitAtualizado;
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
        if (habitId <= 0) {
            throw new HabitNotFoundException("ID do hábito inválido para exclusão: " + habitId);
        }
        getHabitById(habitId); // Valida se existe

        try {
            boolean sucessoNoDAO = habitDAO.deleteHabit(habitId);
            if (!sucessoNoDAO) {
                throw new PersistenceException("Falha ao excluir o hábito com ID " + habitId + " (DAO retornou false).");
            }
            System.out.println("HabitService: Hábito com ID " + habitId + " excluído.");
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
            System.out.println("HabitService: Usuário encontrado: " + usuario.getNome() + " (ID: " + usuario.getId() + ")");
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
    if (nome.trim().length() > 100) {
        throw new ValidationException("O nome do usuário é muito longo (máximo 100 caracteres).");
    }

    // CORREÇÃO: Usar o construtor Usuario(String nome) existente
    Usuario novoUsuario = new Usuario(nome.trim());
    // O construtor Usuario(String nome) já define os pontos como 0.

    try {
        Usuario usuarioCriado = usuarioDAO.addUsuario(novoUsuario); // Assumindo que seu DAO lida com isso
        if (usuarioCriado == null || usuarioCriado.getId() <= 0) {
            // Esta verificação é importante se o DAO puder retornar null
            // ou se o ID não for preenchido corretamente após a inserção.
            throw new PersistenceException("Falha ao criar o usuário no banco de dados ou ID não retornado.");
        }
        System.out.println("HabitService: Usuário '" + usuarioCriado.getNome() + "' criado com ID: " + usuarioCriado.getId());
        return usuarioCriado;
    } catch (Exception e) { // Captura qualquer exceção do DAO (ex: SQLException, ConstraintViolationException)
        // Logue o erro original se tiver um sistema de log
        // logger.error("Erro de persistência ao tentar adicionar novo usuário: " + nome, e);
        throw new PersistenceException("Erro de persistência ao tentar adicionar novo usuário: " + e.getMessage(), e);
    }
}

    @Override
    public FeedbackMarcacaoDTO marcarHabitoComoFeito(int usuarioId, int habitoId, LocalDate data)
            throws UserNotFoundException, HabitNotFoundException, PersistenceException, ValidationException {
        
        Usuario usuarioParaMarcar = getUsuarioById(usuarioId);
        Habit habitParaMarcar = getHabitById(habitoId);
        
        if (data == null) {
            throw new ValidationException("Data para marcar hábito não pode ser nula.");
        }

        final boolean STATUS_A_MARCAR_COMO_FEITO = true;
        ProgressoDiario progressoExistente = null;
        try {
            progressoExistente = progressoDiarioDAO.getProgresso(usuarioId, habitoId, data);
        } catch (Exception e) {
            throw new PersistenceException("Erro ao verificar progresso existente.", e);
        }

        boolean foiUmaNovaConclusaoReal = false;
        int pontosGanhosPeloHabito = 10; // Placeholder: Idealmente viria de habitParaMarcar.getPontosRecompensa();

        if (progressoExistente != null) {
            if (progressoExistente.isStatusCumprido() == STATUS_A_MARCAR_COMO_FEITO) {
                System.out.println("HabitService: Progresso (ID: " + progressoExistente.getId() + ") já existe como 'cumprido'.");
                 return new FeedbackMarcacaoDTO(
                    true,
                    "Hábito já estava marcado como feito para esta data.",
                    0,
                    usuarioParaMarcar.getPontos(),
                    Collections.emptyList()
                );
            } else {
                System.err.println("HabitService: Progresso (ID: " + progressoExistente.getId() + ") já existe com status 'não cumprido'. Alteração não suportada.");
                throw new ValidationException("Hábito já registrado como 'não cumprido' para esta data. Alteração não suportada.");
            }
        } else {
            ProgressoDiario novoProgresso = new ProgressoDiario(usuarioId, habitoId, data, STATUS_A_MARCAR_COMO_FEITO);
            ProgressoDiario progressoProcessado = null;
            try {
                progressoProcessado = progressoDiarioDAO.addProgresso(novoProgresso);
            } catch (Exception e) {
                throw new PersistenceException("Falha ao adicionar novo progresso.", e);
            }

            if (progressoProcessado == null || progressoProcessado.getId() <= 0) {
                throw new PersistenceException("Falha ao salvar novo progresso ou obter ID gerado.");
            }
            System.out.println("HabitService: Novo progresso adicionado (ID: " + progressoProcessado.getId() + ").");
            foiUmaNovaConclusaoReal = true;
        }

        List<Conquista> novasConquistasNestaRodada = Collections.emptyList();
        int pontosAtuaisDoUsuario = usuarioParaMarcar.getPontos();

        if (foiUmaNovaConclusaoReal) {
            int pontosUsuarioAposHabito = pontosAtuaisDoUsuario + pontosGanhosPeloHabito;
            
            try {
                if (!usuarioDAO.updatePontosUsuario(usuarioId, pontosUsuarioAposHabito)) {
                    throw new PersistenceException("Falha ao atualizar pontos do usuário (" + usuarioId + ").");
                }
            } catch (Exception e) {
                 throw new PersistenceException("Erro de persistência ao atualizar pontos do usuário.", e);
            }
            usuarioParaMarcar.setPontos(pontosUsuarioAposHabito);
            System.out.println("HabitService: Usuário ID " + usuarioId + " ganhou " + pontosGanhosPeloHabito + " pontos. Total: " + pontosUsuarioAposHabito);
            
            novasConquistasNestaRodada = verificarEConcederConquistasInterno(usuarioId, usuarioParaMarcar);
        }

        return new FeedbackMarcacaoDTO(
            true,
            foiUmaNovaConclusaoReal ? "Hábito marcado como feito com sucesso!" : "Hábito já estava marcado como feito.",
            pontosGanhosPeloHabito,
            usuarioParaMarcar.getPontos(),
            novasConquistasNestaRodada
        );
    }

    private List<Conquista> verificarEConcederConquistasInterno(int usuarioId, Usuario usuarioComPontosAtualizados) throws UserNotFoundException, PersistenceException {
        System.out.println("HabitService: Verificando conquistas para usuário ID: " + usuarioId + " com " + usuarioComPontosAtualizados.getPontos() + " pontos.");

        List<Conquista> todasAsDefinicoes = getAllConquistasPossiveis();
        List<Conquista> conquistasAtuaisDoUsuario = getConquistasDesbloqueadasUsuario(usuarioId);
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
                // EXEMPLOS DE CRITÉRIOS (você precisará expandir e ajustar à sua classe Conquista):
                if ("Primeiros Passos".equalsIgnoreCase(definicaoConquista.getNome())) {
                    // Este critério pode ser, por exemplo, se o usuário tem > 0 pontos
                    // ou se completou pelo menos 1 hábito.
                    // int totalCumpridos = progressoDiarioDAO.getCountProgressoCumprido(usuarioId);
                    // if (totalCumpridos >= 1) criterioAtendido = true;
                    if (usuarioComPontosAtualizados.getPontos() > 0) { // Exemplo simplificado
                        criterioAtendido = true;
                    }
                }
                // CORREÇÃO 1: A lógica de "Pontuador Nato" foi comentada pois getCondicaoPontos() não existe.
                // Adapte esta seção com os atributos reais da sua classe Conquista.
                /*
                else if ("Pontuador Nato".equalsIgnoreCase(definicaoConquista.getNome())) {
                    // Exemplo: Suponha que a definição da conquista tenha um campo para a pontuação necessária.
                    // if (usuarioComPontosAtualizados.getPontos() >= definicaoConquista.getPontosNecessariosParaEstaConquista()) {
                    // criterioAtendido = true;
                    // }
                    // Como não temos getCondicaoPontos(), vamos usar um valor fixo para demonstração:
                    if (usuarioComPontosAtualizados.getPontos() >= 50) { // Valor de exemplo
                         criterioAtendido = true;
                    }
                }
                */
                // Adicione mais 'else if' para outras conquistas

                if (criterioAtendido) {
                    System.out.println("HabitService: Critério para conquista '" + definicaoConquista.getNome() + "' ATENDIDO.");
                    try {
                        if (conquistaService.darConquistaParaUsuario(usuarioId, definicaoConquista.getId())) {
                            System.out.println("HabitService: Conquista '" + definicaoConquista.getNome() + "' concedida.");
                            novasConquistasDesbloqueadasNestaRodada.add(definicaoConquista);

                            if (definicaoConquista.getPontosBonus() > 0) {
                                int pontosAntesBonusConquista = usuarioComPontosAtualizados.getPontos();
                                int pontosAposBonusConquista = pontosAntesBonusConquista + definicaoConquista.getPontosBonus();
                                if (!usuarioDAO.updatePontosUsuario(usuarioId, pontosAposBonusConquista)) {
                                    throw new PersistenceException("Falha ao aplicar bônus da conquista '" + definicaoConquista.getNome() + "'.");
                                }
                                usuarioComPontosAtualizados.setPontos(pontosAposBonusConquista);
                                System.out.println("HabitService: Bônus de " + definicaoConquista.getPontosBonus() + " pontos concedido. Total: " + pontosAposBonusConquista);
                            }
                        } else {
                            System.err.println("HabitService: Falha ao registrar '" + definicaoConquista.getNome() + "' no DAO.");
                        }
                    } catch (Exception e) {
                        throw new PersistenceException("Erro de persistência ao conceder conquista: " + e.getMessage(), e);
                    }
                }
            }
        }
        return novasConquistasDesbloqueadasNestaRodada;
    }

    @Override
    public int getPontuacaoUsuario(int usuarioId) throws UserNotFoundException, PersistenceException {
        Usuario usuario = getUsuarioById(usuarioId);
        return usuario.getPontos();
    }

    @Override
    public List<Conquista> getConquistasDesbloqueadasUsuario(int usuarioId) throws UserNotFoundException, PersistenceException {
        getUsuarioById(usuarioId); // Valida se o usuário existe
        try {
            List<Conquista> conquistas = conquistaService.getConquistasDoUsuario(usuarioId);
            return conquistas != null ? conquistas : new ArrayList<>();
        } catch (Exception e) {
            throw new PersistenceException("Erro de persistência ao buscar conquistas do usuário.", e);
        }
    }

    @Override
    public List<Conquista> getAllConquistasPossiveis() throws PersistenceException {
        try {
            List<Conquista> definicoes = conquistaService.getAllConquistaDefinicoes();
            return definicoes != null ? definicoes : new ArrayList<>();
        } catch (Exception e) {
            throw new PersistenceException("Erro de persistência ao buscar todas as definições de conquistas.", e);
        }
    }
}