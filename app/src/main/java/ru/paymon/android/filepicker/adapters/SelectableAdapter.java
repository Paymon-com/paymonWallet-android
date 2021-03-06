package ru.paymon.android.filepicker.adapters;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.paymon.android.filepicker.models.BaseFile;

public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder, T extends BaseFile> extends RecyclerView.Adapter<VH> implements Selectable<T> {

    private List<T> items;

    private List<T> selectedPhotos;

    SelectableAdapter(List<T> items, List<String> selectedPaths) {
        this.items = items;
        selectedPhotos = new ArrayList<>();

        addPathsToSelections(selectedPaths);
    }

    private void addPathsToSelections(List<String> selectedPaths) {
        if (selectedPaths == null) return;

        for (int index = 0; index < items.size(); index++) {
            for (int jindex = 0; jindex < selectedPaths.size(); jindex++) {
                if (items.get(index).getPath().equals(selectedPaths.get(jindex))) {
                    selectedPhotos.add(items.get(index));
                }
            }
        }
    }

    @Override
    public boolean isSelected(T photo) {
        return selectedPhotos.contains(photo);
    }

    @Override
    public void toggleSelection(T photo) {
        if (selectedPhotos.contains(photo)) {
            selectedPhotos.remove(photo);
        } else {
            selectedPhotos.add(photo);
        }
    }

    public void setData(List<T> items) {
        this.items = items;
    }

    public List<T> getItems() {
        return items;
    }

}
