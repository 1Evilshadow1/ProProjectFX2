package com.example.proprojectfx.models.Probleme;

import java.time.LocalDate;
import  com.example.proprojectfx.models.Exception.*;
import com.example.proprojectfx.models.Probleme.*;

public sealed abstract class Probleme
        permits Bug, ProblemeSysteme {

    protected String idProbleme;
    protected String message;
    protected StatutProbleme statut;
    protected LocalDate dateApparition;
    protected LocalDate dateResolution;

    public Probleme(String idProbleme, String message) throws ParametreInvalideException {
        if (idProbleme == null || idProbleme.isBlank()) {
            throw new ParametreInvalideException("ID problème ne peut pas être vide");
        }
        if (message == null || message.isBlank()) {
            throw new ParametreInvalideException("Message ne peut pas être vide");
        }
        this.idProbleme = idProbleme;
        this.message = message;
        this.statut = StatutProbleme.NonResolu;
        this.dateApparition = LocalDate.now();
        this.dateResolution = null;
    }

    public String getId() {
        return idProbleme;
    }

    public String getMessage() {
        return message;
    }

    public StatutProbleme getStatut() {
        return statut;
    }

    public LocalDate getDateApparition() {
        return dateApparition;
    }

    public LocalDate getDateResolution() {
        return dateResolution;
    }

    public abstract void updateStatutProbleme();
}
