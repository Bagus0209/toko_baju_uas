package com.bagus.toko_baju_uas;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.bagus.toko_baju_uas.util.AnimationUtil;
import com.google.android.material.button.MaterialButton;

public class CheckoutSuccessActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_checkout_success);

        // Extract purchase details from intent
        String itemsJson = getIntent().getStringExtra("purchase_items_json");
        int totalBayar = getIntent().getIntExtra("total_bayar", 0);
        String alamat = getIntent().getStringExtra("alamat");

        MaterialButton btnCetakNota = findViewById(R.id.btnCetakNota);
        MaterialButton btnHome = findViewById(R.id.btnBackToHome);

        btnCetakNota.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            if (itemsJson != null && !itemsJson.isEmpty()) {
                printReceipt(itemsJson, totalBayar, alamat);
            } else {
                android.widget.Toast.makeText(this, "Data transaksi tidak tersedia untuk dicetak!", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        btnHome.setOnClickListener(v -> {
            AnimationUtil.animateButtonClick(v);
            Intent intent = new Intent(this, PengunjungActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void printReceipt(String itemsJson, int totalBayar, String alamat) {
        java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<java.util.List<com.bagus.toko_baju_uas.model.CartModel>>(){}.getType();
        java.util.List<com.bagus.toko_baju_uas.model.CartModel> items = new com.google.gson.Gson().fromJson(itemsJson, listType);

        if (items == null || items.isEmpty()) {
            android.widget.Toast.makeText(this, "Data transaksi tidak valid!", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate HTML Receipt
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
                .append("<b>Tanggal:</b> ").append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date())).append("<br/>")
                .append("<b>Alamat Kirim:</b> ").append(alamat).append("<br/>")
                .append("<b>Status:</b> Lunas (Selesai)<br/>")
                .append("</div>")
                .append("<table class='item-table'>")
                .append("<thead><tr><th>Item</th><th>Qty</th><th style='text-align:right'>Harga</th></tr></thead>")
                .append("<tbody>");

        java.text.NumberFormat formatRupiah = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("id-ID"));
        int subtotal = 0;
        for (com.bagus.toko_baju_uas.model.CartModel item : items) {
            int itemTotal = item.getHarga() * item.getJumlah();
            subtotal += itemTotal;
            htmlBuilder.append("<tr>")
                    .append("<td>").append(item.getNamaBarang()).append("</td>")
                    .append("<td>").append(item.getJumlah()).append("</td>")
                    .append("<td style='text-align:right'>").append(formatRupiah.format(item.getHarga())).append("</td>")
                    .append("</tr>");
        }
        
        int serviceFee = 2000;
        
        htmlBuilder.append("</tbody></table>")
                .append("<div class='total-box'>")
                .append("Subtotal: ").append(formatRupiah.format(subtotal)).append("<br/>")
                .append("Biaya Layanan: ").append(formatRupiah.format(serviceFee)).append("<br/>")
                .append("<b>Total Bayar: ").append(formatRupiah.format(totalBayar)).append("</b>")
                .append("</div>")
                .append("<div class='footer'>")
                .append("<p>Terima kasih telah berbelanja di Luxe Threads!</p>")
                .append("<p>Nota ini sah sebagai bukti pembayaran digital.</p>")
                .append("</div>")
                .append("</div></body></html>");

        String htmlContent = htmlBuilder.toString();

        // Use Android Printing Framework to print
        android.webkit.WebView webView = new android.webkit.WebView(this);
        webView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
                android.print.PrintManager printManager = (android.print.PrintManager) getSystemService(android.content.Context.PRINT_SERVICE);
                if (printManager != null) {
                    String jobName = getString(R.string.app_name) + " Nota Pembayaran";
                    android.print.PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
                    printManager.print(jobName, printAdapter, new android.print.PrintAttributes.Builder().build());
                }
            }
        });
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }
}