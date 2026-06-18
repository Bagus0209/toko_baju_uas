package com.bagus.toko_baju_uas.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.bagus.toko_baju_uas.R;
import com.bagus.toko_baju_uas.api.ApiClient;
import com.bagus.toko_baju_uas.api.ApiInterface;
import com.bagus.toko_baju_uas.model.BaseResponse;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import android.print.PrintManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public static class HistoryItem {
        private final int idTransaksi;
        private final String date;
        private final String status; // "Selesai", "Berlangsung", "Dibatalkan", "Gagal"
        private final String productName;
        private final int totalPrice;
        private final String imageUrl;

        public HistoryItem(int idTransaksi, String date, String status, String productName, int totalPrice, String imageUrl) {
            this.idTransaksi = idTransaksi;
            this.date = date;
            this.status = status;
            this.productName = productName;
            this.totalPrice = totalPrice;
            this.imageUrl = imageUrl;
        }

        public int getIdTransaksi() { return idTransaksi; }
        public String getDate() { return date; }
        public String getStatus() { return status; }
        public String getProductName() { return productName; }
        public int getTotalPrice() { return totalPrice; }
        public String getImageUrl() { return imageUrl; }
    }

    private final Context context;
    private final List<HistoryItem> listHistory;
    private final Runnable onStatusUpdated;

    public HistoryAdapter(Context context, List<HistoryItem> listHistory, Runnable onStatusUpdated) {
        this.context = context;
        this.listHistory = listHistory;
        this.onStatusUpdated = onStatusUpdated;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = listHistory.get(position);

        holder.tvDate.setText(item.getDate());
        holder.tvProductName.setText(item.getProductName());

        // Format Price to Rupiah
        Locale localeID = Locale.forLanguageTag("id-ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        holder.tvTotalPrice.setText(context.getString(R.string.total_price_format, formatRupiah.format(item.getTotalPrice())));

        // Set Status Badge Style
        holder.tvStatus.setText(item.getStatus());
        holder.btnActionSecondary.setVisibility(View.VISIBLE);
        holder.btnActionSecondary.setText("Beli Lagi");

        switch (item.getStatus().toLowerCase()) {
            case "selesai":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_pill_status_completed);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_completed));
                holder.btnActionPrimary.setVisibility(View.VISIBLE);
                holder.btnActionPrimary.setText("Beri Ulasan");
                holder.btnActionSecondary.setText("Cetak Nota");
                break;
            case "berlangsung":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_pill_status_paid);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_paid));
                holder.btnActionPrimary.setVisibility(View.VISIBLE);
                holder.btnActionPrimary.setText("Bayar Sekarang");
                holder.btnActionSecondary.setText("Batalkan");
                break;
            case "gagal":
            case "dibatalkan":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_pill_status_cancelled);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_cancelled));
                holder.btnActionPrimary.setVisibility(View.GONE);
                break;
        }

        // Load image with Glide
        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(holder.ivProductThumb);

        // Actions
        holder.btnActionSecondary.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            if (item.getStatus().equalsIgnoreCase("berlangsung")) {
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Batalkan Pesanan")
                        .setMessage("Apakah Anda yakin ingin membatalkan pesanan ini?")
                        .setPositiveButton("Ya, Batalkan", (dialog, which) -> {
                            updateStatus(item.getIdTransaksi(), "Dibatalkan");
                        })
                        .setNegativeButton("Tidak", null)
                        .show();
            } else if (item.getStatus().equalsIgnoreCase("selesai")) {
                printReceipt(item);
            } else {
                Toast.makeText(context, "Membeli ulang: " + item.getProductName(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnActionPrimary.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            if (item.getStatus().equalsIgnoreCase("berlangsung")) {
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Konfirmasi Pembayaran")
                        .setMessage("Apakah Anda ingin menyelesaikan pembayaran sebesar " + formatRupiah.format(item.getTotalPrice()) + "?")
                        .setPositiveButton("Bayar", (dialog, which) -> {
                            updateStatusAndPrint(item);
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            } else {
                Toast.makeText(context, holder.btnActionPrimary.getText() + " untuk " + item.getProductName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatus(int idTransaksi, String status) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.updateTransaksi(idTransaksi, status).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(context, "Transaksi berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                    if (onStatusUpdated != null) {
                        onStatusUpdated.run();
                    }
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Gagal memperbarui transaksi";
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(context, "Koneksi internet error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void printReceipt(HistoryItem item) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html><head><style>")
                .append("body { font-family: monospace; padding: 20px; color: #333; }")
                .append(".receipt-box { max-width: 400px; margin: auto; border: 1px dashed #aaa; padding: 15px; }")
                .append(".header { text-align: center; margin-bottom: 20px; }")
                .append(".header h2 { margin: 0; color: #111; }")
                .append(".info { font-size: 12px; margin-bottom: 15px; border-bottom: 1px dashed #ccc; padding-bottom: 10px; }")
                .append(".item-table { width: 100%; border-collapse: collapse; font-size: 12px; }")
                .append(".item-table th { border-bottom: 1px dashed #ccc; text-align: left; padding: 5px 0; }")
                .append(".item-table td { padding: 5px 0; }")
                .append(".total-box { margin-top: 15px; border-top: 1px dashed #ccc; padding-top: 10px; font-size: 14px; text-align: right; }")
                .append(".footer { text-align: center; margin-top: 25px; font-size: 11px; color: #777; }")
                .append("</style></head><body>")
                .append("<div class='receipt-box'>")
                .append("<div class='header'>")
                .append("<h2>LUXE THREADS</h2>")
                .append("<p>Premium Fashion Boutique</p>")
                .append("</div>")
                .append("<div class='info'>")
                .append("<b>No. Transaksi:</b> ORD-").append(item.getIdTransaksi()).append("<br/>")
                .append("<b>Tanggal:</b> ").append(item.getDate()).append("<br/>")
                .append("<b>Status:</b> Lunas (Selesai)<br/>")
                .append("</div>")
                .append("<table class='item-table'>")
                .append("<thead><tr><th>Item</th><th style='text-align:right'>Total</th></tr></thead>")
                .append("<tbody>");

        Locale localeID = Locale.forLanguageTag("id-ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        
        htmlBuilder.append("<tr>")
                .append("<td>").append(item.getProductName()).append("</td>")
                .append("<td style='text-align:right'>").append(formatRupiah.format(item.getTotalPrice())).append("</td>")
                .append("</tr>");

        htmlBuilder.append("</tbody></table>")
                .append("<div class='total-box'>")
                .append("<b>Total Bayar: ").append(formatRupiah.format(item.getTotalPrice())).append("</b>")
                .append("</div>")
                .append("<div class='footer'>")
                .append("<p>Terima kasih telah berbelanja di Luxe Threads!</p>")
                .append("<p>Nota ini sah sebagai bukti pembayaran digital.</p>")
                .append("</div>")
                .append("</div></body></html>");

        String htmlContent = htmlBuilder.toString();

        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            WebView webView = new WebView(context);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
                    if (printManager != null) {
                        String jobName = context.getString(R.string.app_name) + " Nota ORD-" + item.getIdTransaksi();
                        android.print.PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
                        printManager.print(jobName, printAdapter, new android.print.PrintAttributes.Builder().build());
                    }
                }
            });
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
        });
    }

    private void updateStatusAndPrint(HistoryItem item) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.updateTransaksi(item.getIdTransaksi(), "Selesai").enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse> call, @NonNull Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(context, "Pembayaran Berhasil! Mencetak nota...", Toast.LENGTH_SHORT).show();
                    printReceipt(item);
                    if (onStatusUpdated != null) {
                        onStatusUpdated.run();
                    }
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Gagal memperbarui transaksi";
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse> call, @NonNull Throwable t) {
                Toast.makeText(context, "Koneksi internet error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listHistory.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvStatus, tvProductName, tvTotalPrice;
        ImageView ivProductThumb;
        MaterialButton btnActionSecondary, btnActionPrimary;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            ivProductThumb = itemView.findViewById(R.id.ivProductThumb);
            btnActionSecondary = itemView.findViewById(R.id.btnActionSecondary);
            btnActionPrimary = itemView.findViewById(R.id.btnActionPrimary);
        }
    }
}
