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

public class LugarSalvoAdapter extends RecyclerView.Adapter<LugarSalvoAdapter.ViewHolder> {

    private List<LugarSalvo> lista;

    public LugarSalvoAdapter(List<LugarSalvo> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_local_salvo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        LugarSalvo lugar = lista.get(position);

        holder.txtNome.setText("Nome: " + lugar.getNome());
        holder.txtCategoria.setText("Categoria: " + lugar.getCategoria());
        holder.txtCoordenadas.setText(
                "Lat: " + lugar.getLatitude() + " | Lon: " + lugar.getLongitude()
        );
        holder.txtObs.setText("Observação: " + lugar.getObservacao());

        holder.itemView.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Editar Local");

            View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.salvo, null);

            EditText edtObs = dialogView.findViewById(R.id.edtObservacao);
            Spinner spinnerCategoria = dialogView.findViewById(R.id.spinnerCategoria);

            edtObs.setText(lugar.getObservacao());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    v.getContext(),
                    android.R.layout.simple_spinner_item,
                    new String[]{"Estudo", "Saúde", "Lazer", "Alimentação", "Compras", "Outros"}
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategoria.setAdapter(adapter);

            String categoriaAtual = lugar.getCategoria();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(categoriaAtual)) {
                    spinnerCategoria.setSelection(i);
                    break;
                }
            }

            builder.setView(dialogView);

            builder.setPositiveButton("Salvar", (dialog, which) -> {
                lugar.setObservacao(edtObs.getText().toString());
                lugar.setCategoria(spinnerCategoria.getSelectedItem().toString());

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("lugares");
                ref.child(lugar.getId()).setValue(lugar);

                notifyItemChanged(position);

                Toast.makeText(v.getContext(), "Atualizado!", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });


        holder.itemView.setOnLongClickListener(v -> {

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Excluir")
                    .setMessage("Deseja excluir este local?")
                    .setPositiveButton("Sim", (dialog, which) -> {

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("lugares");
                        ref.child(lugar.getId()).removeValue();

                        lista.remove(position);
                        notifyItemRemoved(position);

                        Toast.makeText(v.getContext(), "Excluído!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Não", null)
                    .show();

            return true;
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtNome;
        public TextView txtCategoria;
        public TextView txtCoordenadas;
        public TextView txtObs;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txtNome);
            txtCategoria = itemView.findViewById(R.id.txtCategoria);
            txtCoordenadas = itemView.findViewById(R.id.txtCoordenadas);
            txtObs = itemView.findViewById(R.id.txtObs);
        }
    }
}