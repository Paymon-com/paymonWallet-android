package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;

import ru.paymon.android.R;
import ru.paymon.android.adapters.CreateGroupAdapter;
import ru.paymon.android.data.CreateGroupItem;
import ru.paymon.android.utils.Utils;

public class FragmentCreateGroup extends Fragment {
    public LinkedList<CreateGroupItem> createGroupItemList = new LinkedList<>();
    private static FragmentCreateGroup instance;

    public static synchronized FragmentCreateGroup newInstance(){
        instance = new FragmentCreateGroup();
        return instance;
    }

    public static synchronized FragmentCreateGroup getInstance(){
        if (instance == null)
            instance = new FragmentCreateGroup();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.setActionBarWithTitle(getActivity(), getString(R.string.create_group));
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_group, container, false);

        RecyclerView contactsRecView = (RecyclerView) view.findViewById(R.id.fragment_create_group_rv);
        contactsRecView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        contactsRecView.setLayoutManager(llm);

        CreateGroupAdapter adapter = new CreateGroupAdapter(createGroupItemList);
        contactsRecView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.create_group_menu, menu);

        MenuItem createGroupButton = menu.findItem(R.id.create_group_done_item);
        createGroupButton.setOnMenuItemClickListener(item -> {
            //TODO создание
            return false;
        });
    }
}
