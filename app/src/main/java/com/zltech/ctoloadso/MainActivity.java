package com.zltech.ctoloadso;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.zltech.ctoloadso.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'ctoloadso' library on application startup.
    static {
        System.loadLibrary("ctoloadso");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        tv.setText(CApis.getInstance().stringFromJNI("你好 Hellow  wordl! ".getBytes()));
    }

    /**
     * A native method that is implemented by the 'ctoloadso' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}