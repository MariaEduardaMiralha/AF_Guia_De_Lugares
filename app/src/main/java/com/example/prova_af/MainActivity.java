package com.example.prova_af;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int CODIGO_PERMISSAO_LOCALIZACAO = 100;
    private static final int CODIGO_ATIVAR_GPS = 200;

    private Button btnLocalizacao;
    private Button btnBuscarLugares;
    private TextView txtLocalizacao;
    private TextView txtEndereco;
    private TextView txtStatusBusca;
    private Spinner spinnerCategoria;
    private RecyclerView recyclerLugares;

    private FusedLocationProviderClient clienteLocalizacao;

    private double latitudeAtual;
    private double longitudeAtual;
    private boolean localizacaoCapturada = false;

    private ArrayList<Local> listaLugares;
    private LugarAdapter lugarAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnLocalizacao = findViewById(R.id.btnLocalizacao);
        btnBuscarLugares = findViewById(R.id.btnBuscarLugares);
        txtLocalizacao = findViewById(R.id.txtLocalizacao);
        txtEndereco = findViewById(R.id.txtEndereco);
        txtStatusBusca = findViewById(R.id.txtStatusBusca);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        recyclerLugares = findViewById(R.id.recyclerLugares);

        Button btnVerSalvos = findViewById(R.id.btnVerSalvos);
        btnVerSalvos.setOnClickListener(v -> {
            startActivity(new Intent(this, ListarSalvosActivity.class));
        });

        clienteLocalizacao = LocationServices.getFusedLocationProviderClient(this);

        configurarSpinner();
        configurarRecyclerView();

        btnLocalizacao.setOnClickListener(v -> verificarPermissaoLocalizacao());

        btnBuscarLugares.setOnClickListener(v -> {
            if (!localizacaoCapturada) {
                Toast.makeText(
                        this,
                        "Capture a localização antes de buscar lugares.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            buscarLugaresProximos();
        });
    }

    private void configurarSpinner() {
        String[] categorias = {
                "Farmácia",
                "Hospital",
                "Escola",
                "Restaurante",
                "Praça",
                "Mercado"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categorias
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);
    }

    private void configurarRecyclerView() {
        listaLugares = new ArrayList<>();
        lugarAdapter = new LugarAdapter(listaLugares);

        recyclerLugares.setLayoutManager(new LinearLayoutManager(this));
        recyclerLugares.setAdapter(lugarAdapter);
    }

    private void verificarPermissaoLocalizacao() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_PERMISSAO_LOCALIZACAO
            );

        } else {
            verificarGpsAtivo();
        }
    }

    private void verificarGpsAtivo() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000
        ).build();

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest)
                        .setAlwaysShow(true);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);

        settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener(locationSettingsResponse -> capturarLocalizacao())
                .addOnFailureListener(e -> {
                    if (e instanceof ResolvableApiException) {
                        try {
                            ResolvableApiException resolvableApiException =
                                    (ResolvableApiException) e;

                            resolvableApiException.startResolutionForResult(
                                    MainActivity.this,
                                    CODIGO_ATIVAR_GPS
                            );

                        } catch (IntentSender.SendIntentException sendEx) {
                            Toast.makeText(
                                    this,
                                    "Não foi possível solicitar a ativação do GPS.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    } else {
                        Toast.makeText(
                                this,
                                "Ative a localização do aparelho para continuar.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void capturarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        clienteLocalizacao.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        mostrarLocalizacao(location);
                    } else {
                        Toast.makeText(
                                this,
                                "Localização ainda indisponível. Aguarde alguns segundos e tente novamente.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Erro ao capturar localização: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    private void mostrarLocalizacao(Location location) {
        latitudeAtual = location.getLatitude();
        longitudeAtual = location.getLongitude();
        localizacaoCapturada = true;

        txtLocalizacao.setText(
                "Latitude: " + latitudeAtual +
                        "\nLongitude: " + longitudeAtual
        );

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> enderecos = geocoder.getFromLocation(latitudeAtual, longitudeAtual, 1);

            if (enderecos != null && !enderecos.isEmpty()) {
                Address endereco = enderecos.get(0);
                txtEndereco.setText(endereco.getAddressLine(0));
            } else {
                txtEndereco.setText("Endereço não encontrado");
            }

        } catch (IOException e) {
            e.printStackTrace();
            txtEndereco.setText("Erro ao obter endereço");
        }
    }

    private void buscarLugaresProximos() {
        txtStatusBusca.setText("Buscando lugares próximos...");
        btnBuscarLugares.setEnabled(false);

        listaLugares.clear();
        lugarAdapter.notifyDataSetChanged();

        String categoriaSelecionada = spinnerCategoria.getSelectedItem().toString();

        new Thread(() -> {
            try {
                String query = montarQueryOverpass(categoriaSelecionada);

                String queryCodificada = URLEncoder.encode(query, "UTF-8");

                URL url = new URL("https://overpass-api.de/api/interpreter?data=" + queryCodificada);

                HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
                conexao.setRequestMethod("GET");
                conexao.setRequestProperty("User-Agent", "AF-Mobile-Facens/1.0");
                conexao.setConnectTimeout(30000);
                conexao.setReadTimeout(30000);

                int responseCode = conexao.getResponseCode();

                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conexao.getInputStream())
                    );

                    StringBuilder resposta = new StringBuilder();
                    String linha;

                    while ((linha = reader.readLine()) != null) {
                        resposta.append(linha);
                    }

                    reader.close();

                    interpretarRespostaOverpass(resposta.toString(), categoriaSelecionada);

                } else if (responseCode == 429) {
                    runOnUiThread(() -> txtStatusBusca.setText(
                            "Muitas buscas em pouco tempo. Aguarde alguns segundos e tente novamente."
                    ));

                } else if (responseCode == 504) {
                    runOnUiThread(() -> txtStatusBusca.setText(
                            "A busca demorou demais. Tente novamente ou escolha outra categoria."
                    ));

                } else {
                    runOnUiThread(() -> txtStatusBusca.setText(
                            "Erro na API Overpass. Código: " + responseCode
                    ));
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    txtStatusBusca.setText("Erro ao buscar lugares. Detalhe: " + e.getMessage());
                });

            } finally {
                runOnUiThread(() -> btnBuscarLugares.setEnabled(true));
            }
        }).start();
    }

    private String montarQueryOverpass(String categoria) {
        int raio = 2500;

        String lat = String.valueOf(latitudeAtual);
        String lon = String.valueOf(longitudeAtual);

        if (categoria.equals("Farmácia")) {
            return "[out:json][timeout:25];" +
                    "(" +
                    "node[\"amenity\"=\"pharmacy\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "way[\"amenity\"=\"pharmacy\"](around:" + raio + "," + lat + "," + lon + ");" +
                    ");" +
                    "out center 30;";
        }

        if (categoria.equals("Hospital")) {
            return "[out:json][timeout:25];" +
                    "(" +
                    "node[\"amenity\"=\"hospital\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "node[\"amenity\"=\"clinic\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "node[\"amenity\"=\"doctors\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "way[\"amenity\"=\"hospital\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "way[\"amenity\"=\"clinic\"](around:" + raio + "," + lat + "," + lon + ");" +
                    ");" +
                    "out center 30;";
        }

        if (categoria.equals("Escola")) {
            return "[out:json][timeout:25];" +
                    "(" +
                    "node[\"amenity\"=\"school\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "node[\"amenity\"=\"college\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "node[\"amenity\"=\"university\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "way[\"amenity\"=\"school\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "way[\"amenity\"=\"college\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "way[\"amenity\"=\"university\"](around:" + raio + "," + lat + "," + lon + ");" +
                    ");" +
                    "out center 30;";
        }

        if (categoria.equals("Restaurante")) {
            return "[out:json][timeout:25];" +
                    "(" +
                    "node[\"amenity\"=\"restaurant\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "node[\"amenity\"=\"fast_food\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "node[\"amenity\"=\"cafe\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "way[\"amenity\"=\"restaurant\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "way[\"amenity\"=\"fast_food\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "way[\"amenity\"=\"cafe\"](around:" + raio + "," + lat + "," + lon + ");" +
                    ");" +
                    "out center 30;";
        }

        if (categoria.equals("Praça")) {
            return "[out:json][timeout:25];" +
                    "(" +
                    "node[\"leisure\"=\"park\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "node[\"leisure\"=\"garden\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "node[\"leisure\"=\"playground\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "way[\"leisure\"=\"park\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "way[\"leisure\"=\"garden\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "way[\"leisure\"=\"playground\"](around:" + raio + "," + lat + "," + lon + ");" +
                    ");" +
                    "out center 30;";
        }

        if (categoria.equals("Mercado")) {
            return "[out:json][timeout:25];" +
                    "(" +
                    "node[\"shop\"=\"supermarket\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "node[\"shop\"=\"convenience\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "node[\"shop\"=\"grocery\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "way[\"shop\"=\"supermarket\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "way[\"shop\"=\"convenience\"](around:" + raio + "," + lat + "," + lon + ");" +
                    "way[\"shop\"=\"grocery\"](around:" + raio + "," + lat + "," + lon + ");" +
                    ");" +
                    "out center 30;";
        }

        return "[out:json][timeout:25];" +
                "node[\"amenity\"=\"restaurant\"](around:" + raio + "," + lat + "," + lon + ");" +
                "out 30;";
    }

    private void interpretarRespostaOverpass(String json, String categoriaSelecionada) {
        try {
            JsonObject objetoPrincipal = JsonParser.parseString(json).getAsJsonObject();
            JsonArray elementos = objetoPrincipal.getAsJsonArray("elements");

            ArrayList<Local> lugaresEncontrados = new ArrayList<>();

            for (int i = 0; i < elementos.size(); i++) {
                JsonObject elemento = elementos.get(i).getAsJsonObject();

                double latitude;
                double longitude;

                if (elemento.has("lat") && elemento.has("lon")) {
                    latitude = elemento.get("lat").getAsDouble();
                    longitude = elemento.get("lon").getAsDouble();
                } else if (elemento.has("center")) {
                    JsonObject center = elemento.getAsJsonObject("center");
                    latitude = center.get("lat").getAsDouble();
                    longitude = center.get("lon").getAsDouble();
                } else {
                    continue;
                }

                String nome = "Local sem nome";

                if (elemento.has("tags")) {
                    JsonObject tags = elemento.getAsJsonObject("tags");

                    if (tags.has("name")) {
                        nome = tags.get("name").getAsString();
                    }
                }

                float[] resultadoDistancia = new float[1];

                Location.distanceBetween(
                        latitudeAtual,
                        longitudeAtual,
                        latitude,
                        longitude,
                        resultadoDistancia
                );

                double distancia = resultadoDistancia[0];

                Local lugar = new Local(
                        nome,
                        categoriaSelecionada,
                        latitude,
                        longitude,
                        distancia
                );

                lugaresEncontrados.add(lugar);
            }

            runOnUiThread(() -> {
                listaLugares.clear();
                listaLugares.addAll(lugaresEncontrados);
                lugarAdapter.notifyDataSetChanged();

                if (lugaresEncontrados.isEmpty()) {
                    txtStatusBusca.setText("Nenhum lugar encontrado nessa categoria.");
                } else {
                    txtStatusBusca.setText(
                            "Foram encontrados " + lugaresEncontrados.size() + " lugares próximos."
                    );
                }
            });

        } catch (Exception e) {
            runOnUiThread(() -> {
                txtStatusBusca.setText("Erro ao interpretar resposta da API.");
                Toast.makeText(
                        this,
                        "Erro no JSON: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            });
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            android.content.Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODIGO_ATIVAR_GPS) {
            verificarGpsAtivo();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CODIGO_PERMISSAO_LOCALIZACAO) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                verificarGpsAtivo();

            } else {

                Toast.makeText(
                        this,
                        "Permissão negada! O app não funcionará corretamente.",
                        Toast.LENGTH_LONG
                ).show();

                txtLocalizacao.setText("Permissão de localização negada.");
                txtEndereco.setText("Não é possível obter o endereço sem permissão.");
            }
        }
    }
}