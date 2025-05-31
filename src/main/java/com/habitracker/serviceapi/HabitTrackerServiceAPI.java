package com.habitracker.serviceapi;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// import com.habitracker.model.Conquista; // REMOVIDO
import com.habitracker.model.Habit;
import com.habitracker.model.Usuario;
import com.habitracker.model.ProgressoDiario;
import com.habitracker.serviceapi.dto.FeedbackMarcacaoDTO; // Certifique-se que este DTO também foi ajustado
import com.habitracker.serviceapi.exceptions.HabitNotFoundException;
import com.habitracker.serviceapi.exceptions.PersistenceException;
import com.habitracker.serviceapi.exceptions.UserNotFoundException;
import com.habitracker.serviceapi.exceptions.ValidationException;

public interface HabitTrackerServiceAPI {

    // --- Métodos de Hábito (CRUD) ---
    List<Habit> getAllHabits() throws PersistenceException;
    List<Habit> getHabitsByUserId(int userId) throws PersistenceException, UserNotFoundException;
    Habit addHabit(Habit habit) throws PersistenceException, ValidationException;
    Habit updateHabit(Habit habitToUpdate) throws HabitNotFoundException, PersistenceException, ValidationException;
    boolean deleteHabit(int habitId) throws HabitNotFoundException, PersistenceException;
    Habit getHabitById(int habitId) throws HabitNotFoundException, PersistenceException;

    // --- Métodos de Gamificação e Progresso ---
    FeedbackMarcacaoDTO marcarHabitoComoFeito(int usuarioId, int habitoId, LocalDate data)
            throws UserNotFoundException, HabitNotFoundException, PersistenceException, ValidationException;
    int getPontuacaoUsuario(int usuarioId) throws UserNotFoundException, PersistenceException;
    // List<Conquista> getConquistasDesbloqueadasUsuario(int usuarioId) throws UserNotFoundException, PersistenceException; // REMOVIDO
    // List<Conquista> getAllConquistasPossiveis() throws PersistenceException; // REMOVIDO

    int getSequenciaEfetivaTerminadaEm(int usuarioId, int habitoId, LocalDate dataLimite) 
            throws PersistenceException, UserNotFoundException, HabitNotFoundException; // Adicione exceções que o método pode lançar


    // Para status de hábitos em um dia
    Map<Integer, Boolean> getStatusHabitosPorDia(int usuarioId, List<Integer> habitIds, LocalDate data)
            throws PersistenceException, UserNotFoundException;

    // Para dados do calendário
    List<ProgressoDiario> getProgressoDiarioDoMes(int usuarioId, int ano, int mes)
            throws UserNotFoundException, PersistenceException;

    // --- Métodos de Usuário ---
    Usuario getUsuarioById(int usuarioId) throws UserNotFoundException, PersistenceException;
    Usuario addUsuario(String nome) throws PersistenceException, ValidationException;

    // Se você adicionar a funcionalidade de Objetivos aqui, as assinaturas viriam abaixo:
    // Exemplo:
    // List<Objetivo> getObjetivosDoUsuario(int usuarioId) throws UserNotFoundException, PersistenceException;
    // Objetivo addObjetivo(Objetivo objetivo) throws PersistenceException, ValidationException;
    // etc.
}