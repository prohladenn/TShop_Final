package com.example.tshop.t_shop;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SimpleWebViewClientImpl extends WebViewClient {

    private Activity activity = null;
    private String shopPath;
    private String basketPath;
    private String orderPath;
    private FirebaseFirestore mFirestore;
    private Long number;
    private Long price = 0L;

    public SimpleWebViewClientImpl(Activity activity, String basketPath, String shopPath, String orderPath, Long number) {
        this.activity = activity;
        this.shopPath = shopPath;
        this.basketPath = basketPath;
        this.orderPath = orderPath;
        this.number = number;
    }
    private class Item {
        private Long count;
        private DocumentReference ref;
        private Item(Long count, DocumentReference ref){
            this.count = count;
            this.ref = ref;

        }
        private Long getCount() {
            return count;
        }

        private void setCount(Long count) {
            this.count = count;
        }
        private DocumentReference getRef() {
            return ref;
        }

        private void setRef(DocumentReference ref) {
            this.ref = ref;
        }
    }
    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, String url) {
        // все ссылки, в которых содержится 'sberbank.ru'
        // будут открываться внутри приложения
        if (url.contains("sberbank.ru")) {
            return false;
        }
        mFirestore =FirebaseFirestore.getInstance();
        mFirestore.document(basketPath).collection("products").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                ArrayList<Item> arrayList = new ArrayList<>();
                for(DocumentSnapshot doc: task.getResult()){
                    Item item = new Item(doc.getLong("count"), doc.getDocumentReference("product.reference"));
                    price += doc.getLong("count")*doc.getLong("product.price.amount");
                    arrayList.add(item);
                }
                mFirestore.document(shopPath).collection("items").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(DocumentSnapshot doc2: task.getResult()){
                            for(Item item: arrayList){
                                if(item.getRef().equals(doc2.getDocumentReference("productRef"))){
                                    Map<String, Object> reserved = new HashMap<>();
                                    reserved.put("reserved",doc2.getLong("reserved")-item.getCount());
                                    reserved.put("count", doc2.getLong("count")-item.getCount());
                                    doc2.getReference().set(reserved, SetOptions.merge());
                                }
                            }
                        }
                        Map<String, Object> up = new HashMap<>();
                        Map<String, Object> priceMap = new HashMap<>();
                        Map<String, Object> currency = new HashMap<>();
                        currency.put("name", "Рублей");
                        currency.put("shortName", "Руб");
                        priceMap.put("amount",price);
                        priceMap.put("currency",currency);
                        up.put("price", priceMap);
                        up.put("status", "paid");
                        mFirestore.document(orderPath).set(up,SetOptions.merge());
                        Intent intent = new Intent(activity,OrderResultActivity.class);
                        intent.putExtra("number", number);
                        activity.startActivity(intent);
                        activity.finish();

                    }
                });
            }
        });
        return true;
    }
}