package com.example.proprojectfx.models.Projet;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.example.proprojectfx.models.User.Membre;

public record Rapport(
        String id,
        Project project,
        LocalDate dateGeneration,
        Map<TaskStatue, Integer> statistiquesTaches,
        List<Membre> membresActifs,
        double tauxAvancement
) {

    // Constructeur compact avec validation
    public Rapport {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID ne peut pas être vide");
        }
        if (project == null) {
            throw new IllegalArgumentException("Project ne peut pas être null");
        }
        if (tauxAvancement < 0 || tauxAvancement > 100) {
            throw new IllegalArgumentException("Taux d'avancement doit être entre 0 et 100");
        }
    }

    // Constructeur
    public Rapport(String id, Project project,
                   Map<TaskStatue, Integer> statistiquesTaches,
                   List<Membre> membresActifs, double tauxAvancement) {
        this(id, project, LocalDate.now(), statistiquesTaches, membresActifs, tauxAvancement);
    }


    //Méthode pour obtenir le nombre total de tâches
    public int getNombreTotalTaches() {
        int total = 0;
        for (int v : statistiquesTaches.values()) total += v;
        return total;
    }

    // Méthode pour vérifier si le projet est terminé
    public boolean isProjetTermine() {
        return tauxAvancement >= 100.0;
    }
}

