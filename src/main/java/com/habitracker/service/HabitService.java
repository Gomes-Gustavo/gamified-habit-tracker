package com.habitracker.service;

import com.habitracker.model.Habit;
import java.util.List;

public interface HabitService {

    List<Habit> getAllHabits();

    Habit addHabit(Habit habit);

    Habit updateHabit(Habit habitToUpdate);

    boolean deleteHabit(long habitId);
}