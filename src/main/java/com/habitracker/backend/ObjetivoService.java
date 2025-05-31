package com.habitracker.backend;

import com.habitracker.database.ObjetivoDAO;
import com.habitracker.database.HabitDAO;
import com.habitracker.model.Objetivo;
// import com.habitracker.model.Habit; // Descomente se usar
import com.habitracker.serviceapi.exceptions.PersistenceException;
import com.habitracker.serviceapi.exceptions.ValidationException;
// import com.habitracker.serviceapi.exceptions.UserNotFoundException; // Descomente se usar

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
// import java.util.stream.Collectors; // Descomente se usar

public class ObjetivoService {
    private final ObjetivoDAO objetivoDAO;
    private final HabitDAO habitDAO;

    public ObjetivoService(ObjetivoDAO objetivoDAO, HabitDAO habitDAO) {
        this.objetivoDAO = objetivoDAO;
        this.habitDAO = habitDAO;
    }

    public List<Objetivo> getObjetivosDoUsuario(int usuarioId) throws PersistenceException {
        try {
            List<Objetivo> objetivos = objetivoDAO.getObjetivosByUserId(usuarioId);
            // Lógica para carregar hábitos vinculados (se necessário) permanece a mesma
            return objetivos;
        } catch (Exception e) {
            throw new PersistenceException("Erro ao buscar objetivos do usuário.", e);
        }
    }

    public Objetivo addObjetivo(Objetivo objetivo, List<Integer> idsHabitosVinculados) throws ValidationException, PersistenceException {
        if (objetivo == null || objetivo.getNome() == null || objetivo.getNome().trim().isEmpty()) {
            throw new ValidationException("Nome do objetivo não pode ser vazio.");
        }
        if (objetivo.getUsuarioId() <= 0) {
            throw new ValidationException("ID de usuário inválido para o objetivo.");
        }
        
        // Garante data de criação e estado inicial
        objetivo.setDataCriacao(LocalDate.now()); 
        objetivo.setConcluido(false);

        // Validação opcional para dataMeta
        if (objetivo.getDataMeta() != null && objetivo.getDataMeta().isBefore(objetivo.getDataCriacao())) {
            throw new ValidationException("A data meta não pode ser anterior à data de criação.");
        }

        Objetivo objetivoSalvo = null;
        try {
            objetivoSalvo = objetivoDAO.addObjetivo(objetivo); // DAO já deve lidar com dataMeta
        } catch (Exception e) {
            throw new PersistenceException("Erro ao salvar o objetivo no banco.", e);
        }

        if (objetivoSalvo == null || objetivoSalvo.getId() <= 0) {
            throw new PersistenceException("Falha ao salvar objetivo ou obter ID gerado.");
        }

        // Lógica de vincular hábitos permanece a mesma
        if (idsHabitosVinculados != null && !idsHabitosVinculados.isEmpty()) {
            for (Integer habitoId : idsHabitosVinculados) {
                try {
                    objetivoDAO.addHabitoObjetivoLink(habitoId, objetivoSalvo.getId());
                } catch (Exception e) {
                    System.err.println("Falha ao vincular hábito ID " + habitoId + " ao objetivo ID " + objetivoSalvo.getId() + ": " + e.getMessage());
                }
            }
        }
        return objetivoSalvo; 
    }

    public Objetivo updateObjetivo(Objetivo objetivo, List<Integer> idsHabitosVinculados) throws ValidationException, PersistenceException {
        if (objetivo == null || objetivo.getId() <= 0 || objetivo.getNome() == null || objetivo.getNome().trim().isEmpty()) {
            throw new ValidationException("Dados do objetivo inválidos para atualização.");
        }
        
        Objetivo objExistente = objetivoDAO.getObjetivoById(objetivo.getId());
        if (objExistente == null) {
            throw new PersistenceException("Objetivo com ID " + objetivo.getId() + " não encontrado para atualização.");
        }
        if (objExistente.getUsuarioId() != objetivo.getUsuarioId()){
            throw new ValidationException("Não é permitido alterar o proprietário do objetivo.");
        }

        // Validação opcional para dataMeta (usa a data de criação original do objExistente)
        if (objetivo.getDataMeta() != null && objExistente.getDataCriacao() !=null && objetivo.getDataMeta().isBefore(objExistente.getDataCriacao())) {
            throw new ValidationException("A data meta não pode ser anterior à data de criação original do objetivo.");
        }
        // Se a data de criação puder ser alterada (não recomendado), use objetivo.getDataCriacao()

        boolean sucessoUpdate = false;
        try {
            // Assegure que o objetivo passado para o DAO contém a dataCriacao correta (a original)
            // e não uma nova, a menos que a edição da data de criação seja permitida.
            // Geralmente, dataCriacao não muda.
            objetivo.setDataCriacao(objExistente.getDataCriacao()); // Preserva a data de criação original
            
            sucessoUpdate = objetivoDAO.updateObjetivo(objetivo); // DAO já deve lidar com dataMeta
        } catch (Exception e) {
            throw new PersistenceException("Erro ao atualizar o objetivo no banco.", e);
        }

        if (!sucessoUpdate) {
            throw new PersistenceException("Falha ao atualizar objetivo no banco (DAO retornou false).");
        }

        // Lógica de atualizar links permanece a mesma
        try {
            objetivoDAO.removeAllHabitoLinksForObjetivo(objetivo.getId());
            if (idsHabitosVinculados != null && !idsHabitosVinculados.isEmpty()) {
                for (Integer habitoId : idsHabitosVinculados) {
                    objetivoDAO.addHabitoObjetivoLink(habitoId, objetivo.getId());
                }
            }
        } catch (Exception e) {
            throw new PersistenceException("Erro ao atualizar vínculos do objetivo.", e);
        }
        
        return objetivoDAO.getObjetivoById(objetivo.getId());
    }

    // Métodos deleteObjetivo, toggleConclusaoObjetivo, getHabitoIdsForObjetivo permanecem os mesmos
    // ... (código existente para delete, toggle, getHabitoIds) ...
    public boolean deleteObjetivo(int objetivoId, int usuarioId) throws PersistenceException {
        try {
            return objetivoDAO.deleteObjetivo(objetivoId, usuarioId);
        } catch (Exception e) {
            throw new PersistenceException("Erro ao deletar objetivo.", e);
        }
    }

    public boolean toggleConclusaoObjetivo(int objetivoId, int usuarioId) throws PersistenceException, ValidationException {
        Objetivo objetivo = objetivoDAO.getObjetivoById(objetivoId);
        if (objetivo == null) {
            throw new PersistenceException("Objetivo não encontrado para marcar como concluído.");
        }
        if (objetivo.getUsuarioId() != usuarioId) {
            throw new ValidationException("Este objetivo não pertence ao usuário atual.");
        }

        objetivo.setConcluido(!objetivo.isConcluido());
        if (objetivo.isConcluido()) {
            objetivo.setDataConclusao(LocalDate.now());
        } else {
            objetivo.setDataConclusao(null);
        }
        
        try {
            return objetivoDAO.updateObjetivo(objetivo);
        } catch (Exception e) {
            throw new PersistenceException("Erro ao atualizar status de conclusão do objetivo.", e);
        }
    }
    
    public List<Integer> getHabitoIdsForObjetivo(int objetivoId) throws PersistenceException {
        try {
            return objetivoDAO.getHabitoIdsForObjetivo(objetivoId);
        } catch (Exception e) {
            throw new PersistenceException("Erro ao buscar IDs de hábitos para o objetivo.", e);
        }
    }
}