package com.example.formulas_final_2022;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class InsertarCompuestoActivity extends AppCompatActivity {
    private EditText etNombre, etFormula;
    private String DBname="DB_Compuestos";
    private SQLiteDatabase database;
    private SQLiteOpenHelper aux;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insertar_compuesto);
        declarar();
        aux=new SQLiteOpenHelper(this, DBname, null, 1);


    }

    public void onClickAct3(View view) {
        if(view.getId()==R.id.btnInsertar){
            if((etNombre.getText().toString().length()<1)||(etFormula.getText().toString().length()<1)){
                Toast.makeText(this, getText(R.string.faltaContenido), Toast.LENGTH_SHORT).show();
            }else{
                long l=0;
                try{
                    database=aux.getWritableDatabase();
                    String nombre=etNombre.getText().toString();
                    String formula=etFormula.getText().toString();
                    ContentValues nuevaTupla=new ContentValues();
                    nuevaTupla.put("compuesto", nombre);
                    nuevaTupla.put("formula", formula);
                    l=database.insert("datos", null, nuevaTupla);
                    if (l!=0){
                        //Toast.makeText(this, "Inserción OK!", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(this, "No se pueden insertar los datos.", Toast.LENGTH_SHORT).show();
                    }
                    database.close();
                }catch (Exception e){
                    Toast.makeText(this, "Ha ocurrido un error.", Toast.LENGTH_SHORT).show();
                }
                //Retornamos a la main
                Intent i=new Intent();
                //Devolvemos el control a actividad origen
                if (l!=0){
                    setResult(RESULT_OK,i);
                }else{
                    setResult(RESULT_CANCELED,i);
                }
                //Finalizamos actividad (sino no retorna a main, para evitar apilamientos)
                finish();
            }
        }
    }

    private void declarar(){
        etFormula=findViewById(R.id.etCrearFormula);
        etNombre=findViewById(R.id.etCrearNombre);
    }
}