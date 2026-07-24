package com.sakustats;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<PlanNotification> notifications;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public NotificationAdapter(List<PlanNotification> notifications) {
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlanNotification notif = notifications.get(position);
        holder.tvTitle.setText(notif.getTitle());
        holder.tvMessage.setText(notif.getMessage());
        holder.tvTime.setText(sdf.format(notif.getTimestamp()));

        // Set icon based on type if needed
        if ("achievement".equals(notif.getType())) {
            holder.ivIcon.setImageResource(android.R.drawable.btn_star_big_on);
            holder.indicator.setBackgroundResource(R.color.color_income);
        } else if ("deadline".equals(notif.getType())) {
            holder.indicator.setBackgroundResource(R.color.color_expense);
        } else {
            holder.indicator.setBackgroundResource(R.color.color_peach_dark);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void removeAt(int position) {
        notifications.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        ImageView ivIcon;
        View indicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notif_title);
            tvMessage = itemView.findViewById(R.id.tv_notif_message);
            tvTime = itemView.findViewById(R.id.tv_notif_time);
            ivIcon = itemView.findViewById(R.id.iv_notif_icon);
            indicator = itemView.findViewById(R.id.view_indicator);
        }
    }
}
