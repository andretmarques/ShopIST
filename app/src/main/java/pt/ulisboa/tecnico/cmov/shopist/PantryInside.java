package pt.ulisboa.tecnico.cmov.shopist;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraXConfig;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.BarcodeDetector;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PantryInside extends AppCompatActivity {
    private ArrayList<Item> itemsPantry = new ArrayList<>();
    private ArrayList<PublicItem> listPublic = new ArrayList<>();
    private DatabaseReference myRef;
    private String pantryId;
    Button scanBarcodeBtn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_pantry);
        setSupportActionBar(findViewById(R.id.toolbar_pantry));
        ActionBar actionBar = getSupportActionBar();
        String actionTitle;
        TextView toolbarTitle = findViewById(R.id.toolbar_pantry_title);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://shopist-310217-default-rtdb.europe-west1.firebasedatabase.app/");
        myRef = database.getReference();

        Bundle b = getIntent().getExtras();
        if(b != null){
            itemsPantry = b.getParcelableArrayList("pantryProductsList");
            actionTitle = b.getString("pantryListName");
            actionTitle = "Pantry: " + actionTitle;
            toolbarTitle.setText(actionTitle);
            pantryId = b.getString("pantryListId");
            assert actionBar != null;
            actionBar.setDisplayShowTitleEnabled(false);
        }

        setItemsRecycler(itemsPantry);

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra("returnedItemList", itemsPantry);
                setResult(PantryInside.RESULT_OK, intent);
                finish();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        scanBarcodeBtn = findViewById(R.id.scan_barcode);
        scanBarcodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(PantryInside.this, ScanBarcodeActivity.class), 10025);
            }
        });
    }

    private void setItemsRecycler(ArrayList<Item> products) {
        RecyclerView productsMainRecycler = findViewById(R.id.items_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        productsMainRecycler.setLayoutManager(layoutManager);
        ItemRecyclerAdapter itemRecyclerAdapter = new ItemRecyclerAdapter(this, products, "P");
        productsMainRecycler.setAdapter(itemRecyclerAdapter);
    }

    public void createItem(View view){
        Intent intent = new Intent(this, CreateProductActivity.class);
        startActivityForResult(intent, 10015);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10015) {
            if (resultCode == RESULT_OK) {
                Item newItem = data.getParcelableExtra("returnedProduct");
                itemsPantry.add(newItem);
                myRef.child("Pantries").child(pantryId).child("itemList").setValue(itemsPantry);
                setItemsRecycler(itemsPantry);
            }
            return;
        }
        else if (requestCode == 10025) {
            if (resultCode == RESULT_OK) {
                String barcode = data.getStringExtra("Barcode");
                Intent i = new Intent(PantryInside.this, PriceShopActivity.class);
                i.putExtra("barcode", barcode);
                startActivityForResult(i, 20221);
            }
            return;
        }
        else if (requestCode == 20221) {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
