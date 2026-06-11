package com.example.prova_af;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListarSalvosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LugarSalvoAdapter adapter;
    private List<LugarSalvo> lista;

    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_salvos);

        recyclerView = findViewById(R.id.recyclerSalvos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        lista = new ArrayList<>();
        adapter = new LugarSalvoAdapter(lista);
        recyclerView.setAdapter(adapter);

        ref = FirebaseDatabase.getInstance().getReference("lugares");

        carregarDados();
    }

    private void carregarDados() {

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                lista.clear();

                for (DataSnapshot dados : snapshot.getChildren()) {

                    LugarSalvo lugar = dados.getValue(LugarSalvo.class);

                    if (lugar != null) {
                        lugar.setId(dados.getKey());
                        lista.add(lugar);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        ListarSalvosActivity.this,
                        "Erro ao carregar dados: " + error.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}