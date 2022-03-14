package com.example.formulas_final_2022;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

public class PreguntaFormulaActivity extends AppCompatActivity {
    private String DBname="DB_Compuestos";
    private SQLiteDatabase database;
    private EditText etFormula;
    private RadioButton rbNo;
    private String datoRecibido, formulaCorrecta;
    private LinearLayout llPregunta, llContinuar;
    private int sumarError;
    private SQLiteOpenHelper aux;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregunta_formula);
        //Recuperamos el intent
        Intent intent=getIntent();
        //Recuperamos el dato que recibe de la Actividad 1 con getExtras
        datoRecibido=intent.getExtras().getString("Compuesto");

        declarar();
        etFormula.setHint(datoRecibido);

        aux=new SQLiteOpenHelper(this, DBname, null, 1);

    }

    public void onClickAct2(View view) {
        if(view.getId()==R.id.btnComprobar) {
            //Si no hay datos los pedimos antes de comprobar

            if (etFormula.getText().toString().length()<1) {
                Toast.makeText(this, R.string.introduzcaRespuesta, Toast.LENGTH_SHORT).show();
            }else {
                if (comprobarResultado()) {
                    Toast.makeText(this, etFormula.getText().toString()+": "+getString(R.string.correcto), Toast.LENGTH_SHORT).show();
                    sumarError=0;
                } else {
                    Toast.makeText(this, etFormula.getText().toString()+": "+getString(R.string.incorrecto), Toast.LENGTH_SHORT).show();
                    sumarError=1;
                }
                llPregunta.setVisibility(View.GONE);
                llContinuar.setVisibility(View.VISIBLE);
            }

        }else if(view.getId()==R.id.btnContinuar){
            //Retornamos a la main
            Intent i=new Intent();
            i.putExtra("SumarError",sumarError);
            //Devolvemos el control a actividad origen con envío de datos
            if(rbNo.isChecked()){ //Comprobamos qué tiene que recibir main
                setResult(RESULT_CANCELED,i);
            }else{
                setResult(RESULT_OK,i);
            }
            //Finalizamos actividad (sino no retorna a main, para evitar apilamientos)
            finish();
        }
    }

    private boolean comprobarResultado(){ //Método para comprobar resultado con query
        database=aux.getWritableDatabase();
        boolean res=false;
        try{
            //Consulta con método específico -> query para recuperar la fórmula correcta
            String[] datoARecuperar={"formula"};
            Cursor cursor2=database.query("datos", datoARecuperar, "compuesto = '"+datoRecibido+"'",null,null,null,null);
            if(cursor2.moveToFirst()){ //Hay resultado
                formulaCorrecta=cursor2.getString(0);
            }else{ //Resultado vacío
                Toast.makeText(this, "No he encontrado nada.", Toast.LENGTH_SHORT).show();
            }
            //HAY que cerrar el cursor
            cursor2.close();
        }catch (Exception e){
            Toast.makeText(this, "Ha ocurrido un error.", Toast.LENGTH_SHORT).show();
        }
        database.close();
        if (formulaCorrecta.equals(etFormula.getText().toString())){
            res=true;
        }else{
            res=false;
        }
        return res;
    }

    private void declarar(){
        rbNo=findViewById(R.id.rbNo);
        etFormula=findViewById(R.id.etFormula);
        llContinuar=findViewById(R.id.llContinuar);
        llPregunta=findViewById(R.id.llPregunta);
        llContinuar.setVisibility(View.GONE);
        llPregunta.setVisibility(View.VISIBLE);
        sumarError=0;
        formulaCorrecta="";

    }
}