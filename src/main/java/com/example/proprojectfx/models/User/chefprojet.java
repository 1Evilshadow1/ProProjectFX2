package com.example.proprojectfx.models.User;

import com.example.proprojectfx.models.Projet.Project;
import com.example.proprojectfx.models.Projet.Tache;


import java.util.List;

public final class chefprojet extends User {
    protected List<Project> projetAGerer;

    public chefprojet(String id, String nom, String prenom, String email, String mdp,String role,String type, List<Project> projetAGerer) {
        super(id, nom, prenom, email, mdp,role,type);
        this.projetAGerer = projetAGerer;
    }

    public List<Project> getProjetAGerer() {
        return projetAGerer;
    }

    public void creerProjet(Project p) {
        projetAGerer.add(p);
    }

    public void supprimerProjet(Project p) {
        projetAGerer.remove(p);
    }
    public void assignerMembreAuProjet(Project p, Membre m) {
        p.ajouterMembre(m);
    }
    public void ajouterMembreTache(Membre m , Tache t ){
        m.tachesAssignes.add(t);
    }

}
