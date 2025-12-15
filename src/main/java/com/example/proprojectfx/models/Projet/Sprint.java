package com.example.proprojectfx.models.Projet;

import java.time.LocalDate;
import java.util.List;
import com.example.proprojectfx.models.User.chefprojet;

public class Sprint {
    protected String idSprint;
    protected String objectif;
    protected LocalDate dateDebut;
    protected LocalDate dateFin;
    protected SprintStatut statut;
    protected Project projet;
    protected chefprojet chefprojet;
    protected List<Tache> taches;

    public Sprint(String idSprint, String objectif, LocalDate dateDebut,
                  LocalDate dateFin, String statut, Project projet,
                  chefprojet chefprojet, List<Tache> taches) {
        this.idSprint = idSprint;
        this.objectif = objectif;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = SprintStatut.valueOf(statut);
        this.projet = projet;
        this.chefprojet = chefprojet;
        this.taches = taches;

    }

    public double calculerAvancementSprint() {
        int totalTaches = taches.size();
        if (totalTaches == 0) return 0.0;

        long tachesCompletees = taches.stream()
                .filter(tache -> tache.getStatut() == TaskStatue.TERMINEE)
                .count();

        return (tachesCompletees / (double) totalTaches) * 100;
    }

    public void ajouterTache(Tache tache) {
        taches.add(tache);
    }

    public void retirerTache(Tache tache) {
        taches.remove(tache);
    }

    public void cloturerSprint() {
        this.statut = SprintStatut.TERMINÉ;
    }
}
