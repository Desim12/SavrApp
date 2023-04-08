package com.example.summary;

import android.content.ClipData;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter  extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>{
    private ArrayList<Long> itemList;
    private OnItemClickListener listener;
    public RecyclerViewAdapter(ArrayList<Long> itemList){
        this.listener = listener;
        this.itemList = itemList;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(ArrayList<Long> data){
        itemList = data;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new RecyclerViewHolder(itemView);
    }

    public interface OnItemClickListener {
        void onItemClick(int pos);
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        String data = itemList.get(position).toString();
        holder.textView.setText(data);

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder{
        TextView textView;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_view);
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION){
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }
}
