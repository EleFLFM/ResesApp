package com.example.reses;

public class GettersReses {
    private String Id;
    private String Nombre;
    private String TipoBovino;
    public GettersReses(String id, String nombre,String tipoBovino) {
        this.Id = id;
        this.Nombre = nombre;
        this.TipoBovino=tipoBovino;
    }

    public String getId() {
        return Id;
    }
    public String getNombre() {
        return Nombre;
    }
    public String getTipoBovino() {
        return TipoBovino;
    }
}
