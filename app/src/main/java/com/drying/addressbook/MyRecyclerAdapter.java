package com.drying.addressbook;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Author: drying
 * E-mail: drying@erongdu.com
 * Date: 2018/11/1 11:54
 * <p/>
 * Description:recyclerview 适配器
 */
public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.MyViewHodel> {
    private Context                 context;
    private List<AddressBookItemMo> list;
    private OnItemClickListener     listener;

    public MyRecyclerAdapter(Context context, List<AddressBookItemMo> list) {
        this.context = context;
        this.list = list;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public MyViewHodel onCreateViewHolder(ViewGroup parent, final int viewType) {
        View              view      = LayoutInflater.from(context).inflate(R.layout.address_book_item, parent, false);
        final MyViewHodel viewHodel = new MyViewHodel(view);
        viewHodel.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(v, (Integer) viewHodel.itemView.getTag());
                }
            }
        });
        viewHodel.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onItemLongClick(v, (Integer) viewHodel.itemView.getTag());
                }
                return true;
            }
        });
        return viewHodel;
    }

    @Override
    public void onBindViewHolder(MyViewHodel holder, int position) {
        holder.itemView.setTag(position);
        holder.tv.setText(list.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class MyViewHodel extends RecyclerView.ViewHolder {
        private TextView tv;

        public MyViewHodel(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.item_name);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }
}
