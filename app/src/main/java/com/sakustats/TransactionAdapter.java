package com.sakustats;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final Context context;
    private List<Transaction> transactions = new ArrayList<>();
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Transaction transaction);
    }

    public TransactionAdapter(Context context) {
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction tx = transactions.get(position);
        holder.bind(tx);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvName;
        private final TextView tvSubtitle;
        private final TextView tvAmount;
        private final TextView tvType;

        ViewHolder(View itemView) {
            super(itemView);
            tvName     = itemView.findViewById(R.id.tv_transaction_name);
            tvSubtitle = itemView.findViewById(R.id.tv_transaction_subtitle);
            tvAmount   = itemView.findViewById(R.id.tv_transaction_amount);
            tvType     = itemView.findViewById(R.id.tv_transaction_type);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(transactions.get(pos));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onItemLongClick(transactions.get(pos));
                    return true;
                }
                return false;
            });
        }

        void bind(Transaction tx) {
            tvName.setText(tx.getDescription());

            SimpleDateFormat dateTimeFormat =
                    new SimpleDateFormat(
                            "dd MMM yyyy • HH:mm",
                            new Locale("id", "ID")
                    );

            String dateTimeStr =
                    dateTimeFormat.format(tx.getDate());

            tvSubtitle.setText(
                    dateTimeStr + " • " +
                            (tx.getType() == Transaction.Type.INCOME ? "Masuk" : "Keluar")
            );

            tvAmount.setText(tx.getFormattedAmount());

            if (tx.getType() == Transaction.Type.INCOME) {
                tvAmount.setTextColor(Color.parseColor("#2E7D32")); // green
                tvType.setText(context.getString(R.string.type_masuk));
            } else {
                tvAmount.setTextColor(Color.parseColor("#BA1A1A")); // red
                tvType.setText(context.getString(R.string.type_keluar));
            }
        }
    }
}
