package com.example.proprojectfx.models.User;

import java.util.List;
import com.example.proprojectfx.models.Projet.Tache;

public final  class Membre extends User {
    List<Tache> tachesAssignes;
    String dispo;

    public Membre(String id, String nom, String prenom, String email, String mdp,String role,String type, String dispo, List<Tache> tachesAssignes) {
        super(id, nom, prenom, email, mdp,role,type);
        this.tachesAssignes = tachesAssignes;
        this.dispo = dispo;
    }

    public String getid() {
        return getId();
    }

    public List<Tache> getTaches() {
        return tachesAssignes;
    }

    public String getDispo() {
        return dispo;
    }

    public int getNbrTache(){return tachesAssignes.size() ;}




}
