package com.habitracker.serviceapi;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


import com.habitracker.model.Habit;
import com.habitracker.model.Usuario;
import com.habitracker.model.ProgressoDiario;
import com.habitracker.serviceapi.dto.FeedbackMarcacaoDTO; 
import com.habitracker.serviceapi.exceptions.HabitNotFoundException;
import com.habitracker.serviceapi.exceptions.PersistenceException;
import com.habitracker.serviceapi.exceptions.UserNotFoundException;
import com.habitracker.serviceapi.exceptions.ValidationException;

public interface HabitTrackerServiceAPI {

    
    List<Habit> getAllHabits() throws PersistenceException;
    List<Habit> getHabitsByUserId(int userId) throws PersistenceException, UserNotFoundException;
    Habit addHabit(Habit habit) throws PersistenceException, ValidationException;
    Habit updateHabit(Habit habitToUpdate) throws HabitNotFoundException, PersistenceException, ValidationException;
    boolean deleteHabit(int habitId) throws HabitNotFoundException, PersistenceException;
    Habit getHabitById(int habitId) throws HabitNotFoundException, PersistenceException;

    
    FeedbackMarcacaoDTO marcarHabitoComoFeito(int usuarioId, int habitoId, LocalDate data)
            throws UserNotFoundException, HabitNotFoundException, PersistenceException, ValidationException;
    int getPontuacaoUsuario(int usuarioId) throws UserNotFoundException, PersistenceException;
    
    

    int getSequenciaEfetivaTerminadaEm(int usuarioId, int habitoId, LocalDate dataLimite) 
            throws PersistenceException, UserNotFoundException, HabitNotFoundException; 


    
    Map<Integer, Boolean> getStatusHabitosPorDia(int usuarioId, List<Integer> habitIds, LocalDate data)
            throws PersistenceException, UserNotFoundException;

    
    List<ProgressoDiario> getProgressoDiarioDoMes(int usuarioId, int ano, int mes)
            throws UserNotFoundException, PersistenceException;

    
    Usuario getUsuarioById(int usuarioId) throws UserNotFoundException, PersistenceException;
    Usuario addUsuario(String nome) throws PersistenceException, ValidationException;

}