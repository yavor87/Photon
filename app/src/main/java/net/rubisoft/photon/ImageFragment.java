package net.rubisoft.photon;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import net.rubisoft.photon.categorization.Categorizer;
import net.rubisoft.photon.content.ImageContract;

/**
 * Fragment, containing a single image and a list of categories.
 */
public class ImageFragment extends Fragment {
    private static final String ARG_IMAGE_ID = "image_id";
    private int mImageID;
    private ImageView mImageView;
    private ListView mCategoriesView;

    public ImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param imageId Image ID
     * @return A new instance of fragment ImageFragment.
     */
    public static ImageFragment newInstance(int imageId) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_IMAGE_ID, imageId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mImageID = getArguments().getInt(ARG_IMAGE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        mImageView = (ImageView) view.findViewById(R.id.detail_image);
        mCategoriesView = (ListView) view.findViewById(R.id.detail_category_list);

        //
        // TODO: Replace with loaders
        //
        Uri imageContent = ImageContract.ImageEntry.buildImageUri(mImageID);
        Cursor c = getContext().getContentResolver().query(imageContent, null, null, null, null);
        c.moveToFirst();
        Uri imageUri = Uri.parse(c.getString(c.getColumnIndex(ImageContract.ImageEntry.IMAGE_URI)));
        c.close();

        Uri categoriesUri = ImageContract.ImageEntry.buildImageWithCategoriesUri(mImageID);
        c = getContext().getContentResolver().query(categoriesUri, null, null, null, null);
        if (c.getCount() > 0) {
            String[] categorizations = new String[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                String category = c.getString(c.getColumnIndex(ImageContract.CategoryEntry.NAME));
                categorizations[i++] = category;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, categorizations);
            mCategoriesView.setAdapter(adapter);
        }
        c.close();

        Picasso.with(getContext())
                .load(imageUri)
                .into(mImageView);
        //

        return view;
    }
}
