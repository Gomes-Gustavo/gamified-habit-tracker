package com.habitracker.backend;

import com.habitracker.database.HabitDAO;
import com.habitracker.database.ProgressoDiarioDAO;
import com.habitracker.database.UsuarioDAO;
import com.habitracker.model.Habit;
import com.habitracker.model.ProgressoDiario;
import com.habitracker.model.Usuario;
import com.habitracker.model.Conquista;
import com.habitracker.serviceapi.dto.FeedbackMarcacaoDTO;
import com.habitracker.serviceapi.exceptions.HabitNotFoundException;
import com.habitracker.serviceapi.exceptions.PersistenceException;
import com.habitracker.serviceapi.exceptions.UserNotFoundException;
import com.habitracker.serviceapi.exceptions.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class HabitServiceTest {

    @Mock
    private HabitDAO mockHabitDAO;

    @Mock
    private UsuarioDAO mockUsuarioDAO;

    @Mock
    private ProgressoDiarioDAO mockProgressoDiarioDAO;

    @Mock
    private ConquistaService mockConquistaService;

    @InjectMocks
    private HabitService habitService;

    private static final int DUMMY_USUARIO_ID = 1; // ID de usuário para testes

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- Testes para addHabit ---
    @Test
    void addHabit_quandoHabitoValido_deveRetornarHabitoComId() throws PersistenceException, ValidationException {
        // 1. Arrange
        Habit habitSemId = new Habit("Meditar", "15 minutos", LocalDate.now(), DUMMY_USUARIO_ID);
        Habit habitComIdRetornadoPeloDAO = new Habit(1, "Meditar", "15 minutos", LocalDate.now(), DUMMY_USUARIO_ID);

        when(mockHabitDAO.addHabit(any(Habit.class))).thenReturn(habitComIdRetornadoPeloDAO);

        // 2. Act
        Habit resultadoDoService = habitService.addHabit(habitSemId);

        // 3. Assert
        assertNotNull(resultadoDoService, "O hábito retornado pelo serviço não deve ser nulo.");
        assertEquals(1, resultadoDoService.getId(), "O ID do hábito retornado deve ser 1.");
        assertEquals("Meditar", resultadoDoService.getName(), "O nome do hábito não confere.");
        assertEquals(DUMMY_USUARIO_ID, resultadoDoService.getUsuarioId(), "O usuarioId do hábito não confere.");
        verify(mockHabitDAO, times(1)).addHabit(eq(habitSemId));
    }

    @Test
    void addHabit_quandoNomeDoHabitoNulo_deveLancarValidationException() {
        Habit habitComNomeNulo = new Habit(null, "Descrição", LocalDate.now(), DUMMY_USUARIO_ID);

        ValidationException e = assertThrows(ValidationException.class, () -> {
            habitService.addHabit(habitComNomeNulo);
        });
        assertEquals("Dados do hábito inválidos: nome não pode ser vazio.", e.getMessage());
        verify(mockHabitDAO, never()).addHabit(any(Habit.class));
    }

    @Test
    void addHabit_quandoDAOFalhaAoSalvar_deveLancarPersistenceException() {
        Habit habitValido = new Habit("Exercício", "30 minutos", LocalDate.now(), DUMMY_USUARIO_ID);
        when(mockHabitDAO.addHabit(any(Habit.class))).thenReturn(null); // DAO retorna null simulando falha

        PersistenceException exception = assertThrows(PersistenceException.class, () -> {
            habitService.addHabit(habitValido);
        });
        // A mensagem no HabitService para este caso é "Falha ao salvar o hábito ou obter ID gerado."
        assertEquals("Falha ao salvar o hábito ou obter ID gerado.", exception.getMessage());
        verify(mockHabitDAO).addHabit(eq(habitValido));
    }

    // --- Testes para getHabitById ---
    @Test
    void getHabitById_quandoIdValidoEHabitoExiste_deveRetornarHabitCorreto() throws HabitNotFoundException, PersistenceException {
        int idExistente = 5;
        Habit habitEsperado = new Habit(idExistente, "Beber Água", "2L por dia", LocalDate.now().minusDays(3), DUMMY_USUARIO_ID);
        when(mockHabitDAO.getHabitById(idExistente)).thenReturn(habitEsperado);

        Habit resultado = habitService.getHabitById(idExistente);

        assertNotNull(resultado);
        assertEquals(idExistente, resultado.getId());
        assertEquals(habitEsperado.getName(), resultado.getName());
        assertEquals(DUMMY_USUARIO_ID, resultado.getUsuarioId());
        verify(mockHabitDAO).getHabitById(idExistente);
    }

    @Test
    void getHabitById_quandoIdInvalido_deveLancarHabitNotFoundException() {
        int idInvalido = 0;
        HabitNotFoundException exception = assertThrows(HabitNotFoundException.class, () -> {
            habitService.getHabitById(idInvalido);
        });
        assertTrue(exception.getMessage().contains("ID do hábito inválido: " + idInvalido));
        verify(mockHabitDAO, never()).getHabitById(anyInt());
    }

    @Test
    void getHabitById_quandoDAOLancaExcecao_deveLancarPersistenceException() {
        int habitId = 1;
        when(mockHabitDAO.getHabitById(habitId)).thenThrow(new RuntimeException("Erro no DAO")); // Simula SQLException ou outra

        PersistenceException exception = assertThrows(PersistenceException.class, () -> {
            habitService.getHabitById(habitId);
        });
        assertTrue(exception.getMessage().contains("Erro de persistência ao buscar hábito por ID: " + habitId));
        verify(mockHabitDAO).getHabitById(habitId);
    }


    // --- Testes para updateHabit ---
    @Test
    void updateHabit_quandoHabitoValidoEExistente_deveRetornarHabitoAtualizado() throws Exception {
        int habitIdExistente = 1;
        Habit habitOriginalNoBanco = new Habit(habitIdExistente, "Nome Antigo", "Desc Antiga", LocalDate.now().minusDays(1), DUMMY_USUARIO_ID);
        Habit habitComNovosDados = new Habit(habitIdExistente, "Nome Atualizado", "Desc Atualizada", LocalDate.now().minusDays(1), DUMMY_USUARIO_ID);

        // Configura o mock para as duas chamadas de getHabitById
        when(mockHabitDAO.getHabitById(habitIdExistente))
                .thenReturn(habitOriginalNoBanco)  // Para a primeira chamada (verificação de existência)
                .thenReturn(habitComNovosDados);   // Para a segunda chamada (re-busca após update)

        when(mockHabitDAO.updateHabit(any(Habit.class))).thenReturn(true); // Update no DAO bem-sucedido

        Habit resultado = habitService.updateHabit(habitComNovosDados);

        assertNotNull(resultado);
        assertEquals(habitIdExistente, resultado.getId());
        assertEquals("Nome Atualizado", resultado.getName());
        assertEquals("Desc Atualizada", resultado.getDescription());
        assertEquals(DUMMY_USUARIO_ID, resultado.getUsuarioId());

        verify(mockHabitDAO, times(2)).getHabitById(habitIdExistente);
        verify(mockHabitDAO).updateHabit(eq(habitComNovosDados));
    }

    @Test
    void updateHabit_quandoHabitoNaoExiste_deveLancarHabitNotFoundException() {
        int idNaoExistente = 99;
        Habit habitParaAtualizar = new Habit(idNaoExistente, "Nome Qualquer", "Desc Qualquer", LocalDate.now(), DUMMY_USUARIO_ID);
        when(mockHabitDAO.getHabitById(idNaoExistente)).thenReturn(null); // Simula que não encontrou

        HabitNotFoundException exception = assertThrows(HabitNotFoundException.class, () -> {
            habitService.updateHabit(habitParaAtualizar);
        });
        assertEquals("Hábito com ID " + idNaoExistente + " não encontrado.", exception.getMessage());
        verify(mockHabitDAO, never()).updateHabit(any(Habit.class));
    }

    @Test
    void updateHabit_quandoDAOFalhaNoUpdate_deveLancarPersistenceException() throws HabitNotFoundException, PersistenceException {
        int habitIdExistente = 1;
        Habit habitOriginalNoBanco = new Habit(habitIdExistente, "Nome Antigo", "Desc Antiga", LocalDate.now(), DUMMY_USUARIO_ID);
        Habit habitComNovosDados = new Habit(habitIdExistente, "Nome Atualizado", "Desc Atualizada", LocalDate.now(), DUMMY_USUARIO_ID);

        when(mockHabitDAO.getHabitById(habitIdExistente)).thenReturn(habitOriginalNoBanco);
        when(mockHabitDAO.updateHabit(any(Habit.class))).thenReturn(false); // DAO falha no update

        PersistenceException exception = assertThrows(PersistenceException.class, () -> {
            habitService.updateHabit(habitComNovosDados);
        });
        assertEquals("Falha ao atualizar o hábito no banco (DAO retornou false), ID: " + habitIdExistente, exception.getMessage());
        verify(mockHabitDAO).updateHabit(eq(habitComNovosDados));
    }

    // --- Testes para deleteHabit ---
    @Test
    void deleteHabit_quandoHabitoExiste_deveRetornarTrue() throws Exception {
        int idExistente = 1;
        Habit habitExistente = new Habit(idExistente, "Hábito a Deletar", "...", LocalDate.now(), DUMMY_USUARIO_ID);
        when(mockHabitDAO.getHabitById(idExistente)).thenReturn(habitExistente);
        when(mockHabitDAO.deleteHabit(idExistente)).thenReturn(true);

        boolean resultado = habitService.deleteHabit(idExistente);

        assertTrue(resultado);
        verify(mockHabitDAO).getHabitById(idExistente);
        verify(mockHabitDAO).deleteHabit(idExistente);
    }

    @Test
    void deleteHabit_quandoHabitoNaoExiste_deveLancarHabitNotFoundException() {
        int idNaoExistente = 99;
        when(mockHabitDAO.getHabitById(idNaoExistente)).thenReturn(null);

        HabitNotFoundException exception = assertThrows(HabitNotFoundException.class, () -> {
            habitService.deleteHabit(idNaoExistente);
        });
        assertEquals("Hábito com ID " + idNaoExistente + " não encontrado.", exception.getMessage());
        verify(mockHabitDAO).getHabitById(idNaoExistente);
        verify(mockHabitDAO, never()).deleteHabit(anyInt());
    }

    // --- Testes para marcarHabitoComoFeito ---
    @Test
    void marcarHabitoComoFeito_novoProgressoSemNovasConquistas_deveRetornarFeedbackCorreto() throws Exception {
        int usuarioId = 1;
        int habitoId = 1;
        LocalDate hoje = LocalDate.now();
        int pontosIniciaisUsuario = 50;
        int pontosPeloHabito = 10; // Definido no HabitService

        Usuario usuarioMock = new Usuario(usuarioId, "Usuario Teste", pontosIniciaisUsuario);
        Habit habitMock = new Habit(habitoId, "Hábito de Teste", "Descrição", hoje.minusDays(1), usuarioId);
        ProgressoDiario progressoSalvoPeloDAO = new ProgressoDiario(100, usuarioId, habitoId, hoje, true);

        when(mockUsuarioDAO.getUsuarioById(usuarioId)).thenReturn(usuarioMock);
        when(mockHabitDAO.getHabitById(habitoId)).thenReturn(habitMock);
        when(mockProgressoDiarioDAO.getProgresso(usuarioId, habitoId, hoje)).thenReturn(null);
        when(mockProgressoDiarioDAO.addProgresso(any(ProgressoDiario.class))).thenReturn(progressoSalvoPeloDAO);
        when(mockUsuarioDAO.updatePontosUsuario(eq(usuarioId), eq(pontosIniciaisUsuario + pontosPeloHabito))).thenReturn(true);

        // Simular que não há novas conquistas (getAllConquistasPossiveis retorna vazio)
        when(mockConquistaService.getAllConquistaDefinicoes()).thenReturn(Collections.emptyList());
        // Também é preciso mockar getConquistasDesbloqueadasUsuario, pois é chamado dentro de verificarEConcederConquistasInterno
        when(mockConquistaService.getConquistasDoUsuario(usuarioId)).thenReturn(Collections.emptyList());


        FeedbackMarcacaoDTO feedback = habitService.marcarHabitoComoFeito(usuarioId, habitoId, hoje);

        assertTrue(feedback.isSucesso());
        assertEquals(pontosPeloHabito, feedback.getPontosGanhosNestaMarcacao());
        assertEquals(pontosIniciaisUsuario + pontosPeloHabito, feedback.getTotalPontosUsuarioAposMarcacao());
        assertTrue(feedback.getNovasConquistasDesbloqueadas().isEmpty());
        assertEquals("Hábito marcado como feito com sucesso!", feedback.getMensagem());

        verify(mockProgressoDiarioDAO).addProgresso(any(ProgressoDiario.class));
        verify(mockUsuarioDAO).updatePontosUsuario(usuarioId, pontosIniciaisUsuario + pontosPeloHabito);
    }

    @Test
    void marcarHabitoComoFeito_progressoJaExistiaECumprido_naoDeveDarNovosPontos() throws Exception {
        int usuarioId = 1;
        int habitoId = 1;
        LocalDate hoje = LocalDate.now();
        int pontosIniciaisUsuario = 20;

        Usuario usuarioMock = new Usuario(usuarioId, "Usuario Teste", pontosIniciaisUsuario);
        Habit habitMock = new Habit(habitoId, "Hábito Teste", "Desc", hoje.minusDays(1), usuarioId);
        ProgressoDiario progressoExistenteMock = new ProgressoDiario(100, usuarioId, habitoId, hoje, true);

        when(mockUsuarioDAO.getUsuarioById(usuarioId)).thenReturn(usuarioMock);
        when(mockHabitDAO.getHabitById(habitoId)).thenReturn(habitMock);
        when(mockProgressoDiarioDAO.getProgresso(usuarioId, habitoId, hoje)).thenReturn(progressoExistenteMock);

        FeedbackMarcacaoDTO feedback = habitService.marcarHabitoComoFeito(usuarioId, habitoId, hoje);

        assertTrue(feedback.isSucesso());
        assertEquals(0, feedback.getPontosGanhosNestaMarcacao());
        assertEquals(pontosIniciaisUsuario, feedback.getTotalPontosUsuarioAposMarcacao());
        assertTrue(feedback.getNovasConquistasDesbloqueadas().isEmpty());
        assertEquals("Hábito já estava marcado como feito para esta data.", feedback.getMensagem());

        verify(mockProgressoDiarioDAO, never()).addProgresso(any(ProgressoDiario.class));
        verify(mockUsuarioDAO, never()).updatePontosUsuario(anyInt(), anyInt());
    }

    @Test
    void marcarHabitoComoFeito_progressoJaExistiaNaoCumprido_deveLancarValidationException() throws Exception {
        int usuarioId = 1;
        int habitoId = 1;
        LocalDate hoje = LocalDate.now();

        Usuario usuarioMock = new Usuario(usuarioId, "Usuario Teste", 0);
        Habit habitMock = new Habit(habitoId, "Hábito Teste", "Desc", hoje.minusDays(1), usuarioId);
        ProgressoDiario progressoExistenteMock = new ProgressoDiario(101, usuarioId, habitoId, hoje, false); // NÃO cumprido

        when(mockUsuarioDAO.getUsuarioById(usuarioId)).thenReturn(usuarioMock);
        when(mockHabitDAO.getHabitById(habitoId)).thenReturn(habitMock);
        when(mockProgressoDiarioDAO.getProgresso(usuarioId, habitoId, hoje)).thenReturn(progressoExistenteMock);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            habitService.marcarHabitoComoFeito(usuarioId, habitoId, hoje);
        });

        assertEquals("Hábito já registrado como 'não cumprido' para esta data. Alteração não suportada.", exception.getMessage());
        verify(mockProgressoDiarioDAO, never()).addProgresso(any(ProgressoDiario.class));
        verify(mockUsuarioDAO, never()).updatePontosUsuario(anyInt(), anyInt());
    }

    @Test
    void marcarHabitoComoFeito_quandoUsuarioNaoEncontrado_deveLancarUserNotFoundException() {
        int usuarioIdInexistente = 99;
        int habitoId = 1;
        LocalDate hoje = LocalDate.now();

        // O método getUsuarioById do serviço lança a exceção antes de chamar o DAO
        // Então, precisamos mockar o comportamento do método do serviço se o DAO falhar,
        // ou, mais corretamente, mockar o DAO para retornar null.
        // O serviço chama getUsuarioById(usuarioId), que internamente chama mockUsuarioDAO.getUsuarioById()
        when(mockUsuarioDAO.getUsuarioById(usuarioIdInexistente)).thenReturn(null);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            habitService.marcarHabitoComoFeito(usuarioIdInexistente, habitoId, hoje);
        });
        assertEquals("Usuário com ID " + usuarioIdInexistente + " não encontrado.", exception.getMessage());
    }

    @Test
    void marcarHabitoComoFeito_quandoHabitoNaoEncontrado_deveLancarHabitNotFoundException() {
        int usuarioId = 1;
        int habitoIdInexistente = 99;
        LocalDate hoje = LocalDate.now();

        Usuario usuarioMock = new Usuario(usuarioId, "Usuario Teste", 0);
        when(mockUsuarioDAO.getUsuarioById(usuarioId)).thenReturn(usuarioMock);
        when(mockHabitDAO.getHabitById(habitoIdInexistente)).thenReturn(null);

        HabitNotFoundException exception = assertThrows(HabitNotFoundException.class, () -> {
            habitService.marcarHabitoComoFeito(usuarioId, habitoIdInexistente, hoje);
        });
        assertEquals("Hábito com ID " + habitoIdInexistente + " não encontrado.", exception.getMessage());
    }

    @Test
    void marcarHabitoComoFeito_quandoDataNula_deveLancarValidationException() throws UserNotFoundException, HabitNotFoundException, PersistenceException {
        int usuarioId = 1;
        int habitoId = 1;

        Usuario usuarioMock = new Usuario(usuarioId, "Usuario Teste", 0);
        Habit habitMock = new Habit(habitoId, "Hábito Teste", "Desc", LocalDate.now(), usuarioId);

        when(mockUsuarioDAO.getUsuarioById(usuarioId)).thenReturn(usuarioMock);
        when(mockHabitDAO.getHabitById(habitoId)).thenReturn(habitMock);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            habitService.marcarHabitoComoFeito(usuarioId, habitoId, null);
        });
        assertEquals("Data para marcar hábito não pode ser nula.", exception.getMessage());
    }

    // --- Testes para getPontuacaoUsuario ---
    @Test
    void getPontuacaoUsuario_quandoUsuarioExiste_deveRetornarPontosCorretos() throws UserNotFoundException, PersistenceException {
        int usuarioId = 1;
        int pontosEsperados = 150;
        Usuario usuarioMock = new Usuario(usuarioId, "Usuario Pontuador", pontosEsperados);
        when(mockUsuarioDAO.getUsuarioById(usuarioId)).thenReturn(usuarioMock);

        int pontosResultado = habitService.getPontuacaoUsuario(usuarioId);

        assertEquals(pontosEsperados, pontosResultado);
        verify(mockUsuarioDAO).getUsuarioById(usuarioId);
    }

    @Test
    void getPontuacaoUsuario_quandoUsuarioNaoExiste_deveLancarUserNotFoundException() {
        int usuarioIdInexistente = 99;
        when(mockUsuarioDAO.getUsuarioById(usuarioIdInexistente)).thenReturn(null);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            habitService.getPontuacaoUsuario(usuarioIdInexistente);
        });
        // A mensagem vem do getUsuarioById interno do serviço
        assertEquals("Usuário com ID " + usuarioIdInexistente + " não encontrado.", exception.getMessage());
    }

    // --- Testes para getConquistasDesbloqueadasUsuario ---
    @Test
    void getConquistasDesbloqueadasUsuario_quandoUsuarioExisteComConquistas_deveRetornarLista() throws UserNotFoundException, PersistenceException {
        int usuarioId = 1;
        Usuario usuarioMock = new Usuario(usuarioId, "Usuario Conquistador", 100);
        List<Conquista> conquistasEsperadas = new ArrayList<>();
        // Supondo que Conquista tenha um construtor (id, nome, desc, criterio, pontosBonus)
        conquistasEsperadas.add(new Conquista(1, "Conquista Alfa", "Desc Alfa", "Crit Alfa", 10));

        when(mockUsuarioDAO.getUsuarioById(usuarioId)).thenReturn(usuarioMock);
        when(mockConquistaService.getConquistasDoUsuario(usuarioId)).thenReturn(conquistasEsperadas);

        List<Conquista> resultado = habitService.getConquistasDesbloqueadasUsuario(usuarioId);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Conquista Alfa", resultado.get(0).getNome());
        verify(mockConquistaService).getConquistasDoUsuario(usuarioId);
    }

    @Test
    void getConquistasDesbloqueadasUsuario_quandoUsuarioNaoExiste_deveLancarUserNotFoundException() {
        int usuarioIdInexistente = 99;
        when(mockUsuarioDAO.getUsuarioById(usuarioIdInexistente)).thenReturn(null);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            habitService.getConquistasDesbloqueadasUsuario(usuarioIdInexistente);
        });
        assertEquals("Usuário com ID " + usuarioIdInexistente + " não encontrado.", exception.getMessage());
        verify(mockConquistaService, never()).getConquistasDoUsuario(anyInt());
    }

    // --- Teste para getAllConquistasPossiveis ---
    @Test
    void getAllConquistasPossiveis_quandoExistemDefinicoes_deveRetornarLista() throws PersistenceException {
        List<Conquista> definicoesEsperadas = new ArrayList<>();
        definicoesEsperadas.add(new Conquista(10, "Mestre", "Desc", "Crit", 1000));
        when(mockConquistaService.getAllConquistaDefinicoes()).thenReturn(definicoesEsperadas);

        List<Conquista> resultado = habitService.getAllConquistasPossiveis();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(mockConquistaService).getAllConquistaDefinicoes();
    }

    @Test
    void getAllConquistasPossiveis_quandoNaoExistemDefinicoes_deveRetornarListaVazia() throws PersistenceException {
        when(mockConquistaService.getAllConquistaDefinicoes()).thenReturn(Collections.emptyList());
        List<Conquista> resultado = habitService.getAllConquistasPossiveis();
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void marcarHabitoComoFeito_novoProgressoComDesbloqueioConquistaEBonus_deveRetornarFeedbackCompleto() throws Exception {
        int usuarioId = 1;
        int habitoId = 1;
        LocalDate hoje = LocalDate.now();
        int pontosIniciaisUsuario = 0;
        int pontosPeloHabito = 10;

        Usuario usuarioMock = new Usuario(usuarioId, "Conquistador", pontosIniciaisUsuario);
        Habit habitMock = new Habit(habitoId, "Hábito Conquista", "Desc", hoje.minusDays(1), usuarioId);
        ProgressoDiario progressoSalvoPeloDAO = new ProgressoDiario(200, usuarioId, habitoId, hoje, true);
        Conquista definicaoPrimeirosPassos = new Conquista(1, "Primeiros Passos", "Completou um hábito", "Completar 1 hábito", 10);

        when(mockUsuarioDAO.getUsuarioById(usuarioId)).thenReturn(usuarioMock);
        when(mockHabitDAO.getHabitById(habitoId)).thenReturn(habitMock);
        when(mockProgressoDiarioDAO.getProgresso(usuarioId, habitoId, hoje)).thenReturn(null);
        when(mockProgressoDiarioDAO.addProgresso(any(ProgressoDiario.class))).thenReturn(progressoSalvoPeloDAO);
        
        int pontosAposHabito = pontosIniciaisUsuario + pontosPeloHabito;
        when(mockUsuarioDAO.updatePontosUsuario(eq(usuarioId), eq(pontosAposHabito))).thenReturn(true);
        
        // Mocks para a lógica de conquista
        when(mockConquistaService.getAllConquistaDefinicoes()).thenReturn(Collections.singletonList(definicaoPrimeirosPassos));
        when(mockConquistaService.getConquistasDoUsuario(usuarioId)).thenReturn(Collections.emptyList()); // Não tem a conquista ainda
        // A lógica atual no HabitService para "Primeiros Passos" é: if (usuarioComPontosAtualizados.getPontos() > 0)
        // Isso será verdade após ganhar pontosPeloHabito.
        when(mockConquistaService.darConquistaParaUsuario(usuarioId, definicaoPrimeirosPassos.getId())).thenReturn(true);

        int pontosAposBonusConquista = pontosAposHabito + definicaoPrimeirosPassos.getPontosBonus();
        when(mockUsuarioDAO.updatePontosUsuario(eq(usuarioId), eq(pontosAposBonusConquista))).thenReturn(true);

        FeedbackMarcacaoDTO feedback = habitService.marcarHabitoComoFeito(usuarioId, habitoId, hoje);

        assertTrue(feedback.isSucesso());
        assertEquals(pontosPeloHabito, feedback.getPontosGanhosNestaMarcacao());
        assertEquals(pontosAposBonusConquista, feedback.getTotalPontosUsuarioAposMarcacao());
        assertEquals(1, feedback.getNovasConquistasDesbloqueadas().size());
        assertEquals("Primeiros Passos", feedback.getNovasConquistasDesbloqueadas().get(0).getNome());

        verify(mockProgressoDiarioDAO).addProgresso(any(ProgressoDiario.class));
        verify(mockUsuarioDAO).updatePontosUsuario(usuarioId, pontosAposHabito);
        verify(mockConquistaService).darConquistaParaUsuario(usuarioId, definicaoPrimeirosPassos.getId());
        verify(mockUsuarioDAO).updatePontosUsuario(usuarioId, pontosAposBonusConquista);
    }
}