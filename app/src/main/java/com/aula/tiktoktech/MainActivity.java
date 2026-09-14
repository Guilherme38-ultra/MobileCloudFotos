package com.aula.tiktoktech;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String CLOUD_NAME = "id_do_cloud";
    private static final String UPLOAD_PRESET = "nome_do_upload";
    private static final String FOLDER = "nome_do_diretório";
    private static boolean cloudinaryConfigurado = false;
//    Escolher da galeria
    private FloatingActionButton fabGaleria;
//    Abrir câmera e tirar foto
    private FloatingActionButton fabNovaFoto;
    private ProgressBar progress;
    private TextView txtVazio;
    private Uri fotoUri;
    private final ActivityResultLauncher<String> selecionarFoto =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) enviarFoto(uri);
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), tirouFoto -> {
                if (tirouFoto != null && tirouFoto) enviarFoto(fotoUri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabNovaFoto = findViewById(R.id.fabNovaFoto);
        fabGaleria = findViewById(R.id.fabGaleria);
        progress = findViewById(R.id.progress);
        txtVazio = findViewById(R.id.txtVazio);
        configurarCloudinary();

        // Exibe a mensagem e o ícone definidos no XML.
        txtVazio.setVisibility(View.VISIBLE);
        txtVazio.setTextIsSelectable(true);
        progress.setVisibility(View.GONE);
        fabGaleria.setOnClickListener(v -> selecionarFoto.launch("image/*"));
        fabNovaFoto.setOnClickListener(v -> tirarFoto());
    }

//    Configuração do Cloduinary, onde vamos enviar as fotos da câmera/galeria
    private void configurarCloudinary() {
        // Não repete a inicialização ao recriar a Activity.
        if (!cloudinaryConfigurado) {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            config.put("secure", true);
            MediaManager.init(getApplicationContext(), config);
            cloudinaryConfigurado = true;
        }
    }
// Enviar ao Cloudinary
    private void enviarFoto(Uri fotoUri) {
        fabGaleria.setEnabled(false);
        fabNovaFoto.setEnabled(false);
        txtVazio.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        try {
            MediaManager.get()
                    .upload(fotoUri)
                    .unsigned(UPLOAD_PRESET)
                    .option("resource_type", "image")
                    .option("folder", FOLDER)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            // O indicador já foi exibido antes do envio.
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            // O ProgressBar do XML é indeterminado.
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            Object resultado = resultData.get("secure_url");

                            if (resultado instanceof String && ((String) resultado).startsWith("https://")) mostrarResultado((String) resultado);
                            else mostrarResultado("O envio terminou, mas não retornou uma URL HTTPS.");

                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            mostrarResultado("Erro ao enviar a foto:\n"+ error.getDescription());
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            runOnUiThread(() -> {
                                if (isFinishing() || isDestroyed()) return;
                                progress.setVisibility(View.GONE);
                                txtVazio.setText("Aguardando conexão para tentar novamente...");
                                txtVazio.setVisibility(View.VISIBLE);
                            });
                        }
                    }).dispatch();

        } catch (Exception e) {
            mostrarResultado("Não foi possível iniciar o envio:\n" + e.getMessage());
        }
    }

    private void mostrarResultado(String mensagem) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;

            progress.setVisibility(View.GONE);
            fabGaleria.setEnabled(true);
            fabNovaFoto.setEnabled(true);
            // Retira apenas o ícone de feed vazio ao mostrar o resultado.
            txtVazio.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            txtVazio.setText(mensagem);
            txtVazio.setVisibility(View.VISIBLE);

        });
    }

    private void tirarFoto() {
        File arquivo = new File(getExternalFilesDir(null), "2G_foto_" + System.currentTimeMillis() + ".jpg");
        fotoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", arquivo);
        cameraLauncher.launch(fotoUri);
    }
}