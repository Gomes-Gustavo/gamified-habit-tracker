package com.habitracker.service;

import com.habitracker.model.Habit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class MockHabitService implements HabitService {

    private List<Habit> habits;
    private AtomicLong nextId = new AtomicLong(1);

    public MockHabitService() {
        this.habits = new ArrayList<>();
        initializeDefaultHabits();
    }

    private void initializeDefaultHabits() {
        addHabitInternal(new Habit("Drink 2L of water", "Stay hydrated throughout the day."));
        addHabitInternal(new Habit("Exercise for 30 minutes", "Any kind of physical activity."));
        addHabitInternal(new Habit("Read for 15 minutes", "A book, an article, or anything."));
    }

    private Habit addHabitInternal(Habit habit) {
        habit.setId(nextId.getAndIncrement());
        this.habits.add(habit);
        return habit;
    }

    @Override
    public List<Habit> getAllHabits() {
        return new ArrayList<>(this.habits);
    }

    @Override
    public Habit addHabit(Habit habit) {
        if (habit == null) {
            return null;
        }
        if (habit.getName() == null || habit.getName().trim().isEmpty()) {
            return null;
        }
        return addHabitInternal(new Habit(habit.getName(), habit.getDescription()));
    }

    @Override
    public Habit updateHabit(Habit habitToUpdate) {
        if (habitToUpdate == null || habitToUpdate.getId() == 0) {
            return null;
        }
        for (Habit existingHabit : this.habits) {
            if (existingHabit.getId() == habitToUpdate.getId()) {
                existingHabit.setName(habitToUpdate.getName());
                existingHabit.setDescription(habitToUpdate.getDescription());
                return existingHabit;
            }
        }
        return null;
    }

    @Override
    public boolean deleteHabit(long habitId) {
        return this.habits.removeIf(habit -> habit.getId() == habitId);
    }
}