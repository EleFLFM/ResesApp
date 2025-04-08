package com.example.reses;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "appreses.db";
    private static final int DATABASE_VERSION = 4;
    public static final String TABLE_USUARIO = "Usuario";
    public static final String COLUMN_IDENTIFICACION = "Identificacion";
    public static final String COLUMN_NOMBREUSER = "Nombre";
    public static final String COLUMN_APELLIDOS = "Apellidos";
    public static final String COLUMN_USUARIO = "Usuario";
    public static final String COLUMN_CLAVE = "Clave";

    public static final String TABLE_RESES = "Reses";
    public static final String COLUMN_RESES_ID = "Id";
    public static final String COLUMN_CHAPETA = "Chapeta";
    public static final String COLUMN_NOMBRE = "Nombre";
    public static final String COLUMN_PADRE = "Padre";
    public static final String COLUMN_MADRE = "Madre";
    public static final String COLUMN_TIPO_BOVINO = "TipoBovino";
    public static final String COLUMN_FECHA_NACIMIENTO = "FechaNacimiento";
    public static final String  COLUMN_USUARIO_FK="UsuarioFk";

    private static final String CREATE_TABLE_USUARIO = "CREATE TABLE " + TABLE_USUARIO + " (" +
            COLUMN_IDENTIFICACION + " TEXT PRIMARY KEY, " +
            COLUMN_NOMBREUSER + " TEXT, " +
            COLUMN_APELLIDOS + " TEXT, " +
            COLUMN_USUARIO + " TEXT, " +
            COLUMN_CLAVE + " TEXT );";

    private static final String CREATE_TABLE_RESES = "CREATE TABLE " + TABLE_RESES + " (" +
            COLUMN_RESES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_CHAPETA + " TEXT, " +
            COLUMN_NOMBRE + " TEXT, " +
            COLUMN_PADRE + " TEXT, " +
            COLUMN_MADRE + " TEXT , " +
            COLUMN_TIPO_BOVINO + " TEXT, " +
            COLUMN_FECHA_NACIMIENTO + " TEXT, " +
            COLUMN_USUARIO_FK + " TEXT , " +
            "FOREIGN KEY(" + COLUMN_USUARIO_FK + ") REFERENCES " + TABLE_USUARIO + "(" + COLUMN_IDENTIFICACION + "));";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USUARIO);
        db.execSQL(CREATE_TABLE_RESES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESES);
        onCreate(db);
    }

    public List<Map<String, String>> SearchSQL(String consulta) {
        List<Map<String, String>> obtenciondatos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(consulta, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Map<String, String> fila = new HashMap<>();
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    String nombreColumna = cursor.getColumnName(i);
                    String valorColumna = cursor.getString(i);
                    fila.put(nombreColumna, valorColumna);
                }
                obtenciondatos.add(fila);
            }
            cursor.close();
        }

        return obtenciondatos;
    }
    public long agregarUsuario(String identificacion, String nombre, String apellidos, String usuario, String contrasena) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IDENTIFICACION, identificacion);
        values.put(COLUMN_NOMBREUSER, nombre);
        values.put(COLUMN_APELLIDOS, apellidos);
        values.put(COLUMN_USUARIO, usuario);
        values.put(COLUMN_CLAVE, contrasena);

        long newRowId = db.insert(TABLE_USUARIO, null, values);
        db.close();
        return newRowId;
    }
    public long updateUsuario(String identificacion, String nombre, String apellidos, String usuario, String contrasena) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IDENTIFICACION, identificacion);
        values.put(COLUMN_NOMBREUSER, nombre);
        values.put(COLUMN_APELLIDOS, apellidos);
        values.put(COLUMN_USUARIO, usuario);
        values.put(COLUMN_CLAVE, contrasena);
        long newRowId = db.update(TABLE_USUARIO, values, COLUMN_IDENTIFICACION+"=?", new String[]{identificacion});
        db.close();
        return newRowId;
    }
    public boolean borrarDatos(String identificacion, String tabla,String atributo) {
        SQLiteDatabase db = this.getWritableDatabase();
        int filasBorradas = db.delete(
                tabla,
                atributo+" = ?",
                new String[]{identificacion}
        );

        if (filasBorradas > 0) {
           return true;
        } else {
           return false;
        }
    }
    public long agregarReses(String chapeta, String nombre, String padre, String madre, String tipobovino, String fechanacimiento, String usuariofk) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHAPETA, chapeta);
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_PADRE, padre);
        values.put(COLUMN_MADRE, madre);
        values.put(COLUMN_TIPO_BOVINO, tipobovino);
        values.put(COLUMN_FECHA_NACIMIENTO, fechanacimiento);
        values.put(COLUMN_USUARIO_FK, usuariofk);
        long newRowId = db.insert(TABLE_RESES, null, values);
        db.close();
        return newRowId;
    }
    public long updateReses(String id,String chapeta, String nombre, String padre, String madre, String tipobovino, String fechanacimiento, String usuariofk) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHAPETA, chapeta);
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_PADRE, padre);
        values.put(COLUMN_MADRE, madre);
        values.put(COLUMN_TIPO_BOVINO, tipobovino);
        values.put(COLUMN_FECHA_NACIMIENTO, fechanacimiento);
        values.put(COLUMN_USUARIO_FK, usuariofk);
        long newRowId = db.update(TABLE_RESES, values, COLUMN_RESES_ID+"=?", new String[]{id});
        db.close();
        return newRowId;
    }
}
