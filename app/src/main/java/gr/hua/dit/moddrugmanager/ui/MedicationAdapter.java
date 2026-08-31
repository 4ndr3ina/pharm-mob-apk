package gr.hua.dit.moddrugmanager.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import gr.hua.dit.moddrugmanager.R;
import gr.hua.dit.moddrugmanager.data.PrescriptionDrugWithTimeTerms;

/**
 * RecyclerView adapter showing UID, Short Name and ALL Time-Terms (comma
 * separated, chronologically sorted) for each medication.
 */
public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PrescriptionDrugWithTimeTerms item);
    }

    private List<PrescriptionDrugWithTimeTerms> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public MedicationAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<PrescriptionDrugWithTimeTerms> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        PrescriptionDrugWithTimeTerms item = items.get(position);
        holder.tvItemName.setText(item.drug.getShortName());
        String details = "ID: " + item.drug.getUid() + "  •  " + item.getTimeTermsDisplay();
        holder.tvItemDetails.setText(details);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvItemDetails;

        MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemDetails = itemView.findViewById(R.id.tvItemDetails);
        }
    }
}