package com.example.proprojectfx.models.Probleme;

import java.time.LocalDate;
import com.example.proprojectfx.models.User.*;
import com.example.proprojectfx.models.Exception.*;

public final class Bug extends Probleme {

    private Membre membreAssigne;

    public Bug(String id, String message, StatutProbleme statut, Membre membreAssigne)
            throws ParametreInvalideException {
        super(id, message);
        if (membreAssigne == null) {
            throw new ParametreInvalideException("Membre assigné obligatoire");
        }

        this.membreAssigne = membreAssigne;
        this.statut = StatutProbleme.NonResolu;
        this.dateApparition = LocalDate.now();
        this.dateResolution = null;
    }

    @Override
    public void updateStatutProbleme() {
        this.statut = StatutProbleme.Resolu;
        this.dateResolution = LocalDate.now();
    }

    public void afficherDetailsBug() {
        System.out.println("ID Bug: " + idProbleme);
        System.out.println("Message: " + message);
        System.out.println("Statut: " + statut);
        System.out.println("Date d'apparition: " + dateApparition);
        System.out.println("Date de résolution: " + dateResolution);
        System.out.println("Membre assigné: " + membreAssigne.getName() + " " + membreAssigne.getPrenom());
    }
}
