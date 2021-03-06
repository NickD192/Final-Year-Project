package com.nicholasdavies.bitalinosensorapplication;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Fragment C which links to Patient Information
 *
 * @author Nick Davies
 */
public class FragmentC extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceStat) {
        return inflater.inflate(R.layout.fragment_c, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ImageButton btn3 = (ImageButton) getActivity().findViewById(R.id.imageButton3);

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FragmentC.this.getActivity(), PatientNames.class);
                FragmentC.this.startActivity(intent);

            }
        });

        super.onActivityCreated(savedInstanceState);
    }

}