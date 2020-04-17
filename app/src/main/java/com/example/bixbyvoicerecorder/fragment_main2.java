package com.example.bixbyvoicerecorder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;


public class fragment_main2 extends Fragment {
    fragment_main fm = new fragment_main();

     List<String> list = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view2 = inflater.inflate(R.layout.fragment_main_2, container, false);

        ListView listView = view2.findViewById(R.id.listview);
        System.out.println("fmtotalcount"+fm.totalcount);

        for (int i = 0; i < 3; i++) {
            list.add(i + "번 + ");
        }

        // 어댑터 생성
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        return view2;
    }
}
