package com.example.formulas_final_2022;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences preferencia; //Usamos preferencias compartidas para los aciertos
    private SharedPreferences.Editor editor;
    private ListView lvCompuestos;
    private ImageButton imgBtn;
    private LinearLayout llInicio, llCompuestos;
    private String DBname="DB_Compuestos";
    private SQLiteDatabase database;
    private int CODIGO_LLAMADA_PREGUNTAFORMULA,CODIGO_LLAMADA_INSERT, LLAMADA_TELEFONICA, totalErrores, aciertos;
    private static final int DIALOGO_2BOTON=2;
    private static final int NOTIFICACION2_ID=2;
    private static final String CHANEL_ID="canal1";
    private AlertDialog.Builder ventana;
    private SQLiteOpenHelper aux;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        declarar();
        crearCanal(); //Para notificación de errores
        aux=new SQLiteOpenHelper(this, DBname, null, 1);

        cargarLista();
        recuperarAciertos();

        //Listener de la ListView de planetas. Ojo, es onItemClick.
        lvCompuestos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Cambio Activity
                database.close();
                Intent intent=new Intent(MainActivity.this, PreguntaFormulaActivity.class);
                intent.putExtra("Compuesto",adapterView.getItemAtPosition(i).toString());
                startActivityForResult(intent,CODIGO_LLAMADA_PREGUNTAFORMULA);
            }
        });

    }
    //Método donde nos enteramos de las respuestas
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Comprobamos el código de quién responde
        if(requestCode==CODIGO_LLAMADA_PREGUNTAFORMULA){
            int retornado=0;
            try{
                //De no hacer esto en try-catch rompería al retornar por medio distinto al botón.
                //También podría evitarse comprobando antes el RESULT
                retornado=data.getExtras().getInt("SumarError");
            }catch (Exception e){

            }
            totalErrores=totalErrores+retornado;
            if(retornado==0){
                aciertos+=1;
                editor.putInt("Aciertos", aciertos);
                editor.apply();
            }
            if (totalErrores>2){
                totalErrores=3; //Para evitar un sumatorio infinito
                notificacionErrores();
            }
            if(resultCode==RESULT_CANCELED){
                finish();
            }
        }else if(requestCode==CODIGO_LLAMADA_INSERT){
            if(resultCode==RESULT_OK){
                //RecargarLista
                cargarLista();
            }
        }
    }
    private void declarar(){
        llCompuestos=findViewById(R.id.llCompuestos);
        llInicio=findViewById(R.id.llInicio);
        imgBtn=findViewById(R.id.btnLogo);
        lvCompuestos=findViewById(R.id.lvCompuestos);
        CODIGO_LLAMADA_PREGUNTAFORMULA=1;
        CODIGO_LLAMADA_INSERT=3;
        LLAMADA_TELEFONICA=5;
        totalErrores=0;
        //Crear un objeto SharedPreferences, tomamos el defectivo del PreferenceManager
        preferencia= PreferenceManager.getDefaultSharedPreferences(this);
        //Generamos el editor necesario para guardar las preferencias.
        editor=preferencia.edit();
    }

    public void onClickBtn(View view) {
        if(view.getId()==R.id.btnLogo){
            llInicio.setVisibility(View.GONE);
            llCompuestos.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_opciones, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.opcContacto:
                //Lanza notificación de contacto
                showDialog(DIALOGO_2BOTON);
                break;
            case R.id.opcInsertar:
                Intent intent=new Intent(MainActivity.this, InsertarCompuestoActivity.class);
                startActivityForResult(intent,CODIGO_LLAMADA_INSERT);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id==DIALOGO_2BOTON){
            dlg_2btn();
        }
        return ventana.create();
    }

    private void dlg_2btn(){
        //Creamos un objeto AlertDialog.Builder
        //AlertDialog.Builder ventana=new AlertDialog.Builder(this);
        ventana=new AlertDialog.Builder(this);
        //Rellenamos la ventana
        ventana.setTitle(R.string.contacto);
        ventana.setIcon(R.drawable.flask1);
        ventana.setMessage(R.string.contactarConNosotros);
        //Creamos el PositiveButton con el texto y su escuchador (DialogInterface.OnClickListener):
        ventana.setPositiveButton(R.string.s, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Llamada telefónica
                Intent intent;
                //Realizar llamada de forma directa
                //¿Tenemos permiso? - La forma de actuar difiere según la versión de API
                //Testeamos el nivel de api para poder comprobar el permiso sin que las v<23 rompan:
                if(Build.VERSION.SDK_INT>=23){
                    int permiso= checkSelfPermission(Manifest.permission.CALL_PHONE);
                    if (permiso== PackageManager.PERMISSION_GRANTED){ //Estas dos lineas podrían ser una
                        //Tenemos permiso, lanzamos el intent
                        intent=new Intent(Intent.ACTION_CALL, Uri.parse("tel:(+34)915555555"));
                        startActivity(intent);
                    }else{
                        //No tenemos permiso, lo pedimos (diálogo del sistema)
                        //LLAMADA_TELEFONICA es la constante para recuperar la respuesta
                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, LLAMADA_TELEFONICA);
                        //Método para interceptar respuesta debajo
                    }
                }else{
                    //Para versión menor a 23 (PERMISOS EN MANIFEST) valdría con el intent directamente:
                    intent=new Intent(Intent.ACTION_CALL,Uri.parse("tel:(+34)915555555"));
                    startActivity(intent);
                }

                dialogInterface.cancel();
            }
        });
        ventana.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

    }

    //Método para escuchar la respuesta del usuario tras pedirle permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==LLAMADA_TELEFONICA){
            //Si la respuesta corresponde a la petición de llamada telefónica
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                //Si el permiso se ha concedido
                Intent i=new Intent(Intent.ACTION_CALL,Uri.parse("tel:(+34)915555555"));
                startActivity(i);
            }else{
                //Si el usuario no da permiso
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void crearCanal(){ //Crear canal para notificaciones
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O){
            CharSequence name="Notificaciones básicas";
            String descripcion="Canal para notificaciones sencillas";
            int importance= NotificationManager.IMPORTANCE_DEFAULT; //prioridad del canal
            NotificationChannel channel= new NotificationChannel(CHANEL_ID,name, importance);
            if(channel==null){
                channel.enableVibration(false); //deshabilitar vibración
            }
            channel.setDescription(descripcion);
            NotificationManager manager =getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public void notificacionErrores(){ //Lanzar notificación tras errores
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this, CHANEL_ID);
        builder.setSmallIcon(android.R.drawable.ic_dialog_alert);
        //builder.setTicker("Alerta!"); // Notificación emergente NO APARECE
        builder.setContentTitle(getString(R.string.mas2errores));
        Bitmap largeIcon= BitmapFactory.decodeResource(getResources(), R.drawable.flask1);
        builder.setLargeIcon(largeIcon);
        //Para que aparezcan varias lineas:
        builder.setContentText(getString(R.string.consulteweb));
        Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.formulacionquimica.com"));
        PendingIntent pendingIntent=PendingIntent.getActivity(this, 0,i,0);
        builder.setContentIntent(pendingIntent);
        //Desaparición de la barra de estado
        builder.setAutoCancel(true);
        NotificationManager notifManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notificacion=builder.build();
        notifManager.notify(NOTIFICACION2_ID,notificacion);
    }

    public void cargarLista(){//Cargar los datos de la bbdd a la lista
        database=aux.getWritableDatabase();
        ArrayList<String> compuestos=new ArrayList<>();
        try{
            String[] datosARecuperar={"compuesto"};
            Cursor c = database.query("datos", datosARecuperar, null,null,null,null,null,null);
            if (c.moveToFirst()) { //significa que se ha recuperado algo en la consulta
                //recorremos el cursor hasta que no haya más registros
                do {
                    String compuesto = c.getString(0);
                    compuestos.add(compuesto);
                }while (c.moveToNext());
                c.close();
            }

        }catch (Exception e){
            Toast.makeText(this, "Ha ocurrido un error.", Toast.LENGTH_LONG).show();
        }
        String[] arrayCompuestos = compuestos.toArray(new String[0]);

        //Adaptador personalizado para el array con imagen única, le asignamos el recurso de fila_simple que hemos creado
        ArrayAdapter<String> AdaptadorPersonalizadoImagenUnica=new ArrayAdapter(this,R.layout.fila_imagen_diferente,
                R.id.tvCompuesto, arrayCompuestos);
        lvCompuestos.setAdapter(AdaptadorPersonalizadoImagenUnica);
    }

    private void recuperarAciertos(){ //Recuperar aciertos al inicio y mostrar Toast si existen
        int aciertos=preferencia.getInt("Aciertos",-1);
        if (aciertos!=-1){
            Toast.makeText(this, "Aciertos en última ejecución: "+aciertos, Toast.LENGTH_SHORT).show();
        }
        editor.putInt("Aciertos", 0);
        editor.apply();
    }
}