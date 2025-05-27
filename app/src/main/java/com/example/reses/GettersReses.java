package com.example.reses;

public class GettersReses {
    private String id;
    private String nombre;
    private String sexo;
    private String tipoRes;
    private String fechaNacimiento;
    private String madre;
    private String padre;
    private String usuarioFk;
    private String imageUrl;

    public GettersReses(String id, String nombre, String sexo, String tipoRes,
                        String fechaNacimiento, String madre, String padre, String imageUrl) {
        this.id = id;
        this.nombre = nombre;
        this.sexo = sexo;
        this.tipoRes = tipoRes;
        this.fechaNacimiento = fechaNacimiento;
        this.madre = madre;
        this.padre = padre;
        this.imageUrl = imageUrl;
    }

    public GettersReses(String id, String nombre, String sexo) {
        this.id = id;
        this.nombre = nombre;
        this.sexo = sexo;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getSexo() { return sexo; }
    public String getTipoBovino() { return sexo; }
    public String getTipoRes() { return tipoRes; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public String getMadre() { return madre; }
    public String getPadre() { return padre; }
    public String getUsuarioFk() { return usuarioFk; }
    public String getImageUrl() { return imageUrl; }

}