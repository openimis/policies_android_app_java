package org.openimis.imispolicies.domain.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.openimis.imispolicies.R;

import java.util.List;

public class FamilyMemberAdapter extends BaseAdapter {
    private Context context;
    private List<Family.Member> members;
    private LayoutInflater inflater;

    public FamilyMemberAdapter(Context context, List<Family.Member> members) {
        this.context = context;
        this.members = members;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return members != null ? members.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return members.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.activity_enquire, parent, false);

            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.imageView);
            holder.tvCHFID = convertView.findViewById(R.id.tvCHFID);
            holder.tvName = convertView.findViewById(R.id.tvName);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Family.Member member = members.get(position);

        holder.tvCHFID.setText("CHFID: " + member.getChfId());
        holder.tvName.setText(member.getLastName() + " " + member.getOtherNames());

        if (member.getPhotoPath() != null && !member.getPhotoPath().isEmpty()) {
            loadMemberPhoto(holder.imageView, member.getPhotoPath());
        } else {
            holder.imageView.setImageResource(R.drawable.person);
        }

        return convertView;
    }

    private void loadMemberPhoto(ImageView imageView, String photoPath) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.person);
            }
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.person);
        }
    }

    static class ViewHolder {
        ImageView imageView;
        TextView tvCHFID;
        TextView tvName;
    }
}