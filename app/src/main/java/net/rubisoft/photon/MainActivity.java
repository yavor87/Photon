package net.rubisoft.photon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity
        implements PhotoListFragment.OnImageSelectedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onItemSelected(int imageId) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.IMAGE_ID_EXTRA, imageId);
        startActivity(intent);
    }
}
