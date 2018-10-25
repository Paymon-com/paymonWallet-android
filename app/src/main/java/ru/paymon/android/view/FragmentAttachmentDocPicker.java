package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.paymon.android.R;
import ru.paymon.android.filepicker.adapters.SectionsPagerAdapter;
import ru.paymon.android.filepicker.models.Document;
import ru.paymon.android.filepicker.MediaStoreHelper;

public class FragmentAttachmentDocPicker extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DocPickerFragmentListener mListener;

    public FragmentAttachmentDocPicker() {
    }

    public interface DocPickerFragmentListener {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attachment_document, container, false);

        viewPager = view.findViewById(R.id.attachment_view_pager);
        tabLayout = view.findViewById(R.id.tabs);

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        tabLayout.setupWithViewPager(viewPager);

        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getChildFragmentManager());
        ArrayList<String> supportedTypes = MediaStoreHelper.extensionsList;
        for (int index = 0; index < supportedTypes.size(); index++) {
            adapter.addFragment(FragmentAttachmentDocument.newInstance(supportedTypes.get(index)), supportedTypes.get(index));
        }

        viewPager.setOffscreenPageLimit(supportedTypes.size());
        viewPager.setAdapter(adapter);

        MediaStoreHelper.getDocs(getActivity().getContentResolver(), files -> {
            final Map<String, List<Document>> filesMap = new HashMap<>();

            for (final Document doc : files) {
                final String extension = FilenameUtils.getExtension(doc.getTitle());
                for (final String key : MediaStoreHelper.extensionsMap.keySet()) {
                    List<Document> list = filesMap.get(key);
                    if (list == null) {
                        list = new ArrayList<>();
                        filesMap.put(key, list);
                    }

                    final List<String> extensionsList = MediaStoreHelper.extensionsMap.get(key);
                    if (extensionsList.contains(extension)) {
                        list.add(doc);
                    }
                }
            }

            setDataOnFragments(filesMap);
        });

        return view;
    }



    public static FragmentAttachmentDocPicker newInstance() {
        FragmentAttachmentDocPicker fragmentAttachmentDocPicker = new FragmentAttachmentDocPicker();
        return fragmentAttachmentDocPicker;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setDataOnFragments(Map<String, List<Document>> filesMap) {
        SectionsPagerAdapter sectionsPagerAdapter = (SectionsPagerAdapter) viewPager.getAdapter();
        if (sectionsPagerAdapter != null) {
            for (int index = 0; index < sectionsPagerAdapter.getCount(); index++) {
                FragmentAttachmentDocument docFragment = (FragmentAttachmentDocument) ((SectionsPagerAdapter) viewPager.getAdapter()).getItem(index);
                String fileType = docFragment.getFileType();
                if (fileType != null) {
                    List<Document> filesFilteredByType = filesMap.get(fileType);
                    if (filesFilteredByType != null)
                        docFragment.updateList(filesFilteredByType);
                }
            }
        }
    }
}
