package com.duck.owlcctv.adapter;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.duck.owlcctv.R;
import com.duck.owlcctv.model.Recorded;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class RecordedArrayAdapter extends BaseAdapter {
    private static final String TAG = "[RecordedArrayAdapter]";
    private final List<Recorded> recordeds = new ArrayList<>();
    private final List<Recorded> selectedRecords = new ArrayList<>();

    public RecordedArrayAdapter(List<Recorded> recordeds) {
        this.recordeds.addAll(recordeds);
    }

    public List<Recorded> getSelectedRecords() {
        return this.selectedRecords;
    }

    @Override
    public int getCount() {
        if (this.recordeds != null)
            return recordeds.size();
        else
            return 0;
    }

    @Override
    public Recorded getItem(int position) {
        if (this.recordeds != null)
            return recordeds.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @NotNull
    public View getView(final int position, View contentView, @NotNull ViewGroup parent) {
        if (contentView == null) {
            contentView =
                    LayoutInflater.from(
                            parent.getContext())
                            .inflate(R.layout.item_recorded, parent, false);
        }
        final Recorded recorded = this.recordeds.get(position);
        final TextView txtName = (TextView)contentView.findViewById(R.id.txtName);
        txtName.setText(recorded.getName());
        final TextView txtSize = (TextView)contentView.findViewById(R.id.txtSize);
        txtSize.setText(recorded.getReadableFileSize());
        final TextView txtLastModified = (TextView)contentView.findViewById(R.id.txtLastModified);
        txtLastModified.setText(recorded.getLastModified());
        if (recorded.getThumb() != null) {
            ImageView ivThumb = (ImageView)contentView.findViewById(R.id.ivThumb);
            ivThumb.setImageBitmap(recorded.getThumb());
        }
        final CheckBox checkBox = (CheckBox)contentView.findViewById(R.id.cbxSelected);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selectedRecords.add(recorded);
                } else {
                    selectedRecords.remove(recorded);
                }
            }
        });
        return contentView;
    }
}
