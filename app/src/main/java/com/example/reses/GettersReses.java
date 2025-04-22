package com.example.reses;

public class GettersReses {
    private String id;
    private String nombre;
    private String sexo;
    private String tipoRes; // Firebase field
    private String fechaNacimiento;
    private String madre;
    private String padre;
    private String usuarioFk;

    // Constructor for Firebase data
    public GettersReses(String id, String nombre, String sexo, String tipoRes,
                        String fechaNacimiento, String madre, String padre) {
        this.id = id;
        this.nombre = nombre;
        this.sexo = sexo;
        this.tipoRes = tipoRes;
        this.fechaNacimiento = fechaNacimiento;
        this.madre = madre;
        this.padre = padre;
    }

    // Constructor for simplified usage
    public GettersReses(String id, String nombre, String sexo) {
        this.id = id;
        this.nombre = nombre;
        this.sexo = sexo;
    }

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getSexo() { return sexo; }

    // For backward compatibility with your existing code
    public String getTipoBovino() {
        return sexo; // Now returns sexo instead of tipoRes
    }

    public String getTipoRes() { return tipoRes; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public String getMadre() { return madre; }
    public String getPadre() { return padre; }
    public String getUsuarioFk() { return usuarioFk; }
}