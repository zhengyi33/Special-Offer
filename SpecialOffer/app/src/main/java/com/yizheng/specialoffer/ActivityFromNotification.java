package com.yizheng.specialoffer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.squareup.picasso.Picasso;

public class ActivityFromNotification extends AppCompatActivity {

    private ImageView logo, qr;
    private TextView name, address, website, offer;

    private Typeface myCustomFont;

    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_from_notification);

        myCustomFont = Typeface.createFromAsset(getAssets(), "fonts/Acme-Regular.ttf");

        view = findViewById(R.id.constraintView);

        logo = findViewById(R.id.imageView3);
        name = findViewById(R.id.textView);
        address = findViewById(R.id.textView2);
        website = findViewById(R.id.textView4);
        offer = findViewById(R.id.textView5);
        qr = findViewById(R.id.imageView4);

        Intent intent = getIntent();
        FenceData fd = (FenceData) intent.getSerializableExtra("fence_data");
        name.setText(fd.getId());
        address.setText(fd.getAddress());
        website.setText(fd.getWebsite());
        offer.setText(fd.getMessage());

        name.setTypeface(myCustomFont);
        address.setTypeface(myCustomFont);
        website.setTypeface(myCustomFont);
        offer.setTypeface(myCustomFont);

        Linkify.addLinks(address, Linkify.ALL);
        Linkify.addLinks(website, Linkify.ALL);

        loadImage(fd.getLogo(), logo);

        makeQR(fd.getMessage(), qr);

        view.setBackgroundColor(Color.parseColor(fd.getFenceColor()));

    }

    public void makeQR(String str, ImageView imageView) {

        if (str.trim().isEmpty())
            return;

        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, 512, 512);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();

            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            imageView.setImageBitmap(bmp);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void loadImage(final String url, ImageView imageView) {

        Picasso picasso = new Picasso.Builder(this).build();
        picasso.setLoggingEnabled(true);
        picasso.load(url).into(imageView);

    }
}
