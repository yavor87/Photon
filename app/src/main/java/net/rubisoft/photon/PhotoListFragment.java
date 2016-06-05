package net.rubisoft.photon;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import net.rubisoft.photon.data.ImageProvider;

/**
 * A fragment representing a list of Photos.
 */
public class PhotoListFragment extends Fragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PhotoListFragment() {
    }

    private ImageProvider mProvider;
    private GridView mGridView;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 101;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mProvider = new net.rubisoft.photon.data.LocalImageProvider(getContext());
        } else {
            requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    READ_EXTERNAL_STORAGE_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_REQUEST && grantResults.length == 1 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mProvider = new net.rubisoft.photon.data.LocalImageProvider(getContext());
        } else {
            mProvider = new net.rubisoft.photon.data.SampleImageProvider();
        }
        setAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_list, container, false);
        mGridView = (GridView) view.findViewById(R.id.list);
        if (mProvider != null) {
            setAdapter();
        }
        return view;
    }

    private void setAdapter() {
        mGridView.setAdapter(new SampleGridViewAdapter(this.getContext(), mProvider.getImages()));
    }
}
