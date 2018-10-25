package ru.paymon.android.filepicker.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.paymon.android.R;
import ru.paymon.android.filepicker.models.Document;
import ru.paymon.android.filepicker.utils.FilePickerConst;
import ru.paymon.android.filepicker.PickerManager;
import ru.paymon.android.filepicker.SmoothCheckBox;

/**
 * Created by droidNinja on 29/07/16.
 */
public class FileListAdapter extends SelectableAdapter<FileListAdapter.FileViewHolder, Document>
        implements Filterable {

    private final Context context;
    private final FileAdapterListener mListener;
    private List<Document> mFilteredList;

    public FileListAdapter(Context context, List<Document> items, List<String> selectedPaths,
                           FileAdapterListener fileAdapterListener) {
        super(items, selectedPaths);
        mFilteredList = items;
        this.context = context;
        this.mListener = fileAdapterListener;
    }

    @Override public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_doc_layout, parent, false);

        return new FileViewHolder(itemView);
    }

    @Override public void onBindViewHolder(final FileViewHolder holder, int position) {
        final Document document = mFilteredList.get(position);

        holder.fileNameTextView.setText(document.getTitle());

        holder.itemView.setOnClickListener(v -> onItemClicked(document, holder));

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setOnClickListener(view -> onItemClicked(document, holder));

        holder.checkBox.setChecked(isSelected(document));

        holder.itemView.setBackgroundResource(
                isSelected(document) ? R.color.gray_transparent : android.R.color.white);
        holder.checkBox.setVisibility(isSelected(document) ? View.VISIBLE : View.GONE);

        holder.checkBox.setOnCheckedChangeListener((checkBox, isChecked) -> {
            toggleSelection(document);
            holder.itemView.setBackgroundResource(isChecked ? R.color.gray_transparent : android.R.color.white);
        });
    }

    private void onItemClicked(Document document, FileViewHolder holder) {
        if (PickerManager.getInstance().getMaxCount() == 1) {
            PickerManager.getInstance().add(document.getPath(), FilePickerConst.FILE_TYPE_DOCUMENT);
        } else {
            if (holder.checkBox.isChecked()) {
                PickerManager.getInstance().remove(document.getPath(), FilePickerConst.FILE_TYPE_DOCUMENT);
                holder.checkBox.setChecked(!holder.checkBox.isChecked(), true);
                holder.checkBox.setVisibility(View.GONE);
            } else if (PickerManager.getInstance().shouldAdd()) {
                PickerManager.getInstance().add(document.getPath(), FilePickerConst.FILE_TYPE_DOCUMENT);
                holder.checkBox.setChecked(!holder.checkBox.isChecked(), true);
                holder.checkBox.setVisibility(View.VISIBLE);
            }
        }

        if (mListener != null) mListener.onItemSelected();
    }

    @Override public int getItemCount() {
        return mFilteredList.size();
    }

    @Override public Filter getFilter() {
        return new Filter() {
            @Override protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();

                if (charString.isEmpty()) {

                    mFilteredList = getItems();
                } else {

                    ArrayList<Document> filteredList = new ArrayList<>();

                    for (Document document : getItems()) {

                        if (document.getTitle().toLowerCase().contains(charString)) {

                            filteredList.add(document);
                        }
                    }

                    mFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredList = (ArrayList<Document>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileTypeTv;

        SmoothCheckBox checkBox;

        ImageView imageView;

        TextView fileNameTextView;

        TextView fileSizeTextView;

        public FileViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            imageView = itemView.findViewById(R.id.file_iv);
            fileNameTextView = itemView.findViewById(R.id.file_name_tv);
            fileTypeTv = itemView.findViewById(R.id.file_type_tv);
            fileSizeTextView = itemView.findViewById(R.id.file_size_tv);
        }
    }
}
