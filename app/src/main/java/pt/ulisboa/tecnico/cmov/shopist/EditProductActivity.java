package pt.ulisboa.tecnico.cmov.shopist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class EditProductActivity  extends AppCompatActivity {
    private Item item;
    TextView productName;
    TextView quantityAval;
    TextView quantityToBuy;
    EditText price;
    TextView barcodeView;
    String barcode;
    private DatabaseReference myRef;
    private ArrayList<PublicItem> listPublic = new ArrayList<>();
    String userId;
    String pantryId;
    boolean repeated = false;
    ImageView itemPhoto;
    File photoFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://shopist-310217-default-rtdb.europe-west1.firebasedatabase.app/");
        myRef = database.getReference();
        String actionTitle;
        TextView toolbarTitle = findViewById(R.id.toolbar_product_title);
        setSupportActionBar(findViewById(R.id.toolbar_product));
        ActionBar actionBar = getSupportActionBar();

        productName = findViewById(R.id.product_name);
        quantityAval = findViewById(R.id.product_quantity);
        quantityToBuy = findViewById(R.id.product_quantity_to_buy);
        price = findViewById(R.id.product_price);
        barcodeView = findViewById(R.id.product_barcode_text);
        itemPhoto = findViewById(R.id.item_image);

        userId = getIntent().getStringExtra("UserId");
        pantryId = getIntent().getStringExtra("PantryId");

        Intent i = getIntent();
        if(i != null){
            item = i.getParcelableExtra("product");
            actionTitle = "Product: " + item.getName();
            toolbarTitle.setText(actionTitle);
            assert actionBar != null;
            actionBar.setDisplayShowTitleEnabled(false);
        }

        if (item.getPrice() != 0.0) {
            price.setText(String.valueOf(item.getPrice()));
        }

        if (item.getProductBarcode().equals("No Barcode")) {
            barcodeView.setOnClickListener(view -> startActivityForResult(new Intent(EditProductActivity.this, ScanBarcodeActivity.class), 20025));
        } else {
            barcodeView.setText(item.getProductBarcode());
            barcode = item.getProductBarcode();
            barcodeView.setTypeface(null, Typeface.BOLD);
        }
        if(item.getImageEncoded() != null) {
            Bitmap photo = convertStringToBitmap(item.getImageEncoded());
            itemPhoto.setImageBitmap(photo);
        }
        itemPhoto.setOnClickListener(view -> openDialog());


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.share_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem i) {
        if  (i.getItemId() == R.id.shareButton) {
            String name = item.getName();
            String barcode = item.getProductBarcode();
            String price = String.valueOf(item.getPrice());
            Intent shareIntent = new Intent();
            String toShare = "Hey, check this Product:\n" + name + "\n" + barcode + "\n" + "Price: " + price;
            if (item.getImageEncoded() != null) {
                Bitmap image = convertStringToBitmap(item.getImageEncoded());
                Uri contentUri = getImageUri(EditProductActivity.this, image);


                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, toShare);
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.setType("image/jpeg");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            }
            else {
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, toShare);
                shareIntent.setType("text/plain");
            }
            startActivity(Intent.createChooser(shareIntent, "send"));
        }
        return true;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    public void onClickCancel(View view){
        finish();
    }

    public void openDialog() {
        String[] way = {"Gallery", "Camera"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("How to set the photo");
        builder.setItems(way, new DialogInterface.OnClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (way[which].equals("Gallery")) {
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    @SuppressLint("IntentReset") Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                    startActivityForResult(Intent.createChooser(chooserIntent, "Select Picture"), 111);

                }
                else if (way[which].equals("Camera")) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePictureIntent, 112);
                }
            }
        });
        builder.show();

    }

    public void onClickConfirm(View view){
        String newName = productName.getText().toString();
        String newQuantity = quantityAval.getText().toString();
        String newTobuy = quantityToBuy.getText().toString();
        String newPrice = price.getText().toString();
        String newBarcode = barcodeView.getText().toString();
        int available ;
        int toBuy;
        double doublePrice;

        if(!newName.equals("")) {
            item.setName(newName);
        }
        if(!newQuantity.equals("")) {
            available = Integer.parseInt(newQuantity);
            item.setQuantity(available);
        }
        if(!newTobuy.equals("")) {
            toBuy = Integer.parseInt(newTobuy);
            item.setToPurchase(toBuy);
            item.getPantriesMap().put(pantryId, String.valueOf(toBuy));
        }
        if(!newPrice.equals("")) {
            doublePrice = Double.parseDouble(newPrice);
            doublePrice = Math.floor(doublePrice * 100) / 100;

            if ((!item.getProductBarcode().equals("No Barcode") || barcode != null) && !newPrice.equals(String.valueOf(item.getPrice()))) {
                PublicItem pi = new PublicItem(barcode, doublePrice);
                if (item.getImageEncoded() != null) {
                    pi.setPhotoEncoded(item.getImageEncoded());
                }
                System.out.println(barcode);
                myRef.child("PublicItems").child(barcode).setValue(pi);
                item.setPrice(doublePrice);
            }

        }
        if(newBarcode.equals(barcode)) {
            item.setProductBarcode(newBarcode);
        }

        Intent i = new Intent();
        i.putExtra("returnedItem", item);
        setResult(EditProductActivity.RESULT_OK, i);
        finish();
    }

    private void userHasRepeatedBarcode() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.child("Users").child(userId).child("Pantries").child(pantryId).child("itemList").getChildren()) {
                    if (singleSnapshot.child("productBarcode").getValue().toString().equals(barcode)) {
                        Toast.makeText(EditProductActivity.this, "You already have an item with this barcode", Toast.LENGTH_LONG).show();
                        repeated = true;
                        finish();
                        break;
                    }
                }
                if (!repeated) {
                    scanCrowdSourceBarcode();
                }
        }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void scanCrowdSourceBarcode() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("PublicItems").child(barcode).exists()) {
                    Toast.makeText(EditProductActivity.this, "Product has already been shared. Price has been written", Toast.LENGTH_LONG).show();
                    Double varPrice = Double.parseDouble(snapshot.child("PublicItems").child(barcode).child("price").getValue().toString());
                    String imageEncoded = snapshot.child("PublicItems").child(barcode).child("photoEncoded").getValue().toString();
                    item.setPrice(varPrice);
                    price.setText(String.valueOf(varPrice));
                    price.setFocusable(true);
                    price.requestFocus();
                    barcodeView.setText(barcode);
                    barcodeView.setTypeface(null, Typeface.BOLD);
                    itemPhoto.setImageBitmap(convertStringToBitmap(imageEncoded));
                } else {
                    Toast.makeText(EditProductActivity.this, "Write a Price and press confirm to share product info", Toast.LENGTH_LONG).show();
                    barcodeView.setText(barcode);
                    barcodeView.setTypeface(null, Typeface.BOLD);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 20025) {
            if (resultCode == RESULT_OK) {
                barcode = data.getStringExtra("Barcode");
                userHasRepeatedBarcode();

            }
        }
        if (requestCode == 112) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                Bitmap croppedBmp = Bitmap.createBitmap(imageBitmap, 0, 0,
                        100, 100);

                String stringImage = convertBitmapToString(croppedBmp);
                System.out.println(stringImage);

                itemPhoto.setImageBitmap(croppedBmp);

                item.setImageEncoded(stringImage);
            }
        }

        if (requestCode == 111) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageURI = data.getData();
                try {
                    InputStream image_stream;
                    image_stream = getApplicationContext().getContentResolver().openInputStream(selectedImageURI);
                    Bitmap imageBitmap = BitmapFactory.decodeStream(image_stream);

                    final float densityMultiplier = EditProductActivity.this.getResources().getDisplayMetrics().density;

                    int h= (int) (100*densityMultiplier);
                    int w= (int) (h * imageBitmap.getWidth()/((double) imageBitmap.getHeight()));

                    imageBitmap = Bitmap.createScaledBitmap(imageBitmap, w, h, true);
                    String stringImage = convertBitmapToString(imageBitmap);

                    itemPhoto.setImageBitmap(imageBitmap);

                    item.setImageEncoded(stringImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }


            super.onActivityResult(requestCode, resultCode, data);

    }

    public static String convertBitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static Bitmap convertStringToBitmap(String string) {
        byte[] byteArray1;
        byteArray1 = Base64.decode(string, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(byteArray1, 0, byteArray1.length);
    }




}
