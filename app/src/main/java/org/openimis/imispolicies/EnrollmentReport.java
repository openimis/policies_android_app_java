package org.openimis.imispolicies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import org.openimis.imispolicies.R;

public class EnrollmentReport extends RecyclerView.Adapter {
    private ArrayList<String> msg;
    //Constructor
    Context _context;
    public EnrollmentReport(Context rContext, ArrayList<String> _msg){
        _context = rContext;
        msg = _msg;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
         View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.message,parent,false);

        Reportmsg view = new Reportmsg(row);
        return view;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((Reportmsg) holder).reportsms.setText(String.valueOf(msg.get(position)));

    }

    @Override
    public int getItemCount() {
        return msg.size();
    }

    public class Reportmsg extends RecyclerView.ViewHolder{

        public TextView reportsms;

        public Reportmsg(View itemView) {
            super(itemView);
            reportsms = (TextView) itemView.findViewById(R.id.reportsms);
        }
    }
}
