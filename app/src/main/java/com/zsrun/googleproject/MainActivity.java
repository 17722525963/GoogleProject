package com.zsrun.googleproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zsrun.googleproject.util.IabHelper;
import com.zsrun.googleproject.util.IabResult;
import com.zsrun.googleproject.util.Inventory;
import com.zsrun.googleproject.util.Purchase;

import java.util.ArrayList;
import java.util.List;

import static com.zsrun.googleproject.util.IabHelper.RESPONSE_INAPP_PURCHASE_DATA;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "???";

    private String PRODUCT_NAME_1 = "Product_LaTiao";
    private String PRODUCT_NAME_2 = "Product_LaoGangMa";

    private String PRODUCT_ID_1 = "product1";
    private String PRODUCT_ID_2 = "product2";

    private String PRODUCT_SUB = "Product_Sub";

    private IabHelper mIabHelper;

    private String base64EncodedPublicKey;

    private TextView showProductDetails;

    private TextView showPurchasedItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        initView();
    }

    private void initView() {
        showProductDetails = findViewById(R.id.showProductDetails);

        getProductDetails();

        purchase();

        //购买订阅内容
        purchaseSub();


        consume();
    }

    private void consume() {
        findViewById(R.id.consume)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            mIabHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
                                @Override
                                public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                                    if (inv.hasPurchase(PRODUCT_ID_2)) {
                                        consumeByPurchase(inv.getPurchase(PRODUCT_ID_2));
                                    }
                                    if (inv.hasPurchase(PRODUCT_ID_1)) {
                                        consumeByPurchase(inv.getPurchase(PRODUCT_ID_1));
                                    }
                                }
                            });
                        } catch (IabHelper.IabAsyncInProgressException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    private void purchaseSub() {
        findViewById(R.id.purchase_sub)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            mIabHelper.launchSubscriptionPurchaseFlow(MainActivity.this, PRODUCT_SUB, 222, new IabHelper.OnIabPurchaseFinishedListener() {
                                @Override
                                public void onIabPurchaseFinished(IabResult result, Purchase info) {
                                    Log.i(TAG, "onIabPurchaseFinished: " + result);
                                    Log.i(TAG, "onIabPurchaseFinished: " + info);
                                    Toast.makeText(MainActivity.this, "您购买的订阅内容为：" + info.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }, PRODUCT_SUB);
                        } catch (IabHelper.IabAsyncInProgressException e) {
                            e.printStackTrace();
                        } catch (IllegalStateException e) {
                            Toast.makeText(MainActivity.this, "IAB helper is not set up. Can't perform operation:", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void purchase() {
        findViewById(R.id.purchase_latiao).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseByProductId(PRODUCT_ID_1);
            }
        });

        findViewById(R.id.purchase_laogangma).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseByProductId(PRODUCT_ID_2);
            }
        });
    }


    /**
     * 消耗商品
     *
     * @param purchase 商品
     */
    private void consumeByPurchase(Purchase purchase) {
        if (purchase != null) {
            try {
                mIabHelper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {
                    @Override
                    public void onConsumeFinished(Purchase purchase, IabResult result) {
                        Log.i(TAG, "onConsumeFinished: " + result);
                        Log.i(TAG, "onConsumeFinished: " + purchase.toString());
                        Toast.makeText(MainActivity.this, purchase.getSku() + "消耗成功", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                Toast.makeText(MainActivity.this, "IAB helper is not set up. Can't perform operation:", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "未购买当前商品不能消耗", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 购买商品
     *
     * @param productId 商品ID
     */
    private void purchaseByProductId(final String productId) {
        try {
            mIabHelper.launchPurchaseFlow(this, productId, 111, new IabHelper.OnIabPurchaseFinishedListener() {
                @Override
                public void onIabPurchaseFinished(IabResult result, Purchase info) {
                    Log.i(TAG, "onIabPurchaseFinished: " + result);
                    if (result.isSuccess()) {
                        Toast.makeText(MainActivity.this, "您购买的辣条/老干妈已成功，正在进行消耗，消耗不成功不能再次购买~", Toast.LENGTH_SHORT).show();
                        consumeByPurchase(info);
                    }
                    if (result.getResponse() == 7) {
                        Log.i(TAG, "onIabPurchaseFinished: 已购买，未进行消耗，无法再次购买~" + Thread.currentThread().getName());
                        Toast.makeText(MainActivity.this, "已购买，未进行消耗，无法再次购买~", Toast.LENGTH_SHORT).show();
                    }

                }
            }, productId);//透传参数（传什么，google返回什么）
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            Toast.makeText(this, "IAB helper is not set up. Can't perform operation:", Toast.LENGTH_SHORT).show();
        }
    }


    private void getProductDetails() {

        final List<String> productNameLists = new ArrayList<>();
        productNameLists.add(PRODUCT_NAME_1);
        productNameLists.add(PRODUCT_NAME_2);

        findViewById(R.id.queryProductDetails)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            mIabHelper.queryInventoryAsync(true, productNameLists, null, new IabHelper.QueryInventoryFinishedListener() {
                                @Override
                                public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                                    Log.i(TAG, "onQueryInventoryFinished: " + result);
                                    Log.i(TAG, "onQueryInventoryFinished: 商品信息-->" + inv.toString());
                                    showProductDetails.setText(inv.toString());
                                }
                            });
                        } catch (IabHelper.IabAsyncInProgressException e) {
                            e.printStackTrace();
                        } catch (IllegalStateException e) {
                            Toast.makeText(MainActivity.this, "IAB helper is not set up. Can't perform operation:", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void init() {
        base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiO0e86rTwbR5+ebwIc4mmBaidu/+tfDLxr3CKwEsRC/SV6nWtHi2ZY4k9c68fLoKLaQQJo8+z23WGPYIV03KEL5CAxLYaJ62CkVUZhq1WnxF8DgNOxvadVavcpgBTEI0zj+AOKdzFurgaf4PBJfUIMl9YUM3P7XMfKLB7FeHwz0bnFWteQ5+xjYMNR5kHyZ18zVglaNtLMGKuFr1E5oQYZVPKu3Ml+/3PuQ26z5lveorye013SEiMZmM+uij6U7drhV/f+zy3tqBf//md+xFBQBswhEWGV2GxsivDAVisFOWsGvGKGquiFedXky5fsROGkKQ0MJpexVWgOmhgAyF2QIDAQAB";
        mIabHelper = new IabHelper(this, base64EncodedPublicKey);

        mIabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (result.isSuccess()) {
                    Log.i(TAG, "onIabSetupFinished: 初始化成功" + result.toString());
                    Toast.makeText(MainActivity.this, "初始化成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "" + result.getMessage(), Toast.LENGTH_SHORT).show();
                }

                if (mIabHelper == null) {
                    Log.i(TAG, "onIabSetupFinished: " + mIabHelper);
                    return;
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIabHelper != null) {
            try {
                mIabHelper.dispose();
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }
        mIabHelper = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mIabHelper.handleActivityResult(requestCode, resultCode, data);
        String purchaseData = data.getStringExtra(RESPONSE_INAPP_PURCHASE_DATA);
        Log.i(TAG, "onActivityResult: " + purchaseData);

        Log.i(TAG, "account: " + data.getStringExtra("com.google.android.finsky.analytics.LoggingContext.KEY_ACCOUNT"));

        for (String key : data.getExtras().keySet()) {
            Log.i(TAG, "onActivityResult: " + key);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
