package com.aka.staychill;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class Configuracion extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        findViewById(R.id.account_section).setOnClickListener(v ->
                startActivity(new Intent(this, Cuenta.class))
        );

        findViewById(R.id.privacy_security_section).setOnClickListener(v ->
                startActivity(new Intent(this, PrivacidadSeguridad.class))
        );

        findViewById(R.id.notifications_section).setOnClickListener(v ->
                startActivity(new Intent(this, Notificaciones.class))
        );

        findViewById(R.id.report_problem_section).setOnClickListener(v ->
                startActivity(new Intent(this, ReportarProblema.class))
        );
    }
}
