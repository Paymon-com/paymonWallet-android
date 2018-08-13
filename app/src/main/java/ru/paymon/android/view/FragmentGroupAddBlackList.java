package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.R;
import ru.paymon.android.adapters.AddBlackListAdapter;
import ru.paymon.android.models.AddBlackListItem;
import ru.paymon.android.utils.Utils;

public class FragmentGroupAddBlackList extends Fragment {
    private static FragmentGroupAddBlackList instance;
    private AddBlackListAdapter adapter;
    private LinkedList<AddBlackListItem> list = new LinkedList<>();
    private TextView hintError;

    public static synchronized FragmentGroupAddBlackList newInstance(){
        instance = new FragmentGroupAddBlackList();
        return instance;
    }

    public static synchronized FragmentGroupAddBlackList getInstance(){
        if (instance == null)
            instance = new FragmentGroupAddBlackList();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_add_black_list, container, false);

        RecyclerView groupContactsList = (RecyclerView) view.findViewById(R.id.group_settings_add_black_list_rv);

        hintError = view.findViewById(R.id.add_black_list_hint_error_text_view);
        EditText editText = (EditText) view.findViewById(R.id.edit_text_add_black_list);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        groupContactsList.setLayoutManager(llm);

        adapter = new AddBlackListAdapter(list);
        groupContactsList.setAdapter(adapter);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Utils.setActionBarWithTitle(getActivity(), "Добавить в черный список");
        //Utils.setArrowBackInToolbar(getActivity());
        Utils.hideBottomBar(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.create_group_menu, menu);

        MenuItem addBlockListButton = menu.findItem(R.id.create_group_done_item);

        addBlockListButton.setOnMenuItemClickListener(menuItem -> {
            //TODO:Добавление в список заблокированных ползователей
            return false;
        });
    }


}
