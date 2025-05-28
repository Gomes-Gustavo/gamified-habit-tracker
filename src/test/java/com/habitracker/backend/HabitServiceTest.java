package com.habitracker.backend;

// Todos os imports que você precisará para os testes:
import com.habitracker.database.HabitDAO;
import com.habitracker.database.ProgressoDiarioDAO;
import com.habitracker.database.UsuarioDAO;
import com.habitracker.model.Habit;
import com.habitracker.model.ProgressoDiario;
import com.habitracker.model.Usuario;
import com.habitracker.model.Conquista; // Adicione se for testar métodos que retornam/usam
import com.habitracker.serviceapi.HabitTrackerServiceAPI; // Se for testar através da interface
import com.habitracker.serviceapi.dto.FeedbackMarcacaoDTO;
import com.habitracker.serviceapi.exceptions.HabitNotFoundException;
import com.habitracker.serviceapi.exceptions.PersistenceException;
import com.habitracker.serviceapi.exceptions.UserNotFoundException;
import com.habitracker.serviceapi.exceptions.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations; // Para inicializar os mocks

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Imports estáticos para facilitar a escrita das asserções e configuração do Mockito
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq; // Para argumentos específicos
import static org.mockito.Mockito.doThrow; // Para mockar métodos void que lançam exceção
import static org.mockito.Mockito.never;  // Para verificar que um método NUNCA foi chamado
import static org.mockito.Mockito.times;  // Para verificar quantas vezes foi chamado
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class HabitServiceTest {

    @Mock // Mockito criará um "dublê" para HabitDAO
    private HabitDAO mockHabitDAO;

    @Mock
    private UsuarioDAO mockUsuarioDAO;

    @Mock
    private ProgressoDiarioDAO mockProgressoDiarioDAO;

    @Mock
    private ConquistaService mockConquistaService;

    // @InjectMocks criará uma instância REAL de HabitService
    // e tentará injetar os objetos anotados com @Mock acima em seus campos.
    // Isso funciona porque agora temos o construtor que aceita essas dependências.
    @InjectMocks 
    private HabitService habitService; 

    @BeforeEach // Este método roda ANTES de cada método @Test
    void setUp() {
        // Inicializa os campos anotados com @Mock e @InjectMocks nesta classe de teste.
        MockitoAnnotations.openMocks(this);
    }

    // --- Teste de Exemplo para addHabit (Caminho Feliz) ---
    @Test
    void addHabit_quandoHabitoValido_deveRetornarHabitoComId() throws PersistenceException, ValidationException {
        // 1. Arrange (Organizar)
        Habit habitSemId = new Habit("Meditar", "15 minutos de meditação guiada", LocalDate.now());
        Habit habitComIdRetornadoPeloDAO = new Habit(1, "Meditar", "15 minutos de meditação guiada", LocalDate.now());

        // Configura o comportamento do mock DAO:
        // QUANDO o método addHabit do mockHabitDAO for chamado com QUALQUER objeto Habit...
        when(mockHabitDAO.addHabit(any(Habit.class)))
            .thenReturn(habitComIdRetornadoPeloDAO); // ...ENTÃO retorne este objeto habitComIdRetornadoPeloDAO

        // 2. Act (Agir)
        Habit resultadoDoService = habitService.addHabit(habitSemId);

        // 3. Assert (Verificar)
        assertNotNull(resultadoDoService, "O hábito retornado pelo serviço não deve ser nulo.");
        assertEquals(1, resultadoDoService.getId(), "O ID do hábito retornado deve ser 1.");
        assertEquals("Meditar", resultadoDoService.getName(), "O nome do hábito não confere.");
        
        // Verifica se o método addHabit do mockDAO foi chamado exatamente uma vez com o objeto habitSemId
        verify(mockHabitDAO, times(1)).addHabit(eq(habitSemId)); 
    }

    // --- Teste de Exemplo para addHabit (Exceção de Validação) ---
    @Test
    void addHabit_quandoNomeDoHabitoNulo_deveLancarValidationException() {
        // Arrange
        Habit habitComNomeNulo = new Habit(null, "Descrição", LocalDate.now());

        // Act & Assert
        // Verifica se a chamada a habitService.addHabit(habitComNomeNulo) lança uma ValidationException
        ValidationException e = assertThrows(ValidationException.class, () -> {
            habitService.addHabit(habitComNomeNulo);
        });
        
        // Verifica a mensagem da exceção
        assertEquals("Dados do hábito inválidos: nome não pode ser vazio.", e.getMessage());
        
        // Verifica que o DAO NUNCA foi chamado neste caso
        verify(mockHabitDAO, never()).addHabit(any(Habit.class));
    }

    // --- Teste para addHabit (Falha de Persistência) ---
    @Test
    void addHabit_quandoDAOFalhaAoSalvar_deveLancarPersistenceException() {
        // 1. Arrange
        Habit habitValido = new Habit("Exercício Matinal", "30 minutos de cardio", LocalDate.now());

        // Configura o mock DAO para simular uma falha ao salvar (retornando null)
        when(mockHabitDAO.addHabit(any(Habit.class))).thenReturn(null);

        // 2. Act & 3. Assert
        // Verifica se uma PersistenceException é lançada
        PersistenceException exception = assertThrows(PersistenceException.class, () -> {
            habitService.addHabit(habitValido);
        });

        // Opcional: Verificar a mensagem da exceção, se você padronizou
        assertEquals("Falha ao salvar o hábito no banco de dados.", exception.getMessage());

        // Verifica se o método addHabit do mockDAO foi chamado
        verify(mockHabitDAO).addHabit(eq(habitValido));
    }

    // --- Testes para getHabitById ---
    @Test
    void getHabitById_quandoIdValidoEHabitoExiste_deveRetornarHabitCorreto() throws HabitNotFoundException {
        // 1. Arrange
        int idExistente = 5;
        Habit habitEsperado = new Habit(idExistente, "Beber Água", "Beber 2L de água por dia", LocalDate.now().minusDays(3));

        // Configura o mock DAO para retornar o habitEsperado quando getHabitById for chamado com idExistente
        when(mockHabitDAO.getHabitById(idExistente)).thenReturn(habitEsperado);

        // 2. Act
        Habit resultado = habitService.getHabitById(idExistente);

        // 3. Assert
        assertNotNull(resultado, "O hábito retornado não deveria ser nulo.");
        assertEquals(idExistente, resultado.getId(), "O ID do hábito não confere.");
        assertEquals(habitEsperado.getName(), resultado.getName(), "O nome do hábito não confere.");
        assertEquals(habitEsperado.getDescription(), resultado.getDescription(), "A descrição do hábito não confere.");
        assertEquals(habitEsperado.getCreationDate(), resultado.getCreationDate(), "A data de criação não confere.");

        // Verifica se o método getHabitById do mockDAO foi chamado
        verify(mockHabitDAO).getHabitById(idExistente);
    }

    @Test
    void getHabitById_quandoIdInvalido_deveLancarHabitNotFoundException() {
        // 1. Arrange
        int idInvalido = 0;

        // 2. Act & 3. Assert
        HabitNotFoundException exception = assertThrows(HabitNotFoundException.class, () -> {
            habitService.getHabitById(idInvalido);
        });
        
        // Verifica a mensagem da exceção (ajuste conforme a mensagem exata no seu HabitService)
        assertTrue(exception.getMessage().contains("ID do hábito inválido"), "A mensagem de exceção para ID inválido não está correta.");
        
        // Verifica que o DAO NUNCA foi chamado neste caso
        verify(mockHabitDAO, never()).getHabitById(anyInt());
    }

    // --- Testes para updateHabit ---
    @Test
    void updateHabit_quandoHabitoValidoEExistente_deveRetornarHabitoAtualizado() throws Exception { // Usando Exception genérico por causa das múltiplas exceções checadas
        // 1. Arrange
        int habitIdExistente = 1;
        Habit habitOriginalNoBanco = new Habit(habitIdExistente, "Nome Antigo", "Desc Antiga", LocalDate.now().minusDays(1));
        Habit habitComNovosDados = new Habit(habitIdExistente, "Nome Atualizado", "Desc Atualizada", LocalDate.now().minusDays(1)); // Data de criação não muda geralmente

        // Mock para o getHabitById inicial (que verifica se o hábito existe)
        when(mockHabitDAO.getHabitById(habitIdExistente)).thenReturn(habitOriginalNoBanco);
        
        // Mock para o habitDAO.updateHabit (simula que o update no banco foi bem-sucedido)
        when(mockHabitDAO.updateHabit(any(Habit.class))).thenReturn(true);

        // Mock para o getHabitById que re-busca o hábito APÓS o update bem-sucedido
        // Importante: o objeto retornado aqui deve refletir os dados atualizados.
        when(mockHabitDAO.getHabitById(habitIdExistente)).thenReturn(habitComNovosDados); // Retorna o estado após o update

        // 2. Act
        Habit resultado = habitService.updateHabit(habitComNovosDados); // Passa o objeto com os dados que queremos atualizar

        // 3. Assert
        assertNotNull(resultado);
        assertEquals(habitIdExistente, resultado.getId());
        assertEquals("Nome Atualizado", resultado.getName());
        assertEquals("Desc Atualizada", resultado.getDescription());

        // Verifica as chamadas aos mocks
        verify(mockHabitDAO, times(2)).getHabitById(habitIdExistente); // Uma para checar existência, uma para re-buscar
        verify(mockHabitDAO).updateHabit(eq(habitComNovosDados)); // Verifica se o update foi chamado com o objeto correto
    }

    @Test
    void updateHabit_quandoHabitoNaoExiste_deveLancarHabitNotFoundException() {
        // 1. Arrange
        int idNaoExistente = 99;
        Habit habitParaAtualizar = new Habit(idNaoExistente, "Nome Qualquer", "Desc Qualquer", LocalDate.now());

        // Mock para o getHabitById inicial (que verifica se o hábito existe)
        // Este mock simula que o hábito não foi encontrado
        when(mockHabitDAO.getHabitById(idNaoExistente)).thenReturn(null);

        // 2. Act & 3. Assert
        HabitNotFoundException exception = assertThrows(HabitNotFoundException.class, () -> {
            habitService.updateHabit(habitParaAtualizar);
        });

        assertEquals("Hábito com ID " + idNaoExistente + " não encontrado.", exception.getMessage());

        // Verifica que o update no DAO nunca foi chamado
        verify(mockHabitDAO, never()).updateHabit(any(Habit.class));
    }

    @Test
    void updateHabit_quandoDAOFalhaNoUpdate_deveLancarPersistenceException() throws HabitNotFoundException {
        // 1. Arrange
        int habitIdExistente = 1;
        Habit habitOriginalNoBanco = new Habit(habitIdExistente, "Nome Antigo", "Desc Antiga", LocalDate.now());
        Habit habitComNovosDados = new Habit(habitIdExistente, "Nome Atualizado", "Desc Atualizada", LocalDate.now());

        // Mock para o getHabitById inicial (hábito existe)
        when(mockHabitDAO.getHabitById(habitIdExistente)).thenReturn(habitOriginalNoBanco);
        
        // Mock para o habitDAO.updateHabit (simula que o update no banco FALHOU)
        when(mockHabitDAO.updateHabit(any(Habit.class))).thenReturn(false);

        // 2. Act & 3. Assert
        PersistenceException exception = assertThrows(PersistenceException.class, () -> {
            habitService.updateHabit(habitComNovosDados);
        });
        
        assertTrue(exception.getMessage().contains("Falha ao atualizar o hábito no banco de dados"));

        // Verifica que o update foi tentado
        verify(mockHabitDAO).updateHabit(eq(habitComNovosDados));
    }

    // --- Testes para deleteHabit ---
    @Test
    void deleteHabit_quandoHabitoExiste_deveRetornarTrue() throws Exception {
        // 1. Arrange
        int idExistente = 1;
        Habit habitExistente = new Habit(idExistente, "Hábito a Deletar", "...", LocalDate.now());

        // Mock para getHabitById (hábito existe)
        when(mockHabitDAO.getHabitById(idExistente)).thenReturn(habitExistente);
        // Mock para deleteHabit do DAO (deleção bem-sucedida)
        when(mockHabitDAO.deleteHabit(idExistente)).thenReturn(true);

        // 2. Act
        boolean resultado = habitService.deleteHabit(idExistente);

        // 3. Assert
        assertTrue(resultado, "A deleção deveria retornar true.");
        verify(mockHabitDAO).getHabitById(idExistente); // Verifica se a checagem de existência foi feita
        verify(mockHabitDAO).deleteHabit(idExistente); // Verifica se o delete do DAO foi chamado
    }

    @Test
    void deleteHabit_quandoHabitoNaoExiste_deveLancarHabitNotFoundException() {
        // 1. Arrange
        int idNaoExistente = 99;
        
        // Mock para getHabitById (simula que o hábito não foi encontrado)
        when(mockHabitDAO.getHabitById(idNaoExistente)).thenReturn(null);

        // 2. Act & 3. Assert
        HabitNotFoundException exception = assertThrows(HabitNotFoundException.class, () -> {
            habitService.deleteHabit(idNaoExistente);
        });

        assertEquals("Hábito com ID " + idNaoExistente + " não encontrado.", exception.getMessage());
        verify(mockHabitDAO).getHabitById(idNaoExistente); // Verifica a tentativa de busca
        verify(mockHabitDAO, never()).deleteHabit(anyInt()); // Garante que o delete do DAO não foi chamado
    }

    // --- Testes para marcarHabitoComoFeito ---
    @Test
    void marcarHabitoComoFeito_novoProgressoSemNovasConquistas_deveRetornarFeedbackCorreto() throws Exception {
        // 1. Arrange
        int usuarioId = 1;
        int habitoId = 1;
        LocalDate hoje = LocalDate.now();
        int pontosIniciaisUsuario = 50;
        int pontosPeloHabito = 10;

        Usuario usuarioMock = new Usuario(usuarioId, "Usuario Teste", pontosIniciaisUsuario);
        Habit habitMock = new Habit(habitoId, "Hábito de Teste", "Descrição", hoje.minusDays(1));
        ProgressoDiario progressoSalvoPeloDAO = new ProgressoDiario(100, usuarioId, habitoId, hoje, true); // ID fictício do progresso

        when(mockUsuarioDAO.getUsuarioById(usuarioId)).thenReturn(usuarioMock);
        when(mockHabitDAO.getHabitById(habitoId)).thenReturn(habitMock); // Usando o mockHabitDAO
        when(mockProgressoDiarioDAO.getProgresso(usuarioId, habitoId, hoje)).thenReturn(null); // Nenhum progresso anterior
        when(mockProgressoDiarioDAO.addProgresso(any(ProgressoDiario.class))).thenReturn(progressoSalvoPeloDAO);
        when(mockUsuarioDAO.updatePontosUsuario(eq(usuarioId), eq(pontosIniciaisUsuario + pontosPeloHabito))).thenReturn(true);

        // Simular que não há novas conquistas
        when(mockConquistaService.getAllConquistaDefinicoes()).thenReturn(Collections.emptyList()); // Nenhuma definição para simplificar
        // ou mockar getConquistasDoUsuario para que todas já sejam possuídas ou critérios não atendidos.
        // e garantir que darConquistaParaUsuario não seja chamado ou retorne false.

        // 2. Act
        FeedbackMarcacaoDTO feedback = habitService.marcarHabitoComoFeito(usuarioId, habitoId, hoje);

        // 3. Assert
        assertTrue(feedback.isSucesso());
        assertEquals(pontosPeloHabito, feedback.getPontosGanhosNestaMarcacao());
        assertEquals(pontosIniciaisUsuario + pontosPeloHabito, feedback.getTotalPontosUsuarioAposMarcacao());
        assertTrue(feedback.getNovasConquistasDesbloqueadas().isEmpty());
        assertEquals("Hábito marcado como feito com sucesso!", feedback.getMensagem());

        verify(mockProgressoDiarioDAO).addProgresso(any(ProgressoDiario.class));
        verify(mockUsuarioDAO).updatePontosUsuario(usuarioId, pontosIniciaisUsuario + pontosPeloHabito);
        // Verificar se verificarEConcederConquistasInterno foi chamado (pode ser mais complexo verificar interações internas)
        // ou verificar chamadas ao mockConquistaService se a lógica estiver lá
    }

    @Test
    void marcarHabitoComoFeito_progressoJaExistiaECumprido_naoDeveDarNovosPontos() throws Exception {
        // 1. Arrange
        int usuarioId = 1;
        int habitoId = 1;
        LocalDate hoje = LocalDate.now();
        int pontosIniciaisUsuario = 20; // Usuário já tem pontos

        Usuario usuarioMock = new Usuario(usuarioId, "Usuario Teste", pontosIniciaisUsuario);
        Habit habitMock = new Habit(habitoId, "Hábito Teste", "Desc", hoje.minusDays(1));
        // Progresso já existe e está CUMPRIDO
        ProgressoDiario progressoExistenteMock = new ProgressoDiario(100, usuarioId, habitoId, hoje, true); 

        when(mockUsuarioDAO.getUsuarioById(usuarioId)).thenReturn(usuarioMock);
        when(mockHabitDAO.getHabitById(habitoId)).thenReturn(habitMock);
        when(mockProgressoDiarioDAO.getProgresso(usuarioId, habitoId, hoje)).thenReturn(progressoExistenteMock);

        // 2. Act
        FeedbackMarcacaoDTO feedback = habitService.marcarHabitoComoFeito(usuarioId, habitoId, hoje);

        // 3. Assert
        assertTrue(feedback.isSucesso());
        assertEquals(0, feedback.getPontosGanhosNestaMarcacao(), "Não deveria ganhar pontos por hábito já cumprido.");
        assertEquals(pontosIniciaisUsuario, feedback.getTotalPontosUsuarioAposMarcacao(), "Total de pontos não deveria mudar.");
        assertTrue(feedback.getNovasConquistasDesbloqueadas().isEmpty(), "Não deveria haver novas conquistas.");
        assertEquals("Hábito já estava marcado como feito.", feedback.getMensagem());

        // Verifica que o addProgresso e updatePontos NÃO foram chamados
        verify(mockProgressoDiarioDAO, never()).addProgresso(any(ProgressoDiario.class));
        verify(mockUsuarioDAO, never()).updatePontosUsuario(anyInt(), anyInt());
        // verificarEConcederConquistasInterno não deve ser chamado se não foi uma nova conclusão
        // (A lógica interna do marcarHabitoComoFeito deve prevenir isso)
    }

    @Test
    void marcarHabitoComoFeito_progressoJaExistiaNaoCumprido_deveRetornarFalhaNaAlteracao() throws Exception {
        // 1. Arrange
        int usuarioId = 1;
        int habitoId = 1;
        LocalDate hoje = LocalDate.now();
        int pontosIniciaisUsuario = 0; // Pontos iniciais do mock

        Usuario usuarioMock = new Usuario(usuarioId, "Usuario Teste", pontosIniciaisUsuario);
        Habit habitMock = new Habit(habitoId, "Hábito Teste", "Desc", hoje.minusDays(1));
        ProgressoDiario progressoExistenteMock = new ProgressoDiario(101, usuarioId, habitoId, hoje, false); // NÃO cumprido

        when(mockUsuarioDAO.getUsuarioById(usuarioId)).thenReturn(usuarioMock);
        when(mockHabitDAO.getHabitById(habitoId)).thenReturn(habitMock);
        when(mockProgressoDiarioDAO.getProgresso(usuarioId, habitoId, hoje)).thenReturn(progressoExistenteMock);

        // 2. Act
        FeedbackMarcacaoDTO feedback = habitService.marcarHabitoComoFeito(usuarioId, habitoId, hoje);

        // 3. Assert
        assertFalse(feedback.isSucesso(), "O sucesso deveria ser false pois não se pode alterar o status.");
        assertEquals("Hábito já registrado como 'não cumprido'. Alteração de status não suportada no momento.", feedback.getMensagem());
        assertEquals(0, feedback.getPontosGanhosNestaMarcacao());
        // CORREÇÃO AQUI: Esperamos -1 conforme o construtor do DTO para este cenário
        assertEquals(-1, feedback.getTotalPontosUsuarioAposMarcacao(), "Para este tipo de falha, o total de pontos deveria ser -1 como indicador."); 
        assertTrue(feedback.getNovasConquistasDesbloqueadas().isEmpty());

        verify(mockProgressoDiarioDAO, never()).addProgresso(any(ProgressoDiario.class));
        verify(mockUsuarioDAO, never()).updatePontosUsuario(anyInt(), anyInt());
    }

    @Test
    void marcarHabitoComoFeito_quandoUsuarioNaoEncontrado_deveLancarUserNotFoundException() {
        // 1. Arrange
        int usuarioIdInexistente = 99;
        int habitoId = 1;
        LocalDate hoje = LocalDate.now();

        when(mockUsuarioDAO.getUsuarioById(usuarioIdInexistente)).thenReturn(null);
        // Não precisamos mockar habitDAO.getHabitById pois a checagem do usuário vem antes

        // 2. Act & 3. Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            habitService.marcarHabitoComoFeito(usuarioIdInexistente, habitoId, hoje);
        });

        assertEquals("Usuário com ID " + usuarioIdInexistente + " não encontrado para marcar hábito.", exception.getMessage());
    }

    @Test
    void marcarHabitoComoFeito_quandoHabitoNaoEncontrado_deveLancarHabitNotFoundException() {
        // 1. Arrange
        int usuarioId = 1;
        int habitoIdInexistente = 99;
        LocalDate hoje = LocalDate.now();

        Usuario usuarioMock = new Usuario(usuarioId, "Usuario Teste", 0);
        when(mockUsuarioDAO.getUsuarioById(usuarioId)).thenReturn(usuarioMock);
        when(mockHabitDAO.getHabitById(habitoIdInexistente)).thenReturn(null); // O método getHabitById do serviço lançaria HabitNotFoundException

        // 2. Act & 3. Assert
        HabitNotFoundException exception = assertThrows(HabitNotFoundException.class, () -> {
            habitService.marcarHabitoComoFeito(usuarioId, habitoIdInexistente, hoje);
        });

        assertEquals("Hábito com ID " + habitoIdInexistente + " não encontrado.", exception.getMessage());
    }

    @Test
    void marcarHabitoComoFeito_quandoDataNula_deveLancarValidationException() throws UserNotFoundException, HabitNotFoundException {
        // 1. Arrange
        int usuarioId = 1;
        int habitoId = 1;

        Usuario usuarioMock = new Usuario(usuarioId, "Usuario Teste", 0);
        Habit habitMock = new Habit(habitoId, "Hábito Teste", "Desc", LocalDate.now());

        when(mockUsuarioDAO.getUsuarioById(usuarioId)).thenReturn(usuarioMock);
        when(mockHabitDAO.getHabitById(habitoId)).thenReturn(habitMock); //Necessário para passar da validação de hábito

        // 2. Act & 3. Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            habitService.marcarHabitoComoFeito(usuarioId, habitoId, null); // Data nula
        });
        
        assertEquals("Data para marcar hábito não pode ser nula.", exception.getMessage());
    }

    // --- Testes para getPontuacaoUsuario ---
    @Test
    void getPontuacaoUsuario_quandoUsuarioExiste_deveRetornarPontosCorretos() throws UserNotFoundException {
        // 1. Arrange
        int usuarioId = 1;
        int pontosEsperados = 150;
        Usuario usuarioMock = new Usuario(usuarioId, "Usuario Pontuador", pontosEsperados);
        when(mockUsuarioDAO.getUsuarioById(usuarioId)).thenReturn(usuarioMock);

        // 2. Act
        int pontosResultado = habitService.getPontuacaoUsuario(usuarioId);

        // 3. Assert
        assertEquals(pontosEsperados, pontosResultado);
        verify(mockUsuarioDAO).getUsuarioById(usuarioId);
    }

    @Test
    void getPontuacaoUsuario_quandoUsuarioNaoExiste_deveLancarUserNotFoundException() {
        // 1. Arrange
        int usuarioIdInexistente = 99;
        when(mockUsuarioDAO.getUsuarioById(usuarioIdInexistente)).thenReturn(null);

        // 2. Act & 3. Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            habitService.getPontuacaoUsuario(usuarioIdInexistente);
        });
        assertEquals("Usuário com ID " + usuarioIdInexistente + " não encontrado ao buscar pontuação.", exception.getMessage());
    }

    // --- Testes para getConquistasDesbloqueadasUsuario ---
    @Test
    void getConquistasDesbloqueadasUsuario_quandoUsuarioExisteComConquistas_deveRetornarLista() throws UserNotFoundException {
        // 1. Arrange
        int usuarioId = 1;
        Usuario usuarioMock = new Usuario(usuarioId, "Usuario Conquistador", 100);
        List<Conquista> conquistasEsperadas = new ArrayList<>();
        conquistasEsperadas.add(new Conquista(1, "Conquista Alfa", "Desc Alfa", "Crit Alfa", 10));
        conquistasEsperadas.add(new Conquista(2, "Conquista Beta", "Desc Beta", "Crit Beta", 20));

        when(mockUsuarioDAO.getUsuarioById(usuarioId)).thenReturn(usuarioMock); // Para a checagem de existência do usuário
        when(mockConquistaService.getConquistasDoUsuario(usuarioId)).thenReturn(conquistasEsperadas);

        // 2. Act
        List<Conquista> resultado = habitService.getConquistasDesbloqueadasUsuario(usuarioId);

        // 3. Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Conquista Alfa", resultado.get(0).getNome());
        verify(mockConquistaService).getConquistasDoUsuario(usuarioId);
    }

    @Test
    void getConquistasDesbloqueadasUsuario_quandoUsuarioNaoExiste_deveLancarUserNotFoundException() {
        // 1. Arrange
        int usuarioIdInexistente = 99;
        when(mockUsuarioDAO.getUsuarioById(usuarioIdInexistente)).thenReturn(null); // Usuário não encontrado

        // 2. Act & 3. Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            habitService.getConquistasDesbloqueadasUsuario(usuarioIdInexistente);
        });
        assertEquals("Usuário com ID " + usuarioIdInexistente + " não encontrado ao buscar conquistas desbloqueadas.", exception.getMessage());
        verify(mockConquistaService, never()).getConquistasDoUsuario(anyInt()); // Não deve nem chamar o conquistaService
    }

    // --- Teste para getAllConquistasPossiveis ---
    @Test
    void getAllConquistasPossiveis_quandoExistemDefinicoes_deveRetornarLista() {
        // 1. Arrange
        List<Conquista> definicoesEsperadas = new ArrayList<>();
        definicoesEsperadas.add(new Conquista(10, "Mestre dos Hábitos", "Todos os hábitos no máximo", "...", 1000));
        definicoesEsperadas.add(new Conquista(11, "Colecionador Supremo", "Ter 20 hábitos", "...", 500));
        when(mockConquistaService.getAllConquistaDefinicoes()).thenReturn(definicoesEsperadas);

        // 2. Act
        List<Conquista> resultado = habitService.getAllConquistasPossiveis();

        // 3. Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Mestre dos Hábitos", resultado.get(0).getNome());
        verify(mockConquistaService).getAllConquistaDefinicoes();
    }

    @Test
    void getAllConquistasPossiveis_quandoNaoExistemDefinicoes_deveRetornarListaVazia() {
        // 1. Arrange
        when(mockConquistaService.getAllConquistaDefinicoes()).thenReturn(Collections.emptyList());

        // 2. Act
        List<Conquista> resultado = habitService.getAllConquistasPossiveis();

        // 3. Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(mockConquistaService).getAllConquistaDefinicoes();
    }

    @Test
    void marcarHabitoComoFeito_novoProgressoComDesbloqueioConquistaEBonus_deveRetornarFeedbackCompleto() throws Exception {
        // 1. Arrange
        int usuarioId = 1;
        int habitoId = 1;
        LocalDate hoje = LocalDate.now();
        int pontosIniciaisUsuario = 0;
        int pontosPeloHabito = 10; // Pontos por marcar o hábito

        Usuario usuarioMock = new Usuario(usuarioId, "Usuario Conquistador", pontosIniciaisUsuario);
        // É importante que o objeto usuarioMock seja o mesmo que o verificarEConcederConquistasInterno vai usar para setar os pontos
        // Por isso, no service, passamos o objeto 'usuarioParaMarcar' para verificarEConcederConquistasInterno

        Habit habitMock = new Habit(habitoId, "Hábito da Conquista", "Descrição", hoje.minusDays(1));
        ProgressoDiario progressoSalvoPeloDAO = new ProgressoDiario(200, usuarioId, habitoId, hoje, true);

        Conquista definicaoPrimeirosPassos = new Conquista(1, "Primeiros Passos", "Completou um hábito", "Completar 1 hábito", 10); // 10 pontos de bônus

        // Configuração dos Mocks
        when(mockUsuarioDAO.getUsuarioById(usuarioId)).thenReturn(usuarioMock);
        when(mockHabitDAO.getHabitById(habitoId)).thenReturn(habitMock);
        when(mockProgressoDiarioDAO.getProgresso(usuarioId, habitoId, hoje)).thenReturn(null); // Nenhum progresso anterior
        when(mockProgressoDiarioDAO.addProgresso(any(ProgressoDiario.class))).thenReturn(progressoSalvoPeloDAO);
        
        // 1ª atualização de pontos (pelo hábito)
        when(mockUsuarioDAO.updatePontosUsuario(eq(usuarioId), eq(pontosIniciaisUsuario + pontosPeloHabito))).thenReturn(true);
        
        // Mocks para verificarEConcederConquistasInterno
        when(mockConquistaService.getAllConquistaDefinicoes()).thenReturn(Collections.singletonList(definicaoPrimeirosPassos));
        when(mockConquistaService.getConquistasDoUsuario(usuarioId)).thenReturn(Collections.emptyList()); // Usuário ainda não tem "Primeiros Passos"
        when(mockProgressoDiarioDAO.getCountProgressoCumprido(usuarioId)).thenReturn(1); // Critério para "Primeiros Passos" atendido
        when(mockConquistaService.darConquistaParaUsuario(usuarioId, definicaoPrimeirosPassos.getId())).thenReturn(true); // Conquista concedida com sucesso

        // 2ª atualização de pontos (pelo bônus da conquista)
        int pontosAposHabito = pontosIniciaisUsuario + pontosPeloHabito;
        int pontosAposBonusConquista = pontosAposHabito + definicaoPrimeirosPassos.getPontosBonus();
        when(mockUsuarioDAO.updatePontosUsuario(eq(usuarioId), eq(pontosAposBonusConquista))).thenReturn(true);

        // 2. Act
        FeedbackMarcacaoDTO feedback = habitService.marcarHabitoComoFeito(usuarioId, habitoId, hoje);

        // 3. Assert
        assertTrue(feedback.isSucesso());
        assertEquals(pontosPeloHabito, feedback.getPontosGanhosNestaMarcacao(), "Pontos ganhos pelo hábito estão incorretos.");
        assertEquals(pontosAposBonusConquista, feedback.getTotalPontosUsuarioAposMarcacao(), "Total de pontos do usuário após todos os bônus está incorreto.");
        assertFalse(feedback.getNovasConquistasDesbloqueadas().isEmpty(), "Deveria haver uma nova conquista desbloqueada.");
        assertEquals(1, feedback.getNovasConquistasDesbloqueadas().size(), "Número de novas conquistas incorreto.");
        assertEquals("Primeiros Passos", feedback.getNovasConquistasDesbloqueadas().get(0).getNome(), "Nome da conquista desbloqueada incorreto.");
        assertEquals("Hábito marcado como feito com sucesso!", feedback.getMensagem());

        // Verifica as chamadas importantes
        verify(mockProgressoDiarioDAO).addProgresso(any(ProgressoDiario.class));
        verify(mockUsuarioDAO).updatePontosUsuario(usuarioId, pontosAposHabito); // Update de pontos do hábito
        verify(mockConquistaService).darConquistaParaUsuario(usuarioId, definicaoPrimeirosPassos.getId());
        verify(mockUsuarioDAO).updatePontosUsuario(usuarioId, pontosAposBonusConquista); // Update de pontos do bônus da conquista
    }

    // TODO: Adicionar mais testes para os outros métodos e cenários!
    // Por exemplo:
    // - addHabit quando o DAO retorna null (deve lançar PersistenceException)
    // - getHabitById (encontrado, não encontrado com ID inválido, não encontrado com ID válido mas ausente)
    // - updateHabit (sucesso, não encontrado, validação)
    // - deleteHabit (sucesso, não encontrado)
    // - marcarHabitoComoFeito (novo progresso, progresso já existe e está feito, progresso existe e não está feito, usuário/hábito não encontrado, com/sem desbloqueio de conquista)
    // - getPontuacaoUsuario
    // - etc.
}