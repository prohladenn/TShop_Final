package com.example.tshop.t_shop.Basket;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tshop.t_shop.Api;
import com.example.tshop.t_shop.AquiringActivity;
import com.example.tshop.t_shop.Products.PreviewDialogActivity;
import com.example.tshop.t_shop.Products.Product;
import com.example.tshop.t_shop.Products.ProductAdapter;
import com.example.tshop.t_shop.R;
import com.example.tshop.t_shop.Result;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.support.constraint.Constraints.TAG;

public class BasketActivity extends AppCompatActivity {

    ArrayList<Product> products;
    private FirebaseFunctions mFunctions;
    private FirebaseFirestore mFirestore;
    ProductAdapter productAdapter;
    RecyclerView recyclerView;
    TextView amountTextView;
    TextView amountCurTextView;
    TextView textType;
    ImageView buttonBack;
    FrameLayout buttonBuy;
    FrameLayout buttonCard;
    FrameLayout buttonPay;
    ImageView checkCard;
    ImageView checkPay;
    DocumentReference ref;
    Boolean cardFlag = true;
    Boolean payFlag = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);
        mFunctions = FirebaseFunctions.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        buttonBuy = findViewById(R.id.basket_button_buy);
        buttonBack = findViewById(R.id.basket_button_back);
        amountTextView = findViewById(R.id.basket_text_amount);
        amountCurTextView = findViewById(R.id.basket_text_amount_cur);
        recyclerView = findViewById(R.id.basket_recycler_view);
        buttonCard = findViewById(R.id.card);
        //buttonPay = findViewById(R.id.pay);
        //buttonPay.setVisibility(View.GONE);
        textType = findViewById(R.id.type);
        checkCard = findViewById(R.id.check_card);
        //checkPay = findViewById(R.id.check_pay);
        //checkPay.setVisibility(View.GONE);
        ref = FirebaseFirestore.getInstance().document(getIntent().getStringExtra("refPath"));
        buttonCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cardFlag) {

                } else {
                    cardFlag = true;
                    payFlag = false;
                    checkCard.setVisibility(View.VISIBLE);
                    checkPay.setVisibility(View.GONE);
                    textType.setText("Картой");
                    buttonBuy.setBackground(getDrawable(R.drawable.button));
                }
            }
        });
        /*buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (payFlag) {

                } else {
                    payFlag = true;
                    cardFlag = false;
                    checkCard.setVisibility(View.GONE);
                    checkPay.setVisibility(View.VISIBLE);
                    textType.setText("GooglePay");
                    buttonBuy.setBackground(getDrawable(R.drawable.button_black));
                }
            }
        });*/
        products = generateStudentList();
        productAdapter = new ProductAdapter(products, this, this::onProductClick,
                amountTextView, amountCurTextView);
        recyclerView.setAdapter(productAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);


        buyListener();
        backListener();

    }
    private void buyListener() {
        buttonBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addMessage()
                        .addOnCompleteListener(new OnCompleteListener<HashMap>() {
                            @Override
                            public void onComplete(@NonNull Task<HashMap> task) {
                                if (!task.isSuccessful()) {

                                    Exception e = task.getException();
                                    //Log.d("resultLog", e.getMessage());
                                    if (e instanceof FirebaseFunctionsException) {
                                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                        FirebaseFunctionsException.Code code = ffe.getCode();
                                        //og.d("ninhibi",code.toString());
                                        Object details = ffe.getDetails();
                                    }
                                } else {

                                    HashMap<Object, String> hashMap = task.getResult();
                                    //Log.d("uvyh",hashMap.get("orderID"));
                                    String orderID = hashMap.get("orderID");

                                    mFirestore.collection("orders").document(orderID).addSnapshotListener((doc, e) -> {
                                        if (e != null) {
                                            Log.w(TAG, "Listen failed.", e);
                                            return;
                                        }
                                        DocumentSnapshot documentSnapshot = doc;
                                        Log.d("uvyh",documentSnapshot.getId());
                                        Log.d("uvyh",documentSnapshot.getDocumentReference("basket").getId());
                                        if( documentSnapshot.getLong("number")!=null) {
                                            Retrofit retrofit = new Retrofit.Builder()
                                                    .baseUrl("https://3dsec.sberbank.ru")
                                                    .addConverterFactory(GsonConverterFactory.create())
                                                    .build();
                                            Api api = retrofit.create(Api.class);

                                            Map<String, Object> jsonObject = new HashMap<>();
                                            jsonObject.put("userName", "storeappsdetails_9-api");
                                            jsonObject.put("password", "storeappsdetails_9");
                                            jsonObject.put("orderNumber", documentSnapshot.getLong("number"));
                                            jsonObject.put("amount", Integer.valueOf(amountTextView.getText().toString()) * 100);
                                            jsonObject.put("returnUrl", "https://www.cocoacontrols.com/controls");
                                            Call<Result> call = api.postOrder(jsonObject);
                                            call.enqueue(new Callback<Result>() {
                                                @Override
                                                public void onResponse(Call<Result> call, Response<Result> response) {
                                                    if (response.body().getFormUrl() == null) {
                                                    } else {

                                                        for (Product pr : products) {
                                                            if (pr.getSelected() > 0) {
                                                                Map<String, Object> reserved = new HashMap<>();
                                                                reserved.put("reserved", pr.getReserved() + pr.getSelected());
                                                                ref.collection("items").document(pr.getItemId()).set(reserved, SetOptions.merge());
                                                            }
                                                        }
                                                        Log.d("vguvi", response.body().getFormUrl());
                                                        Intent intent = new Intent(BasketActivity.this, AquiringActivity.class);
                                                        intent.putExtra("basketPath", doc.getDocumentReference("basket").getPath());
                                                        intent.putExtra("shopPath", ref.getPath());
                                                        intent.putExtra("orderPath", documentSnapshot.getReference().getPath());
                                                        intent.putExtra("number", documentSnapshot.getLong("number"));
                                                        intent.putExtra("url", response.body().getFormUrl());
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<Result> call, Throwable t) {

                                                }
                                            });
                                        }
                                    });

                                }

                            }
                        });
            }});
    }
    void backListener() {
        buttonBack.setOnClickListener(v -> onBackPressed());
    }

    private void onProductClick(int i) {
        Intent dialog = new Intent(BasketActivity.this, PreviewDialogActivity.class);
        dialog.putExtra("product", products.get(i));
        startActivityForResult(dialog, 1111);
    }
    private Task<HashMap> addMessage() {
        // Create the arguments to the callable function.
        JSONObject basketItem = null;
        try {
            basketItem = new JSONObject();
            JSONArray array = new JSONArray();

            for (Product pr : products) {
                JSONObject item = new JSONObject();

                item.put("count", pr.getSelected());

                item.put("product", pr.getId());
                array.put(item);
            }
            basketItem.put("basketItems", array);
            basketItem.put("shop", getIntent().getStringExtra("shopPath"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = basketItem.toString();
        if(data == null || data.isEmpty())
            data = "1";
        Log.d("vszavd",data.toString());
        return mFunctions
                .getHttpsCallable("createNewOrder")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, HashMap>() {
                    @Override
                    public HashMap then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        HashMap result =  (HashMap) task.getResult().getData();
                        return result;
                    }
                });
    }


    @Override
    public void onBackPressed() {
        products = productAdapter.getBasket();
        Intent intent = new Intent();
        intent.putExtra("data", products);
        intent.putExtra("sum", ProductAdapter.getAmountString());
        setResult(RESULT_OK, intent);
        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1111 && resultCode == RESULT_OK && data != null) {
            Product product = (Product) data.getSerializableExtra("product");
            for (Product p :
                    products) {
                if (p.equals(product)) {
                    p.setSelected(product.getSelected());
                }
            }
            productAdapter = new ProductAdapter(products, this, this::onProductClick, amountTextView,
                    amountCurTextView);
            recyclerView.setAdapter(productAdapter);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager);
        }
    }

    private ArrayList<Product> generateStudentList() {
        ArrayList<Product> data = (ArrayList<Product>) getIntent().getSerializableExtra("data");
        ListIterator<Product> i = data.listIterator();
        while (i.hasNext()) {
            Product p = i.next();
            if (p.getSelected() == 0)
                i.remove();
        }
        return data;
    }

}