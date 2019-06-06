package com.example.tshop.t_shop.Orders;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.tshop.t_shop.Products.ProductsActivity;
import com.example.tshop.t_shop.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class OrdersActivity extends AppCompatActivity {

    ArrayList<Order> orders = new ArrayList<>();
    OrderAdapter orderAdapter;
    RecyclerView recyclerView;
    ImageView buttonBack;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser currntUser;
    private LinearLayout load;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        mAuth = FirebaseAuth.getInstance();
        currntUser = mAuth.getCurrentUser();
        mFirestore = FirebaseFirestore.getInstance();
        buttonBack = findViewById(R.id.orders_button_back);
        recyclerView = findViewById(R.id.orders_recycler_view);
        load = (LinearLayout) findViewById(R.id.load);
        generateOrderList();

        orderAdapter = new OrderAdapter(orders, this::onOrderListener);
        recyclerView.setAdapter(orderAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        backListener();
    }

    private void onOrderListener(int i) {
        /*
        todo inventoryRef с тебя, а флвг готов
         */
        Intent intent = new Intent(OrdersActivity.this, ProductsActivity.class);
        intent.putExtra("refPath", orders.get(i).getBasketRef().getPath());
        intent.putExtra("order", true);
        startActivity(intent);
    }

    void backListener() {
        buttonBack.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /*
    todo на тебе через бд
     */
    private void generateOrderList() {
        mFirestore.collection("orders").orderBy("dateCreation").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> queryTask) {
                DocumentReference reference = mFirestore.collection("users").document(currntUser.getUid());
                for (DocumentSnapshot docItem : queryTask.getResult()) {
                if (docItem.getDocumentReference("customer").equals(reference)) {
                    Long price;
                    if(docItem.getLong("price.amount")==null)
                        price = 0L;
                    else  price = docItem.getLong("price.amount");
                    orders.add(new Order(price,
                            docItem.getTimestamp("dateCreation"),
                            docItem.getLong("number"),
                            docItem.getDocumentReference("basket"),
                            docItem.getString("status"),
                            docItem.getDocumentReference("customer"),
                            docItem.getDocumentReference("shop")

                    ));
                }

            }
            orderAdapter = new OrderAdapter(orders, OrdersActivity.this::onOrderListener);
            recyclerView.setAdapter(orderAdapter);
            load.setVisibility(View.GONE);
        }});
    }
}