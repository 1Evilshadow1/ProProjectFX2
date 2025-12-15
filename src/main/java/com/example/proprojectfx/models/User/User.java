package com.example.proprojectfx.models.User;
import java.time.LocalDateTime;


public sealed class User permits chefprojet,Membre,Administrateur  {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    private String mdp;
    private LocalDateTime dateCreation;
    public RoleUser role;
    private TypeUser type;

    //methods
    public User(String id , String nom, String prenom, String email, String mdp,String Role,String type){
        this.id=id;
        this.nom=nom;
        this.prenom=prenom;
        this.email=email;
        this.mdp=mdp;
        this.dateCreation=LocalDateTime.now();
        this.role=RoleUser.valueOf(Role);
        this.type= TypeUser.valueOf(type);


    }
    public String getId(){
        return id;
    }
    public String getName(){
        return nom;
    }
    public String getPrenom(){
        return prenom;
    }
    public String getEmail(){
        return  email;
    }
    public String getMdp(){
        return mdp;
    }
    public void ChangerMDP(String mdp){
        this.mdp=mdp;
    }
}


