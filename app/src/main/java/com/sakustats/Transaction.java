package com.sakustats;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class Transaction {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);
    public static final java.util.List<Transaction> DATA =
            new java.util.ArrayList<>();

    public enum Type {
        INCOME,
        EXPENSE
    }

    private long id;
    private String description;
    private String note;
    private long amount; 
    private Type type;
    private Date date;

    public Transaction() {
        this.id = ID_GENERATOR.getAndIncrement();
    }

    public Transaction(String description, long amount, Type type, Date date) {
        this();
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.date = date;
    }

    public Transaction(String description, String note, long amount, Type type, Date date) {
        this();
        this.description = description;
        this.note = note;
        this.amount = amount;
        this.type = type;
        this.date = date;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    /**
     * Returns a formatted Rupiah string, e.g. "Rp 150.000"
     */
    public String getFormattedAmount() {
        long abs = Math.abs(amount);
        String sign = (type == Type.INCOME) ? "+ " : "- ";
        return sign + "Rp " + formatRupiah(abs);
    }

    public static String formatRupiah(long value) {
        String raw = String.valueOf(value);
        StringBuilder result = new StringBuilder();
        int count = 0;
        for (int i = raw.length() - 1; i >= 0; i--) {
            if (count > 0 && count % 3 == 0) result.insert(0, ".");
            result.insert(0, raw.charAt(i));
            count++;
        }
        return result.toString();
    }
}
