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
import android.widget.EditText;
import android.widget.TextView;

import java.util.LinkedList;

import ru.paymon.android.R;
import ru.paymon.android.adapters.AddBlackListAdapter;
import ru.paymon.android.models.UserItem;
import ru.paymon.android.utils.Utils;

public class FragmentGroupAddBlackList extends Fragment {
    private AddBlackListAdapter adapter;
    private LinkedList<UserItem> list = new LinkedList<>();
    private TextView hintError;


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
