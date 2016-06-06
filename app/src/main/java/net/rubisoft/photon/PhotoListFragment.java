package net.rubisoft.photon;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import net.rubisoft.photon.content.PhotoItem;
import net.rubisoft.photon.utils.PhotoGalleryAsyncLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Photos.
 */
public class PhotoListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<PhotoItem>> {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PhotoListFragment() {
    }

    private static final int LOADER_ID = 0;

    private ImageGridViewAdapter mImageAdapter;
    private List<PhotoItem> mPhotos;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 101;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getLoaderManager().initLoader(LOADER_ID, null, this);
        } else {
            requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    READ_EXTERNAL_STORAGE_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_REQUEST && grantResults.length == 1 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_list, container, false);
        GridView gridView = (GridView) view.findViewById(R.id.list);
        mPhotos = new ArrayList<>();
        mImageAdapter = new ImageGridViewAdapter(getContext(), mPhotos);
        gridView.setAdapter(mImageAdapter);
        return view;
    }

    @Override
    public Loader<List<PhotoItem>> onCreateLoader(int id, Bundle args) {
        return new PhotoGalleryAsyncLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<PhotoItem>> loader, List<PhotoItem> data) {
        mPhotos.clear();

        for(int i = 0; i < data.size();i++){
            PhotoItem item = data.get(i);
            mPhotos.add(item);
        }

        mImageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<PhotoItem>> loader) {
        mPhotos.clear();
        mImageAdapter.notifyDataSetChanged();
    }
}
