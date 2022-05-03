package com.lamaq.luck_game;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.jirbo.adcolony.AdColonyAdapter;
import com.jirbo.adcolony.AdColonyBundleBuilder;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static int total_amount = 2000;
    private static int bet = 200;
    private boolean one_six;
    private boolean seven;
    private boolean eight_twelve;
    boolean doubleBackToExitPressedOnce = false;
    TextView amount_display;
    TextView value;
    TextView textView1;
    ImageButton up ;
    ImageButton down;
    ImageView imageView;
    ImageView imageView2;
    Button roll;
    Button moreCoinBtn;
    int rewardAmount;
    int total;
    int[] images = {R.drawable.dice1,R.drawable.dice2,R.drawable.dice3,R.drawable.dice4, R.drawable.dice5, R.drawable.dice6};
    public RewardedAd mRewardedAd = null;
    private final String TAG = "MainActivity";
    AdRequest adRequest = new AdRequest.Builder().build();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Remote Config keys
    private static final String COINS = "coins";
    // Firebase Remote Config
    private FirebaseRemoteConfig firebaseRemoteConfig;
    MediaPlayer spin;
    MediaPlayer dice;
    MediaPlayer coins;
    MediaPlayer winCoin;
    MediaPlayer lose;

    private String game_ID = "4242911";
    private boolean test_mode = false;
    private String ad_placement = "Rewarded_Android";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize ads
        MobileAds.initialize(this, initializationStatus -> {});
        UnityAds.initialize(MainActivity.this, game_ID, test_mode);
        //initializing end

        //remote config
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings.Builder configBuilder = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(10);

        if (BuildConfig.DEBUG) {
            long cacheInterval = 0;
            configBuilder.setMinimumFetchIntervalInSeconds(cacheInterval);
        }
        firebaseRemoteConfig.setConfigSettingsAsync(configBuilder.build());
        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        fetchRemoteTitle();

//        //database
//        // Create a new user with a first and last name
//        Map<String, Object> user = new HashMap<>();
//        user.put("highScore", "0");
//
//        // Add a new document with a generated ID
//        db.collection("users")
//                .add(user)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w(TAG, "Error adding document", e);
//                    }
//                });
//
//        db.collection("users")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, document.getId() + " => " + document.getData());
//                            }
//                        } else {
//                            Log.w(TAG, "Error getting documents.", task.getException());
//                        }
//                    }
//                });

        roll = findViewById(R.id.button);
        textView1 = findViewById(R.id.textView1);
        moreCoinBtn = findViewById(R.id.button2);
        value = findViewById(R.id.value);
        amount_display = findViewById(R.id.amount);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        up = findViewById(R.id.up);
        down = findViewById(R.id.down);
        imageView = findViewById(R.id.imageView);
        imageView2 = findViewById(R.id.imageView2);

        spin = MediaPlayer.create(this, R.raw.tick);
        dice = MediaPlayer.create(this, R.raw.dice);
        coins = MediaPlayer.create(this, R.raw.coins);
        winCoin = MediaPlayer.create(this, R.raw.wincoin);
        lose = MediaPlayer.create(this, R.raw.lose);

        Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide);
        Animation slide2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide2);

        loadData();
        value.setText(String.valueOf(bet));
//        loadAd();
//        new Handler().postDelayed(() -> moreCoinBtn.setEnabled(true), 10000);
        chkZero();
        check();
        moreCoinBtn.setEnabled(false);

        //unity ads listner
        IUnityAdsListener unityAdsListener = new IUnityAdsListener() {
            @Override
            public void onUnityAdsReady(String s) {
                moreCoinBtn.setEnabled(true);
            }

            @Override
            public void onUnityAdsStart(String s) {

            }

            @Override
            public void onUnityAdsFinish(String s, UnityAds.FinishState finishState) {
                Random r = new Random();
                int low = 2;
                int high = 5;
                int result = r.nextInt((high-low) + 2 )* 1000;
                total_amount = total_amount+result;
                amount_display.setText(String.valueOf(total_amount));
                Toast.makeText(MainActivity.this, result+" coins earned!!", Toast.LENGTH_SHORT).show();
                roll.setEnabled(true);
                up.setEnabled(true);
                down.setEnabled(true);
            }

            @Override
            public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String s) {
                Toast.makeText(MainActivity.this, unityAdsError.toString(), Toast.LENGTH_LONG).show();
            }
        };

        UnityAds.setListener(unityAdsListener);

        //more coin btn
        moreCoinBtn.setOnClickListener(v -> {
            UnityAds.load(ad_placement);
            unity_ad_display();
//            showAd();
//            if (mRewardedAd != null) {
//                Activity activityContext = MainActivity.this;
//                mRewardedAd.show(activityContext, rewardItem -> {
//                    // Handle the reward.
//                    Log.d(TAG, "The user earned the reward.");
//                    rewardAmount = rewardItem.getAmount();
//                    total_amount = (total_amount + rewardAmount * 100);
//                    amount_display.setText(String.valueOf(total_amount));
//                    roll.setEnabled(true);
//                    up.setEnabled(true);
//                    down.setEnabled(true);
//                    moreCoinBtn.setEnabled(false);
//                });
//            } else {
//                Log.d(TAG, "The rewarded ad wasn't ready yet.");
//            }
//            loadAd();
        });
        //more coin btn end

        //roll button
        roll.setEnabled(true);
        roll.setOnClickListener(view -> {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    moreCoinBtn.setEnabled(true);
                }
            }, 10000);
            if (radioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "plz select a option", Toast.LENGTH_SHORT).show();
            } else {
                spin.start();
                loadAd();
                chkZero();
                if (total_amount <= 0) {
                    Toast.makeText(this, "You don't have enough coins, watch a video", Toast.LENGTH_SHORT).show();
                    roll.setEnabled(false);
                } else {
                    roll.setEnabled(false);
                    dice.start();
                    imageView.startAnimation(slide);
                    imageView2.startAnimation(slide2);
                    Random r1 = new Random();
                    int ran1 = r1.nextInt(6) + 1;
                    imageView.setImageResource(images[ran1 - 1]);
                    Random r2 = new Random();
                    int ran2 = r2.nextInt(6) + 1;
                    imageView2.setImageResource(images[ran2 - 1]);
                    total = ran1 + ran2;

                    int id = radioGroup.getCheckedRadioButtonId();
                    if (id == R.id.rd1) {
                        one_six = true;
                        seven = false;
                        eight_twelve = false;
                    } else if (id == R.id.rd2) {
                        one_six = false;
                        seven = true;
                        eight_twelve = false;
                    } else if (id == R.id.rd3) {
                        one_six = false;
                        seven = false;
                        eight_twelve = true;
                    }
                    new Handler().postDelayed(this::check, 700);
                    new Handler().postDelayed(() -> roll.setEnabled(true), 500);
                    saveData();
                }
            }
        });
        //roll button end

        //increment button
        up.setOnClickListener(view -> {
            coins.start();
            bet = bet + 100;
            value.setText("" + bet);
            if (bet > 100 && bet < total_amount) {
                down.setEnabled(true);
                up.setEnabled(true);
            } else {
                up.setEnabled(false);
            }
            up.setEnabled(bet < total_amount);
        });
        //increment button end

        //decrement button
        down.setOnClickListener(view -> {
            coins.start();
            bet = bet - 100;
            value.setText("" + bet);
            if (bet == 0) {
                down.setEnabled(false);
            }
            up.setEnabled(bet < total_amount);
        });
        //decrement button

        loadData();
    }

    //show ad
    private void showAd() {
        try {

//            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                @Override
//                public void onAdShowedFullScreenContent() {
//                    // Called when ad is shown.
//                    Log.d(TAG, "Ad was shown.");
//                }
//
//                @Override
//                public void onAdFailedToShowFullScreenContent(@NotNull AdError adError) {
//                    // Called when ad fails to show.
//                    Log.d(TAG, "Ad failed to show.");
//                }
//
//                @Override
//                public void onAdDismissedFullScreenContent() {
//                    // Called when ad is dismissed.
//                    // Set the ad reference to null so you don't show the ad a second time.
//                    Log.d(TAG, "Ad was dismissed.");
//                    mRewardedAd = null;
//                }
//            });
        }catch (Exception a){
            Toast.makeText(this, "Unable to show ad, plz try again later", Toast.LENGTH_LONG).show();
        }
        saveData();
    }
    //show ad end

    //chk if won or lose
    @SuppressLint("SetTextI18n")
    private void check() {
        if (one_six) {
            if (total == 2 || total == 3 || total == 4 || total == 5 || total == 6) {
                total_amount = total_amount + bet;
                if (bet > total_amount){
                    bet = total_amount;
                    value.setText(String.valueOf(bet));
                }
                else {
                    winCoin.start();
                    amount_display.setText(String.valueOf(total_amount));
                    textView1.setText("You Win!!");
                }
            } else {
                total_amount = total_amount - bet;
                if (total_amount < 0){
                    Toast.makeText(this, "Not enough coins", Toast.LENGTH_SHORT).show();
                } else {
                    lose.start();
                    amount_display.setText(String.valueOf(total_amount));
                    textView1.setText("You Lose");
                }
            }
        } else if (eight_twelve) {
            if (total == 8 || total == 9 || total == 10 || total == 11 || total == 12) {
                total_amount = total_amount + bet;
                if (bet > total_amount){
                    bet = total_amount;
                    value.setText(String.valueOf(bet));
                }
                else {
                    winCoin.start();
                    amount_display.setText(String.valueOf(total_amount));
                    textView1.setText("You Win!!");
                }
            } else {
                total_amount = total_amount - bet;
                if (total_amount<0){
                    Toast.makeText(this, "Not enough coins", Toast.LENGTH_SHORT).show();
                }else{
                    lose.start();
                    amount_display.setText(String.valueOf(total_amount));
                    textView1.setText("You Lose");
                }
            }
        } else if (seven){
            if (total == 7) {
                total_amount = total_amount + bet;
                if (bet > total_amount){
                    bet = total_amount;
                    value.setText(String.valueOf(bet));
                }
                else {
                    winCoin.start();
                    amount_display.setText(String.valueOf(total_amount));
                    textView1.setText("You Win!!");
                }
            } else {
                total_amount = total_amount - bet;
                if (total_amount<0){
                    Toast.makeText(this, "Not enough coins", Toast.LENGTH_SHORT).show();
                }else{
                    lose.start();
                    amount_display.setText(String.valueOf(total_amount));
                    textView1.setText("You Lose");
                }
            }
        }
    }
    //chk if won or lose end

    //chk if the total_amount is zero
    public void chkZero(){
        if (total_amount <= 0){
            Toast.makeText(this, "You don't have enough credit", Toast.LENGTH_SHORT).show();
            up.setEnabled(false);
            down.setEnabled(false);
            bet = 0;
            value.setText(String.valueOf(bet));
            roll.setEnabled(false);
            Toast.makeText(this, "watch an add to get more coins", Toast.LENGTH_SHORT).show();
        }else{
            up.setEnabled(true);
            down.setEnabled(true);
            roll.setEnabled(true);
        }
    }
    // chk if the total_amount is zero end

    //load ad
    public void loadAd() {
//        RewardedAd.load(this, "ca-app-pub-9217099860265775/7010700408", //test - ca-app-pub-3940256099942544/5224354917
//                adRequest, new RewardedAdLoadCallback() {
//                    @Override
//                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                        // Handle the error.
//                        Log.d(TAG, loadAdError.getMessage());
//                        mRewardedAd = null;
//                    }
//
//                    @Override
//                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
//                        mRewardedAd = rewardedAd;
//                        Log.d(TAG, "Ad was loaded.");
//                    }
//                });
    }
    //load ad end

    //double back button
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Game")
                .setMessage("Are you sure you want to close this game?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    finish();
                    saveData();
                })
                .setNegativeButton("No", null)
                .show();
    }
    //double back

    public void saveData(){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("total_coins", total_amount);
        editor.apply();

    }

    public void loadData(){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        total_amount = sharedPref.getInt("total_coins", total_amount);
        amount_display.setText(String.valueOf(total_amount));;
    }

    private void fetchRemoteTitle () {
        // [START fetch_config_with_callback]
        // override default value from Remote Config
        firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        boolean updated = task.getResult();
                        Log.d(TAG, "Config params updated: " + updated);
                    } else {
                        Toast.makeText(this, "Failed to get reward", Toast.LENGTH_SHORT).show();
                    }
                    total_amount = (int) (total_amount + (Double.parseDouble(String.valueOf(firebaseRemoteConfig.getDouble("coins")))));
                    amount_display.setText(String.valueOf(total_amount));
                });
        // [END fetch_config_with_callback]
    }
    private void unity_ad_display(){
        if (UnityAds.isReady(ad_placement)){
            try {
                UnityAds.show(MainActivity.this, ad_placement);
            }catch (Exception e){
                Toast.makeText(this, "Unable to show Ad, plz try again after sometime", Toast.LENGTH_SHORT).show();
            }

        }
    }
}