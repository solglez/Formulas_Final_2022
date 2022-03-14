package com.example.formulas_final_2022;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

public class SQLiteOpenHelper extends android.database.sqlite.SQLiteOpenHelper {
    private String onCreateTable="CREATE TABLE datos (compuesto TEXT UNIQUE, formula TEXT PRIMARY KEY)";
    private String insert1="INSERT INTO datos (compuesto, formula) VALUES ('Ácido sulfúrico', 'SO4H2')";
    private String insert2="INSERT INTO datos (compuesto, formula) VALUES ('Agua', 'H2O')";
    private String insert3="INSERT INTO datos (compuesto, formula) VALUES ('Carbonato cálcico', 'CO3CA')";
    private String insert4="INSERT INTO datos (compuesto, formula) VALUES ('Anhídrido carbónico', 'CO2')";
    //Constructor
    public SQLiteOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Sentencias de creación cuando la bd NO EXISTA
        //Creamos la(s) tabla(s)
        sqLiteDatabase.execSQL(onCreateTable);
        //Podriamos insertar datos iniciales
        sqLiteDatabase.execSQL(insert1);
        sqLiteDatabase.execSQL(insert2);
        sqLiteDatabase.execSQL(insert3);
        sqLiteDatabase.execSQL(insert4);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //todo operaciones para modificar la bd (estructura) en sucesivas versiones

    }
}
