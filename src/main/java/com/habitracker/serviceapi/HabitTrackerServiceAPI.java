package com.habitracker.serviceapi;

import java.time.LocalDate;
import java.util.List;

import com.habitracker.model.Conquista;
import com.habitracker.model.Habit;
import com.habitracker.model.Usuario;
import com.habitracker.serviceapi.dto.FeedbackMarcacaoDTO;
import com.habitracker.serviceapi.exceptions.HabitNotFoundException;
import com.habitracker.serviceapi.exceptions.PersistenceException;
import com.habitracker.serviceapi.exceptions.UserNotFoundException;
import com.habitracker.serviceapi.exceptions.ValidationException;

public interface HabitTrackerServiceAPI {

    // --- Métodos de Hábito (CRUD) ---
    List<Habit> getAllHabits() throws PersistenceException; // Adicionado PersistenceException

    List<Habit> getHabitsByUserId(int userId) throws PersistenceException, UserNotFoundException;

    Habit addHabit(Habit habit) throws PersistenceException, ValidationException;

    Habit updateHabit(Habit habitToUpdate) throws HabitNotFoundException, PersistenceException, ValidationException;

    boolean deleteHabit(int habitId) throws HabitNotFoundException, PersistenceException;

    // CORREÇÃO 3: Adicionado PersistenceException aqui
    Habit getHabitById(int habitId) throws HabitNotFoundException, PersistenceException;

    // --- Métodos de Gamificação e Progresso ---
    FeedbackMarcacaoDTO marcarHabitoComoFeito(int usuarioId, int habitoId, LocalDate data)
            throws UserNotFoundException, HabitNotFoundException, PersistenceException, ValidationException;

    int getPontuacaoUsuario(int usuarioId) throws UserNotFoundException, PersistenceException;

    List<Conquista> getConquistasDesbloqueadasUsuario(int usuarioId) throws UserNotFoundException, PersistenceException;

    List<Conquista> getAllConquistasPossiveis() throws PersistenceException;

    // --- Métodos de Usuário ---
    Usuario getUsuarioById(int usuarioId) throws UserNotFoundException, PersistenceException;

    Usuario addUsuario(String nome) throws PersistenceException, ValidationException;

    // List<Usuario> getAllUsuarios() throws PersistenceException;
}