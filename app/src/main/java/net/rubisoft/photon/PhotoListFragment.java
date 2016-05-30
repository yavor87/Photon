package net.rubisoft.photon;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_list, container, false);
        GridView gv = (GridView) view.findViewById(R.id.list);
        gv.setAdapter(new SampleGridViewAdapter(this.getContext(), new net.rubisoft.photon.data.LocalImageProvider(this.getContext())));
        return view;
    }
}
