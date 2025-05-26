// Dentro de src/com/gomesgustavo/gamifiedhabittracker/backend/HabitService.java
package com.gomesgustavo.gamifiedhabittracker.backend; // Ou .service

import com.gomesgustavo.gamifiedhabittracker.model.Habit;
import com.gomesgustavo.gamifiedhabittracker.database.HabitDAO;
import java.util.List;

public class HabitService {

    private HabitDAO habitDAO;

    public HabitService() {
        this.habitDAO = new HabitDAO(); // Instancia o DAO
    }

    public List<Habit> getAllHabits() {
        // Aqui você poderia adicionar lógica de negócio antes ou depois de chamar o DAO
        return habitDAO.getAllHabits();
    }

    public boolean addHabit(Habit habit) {
        // Aqui você poderia adicionar validações ou outra lógica de negócio
        // Ex: if (habit.getName() == null || habit.getName().isEmpty()) { return false; }
        return habitDAO.addHabit(habit);
    }

    // public void markHabitDone(int habitId /*, LocalDate date */) { /* TODO */ }
}