package com.example.reses;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResesAdapter extends ArrayAdapter<GettersReses> {
    DatabaseHelper dbHelper;
    String tiporesesaux;
    String cedulaaux;
    public ResesAdapter(Context context, ArrayList<GettersReses> reses, String tiporeses, String cedula) {
        super(context, 0, reses);
        dbHelper = new DatabaseHelper(context);
        tiporesesaux=tiporeses;
        cedulaaux=cedula;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GettersReses reses = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_reses, parent, false);
        }

        ImageView iconImageView = convertView.findViewById(R.id.iconImageView);
        TextView nombreTextView = convertView.findViewById(R.id.nombreTextView);
        TextView tipoBovinoTextView = convertView.findViewById(R.id.tipoBovinoTextView);


        nombreTextView.setTextColor(Color.parseColor("#000000"));
        tipoBovinoTextView.setTextColor(Color.parseColor("#000000"));
        nombreTextView.setTypeface(null, Typeface.BOLD);

        iconImageView.setImageResource(R.drawable.usuario);
        nombreTextView.setText(reses.getNombre());
        tipoBovinoTextView.setText(reses.getTipoBovino());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), GestiondeReses.class);
                intent.putExtra("id", reses.getId());
                intent.putExtra("cedula",cedulaaux);
                intent.putExtra("tiporeses",tiporesesaux);
                getContext().startActivity(intent);
                if (getContext() instanceof Reses) {
                    ((Reses) getContext()).finish();
                }
            }
        });

        return convertView;
    }
}

