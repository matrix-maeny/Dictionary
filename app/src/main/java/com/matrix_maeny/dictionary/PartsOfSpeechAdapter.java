package com.matrix_maeny.dictionary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.matrix_maeny.dictionary.databinding.PartsOfSpeechModelBinding;

import java.util.List;

public class PartsOfSpeechAdapter extends RecyclerView.Adapter<PartsOfSpeechAdapter.viewHolder> {

    private final Context context;
    private final List<PartsOfSpeechModel> list;

    public PartsOfSpeechAdapter(Context context, List<PartsOfSpeechModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.parts_of_speech_model,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        PartsOfSpeechModelBinding binding = holder.binding;

        PartsOfSpeechModel model = list.get(position);

        binding.partsOfSpTv.setText(model.getPartsOfSpeech());
        binding.definTv.setText(model.getDefinitions());


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        PartsOfSpeechModelBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);

            binding = PartsOfSpeechModelBinding.bind(itemView);
        }
    }
}
