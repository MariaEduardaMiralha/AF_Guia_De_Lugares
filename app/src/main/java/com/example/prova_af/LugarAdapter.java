package com.example.prova_af;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class LugarAdapter extends RecyclerView.Adapter<LugarAdapter.LugarViewHolder> {

    private List<Local> listaLugares;

    public LugarAdapter(List<Local> listaLugares) {
        this.listaLugares = listaLugares;
    }

    @NonNull
    @Override
    public LugarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_local, parent, false);

        return new LugarViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull LugarViewHolder holder, int position) {
        Local lugar = listaLugares.get(position);

        holder.txtNomeLugar.setText(lugar.getNome());
        holder.txtCategoriaLugar.setText("Categoria: " + lugar.getCategoria());
        holder.txtCoordenadasLugar.setText(
                "Lat: " + lugar.getLatitude() + " | Lon: " + lugar.getLongitude()
        );
        holder.txtDistanciaLugar.setText(
                "Distância aproximada: " + String.format("%.0f", lugar.getDistancia()) + " metros"
        );

        holder.itemView.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            View view = LayoutInflater.from(v.getContext()).inflate(R.layout.salvo, null);

            EditText edtObs = view.findViewById(R.id.edtObservacao);
            Spinner spinnerCategoria = view.findViewById(R.id.spinnerCategoria);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    v.getContext(),
                    android.R.layout.simple_spinner_item,
                    new String[]{"Estudo", "Saúde", "Lazer", "Alimentação", "Compras", "Outros"}
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategoria.setAdapter(adapter);

            builder.setView(view);
            builder.setTitle("Salvar Local");

            builder.setPositiveButton("Salvar", (dialog, which) -> {

                String obs = edtObs.getText().toString();
                String categoria = spinnerCategoria.getSelectedItem().toString();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("lugares");

                String id = ref.push().getKey();

                LugarSalvo lugarSalvo = new LugarSalvo(
                        lugar.getNome(),
                        categoria,
                        lugar.getLatitude(),
                        lugar.getLongitude(),
                        obs
                );

                lugarSalvo.setId(id);

                ref.child(id).setValue(lugarSalvo)
                        .addOnSuccessListener(unused ->
                                Toast.makeText(v.getContext(), "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(v.getContext(), "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return listaLugares.size();
    }

    public static class LugarViewHolder extends RecyclerView.ViewHolder {

        public TextView txtNomeLugar;
        public TextView txtCategoriaLugar;
        public TextView txtCoordenadasLugar;
        public TextView txtDistanciaLugar;

        public LugarViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNomeLugar = itemView.findViewById(R.id.txtNomeLugar);
            txtCategoriaLugar = itemView.findViewById(R.id.txtCategoriaLugar);
            txtCoordenadasLugar = itemView.findViewById(R.id.txtCoordenadasLugar);
            txtDistanciaLugar = itemView.findViewById(R.id.txtDistanciaLugar);
        }
    }
}