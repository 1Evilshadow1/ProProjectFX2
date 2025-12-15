package com.example.proprojectfx.models.Probleme;

import java.time.LocalDate;
import com.example.proprojectfx.models.User.*;
import com.example.proprojectfx.models.Exception.*;

public final class ProblemeSysteme extends Probleme {

    private Administrateur adminAssigne;

    public ProblemeSysteme(String id, String message, Administrateur adminAssigne)
            throws ParametreInvalideException {
        super(id, message);
        if (adminAssigne == null) {
            throw new ParametreInvalideException("Administrateur assigné obligatoire");
        }
        this.adminAssigne = adminAssigne;
    }

    @Override
    public void updateStatutProbleme() {
        this.statut = StatutProbleme.Resolu;
        this.dateResolution = LocalDate.now();
    }

    public Administrateur getAdminAssigne() {
        return adminAssigne;
    }
}