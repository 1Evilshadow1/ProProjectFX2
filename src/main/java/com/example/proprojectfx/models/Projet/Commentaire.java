package com.example.proprojectfx.models.Projet;

import java.time.LocalDate;
import com.example.proprojectfx.models.User.Membre;
import com.example.proprojectfx.models.Exception.*;


public class Commentaire {
    protected String id;
    protected String contenue;
    protected Membre auteur;
    protected LocalDate dateCreation;
    protected Tache tacheliee;

    public Commentaire(String id, String contenue , Membre auteur, Tache tacheliee) {
        this.id = id;
        this.contenue = contenue;
        this.auteur = auteur;
        this.tacheliee = tacheliee;
        this.dateCreation = LocalDate.now();
    }

    public void editer(String nouveauContenue) throws ParametreInvalideException {
        if (nouveauContenue == null || nouveauContenue.isBlank()) {
            throw new ParametreInvalideException("Contenu du commentaire ne peut pas être vide");
        }
        this.contenue = nouveauContenue;
    }

    public Membre getAuteur(){
        return auteur;
    }

    public String getContenue(){
        return contenue;
    }

    public void afficherDetailsCommentaire() {
        System.out.println("ID Commentaire: " + id);
        System.out.println("Contenu: " + contenue);
        System.out.println("Auteur: " + auteur.getName() + " " + auteur.getPrenom());
        System.out.println("Date de création: " + dateCreation);
        System.out.println("Tâche liée ID: " + tacheliee.getInfo());
    }
}