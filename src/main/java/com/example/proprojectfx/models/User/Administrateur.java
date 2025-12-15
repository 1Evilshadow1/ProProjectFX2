package com.example.proprojectfx.models.User;
import java.util.List;
import com.example.proprojectfx.models.Probleme.Probleme;


public final  class Administrateur extends User {
    protected List <Membre> Membres;


    public Administrateur( String id , String nom, String prenom, String email, String mdp,String role,String type ,List<Membre> Membres ) {
        super(id, nom, prenom, email, mdp,role,type);
        this.Membres=Membres;

    }
    public List<Membre> getMembres() {
        return Membres;
    }

}
