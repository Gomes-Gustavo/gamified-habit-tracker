package com.habitracker.serviceapi;

import java.time.LocalDate;
import java.util.List;

import com.habitracker.model.Conquista; // Se precisar retornar/passar Usuario completo
import com.habitracker.model.Habit; // Você vai criar este DTO
import com.habitracker.serviceapi.dto.FeedbackMarcacaoDTO; // Você vai criar esta Exceção
import com.habitracker.serviceapi.exceptions.HabitNotFoundException;   // Você vai criar esta Exceção
import com.habitracker.serviceapi.exceptions.PersistenceException;  // Você vai criar esta Exceção
import com.habitracker.serviceapi.exceptions.UserNotFoundException;    // Você vai criar esta Exceção
import com.habitracker.serviceapi.exceptions.ValidationException;

public interface HabitTrackerServiceAPI {

    // --- Métodos de Hábito (CRUD) ---
    List<Habit> getAllHabits();

    Habit addHabit(Habit habit) throws PersistenceException, ValidationException;

    Habit updateHabit(Habit habitToUpdate) throws HabitNotFoundException, PersistenceException, ValidationException;

    boolean deleteHabit(int habitId) throws HabitNotFoundException, PersistenceException;

    Habit getHabitById(int habitId) throws HabitNotFoundException; // Adicionado, pois é útil

    // --- Métodos de Gamificação e Progresso ---
    FeedbackMarcacaoDTO marcarHabitoComoFeito(int usuarioId, int habitoId, LocalDate data)
            throws UserNotFoundException, HabitNotFoundException, PersistenceException, ValidationException;

    int getPontuacaoUsuario(int usuarioId) throws UserNotFoundException;

    List<Conquista> getConquistasDesbloqueadasUsuario(int usuarioId) throws UserNotFoundException;

    List<Conquista> getAllConquistasPossiveis(); // Definições de todas as conquistas

    // --- Métodos de Usuário (Exemplo, se necessário diretamente pela UI) ---
    // Usuario addUsuario(Usuario usuario) throws PersistenceException, ValidationException;
    // Usuario getUsuarioById(int usuarioId) throws UserNotFoundException;
    // List<Usuario> getAllUsuarios();
}