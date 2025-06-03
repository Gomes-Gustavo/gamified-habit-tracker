package com.habitracker.backend;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test; // Adicionado para melhor descrição dos testes
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when; // Adicionado para DiasDaSemana
import org.mockito.MockitoAnnotations; // Adicionado para DiasDaSemana

import com.habitracker.database.HabitDAO;
import com.habitracker.database.ProgressoDiarioDAO;
import com.habitracker.database.UsuarioDAO;
import com.habitracker.model.Habit; // Adicionado para addUsuario
import com.habitracker.model.ProgressoDiario;
import com.habitracker.model.Usuario;
import com.habitracker.serviceapi.dto.FeedbackMarcacaoDTO;
import com.habitracker.serviceapi.exceptions.HabitNotFoundException;
import com.habitracker.serviceapi.exceptions.PersistenceException;
import com.habitracker.serviceapi.exceptions.UserNotFoundException; // Adicionado para simular exceções do DAO
import com.habitracker.serviceapi.exceptions.ValidationException;


public class HabitServiceTest {

    @Mock
    private HabitDAO habitDAOMock;

    @Mock
    private UsuarioDAO usuarioDAOMock;

    @Mock
    private ProgressoDiarioDAO progressoDiarioDAOMock;

    @InjectMocks // Cria uma instância de HabitService e injeta os mocks acima
    private HabitService habitService;

    private Usuario usuarioTeste;
    private Habit habitoTeste;
    private ProgressoDiario progressoTeste;

    @BeforeEach
    void setUp() {
        // Inicializa os mocks anotados com @Mock e @InjectMocks
        // O MockitoAnnotations.openMocks(this) é importante para que os mocks sejam criados e injetados corretamente.
        MockitoAnnotations.openMocks(this);

        // Configuração de objetos de teste padrão
        usuarioTeste = new Usuario(1, "Usuario Teste", 100);
        habitoTeste = new Habit(1, "Ler Livro", "Ler por 30 minutos", LocalDate.now(), usuarioTeste.getId(), LocalTime.of(8, 0));
        habitoTeste.setDiasDaSemana(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)); // Exemplo
        progressoTeste = new ProgressoDiario(1, usuarioTeste.getId(), habitoTeste.getId(), LocalDate.now(), true);
    }

    // --- Testes para Usuários ---

    @Test
    @DisplayName("Deve adicionar um usuário com sucesso")
    void addUsuario_quandoNomeValido_deveRetornarUsuarioComId() throws PersistenceException, ValidationException {
        Usuario novoUsuario = new Usuario("Novo Usuario");
        Usuario usuarioSalvo = new Usuario(2, "Novo Usuario", 0); // Simula o usuário salvo com ID

        when(usuarioDAOMock.addUsuario(any(Usuario.class))).thenReturn(usuarioSalvo);

        Usuario resultado = habitService.addUsuario("Novo Usuario");

        assertNotNull(resultado);
        assertEquals(2, resultado.getId());
        assertEquals("Novo Usuario", resultado.getNome());
        verify(usuarioDAOMock, times(1)).addUsuario(any(Usuario.class));
    }

    @Test
    @DisplayName("Não deve adicionar usuário com nome vazio e deve lançar ValidationException")
    void addUsuario_quandoNomeVazio_deveLancarValidationException() {
        Exception exception = assertThrows(ValidationException.class, () -> {
            habitService.addUsuario("   ");
        });
        assertEquals("O nome do usuário não pode ser vazio.", exception.getMessage());
        verify(usuarioDAOMock, never()).addUsuario(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void getUsuarioById_quandoIdExistente_deveRetornarUsuario() throws UserNotFoundException, PersistenceException {
        when(usuarioDAOMock.getUsuarioById(usuarioTeste.getId())).thenReturn(usuarioTeste);

        Usuario resultado = habitService.getUsuarioById(usuarioTeste.getId());

        assertNotNull(resultado);
        assertEquals(usuarioTeste.getId(), resultado.getId());
        verify(usuarioDAOMock, times(1)).getUsuarioById(usuarioTeste.getId());
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException ao buscar usuário com ID inválido (<=0)")
    void getUsuarioById_quandoIdInvalido_deveLancarUserNotFoundException() {
        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            habitService.getUsuarioById(0);
        });
        assertTrue(exception.getMessage().contains("ID de usuário inválido: 0"));
    }

    @Test
    @DisplayName("Deve lançar UserNotFoundException ao buscar usuário com ID não existente")
    void getUsuarioById_quandoIdNaoExistente_deveLancarUserNotFoundException() throws PersistenceException {
        int idNaoExistente = 99;
        when(usuarioDAOMock.getUsuarioById(idNaoExistente)).thenReturn(null);

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            habitService.getUsuarioById(idNaoExistente);
        });
        assertEquals("Usuário com ID " + idNaoExistente + " não encontrado.", exception.getMessage());
    }


    // --- Testes para Hábitos (CRUD) ---

    @Test
    @DisplayName("Deve adicionar um hábito com sucesso")
    void addHabit_quandoHabitoValido_deveRetornarHabitoComId() throws PersistenceException, ValidationException {
        Habit novoHabito = new Habit("Correr", "Correr por 30 min", LocalDate.now(), usuarioTeste.getId(), null);
        novoHabito.setDiasDaSemana(Set.of(DayOfWeek.TUESDAY)); // Necessário para passar na validação implícita da UI
        Habit habitoSalvoComId = new Habit(2, "Correr", "Correr por 30 min", LocalDate.now(), usuarioTeste.getId(), null);
        habitoSalvoComId.setId(2); // Simulando que o DAO atribuiu um ID

        when(habitDAOMock.addHabit(any(Habit.class))).thenReturn(habitoSalvoComId);

        Habit resultado = habitService.addHabit(novoHabito);

        assertNotNull(resultado);
        assertEquals(2, resultado.getId());
        assertEquals("Correr", resultado.getName());
        verify(habitDAOMock, times(1)).addHabit(novoHabito);
    }

    @Test
    @DisplayName("Não deve adicionar hábito com nome vazio e deve lançar ValidationException")
    void addHabit_quandoNomeVazio_deveLancarValidationException() {
        Habit habitoInvalido = new Habit(" ", "Desc", LocalDate.now(), usuarioTeste.getId(), null);
        Exception exception = assertThrows(ValidationException.class, () -> {
            habitService.addHabit(habitoInvalido);
        });
        assertEquals("Dados do hábito inválidos: nome não pode ser vazio.", exception.getMessage());
        verify(habitDAOMock, never()).addHabit(any(Habit.class));
    }

    @Test
    @DisplayName("Deve buscar hábito por ID com sucesso")
    void getHabitById_quandoIdExistente_deveRetornarHabito() throws HabitNotFoundException, PersistenceException {
        when(habitDAOMock.getHabitById(habitoTeste.getId())).thenReturn(habitoTeste);
        Habit resultado = habitService.getHabitById(habitoTeste.getId());
        assertNotNull(resultado);
        assertEquals(habitoTeste.getId(), resultado.getId());
    }

    @Test
    @DisplayName("Deve lançar HabitNotFoundException ao buscar hábito com ID inválido (<=0)")
    void getHabitById_quandoIdInvalido_deveLancarHabitNotFoundException() {
        Exception exception = assertThrows(HabitNotFoundException.class, () -> {
            habitService.getHabitById(0);
        });
        assertTrue(exception.getMessage().contains("ID do hábito inválido: 0"));
    }
    
    @Test
    @DisplayName("Deve listar hábitos de um usuário")
    void getHabitsByUserId_quandoUsuarioExistente_deveRetornarListaDeHabitos() throws PersistenceException, UserNotFoundException {
        // Primeiro, garantir que o usuário existe
        when(usuarioDAOMock.getUsuarioById(usuarioTeste.getId())).thenReturn(usuarioTeste);
        // Depois, simular o retorno dos hábitos para esse usuário
        List<Habit> listaHabitos = List.of(habitoTeste);
        when(habitDAOMock.getHabitsByUserId(usuarioTeste.getId())).thenReturn(listaHabitos);

        List<Habit> resultado = habitService.getHabitsByUserId(usuarioTeste.getId());

        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals(habitoTeste.getName(), resultado.get(0).getName());
        verify(usuarioDAOMock, times(1)).getUsuarioById(usuarioTeste.getId()); // Verifica se o usuário foi buscado
        verify(habitDAOMock, times(1)).getHabitsByUserId(usuarioTeste.getId());
    }

    @Test
    @DisplayName("Deve retornar lista vazia se usuário não tiver hábitos")
    void getHabitsByUserId_quandoUsuarioSemHabitos_deveRetornarListaVazia() throws PersistenceException, UserNotFoundException {
        when(usuarioDAOMock.getUsuarioById(usuarioTeste.getId())).thenReturn(usuarioTeste);
        when(habitDAOMock.getHabitsByUserId(usuarioTeste.getId())).thenReturn(Collections.emptyList());

        List<Habit> resultado = habitService.getHabitsByUserId(usuarioTeste.getId());

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve atualizar um hábito com sucesso")
    void updateHabit_quandoHabitoValido_deveRetornarHabitoAtualizado() throws PersistenceException, ValidationException, HabitNotFoundException {
        Habit habitoParaAtualizar = new Habit(habitoTeste.getId(), "Nome Atualizado", "Desc Atualizada", habitoTeste.getCreationDate(), usuarioTeste.getId(), null);
        
        // Simula que o hábito original existe
        when(habitDAOMock.getHabitById(habitoTeste.getId())).thenReturn(habitoTeste); 
        // Simula o sucesso da atualização no DAO
        when(habitDAOMock.updateHabit(habitoParaAtualizar)).thenReturn(true);
        // Simula a busca do hábito atualizado (o getHabitById é chamado internamente após o update)
        when(habitDAOMock.getHabitById(habitoParaAtualizar.getId())).thenReturn(habitoParaAtualizar);


        Habit resultado = habitService.updateHabit(habitoParaAtualizar);

        assertNotNull(resultado);
        assertEquals("Nome Atualizado", resultado.getName());
        verify(habitDAOMock, times(1)).updateHabit(habitoParaAtualizar);
        // O getHabitById é chamado duas vezes: uma para verificar a existência antes do update, e outra para retornar o objeto atualizado.
        verify(habitDAOMock, times(2)).getHabitById(habitoTeste.getId()); 
    }
    
    @Test
    @DisplayName("Não deve permitir atualizar o usuarioId de um hábito")
    void updateHabit_quandoTentaMudarUsuarioId_deveLancarValidationException() throws PersistenceException, HabitNotFoundException {
        Habit habitoExistente = new Habit(1, "Hábito Original", "Desc", LocalDate.now(), 1, null); // Pertence ao usuário 1
        Habit habitoComUsuarioDiferente = new Habit(1, "Hábito Modificado", "Desc", LocalDate.now(), 2, null); // Tentando mudar para usuário 2

        when(habitDAOMock.getHabitById(1)).thenReturn(habitoExistente); // Simula que o hábito existe

        Exception exception = assertThrows(ValidationException.class, () -> {
            habitService.updateHabit(habitoComUsuarioDiferente);
        });
        assertEquals("Não é permitido alterar o proprietário do hábito.", exception.getMessage());
        verify(habitDAOMock, never()).updateHabit(any(Habit.class));
    }


    @Test
    @DisplayName("Deve deletar um hábito com sucesso")
    void deleteHabit_quandoHabitoExistente_deveRetornarTrue() throws PersistenceException, HabitNotFoundException {
        when(habitDAOMock.getHabitById(habitoTeste.getId())).thenReturn(habitoTeste); // Garante que getHabitById encontre o hábito
        when(habitDAOMock.deleteHabit(habitoTeste.getId())).thenReturn(true);

        boolean resultado = habitService.deleteHabit(habitoTeste.getId());

        assertTrue(resultado);
        verify(habitDAOMock, times(1)).deleteHabit(habitoTeste.getId());
    }

    @Test
    @DisplayName("Deve lançar HabitNotFoundException ao tentar deletar hábito não existente")
    void deleteHabit_quandoHabitoNaoExistente_deveLancarHabitNotFoundException() throws PersistenceException {
        int idNaoExistente = 99;
        when(habitDAOMock.getHabitById(idNaoExistente)).thenReturn(null); // Simula que o hábito não é encontrado

        assertThrows(HabitNotFoundException.class, () -> {
            habitService.deleteHabit(idNaoExistente);
        });
        verify(habitDAOMock, never()).deleteHabit(idNaoExistente);
    }

    // --- Testes para Marcar Hábito Como Feito e Pontuação ---

    @Test
    @DisplayName("Deve marcar hábito como feito pela primeira vez e dar pontos base")
    void marcarHabitoComoFeito_primeiraVez_deveRetornarFeedbackComPontosBase() throws UserNotFoundException, HabitNotFoundException, PersistenceException, ValidationException {
        LocalDate hoje = LocalDate.now();
        // ProgressoDiario novoProgresso = new ProgressoDiario(usuarioTeste.getId(), habitoTeste.getId(), hoje, true); // Não é usado diretamente
        ProgressoDiario progressoSalvo = new ProgressoDiario(1, usuarioTeste.getId(), habitoTeste.getId(), hoje, true); // Com ID

        // CAPTURE O ESTADO INICIAL DOS PONTOS
        int pontosIniciaisUsuarioTeste = usuarioTeste.getPontos(); // Deveria ser 100 do setUp

        // Crie uma nova instância para o mock retornar, para evitar modificar o this.usuarioTeste original
        Usuario usuarioRetornadoPeloMock = new Usuario(usuarioTeste.getId(), usuarioTeste.getNome(), usuarioTeste.getPontos());

        when(usuarioDAOMock.getUsuarioById(usuarioTeste.getId())).thenReturn(usuarioRetornadoPeloMock);
        when(habitDAOMock.getHabitById(habitoTeste.getId())).thenReturn(habitoTeste);
        when(progressoDiarioDAOMock.getProgresso(usuarioTeste.getId(), habitoTeste.getId(), hoje)).thenReturn(null); // Nenhum progresso anterior
        when(progressoDiarioDAOMock.addProgresso(any(ProgressoDiario.class))).thenReturn(progressoSalvo);
        when(progressoDiarioDAOMock.getProgresso(eq(usuarioTeste.getId()), eq(habitoTeste.getId()), eq(hoje.minusDays(1)))).thenReturn(null); // Sem sequência anterior
        when(usuarioDAOMock.updatePontosUsuario(eq(usuarioTeste.getId()), anyInt())).thenReturn(true);

        FeedbackMarcacaoDTO feedback = habitService.marcarHabitoComoFeito(usuarioTeste.getId(), habitoTeste.getId(), hoje);

        assertTrue(feedback.isSucesso());
        assertEquals(10, feedback.getPontosGanhosNestaMarcacao()); // PONTOS_BASE_POR_HABITO = 10
        
        // USE O ESTADO INICIAL NA ASSERÇÃO
        assertEquals(pontosIniciaisUsuarioTeste + 10, feedback.getTotalPontosUsuarioAposMarcacao()); // Deve ser 100 + 10 = 110
        
        assertTrue(feedback.getMensagem().contains("Hábito marcado como feito! Sequência: 1x"));
        verify(progressoDiarioDAOMock, times(1)).addProgresso(any(ProgressoDiario.class));
        
        // Verifique se updatePontosUsuario foi chamado com o valor correto
        verify(usuarioDAOMock, times(1)).updatePontosUsuario(usuarioTeste.getId(), pontosIniciaisUsuarioTeste + 10);
    }

    @Test
    @DisplayName("Deve marcar hábito como feito com sequência e dar pontos multiplicados")
    void marcarHabitoComoFeito_comSequencia_deveRetornarFeedbackComPontosMultiplicados() throws UserNotFoundException, HabitNotFoundException, PersistenceException, ValidationException {
        LocalDate hoje = LocalDate.now();
        LocalDate ontem = hoje.minusDays(1);
        ProgressoDiario progressoOntem = new ProgressoDiario(1, usuarioTeste.getId(), habitoTeste.getId(), ontem, true);
        ProgressoDiario novoProgressoHoje = new ProgressoDiario(usuarioTeste.getId(), habitoTeste.getId(), hoje, true);
        ProgressoDiario progressoSalvoHoje = new ProgressoDiario(2, usuarioTeste.getId(), habitoTeste.getId(), hoje, true);


        when(usuarioDAOMock.getUsuarioById(usuarioTeste.getId())).thenReturn(new Usuario(usuarioTeste.getId(), usuarioTeste.getNome(), usuarioTeste.getPontos())); // Clonar para não afetar outros testes
        when(habitDAOMock.getHabitById(habitoTeste.getId())).thenReturn(habitoTeste);
        when(progressoDiarioDAOMock.getProgresso(usuarioTeste.getId(), habitoTeste.getId(), hoje)).thenReturn(null); // Nenhum progresso hoje
        when(progressoDiarioDAOMock.addProgresso(any(ProgressoDiario.class))).thenReturn(progressoSalvoHoje);
        // Simular sequência
        when(progressoDiarioDAOMock.getProgresso(eq(usuarioTeste.getId()), eq(habitoTeste.getId()), eq(ontem))).thenReturn(progressoOntem);
        when(progressoDiarioDAOMock.getProgresso(eq(usuarioTeste.getId()), eq(habitoTeste.getId()), eq(ontem.minusDays(1)))).thenReturn(null); // Fim da sequência
        when(usuarioDAOMock.updatePontosUsuario(eq(usuarioTeste.getId()), anyInt())).thenReturn(true);

        FeedbackMarcacaoDTO feedback = habitService.marcarHabitoComoFeito(usuarioTeste.getId(), habitoTeste.getId(), hoje);

        assertTrue(feedback.isSucesso());
        assertEquals(20, feedback.getPontosGanhosNestaMarcacao()); // 10 * 2 (sequência)
        assertEquals(usuarioTeste.getPontos() + 20, feedback.getTotalPontosUsuarioAposMarcacao());
        assertTrue(feedback.getMensagem().contains("Hábito marcado como feito! Sequência: 2x"));
    }
    
    @Test
    @DisplayName("Deve retornar mensagem se hábito já estiver marcado como feito para a data")
    void marcarHabitoComoFeito_quandoJaMarcadoComoFeito_deveRetornarMensagemApropriada() throws UserNotFoundException, HabitNotFoundException, PersistenceException, ValidationException {
        LocalDate hoje = LocalDate.now();
        ProgressoDiario progressoExistenteFeito = new ProgressoDiario(1, usuarioTeste.getId(), habitoTeste.getId(), hoje, true);

        when(usuarioDAOMock.getUsuarioById(usuarioTeste.getId())).thenReturn(usuarioTeste);
        when(habitDAOMock.getHabitById(habitoTeste.getId())).thenReturn(habitoTeste);
        when(progressoDiarioDAOMock.getProgresso(usuarioTeste.getId(), habitoTeste.getId(), hoje)).thenReturn(progressoExistenteFeito);

        FeedbackMarcacaoDTO feedback = habitService.marcarHabitoComoFeito(usuarioTeste.getId(), habitoTeste.getId(), hoje);

        assertTrue(feedback.isSucesso());
        assertEquals(0, feedback.getPontosGanhosNestaMarcacao());
        assertEquals(usuarioTeste.getPontos(), feedback.getTotalPontosUsuarioAposMarcacao());
        assertEquals("Hábito já estava marcado como feito para esta data.", feedback.getMensagem());
        verify(progressoDiarioDAOMock, never()).addProgresso(any(ProgressoDiario.class));
        verify(usuarioDAOMock, never()).updatePontosUsuario(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Deve lançar ValidationException se hábito já estiver marcado como NÃO CUMPRIDO para a data")
    void marcarHabitoComoFeito_quandoJaMarcadoComoNaoCumprido_deveLancarValidationException() throws UserNotFoundException, HabitNotFoundException, PersistenceException {
        LocalDate hoje = LocalDate.now();
        // Usuário e hábito existem
        when(usuarioDAOMock.getUsuarioById(usuarioTeste.getId())).thenReturn(usuarioTeste);
        when(habitDAOMock.getHabitById(habitoTeste.getId())).thenReturn(habitoTeste);
        // Progresso existe e está como NÃO CUMPRIDO
        ProgressoDiario progressoExistenteNaoCumprido = new ProgressoDiario(1, usuarioTeste.getId(), habitoTeste.getId(), hoje, false);
        when(progressoDiarioDAOMock.getProgresso(usuarioTeste.getId(), habitoTeste.getId(), hoje)).thenReturn(progressoExistenteNaoCumprido);

        Exception exception = assertThrows(ValidationException.class, () -> {
            habitService.marcarHabitoComoFeito(usuarioTeste.getId(), habitoTeste.getId(), hoje);
        });
        assertEquals("Hábito já registrado como 'não cumprido' para esta data. Alteração não suportada por esta ação.", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar ValidationException se tentar marcar hábito de outro usuário")
    void marcarHabitoComoFeito_quandoHabitoNaoPertenceAoUsuario_deveLancarValidationException() throws UserNotFoundException, HabitNotFoundException, PersistenceException {
        LocalDate hoje = LocalDate.now();
        Usuario outroUsuario = new Usuario(2, "Outro Usuário", 50);
        Habit habitoDoOutroUsuario = new Habit(2, "Hábito do Outro", "Desc", LocalDate.now(), outroUsuario.getId(), null);

        when(usuarioDAOMock.getUsuarioById(usuarioTeste.getId())).thenReturn(usuarioTeste); // Usuário que está tentando marcar
        when(habitDAOMock.getHabitById(habitoDoOutroUsuario.getId())).thenReturn(habitoDoOutroUsuario); // Hábito que pertence a outro

        Exception exception = assertThrows(ValidationException.class, () -> {
            habitService.marcarHabitoComoFeito(usuarioTeste.getId(), habitoDoOutroUsuario.getId(), hoje);
        });
        assertEquals("Este hábito não pertence ao usuário especificado.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve retornar a pontuação do usuário corretamente")
    void getPontuacaoUsuario_quandoUsuarioExistente_deveRetornarPontos() throws UserNotFoundException, PersistenceException {
        when(usuarioDAOMock.getUsuarioById(usuarioTeste.getId())).thenReturn(usuarioTeste);
        int pontuacao = habitService.getPontuacaoUsuario(usuarioTeste.getId());
        assertEquals(usuarioTeste.getPontos(), pontuacao);
    }

}