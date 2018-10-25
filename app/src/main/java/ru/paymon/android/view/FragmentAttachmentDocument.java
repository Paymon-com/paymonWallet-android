package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ru.paymon.android.R;
import ru.paymon.android.filepicker.adapters.FileAdapterListener;
import ru.paymon.android.filepicker.adapters.FileListAdapter;
import ru.paymon.android.filepicker.models.Document;
import ru.paymon.android.filepicker.PickerManager;

public class FragmentAttachmentDocument extends FragmentAttachmentDocPicker implements FileAdapterListener {

    private RecyclerView recyclerView;
    private TextView emptyView;

    public static FragmentAttachmentDocument newInstance(String fileType) {
        FragmentAttachmentDocument photoPickerFragment = new FragmentAttachmentDocument();
        Bundle bun = new Bundle();
        bun.putString("FILE_TYPE", fileType);
        photoPickerFragment.setArguments(bun);
        return photoPickerFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_attachment_document_picker, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    public String getFileType() {
        return getArguments().getString("FILE_TYPE");
    }

    @Override
    public void onItemSelected() {
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        recyclerView = view.findViewById(R.id.attachement_document_recyclerview);
        emptyView = view.findViewById(R.id.attachement_document_empty_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setVisibility(View.GONE);
    }

    public void updateList(List<Document> documents) {
        if (getView() == null) return;

        if (documents.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

            FileListAdapter fileListAdapter = (FileListAdapter) recyclerView.getAdapter();
            if (fileListAdapter == null) {
                fileListAdapter = new FileListAdapter(getActivity(), documents, PickerManager.getInstance().getSelectedFiles(), this);

                recyclerView.setAdapter(fileListAdapter);
            } else {
                fileListAdapter.setData(documents);
                fileListAdapter.notifyDataSetChanged();
            }
            onItemSelected();
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

}
