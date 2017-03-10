package com.citaurus.gmspps;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by rzwisler on 02.09.2015.
 */
public class ProviderAdapter extends ArrayAdapter<Provider> {
    Context context;
    private Activity activity;
    int layoutResourceId;
    Provider data[] = null;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;
    private ProviderHolder holder;


    public ProviderAdapter(Activity context, int layoutResourceId, Provider[] data) {
        super(context, layoutResourceId, data);
        activity = context;
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView=convertView;
        inflater = activity.getLayoutInflater();
        if(convertView==null) {
            rowView= inflater.inflate(R.layout.listview_item_row, null, true);
        }
        //inflater = activity.getLayoutInflater();
        //View rowView= inflater.inflate(R.layout.listview_item_row, null, true);
        holder = new ProviderHolder();
        holder.txtTitle = (TextView) rowView.findViewById(R.id.txtTitle);
        holder.txtProvider = (TextView) rowView.findViewById(R.id.txtProviderTyp);
        holder.imageView = (ImageView) rowView.findViewById(R.id.imgIcon);
        holder.imageView1 = (ImageView) rowView.findViewById(R.id.imgIcon1);
        Provider myprovider = data[position];
        holder.txtTitle.setText(myprovider.Name);
        holder.txtProvider.setText(myprovider.Typ);
        imageLoader.DisplayImage(myprovider.IconUrl, 22,  holder.imageView);



        switch (myprovider.TypID) {
            case 1:
                holder.txtProvider.setTextColor(ContextCompat.getColor(context, R.color.C1_red));
                holder.imageView1.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.alert));

                break;
            case 2:
                holder.txtProvider.setTextColor(ContextCompat.getColor(context, R.color.C1_ligtblue));
                holder.imageView1.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.business));

                break;
            case 3:
                holder.txtProvider.setTextColor(ContextCompat.getColor(context, R.color.C1_green));
                holder.imageView1.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.fun));

                break;

        }
        /*

        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.listview_item_row, null);
        View row = convertView;
        ProviderHolder holder = null;

        if(row == null)
        {
          //  LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ProviderHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);

            row.setTag(holder);
        }
        else
        {
            holder = (ProviderHolder)row.getTag();
        }
        try {
            URL myUrl = new URL("http://www.vaultads.com/wp-content/uploads/2011/03/google-adsense.jpg");
            InputStream inputStream = (InputStream) myUrl.getContent();
            Drawable drawable = Drawable.createFromStream(inputStream, null);
            holder.imgIcon.setImageDrawable(drawable);
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        Provider pro = data[position];
        holder.txtTitle.setText(pro.Name);
        //holder.imgIcon.setImageURI(new Uri("")
       // pro.Url);

*/
        return rowView;
    }

    static class ProviderHolder
    {
        ImageView imageView;
        ImageView imageView1;
        TextView txtTitle;
        TextView txtProvider;
    }
}
