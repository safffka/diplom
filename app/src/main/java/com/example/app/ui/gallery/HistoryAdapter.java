package com.example.app.ui.gallery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app.R;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryItem> historyItems = new ArrayList<>();

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    public void setHistoryItems(List<HistoryItem> historyItems) {
        this.historyItems = historyItems;
        notifyDataSetChanged();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewId;
        private TextView textViewUserLogin;
        private TextView textViewUserPhone;
        private TextView textViewFilename;
        private TextView textViewPlateNumber;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewId = itemView.findViewById(R.id.textViewId);
            textViewUserLogin = itemView.findViewById(R.id.textViewUserLogin);
            textViewUserPhone = itemView.findViewById(R.id.textViewUserPhone);
            textViewFilename = itemView.findViewById(R.id.textViewFilename);
            textViewPlateNumber = itemView.findViewById(R.id.textViewPlateNumber);
        }

        public void bind(HistoryItem item) {
            textViewId.setText(String.valueOf(item.getId()));
            textViewUserLogin.setText(item.getUserLogin());
            textViewUserPhone.setText(item.getUserPhone());
            textViewFilename.setText(item.getFilename());
            textViewPlateNumber.setText(item.getPlateNumber());
        }
    }
}
