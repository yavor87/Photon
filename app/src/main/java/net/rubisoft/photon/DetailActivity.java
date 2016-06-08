package net.rubisoft.photon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DetailActivity extends AppCompatActivity {
    public static final String IMAGE_ID_EXTRA = DetailActivity.class.getPackage().toString() + ".IMAGE_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            int imageId = getIntent().getIntExtra(IMAGE_ID_EXTRA, -1);
            if (imageId != -1) {
                ImageFragment fragment = ImageFragment.newInstance(imageId);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.detail_container, fragment)
                        .commit();
            }
        }
    }
}
