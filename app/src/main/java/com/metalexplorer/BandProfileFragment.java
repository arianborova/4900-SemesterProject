package com.metalexplorer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.loki.afro.metallum.entity.Band;
import com.github.loki.afro.metallum.entity.Disc;
import com.github.loki.afro.metallum.entity.Member;
import com.github.loki.afro.metallum.entity.Track;
import com.github.loki.afro.metallum.enums.DiscType;
import com.github.loki.afro.metallum.search.API;
import com.github.loki.afro.metallum.search.query.entity.BandQuery;
import com.github.loki.afro.metallum.search.query.entity.SearchBandResult;
import com.metalexplorer.databinding.FragmentBandProfileBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BandProfileFragment extends Fragment implements RecyclerViewInterface {
    private FragmentBandProfileBinding binding;

    private ArrayList<Disc> itemList = new ArrayList<>();

    private ArrayList<Member> memberList = new ArrayList<>();

    private View view;

    private TextView roleTextView;

    private ArrayList<Track> tracks = new ArrayList<>();

    private ArrayList<Integer> trackIds = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        binding = FragmentBandProfileBinding.inflate(inflater, container, false);
        view = inflater.inflate(R.layout.fragment_band_profile, container, false);

//        setupLineupRecyclerView();
        setupBandImage();
        setupAlbumRecyclerView();


        return view;


    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("BandProfileFragment", "onCreate() called");

//        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(HomeFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
//            }
//        });
    }

    public void setupBandImage() {
        ImageView bandImageView = view.findViewById(R.id.imageView);
        byte[] photoBytes = getArguments().getByteArray("IMAGE");

        if (photoBytes != null) {
            // Convert the byte array to a Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);

            // Set the Bitmap to the ImageView
            bandImageView.setImageBitmap(bitmap);
        } else {
            // Handle the case where the photo is not present, maybe by setting a default image
            bandImageView.setImageResource(R.drawable.default_band_image);
        }
    }

    public void setupAlbumRecyclerView() {
        if (!itemList.isEmpty()) {
            itemList.clear();
        }
        RecyclerView recyclerView = view.findViewById(R.id.albumRecyclerView);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);

        AlbumRecyclerViewAdapter itemAdapter = new AlbumRecyclerViewAdapter(itemList, this, recyclerView);
        recyclerView.setAdapter(itemAdapter);

        Bundle bundle = getArguments();
        if (bundle != null) {
            ParcelableDisc parcelableUserList = bundle.getParcelable("DISCOGRAPHY");

            if (parcelableUserList != null) {
                List<Disc> userListToAdd = parcelableUserList.getUserList();



                    // Add all User objects from userListToAdd to targetUserList
                    itemList.addAll(userListToAdd);
            }
        }


//        for (int i = 0; i < requireArguments().getIntegerArrayList("DISCOGRAPHY").size(); i++) {
//            Disc disc = API.getDiscById(getArguments().getIntegerArrayList("DISCOGRAPHY").get(i));
////            String name = disc.toString().split("name=")[1].split("\\)")[0];
//            itemList.add(disc);
//        }
    }

    public void setupLineupRecyclerView() {
        if (!memberList.isEmpty()) {
            memberList.clear();
        }
        RecyclerView memberRecyclerView = view.findViewById(R.id.lineupRecyclerView);
        memberRecyclerView.setTag("memberRecyclerView");
        memberRecyclerView.setHasFixedSize(true);
        memberRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        MemberRecyclerViewAdapter memberAdapter = new MemberRecyclerViewAdapter(memberList);
        memberRecyclerView.setAdapter(memberAdapter);

        for (int i = 0; i < requireArguments().getIntegerArrayList("LINEUP").size(); i ++) {
            Member member = API.getMemberById(getArguments().getIntegerArrayList("LINEUP").get(i));
//            String name = disc.toString().split("name=")[1].split("\\)")[0];
            memberList.add(member);
        }

//        for (int j = 0; j < requireArguments().getStringArrayList("ROLES").size(); j ++) {
////            String role = requireArguments().getStringArrayList("ROLES").get(j);
//            roleTextView = memberRecyclerView.findViewById(R.id.textview6);
//            roleTextView.setText(requireArguments().getStringArrayList("ROLES").get(j));
//        }
    }
    @Override
    public void OnItemClick(int position, RecyclerView recyclerView) {

        if (recyclerView == view.findViewById(R.id.albumRecyclerView)) {
            AlbumDetailsFragment albumDetailsFragment = new AlbumDetailsFragment();
            Bundle bundle = new Bundle();

//            bundle.putString("TRACKS", itemList.get(position).getTrackList().toString());
            bundle.putString("ALBUM_NAME", itemList.get(position).getName());
            bundle.putIntegerArrayList("TRACKS", getTracks(itemList.get(position).getId()));
            bundle.putLong("ALBUM", itemList.get(position).getId());


            Optional<byte[]> optionalPhoto = itemList.get(position).getArtwork();

            if (optionalPhoto.isPresent()) {
                byte[] photoBytes = optionalPhoto.get();
                bundle.putByteArray("ALBUM_ART", photoBytes);
            } else {
                // Handle the case where the photo is not present, maybe by putting a placeholder or null
                bundle.putByteArray("ALBUM_ART", (byte[]) null); // Or some placeholder byte array
            }

            albumDetailsFragment.setArguments(bundle);

            FragmentManager fragmentManager = getParentFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, albumDetailsFragment);
            fragmentTransaction.setReorderingAllowed(true);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }

        Log.d("ItemClick", "Item clicked at position: " + position);
    }

    public ArrayList<Integer> getTracks(Long id) {
        Disc disc = API.getDiscById(id);
        tracks.addAll(disc.getTrackList());

        for (int i = 0; i < tracks.size(); i++) {
            String stringId = tracks.get(i).toString().split("id=")[1].split(",")[0];
            int intId = Integer.parseInt(stringId);
            trackIds.add(intId);
//            albumStrings.add(name);
        }

        return trackIds;


//        for (Track track : disc.getTrackList()) {
//            String trackDetails = track.getName() + "         " + track.getPlayTime() + "\n";
//            tracks.add(trackDetails);
//        }
//        return tracks;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        view = null;
    }

}
